package com.ypat.service;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.WorkComplainQo;
import com.ypat.entity.User;
import com.ypat.entity.Work;
import com.ypat.entity.WorkComplain;
import com.ypat.enums.WorkStatus;
import com.ypat.repository.UserRepository;
import com.ypat.repository.WorkComplainRepository;
import com.ypat.repository.WorkRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作品投诉服务
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkComplainService {

    private static final String STATUS_PENDING = "0";
    private static final String STATUS_HANDLED = "1";
    private static final String STATUS_REJECTED = "2";

    @Autowired private WorkRepository workRepository;
    @Autowired private WorkComplainRepository workComplainRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WorkService workService;

    public void complain(WorkComplainQo qo) {
        if (qo == null) throw new SysException(ResponseCode.FAIL_PARA);
        if (qo.getWorkId() == null) throw new SysException(ResponseCode.FAIL_PARA);
        Long workId = Long.parseLong(qo.getWorkId());
        Long userId = Long.parseLong(qo.getUserId());
        Work work = workRepository.findOne(workId);
        if (work == null || WorkStatus.xj.value.equals(work.getStatus()) || work.getDeletedFlag() != null && work.getDeletedFlag() == 1) {
            throw new SysException(ResponseCode.FAIL_WORK_NOT_FOUND);
        }
        if (userId.equals(work.getUserid())) {
            throw new SysException(ResponseCode.FAIL_VAL, "不能投诉自己的作品");
        }
        workService.complain(work, userId, qo.getReason(), qo.getContact());
    }

    public Map<String, Object> adminList(Integer page, Integer size, String status, Long workId, Long userId) {
        final int currentPage = page == null || page < 0 ? 0 : page;
        final int pageSize = size == null || size <= 0 ? 10 : Math.min(size, 50);
        final String normalizedStatus = normalizeQueryStatus(status);

        Specification<WorkComplain> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (normalizedStatus != null) {
                predicates.add(cb.equal(root.get("status"), normalizedStatus));
            }
            if (workId != null) {
                predicates.add(cb.equal(root.get("workId"), workId));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<WorkComplain> result = workComplainRepository.findAll(
                spec,
                new PageRequest(currentPage, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "createdAt"))));

        List<Map<String, Object>> content = new ArrayList<>();
        for (WorkComplain complain : result.getContent()) {
            content.add(toAdminItem(complain));
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
        WorkComplain complain = workComplainRepository.findOne(id);
        if (complain == null) {
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        return toAdminItem(complain);
    }

    public void adminHandle(WorkComplainQo qo) {
        if (qo == null || qo.getId() == null || qo.getId() <= 0 || StringUtils.isBlank(qo.getStatus())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }

        WorkComplain complain = workComplainRepository.findOne(qo.getId());
        if (complain == null) {
            throw new SysException(ResponseCode.FAIL_NOT);
        }

        String targetStatus = normalizeHandleStatus(qo.getStatus());
        workComplainRepository.updateStatus(qo.getId(), targetStatus);

        if (Boolean.TRUE.equals(qo.getOfflineWork())) {
            Work work = workRepository.findOne(complain.getWorkId());
            if (work == null || work.getDeletedFlag() != null && work.getDeletedFlag() == 1) {
                throw new SysException(ResponseCode.FAIL_WORK_NOT_FOUND);
            }
            String offlineReason = StringUtils.isBlank(qo.getReason()) ? "投诉处理下架" : qo.getReason();
            if (WorkStatus.shtg.value.equals(work.getStatus())) {
                workService.adminOffline(work.getId(), offlineReason);
            } else if (!WorkStatus.xj.value.equals(work.getStatus())) {
                workRepository.updateStatusAndAuditReason(work.getId(), WorkStatus.xj.value, offlineReason);
            }
        }
    }

    private Map<String, Object> toAdminItem(WorkComplain complain) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", complain.getId());
        item.put("workId", complain.getWorkId());
        item.put("userId", complain.getUserId());
        item.put("reason", complain.getReason());
        item.put("contact", complain.getContact());
        item.put("status", complain.getStatus());
        item.put("statusText", statusText(complain.getStatus()));
        item.put("createdAt", complain.getCreatedAt());

        Work work = workRepository.findOne(complain.getWorkId());
        if (work != null) {
            item.put("workDescription", work.getDescription());
            item.put("targetUserId", work.getUserid());
            User targetUser = userRepository.findById(work.getUserid());
            if (targetUser != null) {
                item.put("targetNickname", targetUser.getNickname());
            }
        }

        User complainUser = userRepository.findById(complain.getUserId());
        if (complainUser != null) {
            item.put("userNickname", complainUser.getNickname());
        }
        return item;
    }

    private String normalizeQueryStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return null;
        }
        if (STATUS_PENDING.equals(status) || "pending".equalsIgnoreCase(status)) {
            return STATUS_PENDING;
        }
        if (STATUS_HANDLED.equals(status) || "handled".equalsIgnoreCase(status)) {
            return STATUS_HANDLED;
        }
        if (STATUS_REJECTED.equals(status) || "rejected".equalsIgnoreCase(status)) {
            return STATUS_REJECTED;
        }
        throw new SysException(ResponseCode.FAIL_PARA);
    }

    private String normalizeHandleStatus(String status) {
        if ("handled".equalsIgnoreCase(status) || STATUS_HANDLED.equals(status)) {
            return STATUS_HANDLED;
        }
        if ("rejected".equalsIgnoreCase(status) || STATUS_REJECTED.equals(status)) {
            return STATUS_REJECTED;
        }
        throw new SysException(ResponseCode.FAIL_PARA);
    }

    private String statusText(String status) {
        if (STATUS_HANDLED.equals(status)) {
            return "handled";
        }
        if (STATUS_REJECTED.equals(status)) {
            return "rejected";
        }
        return "pending";
    }
}
