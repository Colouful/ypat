package com.ypat.service;

import com.ypat.DepositConfigQo;
import com.ypat.DepositOrderQo;
import com.ypat.PageQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.DepositConfig;
import com.ypat.entity.DepositOrder;
import com.ypat.entity.User;
import com.ypat.enums.PaymentStatus;
import com.ypat.repository.DepositConfigRepository;
import com.ypat.repository.DepositOrderRepository;
import com.ypat.repository.UserRepository;
import com.ypat.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DepositService {

    private static final Long CONFIG_ID = 1L;
    private static final long REUSE_WINDOW_MILLIS = 5 * 60 * 1000L;

    @Autowired
    private DepositConfigRepository depositConfigRepository;
    @Autowired
    private DepositOrderRepository depositOrderRepository;
    @Autowired
    private UserRepository userRepository;

    public DepositConfigQo getConfig() {
        DepositConfig config = loadConfig();
        config.setDisplayAmountFen(effectiveAmountFen(config));
        return CopyUtil.copy(config, DepositConfigQo.class);
    }

    public DepositConfigQo saveConfig(DepositConfigQo qo) {
        if (qo == null) throw new SysException(ResponseCode.FAIL_PARA);
        DepositConfig config = qo.getId() == null ? loadConfig() : depositConfigRepository.findOne(qo.getId());
        if (config == null) config = newDefaultConfig();
        CopyUtil.copyIgnoreNull(qo, config);
        config.setDisplayAmountFen(effectiveAmountFen(config));
        config.setUpdatedAt(new Date());
        return CopyUtil.copy(depositConfigRepository.save(config), DepositConfigQo.class);
    }

    public DepositOrderQo createPendingOrder(Long userId, String channel) {
        if (userId == null || !hasText(channel)) throw new SysException(ResponseCode.FAIL_PARA);
        DepositConfig config = loadConfig();
        Integer amountFen = effectiveAmountFen(config);
        Date now = new Date();

        Page<DepositOrder> recentPage = depositOrderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                userId,
                PaymentStatus.PENDING.value,
                new PageRequest(0, 10, new Sort(Sort.Direction.DESC, "createdAt"))
        );
        for (DepositOrder recent : recentPage.getContent()) {
            if (samePendingOrder(recent, channel, amountFen, now)) {
                return CopyUtil.copy(recent, DepositOrderQo.class);
            }
        }

        DepositOrder order = new DepositOrder();
        order.setOutTradeNo(generateOutTradeNo(userId));
        order.setUserId(userId);
        order.setAmountFen(amountFen);
        order.setChannel(channel);
        order.setStatus(PaymentStatus.PENDING.value);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setVersion(0);
        return CopyUtil.copy(depositOrderRepository.save(order), DepositOrderQo.class);
    }

    public boolean markPaid(String outTradeNo, String txId, Date paidAt) {
        Date now = paidAt == null ? new Date() : paidAt;
        int updated = depositOrderRepository.markPaidIfPending(outTradeNo, txId, now, now);
        if (updated != 1) return false;

        DepositOrder order = depositOrderRepository.findByOutTradeNo(outTradeNo);
        if (order == null) throw new SysException(ResponseCode.FAIL_NOT);
        User user = userRepository.findById(order.getUserId());
        if (user == null) throw new SysException(ResponseCode.FAIL_NOT);
        user.setCreditflag("1");
        userRepository.save(user);
        return true;
    }

    public DepositOrderQo getOrder(String outTradeNo, Long userId) {
        DepositOrder order = depositOrderRepository.findByOutTradeNo(outTradeNo);
        if (order == null) return null;
        if (userId != null && !userId.equals(order.getUserId())) {
            throw new SysException(ResponseCode.FAIL_VAL);
        }
        return CopyUtil.copy(order, DepositOrderQo.class);
    }

    public Map<String, Object> findAdminOrders(DepositOrderQo qo) {
        Page<DepositOrder> page = depositOrderRepository.findAll(depositSpec(qo),
                pageable(qo, new Sort(Sort.Direction.DESC, "createdAt", "id")));
        List<DepositOrderQo> content = page.getContent().stream()
                .map(order -> CopyUtil.copy(order, DepositOrderQo.class))
                .collect(Collectors.toList());
        return pageBody(page, content);
    }

    private DepositConfig loadConfig() {
        DepositConfig config = depositConfigRepository.findOne(CONFIG_ID);
        return config == null ? newDefaultConfig() : config;
    }

    private DepositConfig newDefaultConfig() {
        DepositConfig config = new DepositConfig();
        config.setId(CONFIG_ID);
        config.setEnabled("1");
        config.setAmountFen(19900);
        config.setTestEnabled("0");
        config.setTestAmountFen(1);
        config.setRefundWaitDays(0);
        config.setEarlyRefundFeeRate(0);
        config.setDisplayAmountFen(config.getAmountFen());
        config.setUpdatedAt(new Date());
        return config;
    }

    private Integer effectiveAmountFen(DepositConfig config) {
        if (config == null) throw new SysException(ResponseCode.FAIL_PAY_CONFIG);
        Integer amount = "1".equals(config.getTestEnabled()) ? config.getTestAmountFen() : config.getAmountFen();
        if (amount == null || amount <= 0) throw new SysException(ResponseCode.FAIL_PAY_AMOUNT);
        return amount;
    }

    private boolean samePendingOrder(DepositOrder order, String channel, Integer amountFen, Date now) {
        return order != null
                && PaymentStatus.PENDING.value.equals(order.getStatus())
                && channel.equals(order.getChannel())
                && amountFen.equals(order.getAmountFen())
                && order.getCreatedAt() != null
                && now.getTime() - order.getCreatedAt().getTime() < REUSE_WINDOW_MILLIS;
    }

    private String generateOutTradeNo(Long userId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "D" + sdf.format(new Date()) + userId +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private Specification<DepositOrder> depositSpec(final DepositOrderQo qo) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (qo != null && qo.getUserId() != null) ps.add(cb.equal(root.get("userId"), qo.getUserId()));
            if (qo != null && hasText(qo.getStatus())) ps.add(cb.equal(root.get("status"), qo.getStatus()));
            if (qo != null && hasText(qo.getChannel())) ps.add(cb.equal(root.get("channel"), qo.getChannel()));
            if (qo != null && hasText(qo.getOutTradeNo())) ps.add(cb.like(root.get("outTradeNo"), "%" + qo.getOutTradeNo().trim() + "%"));
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private Pageable pageable(PageQo qo, Sort sort) {
        int page = qo == null || qo.getPage() == null || qo.getPage() < 0 ? 0 : qo.getPage();
        int size = qo == null || qo.getSize() == null || qo.getSize() <= 0 ? 10 : qo.getSize();
        return new PageRequest(page, size, sort);
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
