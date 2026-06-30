package com.ypat.service;

import com.ypat.MemberOrderQo;
import com.ypat.MemberPlanQo;
import com.ypat.MemberStatusQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.MemberOrder;
import com.ypat.entity.MemberPlan;
import com.ypat.entity.UserMember;
import com.ypat.repository.MemberOrderRepository;
import com.ypat.repository.MemberPlanRepository;
import com.ypat.repository.UserMemberRepository;
import com.ypat.util.CopyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 会员数据服务。不调 wxPayClient（跨模块），微信统一下单由 system-wap/MemberController 完成。
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);

    private static final String STATUS_PENDING = "0";
    private static final String STATUS_PAID = "1";
    private static final String STATUS_CANCELLED = "2";
    private static final String STATUS_REFUNDED = "3";

    private static final String LEVEL_NONE = "NONE";
    private static final String LEVEL_BASIC = "BASIC";

    @Autowired
    private MemberPlanRepository memberPlanRepository;
    @Autowired
    private MemberOrderRepository memberOrderRepository;
    @Autowired
    private UserMemberRepository userMemberRepository;

    public List<MemberPlanQo> listActivePlans() {
        List<MemberPlan> plans = memberPlanRepository.findByStatusOrderBySortNoAsc("1");
        return plans.stream().map(p -> CopyUtil.copy(p, MemberPlanQo.class)).toList();
    }

    /**
     * 创建待支付订单并返回 Qo（Feign 友好，调用方不依赖 system-domain 的 Entity）。
     */
    public MemberOrderQo createPendingOrderQo(Long userId, Long planId) {
        MemberOrder order = createPendingOrder(userId, planId);
        return CopyUtil.copy(order, MemberOrderQo.class);
    }

    public MemberPlanQo getPlan(Long planId) {
        MemberPlan plan = memberPlanRepository.findById(planId);
        if (plan == null) throw new SysException(ResponseCode.FAIL_NOT);
        if (!"1".equals(plan.getStatus())) throw new SysException(ResponseCode.FAIL_VAL);
        return CopyUtil.copy(plan, MemberPlanQo.class);
    }

    public MemberStatusQo getStatus(Long userId) {
        MemberStatusQo qo = new MemberStatusQo();
        qo.setLevel(LEVEL_NONE);
        if (userId == null) return qo;
        UserMember um = userMemberRepository.findById(userId);
        if (um == null) return qo;
        qo.setLevel(um.getLevel());
        qo.setExpireAt(um.getExpireAt());
        qo.setSourceOrderNo(um.getSourceOrderNo());
        qo.setActive(um.getExpireAt() != null && um.getExpireAt().after(new Date()));
        return qo;
    }

    /**
     * 创建待支付订单。同一 planId 在 5 分钟内复用最近一条待支付订单，避免重复下单。
     */
    public MemberOrder createPendingOrder(Long userId, Long planId) {
        MemberPlan plan = memberPlanRepository.findById(planId);
        if (plan == null) throw new SysException(ResponseCode.FAIL_NOT);
        if (!"1".equals(plan.getStatus())) throw new SysException(ResponseCode.FAIL_VAL);

        List<MemberOrder> recent = memberOrderRepository.findByUserIdOrderByCredateDesc(
                userId,
                new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "credate"))
        ).getContent();
        if (!recent.isEmpty()) {
            MemberOrder last = recent.get(0);
            if (STATUS_PENDING.equals(last.getStatus())
                    && last.getPlanId().equals(planId)
                    && last.getCredate() != null
                    && (System.currentTimeMillis() - last.getCredate().getTime() < 5 * 60 * 1000L)) {
                return last;
            }
        }

        MemberOrder order = new MemberOrder();
        order.setOutTradeNo(generateOutTradeNo(userId));
        order.setUserId(userId);
        order.setPlanId(plan.getId());
        order.setPlanCode(plan.getCode());
        order.setPriceFen(plan.getPriceFen());
        order.setDurationDays(plan.getDurationDays());
        order.setStatus(STATUS_PENDING);
        Date now = new Date();
        order.setCredate(now);
        order.setUpdatedAt(now);
        try {
            memberOrderRepository.save(order);
        } catch (DataIntegrityViolationException ex) {
            throw new SysException(ResponseCode.FAIL_EXIST);
        }
        return order;
    }

    private String generateOutTradeNo(Long userId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "M" + sdf.format(new Date()) + userId + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public MemberOrderQo getOrder(String outTradeNo, Long userId) {
        MemberOrder order = memberOrderRepository.findByOutTradeNo(outTradeNo);
        if (order == null) return null;
        if (userId != null && !userId.equals(order.getUserId())) {
            throw new SysException(ResponseCode.FAIL_VAL);
        }
        return CopyUtil.copy(order, MemberOrderQo.class);
    }

    public Map<String, Object> findUserOrders(Long userId, MemberOrderQo qo) {
        Pageable pageable = new PageRequest(
                qo.getPage() == null ? 0 : qo.getPage(),
                qo.getSize() == null ? 10 : qo.getSize()
        );
        Page<MemberOrder> page = memberOrderRepository.findByUserIdOrderByCredateDesc(userId, pageable);
        List<MemberOrderQo> content = page.getContent().stream()
                .map(o -> CopyUtil.copy(o, MemberOrderQo.class))
                .toList();
        Map<String, Object> body = new HashMap<>();
        body.put("content", content);
        body.put("totalElements", page.getTotalElements());
        body.put("totalPages", page.getTotalPages());
        body.put("number", page.getNumber());
        body.put("size", page.getSize());
        return body;
    }

    /**
     * 幂等标记已支付：先 update where status=0（行数=0 表示已被处理过），再叠加用户会员时长。
     */
    public boolean markPaid(String outTradeNo, String wxTransactionId, Date paidAt) {
        Date now = new Date();
        int updated = memberOrderRepository.markPaidIfPending(outTradeNo, wxTransactionId, paidAt, now);
        if (updated == 0) {
            logger.warn("member.markPaid.skip out_trade_no={} (already paid or missing)", outTradeNo);
            return false;
        }
        MemberOrder order = memberOrderRepository.findByOutTradeNo(outTradeNo);
        if (order == null) return false;
        grantMemberDuration(order.getUserId(), order.getDurationDays(), outTradeNo);
        return true;
    }

    private void grantMemberDuration(Long userId, int durationDays, String sourceOrderNo) {
        UserMember um = userMemberRepository.findById(userId);
        Date now = new Date();
        Date base = (um != null && um.getExpireAt() != null && um.getExpireAt().after(now))
                ? um.getExpireAt()
                : now;
        Date newExpire = addDays(base, durationDays);
        if (um == null) {
            um = new UserMember();
            um.setUserId(userId);
            um.setLevel(LEVEL_BASIC);
        }
        um.setExpireAt(newExpire);
        um.setSourceOrderNo(sourceOrderNo);
        um.setUpdatedAt(now);
        userMemberRepository.save(um);
    }

    private static Date addDays(Date base, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(base);
        c.add(Calendar.DAY_OF_YEAR, days);
        return c.getTime();
    }

    public boolean cancelOrder(String outTradeNo) {
        MemberOrder order = memberOrderRepository.findByOutTradeNo(outTradeNo);
        if (order == null || !STATUS_PENDING.equals(order.getStatus())) return false;
        order.setStatus(STATUS_CANCELLED);
        order.setUpdatedAt(new Date());
        memberOrderRepository.save(order);
        return true;
    }
}