package com.ypat.service;

import com.ypat.CheckinRecordQo;
import com.ypat.CheckinResultQo;
import com.ypat.CheckinRuleQo;
import com.ypat.CheckinTodayQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.CheckinRecord;
import com.ypat.entity.CheckinRule;
import com.ypat.entity.Record;
import com.ypat.entity.User;
import com.ypat.enums.RecordType;
import com.ypat.enums.YesNo;
import com.ypat.repository.CheckinRecordRepository;
import com.ypat.repository.CheckinRuleRepository;
import com.ypat.repository.RecordRepository;
import com.ypat.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class CheckinService {
    private static final ZoneId CHECKIN_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int DEFAULT_REWARD_PPD = 1;
    private static final String DEFAULT_TITLE = "每日签到";
    private static final String DEFAULT_CONTENT = "签到成功可获得 1 拍豆";
    private static final String MESSAGE_ALREADY_CHECKED = "今日已签到";
    private static final String MESSAGE_CLOSED = "签到活动暂未开启";
    private static final String MESSAGE_SUCCESS = "签到成功";

    private final CheckinRuleRepository checkinRuleRepository;
    private final CheckinRecordRepository checkinRecordRepository;
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    public CheckinService(CheckinRuleRepository checkinRuleRepository,
                          CheckinRecordRepository checkinRecordRepository,
                          RecordRepository recordRepository,
                          UserRepository userRepository) {
        this.checkinRuleRepository = checkinRuleRepository;
        this.checkinRecordRepository = checkinRecordRepository;
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    public CheckinTodayQo today(Long userId) {
        validateUserId(userId);
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        CheckinRule rule = getOrDefaultRule();
        String checkinDate = todayString();
        CheckinRecord record = checkinRecordRepository.findByUseridAndCheckinDate(userId, checkinDate);

        CheckinTodayQo qo = new CheckinTodayQo();
        qo.setEnabled(isEnabled(rule));
        qo.setCheckedIn(record != null);
        qo.setRewardPpd(rule.getRewardPpd());
        qo.setConfirmTitle(rule.getConfirmTitle());
        qo.setConfirmContent(rule.getConfirmContent());
        qo.setCheckinDate(checkinDate);
        return qo;
    }

    public CheckinRuleQo getRule() {
        return toRuleQo(getOrDefaultRule());
    }

    public CheckinRuleQo saveRule(CheckinRuleQo qo) {
        validateRule(qo);
        Date now = new Date();
        CheckinRule rule = qo.getId() == null ? null : checkinRuleRepository.findOne(qo.getId());
        if (rule == null) {
            rule = checkinRuleRepository.findTopByOrderByIdAsc();
        }
        if (rule == null) {
            rule = new CheckinRule();
            rule.setCreatedAt(now);
        }
        rule.setEnabled(qo.getEnabled());
        rule.setRewardPpd(qo.getRewardPpd());
        rule.setConfirmTitle(qo.getConfirmTitle().trim());
        rule.setConfirmContent(qo.getConfirmContent().trim());
        rule.setUpdatedAt(now);
        return toRuleQo(checkinRuleRepository.save(rule));
    }

    public CheckinResultQo doCheckin(Long userId) {
        validateUserId(userId);
        CheckinRule rule = getOrDefaultRule();
        if (!isEnabled(rule)) {
            return result(false, 0, null, null, MESSAGE_CLOSED);
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            throw new SysException(ResponseCode.FAIL_NOT);
        }

        String checkinDate = todayString();
        CheckinRecord existing = checkinRecordRepository.findByUseridAndCheckinDate(userId, checkinDate);
        if (existing != null) {
            return result(true, 0, currentPpd(user), existing.getRecordId(), MESSAGE_ALREADY_CHECKED);
        }

        Date now = new Date();
        CheckinRecord checkinRecord = new CheckinRecord();
        checkinRecord.setUserid(userId);
        checkinRecord.setCheckinDate(checkinDate);
        checkinRecord.setRewardPpd(rule.getRewardPpd());
        checkinRecord.setCreatedAt(now);
        try {
            checkinRecord = checkinRecordRepository.save(checkinRecord);
        } catch (DataIntegrityViolationException e) {
            CheckinRecord racedRecord = checkinRecordRepository.findByUseridAndCheckinDate(userId, checkinDate);
            if (racedRecord == null) {
                throw e;
            }
            User latestUser = userRepository.findById(userId);
            if (latestUser == null) {
                throw new SysException(ResponseCode.FAIL_NOT);
            }
            return result(true, 0, currentPpd(latestUser), racedRecord.getRecordId(), MESSAGE_ALREADY_CHECKED);
        }

        Record record = new Record();
        record.setType(RecordType.CHECKIN.value);
        record.setCredate(now);
        record.setPpd(rule.getRewardPpd());
        record.setUserid(userId);
        record = recordRepository.save(record);

        Integer currentPpd = currentPpd(user) + rule.getRewardPpd();
        user.setPpd(currentPpd);
        userRepository.save(user);

        checkinRecord.setRecordId(record.getId());
        checkinRecordRepository.save(checkinRecord);

        return result(true, rule.getRewardPpd(), currentPpd, record.getId(), MESSAGE_SUCCESS);
    }

    public Map<String, Object> findRecords(CheckinRecordQo queryQo) {
        final CheckinRecordQo query = queryQo == null ? new CheckinRecordQo() : queryQo;
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(query.getPage(), query.getSize(), sort);
        Page<CheckinRecord> recordPage = checkinRecordRepository.findAll(new Specification<CheckinRecord>() {
            @Override
            public Predicate toPredicate(Root<CheckinRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (query.getUserid() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("userid"), query.getUserid()));
                }
                if (isNotBlank(query.getMobile())) {
                    String queryMobile = query.getMobile().trim();
                    Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
                    Root<User> userRoot = subquery.from(User.class);
                    subquery.select(userRoot.get("id"));
                    subquery.where(criteriaBuilder.equal(userRoot.get("mobile"), queryMobile));
                    predicates.add(root.get("userid").in(subquery));
                }
                if (isNotBlank(query.getDateFrom())) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<String>get("checkinDate"), query.getDateFrom().trim()));
                }
                if (isNotBlank(query.getDateTo())) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<String>get("checkinDate"), query.getDateTo().trim()));
                }
                criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
                return criteriaQuery.getRestriction();
            }
        }, pageable);

        List<CheckinRecordQo> content = new ArrayList<CheckinRecordQo>();
        if (!CollectionUtils.isEmpty(recordPage.getContent())) {
            for (CheckinRecord record : recordPage.getContent()) {
                content.add(toRecordQo(record));
            }
        }

        Map<String, Object> page = new HashMap<String, Object>();
        page.put("content", content);
        page.put("totalElements", recordPage.getTotalElements());
        page.put("totalPages", recordPage.getTotalPages());
        page.put("number", recordPage.getNumber());
        page.put("size", recordPage.getSize());
        return page;
    }

    private CheckinRule getOrDefaultRule() {
        CheckinRule rule = checkinRuleRepository.findTopByOrderByIdAsc();
        if (rule != null) return rule;

        CheckinRule defaultRule = new CheckinRule();
        defaultRule.setEnabled(YesNo.yes.value);
        defaultRule.setRewardPpd(DEFAULT_REWARD_PPD);
        defaultRule.setConfirmTitle(DEFAULT_TITLE);
        defaultRule.setConfirmContent(DEFAULT_CONTENT);
        return defaultRule;
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
    }

    private void validateRule(CheckinRuleQo qo) {
        if (qo == null || !isYesNo(qo.getEnabled()) || qo.getRewardPpd() == null || qo.getRewardPpd() < 0
                || !isNotBlank(qo.getConfirmTitle()) || !isNotBlank(qo.getConfirmContent())
                || qo.getConfirmTitle().trim().length() > 64 || qo.getConfirmContent().trim().length() > 256) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
    }

    private boolean isEnabled(CheckinRule rule) {
        return rule != null && YesNo.yes.value.equals(rule.getEnabled());
    }

    private boolean isYesNo(String value) {
        return YesNo.yes.value.equals(value) || YesNo.no.value.equals(value);
    }

    private boolean isNotBlank(String value) {
        return value != null && value.trim().length() > 0;
    }

    private String todayString() {
        return LocalDate.now(CHECKIN_ZONE).toString();
    }

    private Integer currentPpd(User user) {
        return user.getPpd() == null ? 0 : user.getPpd();
    }

    private CheckinResultQo result(Boolean checkedIn, Integer rewardPpd, Integer currentPpd, Long recordId, String message) {
        CheckinResultQo qo = new CheckinResultQo();
        qo.setCheckedIn(checkedIn);
        qo.setRewardPpd(rewardPpd);
        qo.setCurrentPpd(currentPpd);
        qo.setRecordId(recordId);
        qo.setMessage(message);
        return qo;
    }

    private CheckinRuleQo toRuleQo(CheckinRule rule) {
        CheckinRuleQo qo = new CheckinRuleQo();
        qo.setId(rule.getId());
        qo.setEnabled(rule.getEnabled());
        qo.setRewardPpd(rule.getRewardPpd());
        qo.setConfirmTitle(rule.getConfirmTitle());
        qo.setConfirmContent(rule.getConfirmContent());
        qo.setCreatedAt(rule.getCreatedAt());
        qo.setUpdatedAt(rule.getUpdatedAt());
        return qo;
    }

    private CheckinRecordQo toRecordQo(CheckinRecord record) {
        CheckinRecordQo qo = new CheckinRecordQo();
        qo.setId(record.getId());
        qo.setUserid(record.getUserid());
        qo.setCheckinDate(record.getCheckinDate());
        qo.setRewardPpd(record.getRewardPpd());
        qo.setRecordId(record.getRecordId());
        qo.setCreatedAt(record.getCreatedAt());
        User user = record.getUserid() == null ? null : userRepository.findById(record.getUserid());
        if (user != null) {
            qo.setMobile(user.getMobile());
            qo.setNickname(user.getNickname());
        }
        return qo;
    }
}
