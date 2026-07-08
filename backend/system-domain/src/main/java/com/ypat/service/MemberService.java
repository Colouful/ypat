package com.ypat.service;

import com.ypat.MemberBenefitQuoteQo;
import com.ypat.MemberBenefitRuleQo;
import com.ypat.MemberOperationLogQo;
import com.ypat.MemberOrderQo;
import com.ypat.MemberPlanQo;
import com.ypat.PageQo;
import com.ypat.MemberStatusQo;
import com.ypat.MemberUserAdminQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.MemberBenefitRule;
import com.ypat.entity.MemberOperationLog;
import com.ypat.entity.MemberOrder;
import com.ypat.entity.MemberPlan;
import com.ypat.entity.Record;
import com.ypat.entity.User;
import com.ypat.entity.UserMember;
import com.ypat.enums.RecordType;
import com.ypat.repository.MemberBenefitRuleRepository;
import com.ypat.repository.MemberOperationLogRepository;
import com.ypat.repository.MemberOrderRepository;
import com.ypat.repository.MemberPlanRepository;
import com.ypat.repository.RecordRepository;
import com.ypat.repository.UserMemberRepository;
import com.ypat.repository.UserRepository;
import com.ypat.util.Constant;
import com.ypat.util.CopyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private static final String SCENE_SUBMIT_YPAT = "SUBMIT_YPAT";
    private static final String BENEFIT_TYPE_PPD_DISCOUNT = "PPD_DISCOUNT";
    private static final int SUBMIT_YPAT_ORIGINAL_PPD = Constant.PUB_NEED_PPD;

    @Autowired
    private MemberPlanRepository memberPlanRepository;
    @Autowired
    private MemberOrderRepository memberOrderRepository;
    @Autowired
    private UserMemberRepository userMemberRepository;
    @Autowired
    private MemberBenefitRuleRepository memberBenefitRuleRepository;
    @Autowired
    private MemberOperationLogRepository memberOperationLogRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RecordRepository recordRepository;

    public List<MemberPlanQo> listActivePlans() {
        List<MemberPlan> plans = memberPlanRepository.findByStatusOrderBySortNoAsc("1");
        return plans.stream().map(p -> CopyUtil.copy(p, MemberPlanQo.class)).collect(Collectors.toList());
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
        UserMember um = userMemberRepository.findOne(userId);
        if (um == null) return qo;
        qo.setLevel(um.getLevel());
        qo.setExpireAt(um.getExpireAt());
        qo.setSourceOrderNo(um.getSourceOrderNo());
        qo.setActive(um.getExpireAt() != null && um.getExpireAt().after(new Date()));
        return qo;
    }

    public MemberBenefitQuoteQo quoteBenefit(Long userId, String scene) {
        int originalPpd = SCENE_SUBMIT_YPAT.equals(scene) ? SUBMIT_YPAT_ORIGINAL_PPD : 0;
        UserMember member = userId == null ? null : userMemberRepository.findOne(userId);
        String level = member == null ? MemberBenefitCalculator.LEVEL_BASIC : member.getLevel();
        MemberBenefitRule rule = memberBenefitRuleRepository.findByLevelCodeAndSceneAndBenefitType(
                level == null ? MemberBenefitCalculator.LEVEL_BASIC : level,
                scene,
                BENEFIT_TYPE_PPD_DISCOUNT
        );
        return new MemberBenefitCalculator().calculate(scene, originalPpd, member, rule);
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
        order.setPlanNameSnapshot(plan.getName());
        order.setLevelCodeSnapshot(plan.getLevelCode() == null ? LEVEL_BASIC : plan.getLevelCode());
        order.setOriginPriceFen(plan.getOriginPriceFen());
        order.setGiftPpd(plan.getGiftPpd() == null ? 0 : plan.getGiftPpd());
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

    public MemberOrderQo updatePaymentPrepared(String outTradeNo, String channel, String prepayId) {
        MemberOrder order = memberOrderRepository.findByOutTradeNo(outTradeNo);
        if (order == null) throw new SysException(ResponseCode.FAIL_NOT);
        order.setChannel(channel);
        order.setPrepayId(prepayId);
        order.setUpdatedAt(new Date());
        return CopyUtil.copy(memberOrderRepository.save(order), MemberOrderQo.class);
    }

    public Map<String, Object> findUserOrders(Long userId, MemberOrderQo qo) {
        Pageable pageable = new PageRequest(
                qo.getPage() == null ? 0 : qo.getPage(),
                qo.getSize() == null ? 10 : qo.getSize()
        );
        Page<MemberOrder> page = memberOrderRepository.findByUserIdOrderByCredateDesc(userId, pageable);
        List<MemberOrderQo> content = page.getContent().stream()
                .map(o -> CopyUtil.copy(o, MemberOrderQo.class))
                .collect(Collectors.toList());
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
        grantGiftPpd(order);
        saveOperationLog(order.getUserId(), null, "PAY_GRANT", null, null,
                "durationDays=" + order.getDurationDays() + ",giftPpd=" + (order.getGiftPpd() == null ? 0 : order.getGiftPpd()),
                outTradeNo);
        return true;
    }

    private void grantMemberDuration(Long userId, int durationDays, String sourceOrderNo) {
        UserMember um = userMemberRepository.findOne(userId);
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

    private void grantGiftPpd(MemberOrder order) {
        if (order.getGiftPpd() == null || order.getGiftPpd() <= 0) return;
        User user = userRepository.findById(order.getUserId());
        if (user == null) throw new SysException(ResponseCode.FAIL_NOT);
        user.setPpd((user.getPpd() == null ? 0 : user.getPpd()) + order.getGiftPpd());
        userRepository.save(user);

        Record record = new Record();
        record.setCredate(new Date());
        record.setPpd(order.getGiftPpd());
        record.setUserid(order.getUserId());
        record.setType(RecordType.PAY.value);
        recordRepository.save(record);
    }

    private void saveOperationLog(Long userId, Long operatorId, String actionType, String reason,
                                  String beforeValue, String afterValue, String sourceOrderNo) {
        MemberOperationLog log = new MemberOperationLog();
        log.setUserId(userId);
        log.setOperatorId(operatorId);
        log.setActionType(actionType);
        log.setReason(reason);
        log.setBeforeValue(beforeValue);
        log.setAfterValue(afterValue);
        log.setSourceOrderNo(sourceOrderNo);
        log.setCreatedAt(new Date());
        memberOperationLogRepository.save(log);
    }

    public boolean adminGrant(Long userId, int days, Long operatorId, String reason) {
        validateManualAction(userId, days, reason);
        grantMemberDuration(userId, days, "ADMIN-GRANT-" + userId + "-" + System.currentTimeMillis());
        saveOperationLog(userId, operatorId, "ADMIN_GRANT", reason, null, "days=" + days, null);
        return true;
    }

    public boolean adminExtend(Long userId, int days, Long operatorId, String reason) {
        validateManualAction(userId, days, reason);
        grantMemberDuration(userId, days, "ADMIN-EXTEND-" + userId + "-" + System.currentTimeMillis());
        saveOperationLog(userId, operatorId, "ADMIN_EXTEND", reason, null, "days=" + days, null);
        return true;
    }

    public boolean adminCancel(Long userId, Long operatorId, String reason) {
        if (userId == null) throw new SysException(ResponseCode.FAIL_PARA);
        if (reason == null || reason.trim().isEmpty()) throw new SysException(ResponseCode.FAIL_PARA);
        UserMember member = userMemberRepository.findOne(userId);
        if (member == null) return false;
        member.setLevel(LEVEL_NONE);
        member.setExpireAt(new Date());
        member.setUpdatedAt(new Date());
        userMemberRepository.save(member);
        saveOperationLog(userId, operatorId, "ADMIN_CANCEL", reason, null, "level=NONE", null);
        return true;
    }

    public Map<String, Object> findAdminUsers(MemberUserAdminQo qo) {
        Date now = new Date();
        List<MemberUserAdminQo> filtered = new ArrayList<>();
        for (UserMember member : userMemberRepository.findAll()) {
            boolean active = member.getExpireAt() != null && member.getExpireAt().after(now);
            if (qo != null && "ACTIVE".equals(qo.getMemberStatus()) && !active) continue;
            if (qo != null && "EXPIRED".equals(qo.getMemberStatus()) && active) continue;
            if (qo != null && qo.getExpireStart() != null && (member.getExpireAt() == null || member.getExpireAt().before(qo.getExpireStart()))) continue;
            if (qo != null && qo.getExpireEnd() != null && (member.getExpireAt() == null || member.getExpireAt().after(qo.getExpireEnd()))) continue;
            MemberUserAdminQo item = new MemberUserAdminQo();
            item.setUserId(member.getUserId());
            item.setLevelCode(member.getLevel());
            item.setExpireAt(member.getExpireAt());
            item.setMemberStatus(active ? "ACTIVE" : "EXPIRED");
            User user = userRepository.findById(member.getUserId());
            if (user != null) {
                if (qo != null && hasText(qo.getMobile()) && !contains(user.getMobile(), qo.getMobile())) continue;
                if (qo != null && hasText(qo.getNickname()) && !contains(user.getNickname(), qo.getNickname())) continue;
                item.setMobile(user.getMobile());
                item.setNickname(user.getNickname());
            }
            filtered.add(item);
        }
        return pageBody(filtered, qo);
    }

    public Map<String, Object> findOperationLogs(MemberOperationLogQo qo) {
        Page<MemberOperationLog> page = memberOperationLogRepository.findAll(logSpec(qo), pageable(qo, new Sort(Sort.Direction.DESC, "createdAt")));
        List<MemberOperationLogQo> content = page.getContent().stream()
                .map(log -> CopyUtil.copy(log, MemberOperationLogQo.class))
                .collect(Collectors.toList());
        return pageBody(page, content);
    }

    public Map<String, Object> findAdminPlans(MemberPlanQo qo) {
        Page<MemberPlan> page = memberPlanRepository.findAll(planSpec(qo), pageable(qo, new Sort(Sort.Direction.ASC, "sortNo")));
        List<MemberPlanQo> content = page.getContent().stream()
                .map(plan -> CopyUtil.copy(plan, MemberPlanQo.class))
                .collect(Collectors.toList());
        return pageBody(page, content);
    }

    public Map<String, Object> findAdminRules(MemberBenefitRuleQo qo) {
        Page<MemberBenefitRule> page = memberBenefitRuleRepository.findAll(ruleSpec(qo), pageable(qo, new Sort(Sort.Direction.ASC, "levelCode", "scene")));
        List<MemberBenefitRuleQo> content = page.getContent().stream()
                .map(rule -> CopyUtil.copy(rule, MemberBenefitRuleQo.class))
                .collect(Collectors.toList());
        return pageBody(page, content);
    }

    public Map<String, Object> findAdminOrders(MemberOrderQo qo) {
        Page<MemberOrder> page = memberOrderRepository.findAll(orderSpec(qo), pageable(qo, new Sort(Sort.Direction.DESC, "credate", "id")));
        List<MemberOrderQo> content = page.getContent().stream()
                .map(order -> CopyUtil.copy(order, MemberOrderQo.class))
                .collect(Collectors.toList());
        return pageBody(page, content);
    }

    public MemberPlanQo savePlan(MemberPlanQo qo) {
        if (qo == null || qo.getName() == null || qo.getName().trim().isEmpty()) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        MemberPlan entity = qo.getId() == null ? new MemberPlan() : memberPlanRepository.findById(qo.getId());
        if (entity == null) throw new SysException(ResponseCode.FAIL_NOT);
        CopyUtil.copyIgnoreNull(qo, entity);
        entity.setUpdatedAt(new Date());
        return CopyUtil.copy(memberPlanRepository.save(entity), MemberPlanQo.class);
    }

    public MemberBenefitRuleQo saveBenefitRule(MemberBenefitRuleQo qo) {
        if (qo == null || qo.getId() == null) throw new SysException(ResponseCode.FAIL_PARA);
        MemberBenefitRule entity = memberBenefitRuleRepository.findOne(qo.getId());
        if (entity == null) throw new SysException(ResponseCode.FAIL_NOT);
        CopyUtil.copyIgnoreNull(qo, entity);
        entity.setUpdatedAt(new Date());
        return CopyUtil.copy(memberBenefitRuleRepository.save(entity), MemberBenefitRuleQo.class);
    }

    private void validateManualAction(Long userId, int days, String reason) {
        if (userId == null) throw new SysException(ResponseCode.FAIL_PARA);
        if (days <= 0) throw new SysException(ResponseCode.FAIL_PARA);
        if (reason == null || reason.trim().isEmpty()) throw new SysException(ResponseCode.FAIL_PARA);
    }

    private Pageable pageable(PageQo qo, Sort sort) {
        int page = qo == null || qo.getPage() == null || qo.getPage() < 0 ? 0 : qo.getPage();
        int size = qo == null || qo.getSize() == null || qo.getSize() <= 0 ? 10 : qo.getSize();
        return sort == null ? new PageRequest(page, size) : new PageRequest(page, size, sort);
    }

    private Map<String, Object> pageBody(Page<?> page, List<?> content) {
        Map<String, Object> body = new HashMap<>();
        body.put("content", content);
        body.put("totalElements", page.getTotalElements());
        body.put("totalPages", page.getTotalPages());
        body.put("number", page.getNumber());
        body.put("size", page.getSize());
        return body;
    }

    private Map<String, Object> pageBody(List<?> content, PageQo qo) {
        int page = qo == null || qo.getPage() == null || qo.getPage() < 0 ? 0 : qo.getPage();
        int size = qo == null || qo.getSize() == null || qo.getSize() <= 0 ? 10 : qo.getSize();
        int from = Math.min(page * size, content.size());
        int to = Math.min(from + size, content.size());
        Map<String, Object> body = new HashMap<>();
        body.put("content", content.subList(from, to));
        body.put("totalElements", (long) content.size());
        body.put("totalPages", (int) Math.ceil(content.size() / (double) size));
        body.put("number", page);
        body.put("size", size);
        return body;
    }

    private Specification<MemberPlan> planSpec(final MemberPlanQo qo) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (qo != null && hasText(qo.getName())) ps.add(cb.like(root.get("name"), "%" + qo.getName().trim() + "%"));
            if (qo != null && hasText(qo.getStatus())) ps.add(cb.equal(root.get("status"), qo.getStatus()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private Specification<MemberBenefitRule> ruleSpec(final MemberBenefitRuleQo qo) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (qo != null && hasText(qo.getLevelCode())) ps.add(cb.equal(root.get("levelCode"), qo.getLevelCode()));
            if (qo != null && hasText(qo.getScene())) ps.add(cb.equal(root.get("scene"), qo.getScene()));
            if (qo != null && hasText(qo.getStatus())) ps.add(cb.equal(root.get("status"), qo.getStatus()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private Specification<MemberOrder> orderSpec(final MemberOrderQo qo) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (qo != null && qo.getUserId() != null) ps.add(cb.equal(root.get("userId"), qo.getUserId()));
            if (qo != null && hasText(qo.getStatus())) ps.add(cb.equal(root.get("status"), qo.getStatus()));
            if (qo != null && hasText(qo.getChannel())) ps.add(cb.equal(root.get("channel"), qo.getChannel()));
            if (qo != null && hasText(qo.getOutTradeNo())) ps.add(cb.like(root.get("outTradeNo"), "%" + qo.getOutTradeNo().trim() + "%"));
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private Specification<MemberOperationLog> logSpec(final MemberOperationLogQo qo) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (qo != null && qo.getUserId() != null) ps.add(cb.equal(root.get("userId"), qo.getUserId()));
            if (qo != null && qo.getOperatorId() != null) ps.add(cb.equal(root.get("operatorId"), qo.getOperatorId()));
            if (qo != null && hasText(qo.getActionType())) ps.add(cb.equal(root.get("actionType"), qo.getActionType()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean contains(String source, String keyword) {
        return source != null && keyword != null && source.toLowerCase().contains(keyword.trim().toLowerCase());
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
