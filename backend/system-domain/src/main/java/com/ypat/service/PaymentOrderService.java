package com.ypat.service;

import com.ypat.PageQo;
import com.ypat.PaymentOrderQo;
import com.ypat.entity.PaymentOrder;
import com.ypat.enums.PaymentStatus;
import com.ypat.repository.PaymentOrderRepository;
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
public class PaymentOrderService {

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    public PaymentOrder createPending(String businessType, String businessOrderNo, String outTradeNo,
                                      Long userId, String channel, Integer amountFen) {
        Date now = new Date();
        PaymentOrder order = new PaymentOrder();
        order.setPaymentNo(generatePaymentNo(userId));
        order.setBusinessType(businessType);
        order.setBusinessOrderNo(businessOrderNo);
        order.setOutTradeNo(outTradeNo);
        order.setUserId(userId);
        order.setChannel(channel);
        order.setAmountFen(amountFen);
        order.setStatus(PaymentStatus.PENDING.value);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setVersion(0);
        return paymentOrderRepository.save(order);
    }

    public boolean markPaidIfPending(String outTradeNo, String txId, Date paidAt, String eventId, String digest) {
        Date now = paidAt == null ? new Date() : paidAt;
        int updated = paymentOrderRepository.markPaidIfPending(outTradeNo, txId, now, now, eventId, digest);
        return updated == 1;
    }

    public PaymentOrderQo findByOutTradeNo(String outTradeNo) {
        return CopyUtil.copy(paymentOrderRepository.findByOutTradeNo(outTradeNo), PaymentOrderQo.class);
    }

    public Map<String, Object> findAdminPage(PaymentOrderQo qo) {
        Page<PaymentOrder> page = paymentOrderRepository.findAll(paymentSpec(qo),
                pageable(qo, new Sort(Sort.Direction.DESC, "createdAt", "id")));
        List<PaymentOrderQo> content = page.getContent().stream()
                .map(order -> CopyUtil.copy(order, PaymentOrderQo.class))
                .collect(Collectors.toList());
        return pageBody(page, content);
    }

    private String generatePaymentNo(Long userId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "P" + sdf.format(new Date()) + (userId == null ? "" : userId) +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private Specification<PaymentOrder> paymentSpec(final PaymentOrderQo qo) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (qo != null && hasText(qo.getStatus())) ps.add(cb.equal(root.get("status"), qo.getStatus()));
            if (qo != null && hasText(qo.getChannel())) ps.add(cb.equal(root.get("channel"), qo.getChannel()));
            if (qo != null && hasText(qo.getOutTradeNo())) ps.add(cb.like(root.get("outTradeNo"), "%" + qo.getOutTradeNo().trim() + "%"));
            if (qo != null && qo.getUserId() != null) ps.add(cb.equal(root.get("userId"), qo.getUserId()));
            if (qo != null && hasText(qo.getBusinessType())) ps.add(cb.equal(root.get("businessType"), qo.getBusinessType()));
            if (qo != null && hasText(qo.getBusinessOrderNo())) ps.add(cb.like(root.get("businessOrderNo"), "%" + qo.getBusinessOrderNo().trim() + "%"));
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
