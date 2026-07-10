package com.ypat.service;

import com.ypat.InternalTestResourceQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.InternalTestResource;
import com.ypat.enums.InternalTestResourceMediaType;
import com.ypat.enums.InternalTestResourceStatus;
import com.ypat.enums.InternalTestResourceUsageType;
import com.ypat.repository.InternalTestResourceRepository;
import com.ypat.util.CommonUtils;
import com.ypat.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(rollbackFor = Exception.class)
public class InternalTestResourceService {

    @Autowired
    private InternalTestResourceRepository internalTestResourceRepository;

    public Map<String, Object> page(InternalTestResourceQo qo) {
        if (qo == null) {
            qo = new InternalTestResourceQo();
        }
        int page = qo.getPage() == null || qo.getPage() < 0 ? 0 : qo.getPage();
        int size = qo.getSize() == null || qo.getSize() <= 0 ? 20 : qo.getSize();
        Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.ASC, "sortNo").and(new Sort(Sort.Direction.DESC, "id")));
        Page<InternalTestResource> resourcePage = internalTestResourceRepository.findAll(buildSpecification(qo), pageable);

        List<InternalTestResourceQo> content = new ArrayList<InternalTestResourceQo>();
        for (InternalTestResource resource : resourcePage.getContent()) {
            content.add(CopyUtil.copy(resource, InternalTestResourceQo.class));
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("content", content);
        result.put("totalPages", resourcePage.getTotalPages());
        result.put("totalElements", resourcePage.getTotalElements());
        return result;
    }

    public Map<String, Object> batchSave(InternalTestResourceQo qo) {
        validateBatchQo(qo);
        List<String> urls = normalizeUrls(qo.getUrls());
        List<List<String>> groups = splitWorkGroups(urls, qo);
        List<InternalTestResourceQo> saved = new ArrayList<InternalTestResourceQo>();
        int duplicateCount = countInputDuplicateUrls(qo.getUrls());
        Date now = new Date();
        int groupIndex = 0;

        for (List<String> group : groups) {
            String groupNo = InternalTestResourceUsageType.work.value.equals(qo.getUsageType())
                    ? buildGroupNo(groupIndex++)
                    : null;
            int sort = 0;
            for (String url : group) {
                if (existsByUrl(url)) {
                    duplicateCount++;
                    continue;
                }
                InternalTestResource resource = CopyUtil.copy(qo, InternalTestResource.class);
                resource.setId(null);
                resource.setUrl(url);
                resource.setGroupNo(groupNo);
                resource.setGroupSortNo(sort++);
                resource.setUsedFlag(0);
                resource.setStatus(defaultStatus(qo.getStatus()));
                if (resource.getSortNo() == null) {
                    resource.setSortNo(0);
                }
                resource.setCreatedAt(now);
                resource.setUpdatedAt(now);
                saved.add(CopyUtil.copy(internalTestResourceRepository.save(resource), InternalTestResourceQo.class));
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("content", saved);
        result.put("createdCount", saved.size());
        result.put("duplicateCount", duplicateCount);
        return result;
    }

    public Map<String, Object> listAvailableGroups(InternalTestResourceQo qo) {
        if (qo == null) {
            qo = new InternalTestResourceQo();
        }
        qo.setUsageType(InternalTestResourceUsageType.work.value);
        qo.setStatus(InternalTestResourceStatus.enabled.value);
        qo.setUsedFlag(0);

        int page = qo.getPage() == null || qo.getPage() < 0 ? 0 : qo.getPage();
        int size = qo.getSize() == null || qo.getSize() <= 0 ? 20 : qo.getSize();
        List<InternalTestResource> resources = internalTestResourceRepository.findAll(buildSpecification(qo),
                new Sort(Sort.Direction.ASC, "groupNo")
                        .and(new Sort(Sort.Direction.ASC, "groupSortNo"))
                        .and(new Sort(Sort.Direction.ASC, "sortNo"))
                        .and(new Sort(Sort.Direction.DESC, "id")));
        Map<String, List<InternalTestResourceQo>> groups = new LinkedHashMap<String, List<InternalTestResourceQo>>();
        for (InternalTestResource resource : resources) {
            InternalTestResourceQo item = CopyUtil.copy(resource, InternalTestResourceQo.class);
            String groupNo = CommonUtils.isNotNull(item.getGroupNo()) ? item.getGroupNo() : "single-" + item.getId();
            if (!groups.containsKey(groupNo)) {
                groups.put(groupNo, new ArrayList<InternalTestResourceQo>());
            }
            groups.get(groupNo).add(item);
        }

        List<List<InternalTestResourceQo>> pageGroups = pageGroups(groups, page, size);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("content", pageGroups);
        result.put("totalPages", calculateTotalPages(groups.size(), size));
        result.put("totalElements", groups.size());
        return result;
    }

    public void markResourcesUsed(List<InternalTestResource> resources, String batchNo, String targetType, Long targetId) {
        if (CommonUtils.isNull(resources)) {
            return;
        }
        Date now = new Date();
        for (InternalTestResource resource : resources) {
            if (resource == null) {
                continue;
            }
            if (Integer.valueOf(1).equals(resource.getUsedFlag())) {
                throw new SysException(ResponseCode.FAIL_PARA, "资源已被占用");
            }
            resource.setUsedFlag(1);
            resource.setUsedBatchNo(batchNo);
            resource.setUsedTargetType(targetType);
            resource.setUsedTargetId(targetId);
            resource.setUsedAt(now);
            resource.setUpdatedAt(now);
            internalTestResourceRepository.save(resource);
        }
    }

    public int releaseResourcesByBatch(String batchNo) {
        if (CommonUtils.isNull(batchNo)) {
            return 0;
        }
        List<InternalTestResource> resources = internalTestResourceRepository.findByUsedBatchNo(batchNo);
        if (CommonUtils.isNull(resources)) {
            return 0;
        }
        Date now = new Date();
        for (InternalTestResource resource : resources) {
            resource.setUsedFlag(0);
            resource.setUsedBatchNo(null);
            resource.setUsedTargetType(null);
            resource.setUsedTargetId(null);
            resource.setUsedAt(null);
            resource.setUpdatedAt(now);
            internalTestResourceRepository.save(resource);
        }
        return resources.size();
    }

    public InternalTestResourceQo save(InternalTestResourceQo qo) {
        validateSaveQo(qo);
        Date now = new Date();
        InternalTestResource resource;
        if (qo.getId() == null) {
            resource = CopyUtil.copy(qo, InternalTestResource.class);
            resource.setCreatedAt(now);
        } else {
            resource = internalTestResourceRepository.findOne(qo.getId());
            if (resource == null) {
                throw new SysException(ResponseCode.FAIL_NOT);
            }
            CopyUtil.copyIgnoreNull(qo, resource);
        }
        if (CommonUtils.isNull(resource.getStatus())) {
            resource.setStatus(InternalTestResourceStatus.enabled.value);
        }
        if (resource.getSortNo() == null) {
            resource.setSortNo(0);
        }
        resource.setUpdatedAt(now);
        resource = internalTestResourceRepository.save(resource);
        return CopyUtil.copy(resource, InternalTestResourceQo.class);
    }

    public void updateStatus(Long id, String status) {
        if (id == null || !InternalTestResourceStatus.isValid(status)) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        InternalTestResource resource = internalTestResourceRepository.findOne(id);
        if (resource == null) {
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        resource.setStatus(status);
        resource.setUpdatedAt(new Date());
        internalTestResourceRepository.save(resource);
    }

    private Specification<InternalTestResource> buildSpecification(final InternalTestResourceQo qo) {
        return new Specification<InternalTestResource>() {
            @Override
            public Predicate toPredicate(Root<InternalTestResource> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (CommonUtils.isNotNull(qo.getMediaType())) {
                    predicates.add(cb.equal(root.get("mediaType"), qo.getMediaType()));
                }
                if (CommonUtils.isNotNull(qo.getUsageType())) {
                    predicates.add(cb.equal(root.get("usageType"), qo.getUsageType()));
                }
                if (CommonUtils.isNotNull(qo.getStyleCode())) {
                    predicates.add(cb.equal(root.get("styleCode"), qo.getStyleCode()));
                }
                if (CommonUtils.isNotNull(qo.getProfession())) {
                    predicates.add(cb.equal(root.get("profession"), qo.getProfession()));
                }
                if (CommonUtils.isNotNull(qo.getProvince())) {
                    predicates.add(cb.equal(root.get("province"), qo.getProvince()));
                }
                if (CommonUtils.isNotNull(qo.getCity())) {
                    predicates.add(cb.equal(root.get("city"), qo.getCity()));
                }
                if (CommonUtils.isNotNull(qo.getArea())) {
                    predicates.add(cb.equal(root.get("area"), qo.getArea()));
                }
                if (CommonUtils.isNotNull(qo.getStatus())) {
                    predicates.add(cb.equal(root.get("status"), qo.getStatus()));
                }
                if (qo.getUsedFlag() != null) {
                    predicates.add(cb.equal(root.get("usedFlag"), qo.getUsedFlag()));
                }
                if (CommonUtils.isNotNull(qo.getGroupNo())) {
                    predicates.add(cb.equal(root.get("groupNo"), qo.getGroupNo()));
                }
                if (CommonUtils.isNotNull(qo.getKeyword())) {
                    String keyword = "%" + qo.getKeyword().trim() + "%";
                    predicates.add(cb.or(
                            cb.like(root.get("title"), keyword),
                            cb.like(root.get("description"), keyword),
                            cb.like(root.get("url"), keyword),
                            cb.like(root.get("remark"), keyword)
                    ));
                }
                query.where(predicates.toArray(new Predicate[predicates.size()]));
                return query.getRestriction();
            }
        };
    }

    private void validateBatchQo(InternalTestResourceQo qo) {
        if (qo == null || CommonUtils.isNull(qo.getUrls())) {
            throw new SysException(ResponseCode.FAIL_PARA, "请输入资源URL");
        }
        validateResourceTypeQo(qo);
        if (CommonUtils.isNotNull(qo.getStatus()) && !InternalTestResourceStatus.isValid(qo.getStatus())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
    }

    private List<String> normalizeUrls(List<String> urls) {
        List<String> result = new ArrayList<String>();
        Set<String> seen = new HashSet<String>();
        for (String raw : urls) {
            if (CommonUtils.isNull(raw)) {
                continue;
            }
            String url = raw.trim();
            if (url.length() == 0 || seen.contains(url)) {
                continue;
            }
            seen.add(url);
            result.add(url);
        }
        if (result.isEmpty()) {
            throw new SysException(ResponseCode.FAIL_PARA, "请输入资源URL");
        }
        return result;
    }

    List<List<String>> splitWorkGroups(List<String> urls, InternalTestResourceQo qo) {
        List<List<String>> groups = new ArrayList<List<String>>();
        if (!InternalTestResourceUsageType.work.value.equals(qo.getUsageType())) {
            for (String url : urls) {
                List<String> single = new ArrayList<String>();
                single.add(url);
                groups.add(single);
            }
            return groups;
        }

        int groupSize = qo.getGroupSize() == null || qo.getGroupSize() < 1 ? 6 : qo.getGroupSize();
        for (int start = 0; start < urls.size(); start += groupSize) {
            groups.add(urls.subList(start, Math.min(urls.size(), start + groupSize)));
        }
        return groups;
    }

    private String buildGroupNo(int index) {
        return "ITG" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + String.format("%03d", index);
    }

    private boolean existsByUrl(String url) {
        return internalTestResourceRepository.findByUrl(url) != null;
    }

    private String defaultStatus(String status) {
        return CommonUtils.isNotNull(status) ? status : InternalTestResourceStatus.enabled.value;
    }

    private List<List<InternalTestResourceQo>> pageGroups(Map<String, List<InternalTestResourceQo>> groups, int page, int size) {
        List<List<InternalTestResourceQo>> allGroups = new ArrayList<List<InternalTestResourceQo>>(groups.values());
        int start = page * size;
        if (start >= allGroups.size()) {
            return new ArrayList<List<InternalTestResourceQo>>();
        }
        int end = Math.min(start + size, allGroups.size());
        return new ArrayList<List<InternalTestResourceQo>>(allGroups.subList(start, end));
    }

    private int calculateTotalPages(int totalElements, int size) {
        if (totalElements == 0) {
            return 0;
        }
        return (totalElements + size - 1) / size;
    }

    private int countInputDuplicateUrls(List<String> urls) {
        int duplicateCount = 0;
        Set<String> seen = new HashSet<String>();
        for (String raw : urls) {
            if (CommonUtils.isNull(raw)) {
                continue;
            }
            String url = raw.trim();
            if (url.length() == 0) {
                continue;
            }
            if (seen.contains(url)) {
                duplicateCount++;
            } else {
                seen.add(url);
            }
        }
        return duplicateCount;
    }

    private void validateSaveQo(InternalTestResourceQo qo) {
        if (qo == null || CommonUtils.isNull(qo.getUrl())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        validateResourceTypeQo(qo);
        if (CommonUtils.isNotNull(qo.getStatus()) && !InternalTestResourceStatus.isValid(qo.getStatus())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
    }

    private void validateResourceTypeQo(InternalTestResourceQo qo) {
        if (!InternalTestResourceMediaType.isValid(qo.getMediaType())
                || !InternalTestResourceUsageType.isValid(qo.getUsageType())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
    }
}
