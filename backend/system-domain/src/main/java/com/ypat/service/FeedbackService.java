package com.ypat.service;

import com.ypat.FeedbackQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.Feedback;
import com.ypat.entity.User;
import com.ypat.repository.FeedbackRepository;
import com.ypat.repository.UserRepository;
import com.ypat.util.CopyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class FeedbackService {
    private static final String TYPE_FUNCTION = "function";
    private static final String TYPE_EXPERIENCE = "experience";
    private static final String TYPE_ACCOUNT = "account";
    private static final String TYPE_PAYMENT = "payment";
    private static final String TYPE_CONTENT = "content";
    private static final String TYPE_OTHER = "other";
    private static final String STATUS_PENDING = "0";
    private static final String STATUS_HANDLED = "1";
    private static final String STATUS_IGNORED = "2";

    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private UserRepository userRepository;

    public FeedbackQo add(FeedbackQo feedbackQo) {
        if (feedbackQo == null || feedbackQo.getUserid() == null) {
            throw new SysException(ResponseCode.FAIL_AUTH);
        }
        if (StringUtils.isBlank(feedbackQo.getContent())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        Date now = new Date();
        Feedback feedback = CopyUtil.copy(feedbackQo, Feedback.class);
        feedback.setType(normalizeType(feedback.getType()));
        feedback.setStatus(STATUS_PENDING);
        feedback.setCredate(now);
        feedback.setUpddate(now);
        feedbackRepository.save(feedback);
        return CopyUtil.copy(feedback, FeedbackQo.class);
    }

    public Map<String, Object> adminList(Integer page, Integer size, String status, String type, Long userId) {
        final int currentPage = page == null || page < 0 ? 0 : page;
        final int pageSize = size == null || size <= 0 ? 10 : Math.min(size, 50);
        final String normalizedStatus = normalizeQueryStatus(status);
        final String normalizedType = normalizeQueryType(type);

        Specification<Feedback> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (normalizedStatus != null) {
                predicates.add(cb.equal(root.get("status"), normalizedStatus));
            }
            if (normalizedType != null) {
                predicates.add(cb.equal(root.get("type"), normalizedType));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("userid"), userId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Feedback> result = feedbackRepository.findAll(
                spec,
                new PageRequest(currentPage, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "credate"))));

        List<Map<String, Object>> content = new ArrayList<>();
        for (Feedback feedback : result.getContent()) {
            content.add(toAdminItem(feedback));
        }

        Map<String, Object> res = new HashMap<>();
        res.put("content", content);
        res.put("totalElements", result.getTotalElements());
        res.put("totalPages", result.getTotalPages());
        return res;
    }

    public Map<String, Object> adminDetail(Long id) {
        if (id == null || id <= 0) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        Feedback feedback = feedbackRepository.findOne(id);
        if (feedback == null) {
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        return toAdminItem(feedback);
    }

    public void adminHandle(FeedbackQo qo) {
        if (qo == null || qo.getId() == null || qo.getId() <= 0 || StringUtils.isBlank(qo.getStatus())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        String targetStatus = normalizeHandleStatus(qo.getStatus());
        Date now = new Date();
        int updated = feedbackRepository.updatePendingStatus(
                qo.getId(),
                targetStatus,
                StringUtils.trimToNull(qo.getHandleReason()),
                qo.getHandledBy(),
                now,
                now);
        if (updated <= 0) {
            throw new SysException(ResponseCode.FAIL_EXIST.getCode(), "反馈已处理");
        }
    }

    private Map<String, Object> toAdminItem(Feedback feedback) {
        Map<String, Object> item = new HashMap<>();
        String type = normalizeType(feedback.getType());
        item.put("id", feedback.getId());
        item.put("userId", feedback.getUserid());
        item.put("type", type);
        item.put("typeText", typeText(type));
        item.put("content", feedback.getContent());
        item.put("contact", feedback.getContact());
        item.put("pics", feedback.getPics());
        item.put("status", feedback.getStatus());
        item.put("statusText", statusText(feedback.getStatus()));
        item.put("handleReason", feedback.getHandleReason());
        item.put("handledBy", feedback.getHandledBy());
        item.put("handledAt", feedback.getHandledAt());
        item.put("createdAt", feedback.getCredate());
        item.put("updatedAt", feedback.getUpddate());

        User user = feedback.getUserid() == null ? null : userRepository.findById(feedback.getUserid());
        if (user != null) {
            item.put("userNickname", user.getNickname());
        }
        return item;
    }

    private String normalizeQueryStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return null;
        }
        return normalizeStatus(status);
    }

    private String normalizeHandleStatus(String status) {
        String normalized = normalizeStatus(status);
        if (STATUS_HANDLED.equals(normalized) || STATUS_IGNORED.equals(normalized)) {
            return normalized;
        }
        throw new SysException(ResponseCode.FAIL_PARA);
    }

    private String normalizeStatus(String status) {
        if (STATUS_PENDING.equals(status) || "pending".equalsIgnoreCase(status)) {
            return STATUS_PENDING;
        }
        if (STATUS_HANDLED.equals(status) || "handled".equalsIgnoreCase(status)) {
            return STATUS_HANDLED;
        }
        if (STATUS_IGNORED.equals(status) || "ignored".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status)) {
            return STATUS_IGNORED;
        }
        throw new SysException(ResponseCode.FAIL_PARA);
    }

    private String normalizeQueryType(String type) {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        return normalizeType(type);
    }

    private String normalizeType(String type) {
        if (StringUtils.isBlank(type)) {
            return TYPE_OTHER;
        }
        String normalized = StringUtils.trim(type);
        if (TYPE_FUNCTION.equals(normalized)
                || TYPE_EXPERIENCE.equals(normalized)
                || TYPE_ACCOUNT.equals(normalized)
                || TYPE_PAYMENT.equals(normalized)
                || TYPE_CONTENT.equals(normalized)
                || TYPE_OTHER.equals(normalized)) {
            return normalized;
        }
        throw new SysException(ResponseCode.FAIL_PARA);
    }

    private String statusText(String status) {
        if (STATUS_HANDLED.equals(status)) {
            return "已处理";
        }
        if (STATUS_IGNORED.equals(status)) {
            return "已忽略";
        }
        return "待处理";
    }

    private String typeText(String type) {
        if (TYPE_FUNCTION.equals(type)) {
            return "功能异常";
        }
        if (TYPE_EXPERIENCE.equals(type)) {
            return "体验建议";
        }
        if (TYPE_ACCOUNT.equals(type)) {
            return "账号/资料";
        }
        if (TYPE_PAYMENT.equals(type)) {
            return "支付/订单";
        }
        if (TYPE_CONTENT.equals(type)) {
            return "内容/用户举报";
        }
        return "其他";
    }
}
