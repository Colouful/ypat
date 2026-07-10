package com.ypat.service;

import com.ypat.MessagePushLogQo;
import com.ypat.entity.MessagePushLog;
import com.ypat.enums.MessagePushEventType;
import com.ypat.enums.YesNo;
import com.ypat.repository.MessagePushLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class MessagePushLogService {
    private static final int MAX_TEXT_LENGTH = 255;
    private static final int MAX_OPENID_LENGTH = 128;
    private static final int MAX_TEMPLATE_LENGTH = 128;
    private static final int MAX_CODE_LENGTH = 32;
    private static final int MAX_BODY_LENGTH = 1024;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String[] DATE_PATTERNS = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"};

    @Autowired
    private MessagePushLogRepository messagePushLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void record(MessagePushLogQo qo) {
        if (qo == null || isBlank(qo.getEventType())) {
            return;
        }
        MessagePushLog log = new MessagePushLog();
        log.setEventType(trim(qo.getEventType(), 32));
        log.setBusinessType(trim(qo.getBusinessType(), 32));
        log.setMessageId(qo.getMessageId());
        log.setYpatid(qo.getYpatid());
        log.setSendperid(qo.getSendperid());
        log.setRecperid(qo.getRecperid());
        log.setTouserOpenid(trim(qo.getTouserOpenid(), MAX_OPENID_LENGTH));
        log.setTemplateId(trim(qo.getTemplateId(), MAX_TEMPLATE_LENGTH));
        log.setPageUrl(trim(qo.getPageUrl(), MAX_TEXT_LENGTH));
        log.setSuccess(trim(qo.getSuccess(), 8));
        log.setWechatErrcode(trim(qo.getWechatErrcode(), MAX_CODE_LENGTH));
        log.setWechatErrmsg(trim(qo.getWechatErrmsg(), MAX_TEXT_LENGTH));
        log.setResponseBody(trim(qo.getResponseBody(), MAX_BODY_LENGTH));
        log.setRemark(trim(qo.getRemark(), MAX_TEXT_LENGTH));
        log.setCreatedAt(new Date());
        messagePushLogRepository.save(log);
    }

    public Map<String, Object> findPage(MessagePushLogQo queryQo) {
        final MessagePushLogQo query = normalizeQuery(queryQo);
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(query.getPage(), query.getSize(), sort);
        Page<MessagePushLog> logPage = messagePushLogRepository.findAll(buildSpec(query), pageable);

        List<MessagePushLogQo> content = new ArrayList<MessagePushLogQo>();
        for (MessagePushLog log : logPage.getContent()) {
            content.add(toQo(log));
        }

        Map<String, Object> page = new HashMap<String, Object>();
        page.put("content", content);
        page.put("totalElements", logPage.getTotalElements());
        page.put("totalPages", logPage.getTotalPages());
        page.put("number", logPage.getNumber());
        page.put("size", logPage.getSize());
        return page;
    }

    public MessagePushLogQo stats(MessagePushLogQo queryQo) {
        MessagePushLogQo query = normalizeQuery(queryQo);
        List<MessagePushLog> logs = messagePushLogRepository.findAll(buildSpec(query));
        long total = logs.size();
        long success = 0L;
        long wechat = 0L;
        long inApp = 0L;

        for (MessagePushLog log : logs) {
            if (YesNo.yes.value.equals(log.getSuccess())) {
                success++;
            }
            if (MessagePushEventType.WECHAT_SUBSCRIBE_SENT.value.equals(log.getEventType())) {
                wechat++;
            }
            if (MessagePushEventType.IN_APP_CREATED.value.equals(log.getEventType())) {
                inApp++;
            }
        }

        long failed = total - success;
        MessagePushLogQo qo = new MessagePushLogQo();
        qo.setTotal(total);
        qo.setSuccessCount(success);
        qo.setFailedCount(failed);
        qo.setWechatTotal(wechat);
        qo.setInAppTotal(inApp);
        qo.setFailedRate(total == 0 ? "0%" : String.format("%.2f%%", failed * 100.0 / total));
        return qo;
    }

    private MessagePushLogQo normalizeQuery(MessagePushLogQo queryQo) {
        MessagePushLogQo query = queryQo == null ? new MessagePushLogQo() : queryQo;
        if (query.getPage() == null || query.getPage() < 0) {
            query.setPage(0);
        }
        if (query.getSize() == null || query.getSize() <= 0) {
            query.setSize(10);
        }
        if (query.getSize() > MAX_PAGE_SIZE) {
            query.setSize(MAX_PAGE_SIZE);
        }
        return query;
    }

    private Specification<MessagePushLog> buildSpec(final MessagePushLogQo query) {
        return new Specification<MessagePushLog>() {
            @Override
            public Predicate toPredicate(Root<MessagePushLog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (!isBlank(query.getEventType())) {
                    predicates.add(criteriaBuilder.equal(root.get("eventType"), query.getEventType().trim()));
                }
                if (!isBlank(query.getBusinessType())) {
                    predicates.add(criteriaBuilder.equal(root.get("businessType"), query.getBusinessType().trim()));
                }
                if (!isBlank(query.getSuccess())) {
                    predicates.add(criteriaBuilder.equal(root.get("success"), query.getSuccess().trim()));
                }
                if (query.getMessageId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("messageId"), query.getMessageId()));
                }
                if (query.getYpatid() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("ypatid"), query.getYpatid()));
                }
                if (query.getSendperid() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("sendperid"), query.getSendperid()));
                }
                if (query.getRecperid() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("recperid"), query.getRecperid()));
                }
                if (!isBlank(query.getTouserOpenid())) {
                    predicates.add(criteriaBuilder.equal(root.get("touserOpenid"), query.getTouserOpenid().trim()));
                }
                if (!isBlank(query.getDateStart())) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<Date>get("createdAt"), parseDate(query.getDateStart().trim())));
                }
                if (!isBlank(query.getDateEnd())) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<Date>get("createdAt"), parseDate(query.getDateEnd().trim())));
                }
                criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
                return criteriaQuery.getRestriction();
            }
        };
    }

    private MessagePushLogQo toQo(MessagePushLog log) {
        MessagePushLogQo qo = new MessagePushLogQo();
        qo.setId(log.getId());
        qo.setEventType(log.getEventType());
        qo.setBusinessType(log.getBusinessType());
        qo.setMessageId(log.getMessageId());
        qo.setYpatid(log.getYpatid());
        qo.setSendperid(log.getSendperid());
        qo.setRecperid(log.getRecperid());
        qo.setTouserOpenid(log.getTouserOpenid());
        qo.setTemplateId(log.getTemplateId());
        qo.setPageUrl(log.getPageUrl());
        qo.setSuccess(log.getSuccess());
        qo.setWechatErrcode(log.getWechatErrcode());
        qo.setWechatErrmsg(log.getWechatErrmsg());
        qo.setResponseBody(log.getResponseBody());
        qo.setRemark(log.getRemark());
        qo.setCreatedAt(log.getCreatedAt());
        return qo;
    }

    private Date parseDate(String value) {
        for (String pattern : DATE_PATTERNS) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                format.setLenient(false);
                return format.parse(value);
            } catch (ParseException ignored) {
                // 尝试下一个支持的日期格式。
            }
        }
        throw new IllegalArgumentException("日期格式错误：" + value);
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
