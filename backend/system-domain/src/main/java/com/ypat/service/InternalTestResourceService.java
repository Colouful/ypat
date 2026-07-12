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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        List<List<String>> groups = splitWorkGroups(qo.getUrls(), qo);
        List<InternalTestResourceQo> saved = new ArrayList<InternalTestResourceQo>();
        int duplicateCount = countInputDuplicateUrls(qo.getUrls());
        Date now = new Date();
        int groupIndex = 0;
        String groupNoPrefix = buildGroupNoPrefix();

        for (List<String> group : groups) {
            String groupNo = InternalTestResourceUsageType.work.value.equals(qo.getUsageType())
                    ? buildGroupNo(groupNoPrefix, groupIndex++)
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

        int page = qo.getPage() == null || qo.getPage() < 0 ? 0 : qo.getPage();
        int size = qo.getSize() == null || qo.getSize() <= 0 ? 20 : qo.getSize();
        String mediaType = normalizeOptional(qo.getMediaType());
        String styleCode = normalizeOptional(qo.getStyleCode());
        String profession = normalizeOptional(qo.getProfession());
        String province = normalizeOptional(qo.getProvince());
        String city = normalizeOptional(qo.getCity());
        String area = normalizeOptional(qo.getArea());
        String filterGroupNo = normalizeOptional(qo.getGroupNo());
        String keyword = normalizeOptional(qo.getKeyword());
        Integer usedFlag = qo.getUsedFlag();
        long groupCount = valueOrZero(internalTestResourceRepository.countAvailableGroups(
                mediaType, styleCode, profession, province, city, area, filterGroupNo, keyword, usedFlag));
        long singleCount = valueOrZero(internalTestResourceRepository.countAvailableSingleResources(
                mediaType, styleCode, profession, province, city, area, filterGroupNo, keyword, usedFlag));
        long pageStart = (long) page * size;
        Map<String, List<InternalTestResourceQo>> groups = new LinkedHashMap<String, List<InternalTestResourceQo>>();

        int groupLimit = pageStart < groupCount ? (int) Math.min((long) size, groupCount - pageStart) : 0;
        if (groupLimit > 0 && pageStart <= Integer.MAX_VALUE) {
            List<String> groupNos = internalTestResourceRepository.findAvailableGroupNos(
                    mediaType, styleCode, profession, province, city, area, filterGroupNo, keyword, usedFlag,
                    (int) pageStart, groupLimit);
            Map<String, List<InternalTestResource>> fullGroups = groupResourcesByGroupNo(
                    internalTestResourceRepository.findByGroupNoIn(groupNos));
            for (String groupNo : groupNos) {
                List<InternalTestResource> fullGroup = fullGroups.get(groupNo);
                if (isCompleteAvailableGroup(fullGroup, fullGroup, usedFlag)) {
                    sortResourceGroup(fullGroup);
                    groups.put(groupNo, copyResourceGroup(fullGroup));
                }
            }
        }

        int remaining = size - groups.size();
        long singleOffset = pageStart < groupCount ? 0L : pageStart - groupCount;
        if (remaining > 0 && singleOffset < singleCount && singleOffset <= Integer.MAX_VALUE) {
            List<InternalTestResource> singles = internalTestResourceRepository.findAvailableSingleResources(
                    mediaType, styleCode, profession, province, city, area, filterGroupNo, keyword, usedFlag,
                    (int) singleOffset, remaining);
            for (InternalTestResource resource : singles) {
                if (isMatchingWorkResource(resource, usedFlag) && CommonUtils.isNull(resource.getGroupNo())) {
                    groups.put("single-" + resource.getId(), copyResourceGroup(singleResourceList(resource)));
                }
            }
        }

        long totalElements = groupCount + singleCount;
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        List<Map<String, Object>> content = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, List<InternalTestResourceQo>> entry : groups.entrySet()) {
            content.add(buildGroupView(entry.getKey(), entry.getValue()));
        }
        result.put("content", content);
        result.put("totalPages", calculateTotalPages(totalElements, size));
        result.put("totalElements", totalElements);
        return result;
    }

    private Map<String, Object> buildGroupView(String groupNo, List<InternalTestResourceQo> resources) {
        Map<String, Object> group = new LinkedHashMap<String, Object>();
        InternalTestResourceQo first = resources.isEmpty() ? null : resources.get(0);
        group.put("groupNo", groupNo);
        group.put("groupTitle", first == null ? null : first.getGroupTitle());
        group.put("mediaType", first == null ? null : first.getMediaType());
        group.put("usedFlag", first == null ? null : first.getUsedFlag());
        group.put("resources", resources);
        return group;
    }

    public void markResourcesUsed(List<InternalTestResource> resources, String batchNo, String targetType, Long targetId) {
        validateUsageContext(batchNo, targetType, targetId);
        if (CommonUtils.isNull(resources)) {
            return;
        }
        List<Long> ids = collectResourceIds(resources);
        Date now = new Date();
        int updated = internalTestResourceRepository.markResourcesUsedIfAvailable(
                ids, batchNo, targetType, targetId, now, now);
        if (updated != ids.size()) {
            throw new SysException(ResponseCode.FAIL_PARA, "资源已被占用");
        }
    }

    public int releaseResourcesByBatch(String batchNo) {
        if (CommonUtils.isNull(batchNo)) {
            return 0;
        }
        return internalTestResourceRepository.releaseByUsedBatchNo(batchNo, new Date());
    }

    public int releaseAllUsedResources() {
        return internalTestResourceRepository.releaseAllUsed(new Date());
    }

    public int releaseResourcesByTargets(String batchNo, String targetType, List<Long> targetIds) {
        if (CommonUtils.isNull(targetType) || CommonUtils.isNull(targetIds)) {
            return 0;
        }
        return internalTestResourceRepository.releaseByUsedTargets(batchNo, targetType, targetIds, new Date());
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
            validateUsedResourceImmutable(qo, resource);
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
            for (String url : normalizeUrls(urls)) {
                List<String> single = new ArrayList<String>();
                single.add(url);
                groups.add(single);
            }
            return groups;
        }

        List<List<String>> blankLineGroups = splitWorkGroupsByBlankLines(urls);
        if (blankLineGroups.size() > 1) {
            return blankLineGroups;
        }

        List<String> normalizedUrls = normalizeUrls(urls);
        int groupSize = qo.getGroupSize() == null || qo.getGroupSize() < 1 ? 6 : qo.getGroupSize();
        for (int start = 0; start < normalizedUrls.size(); start += groupSize) {
            groups.add(normalizedUrls.subList(start, Math.min(normalizedUrls.size(), start + groupSize)));
        }
        return groups;
    }

    private List<List<String>> splitWorkGroupsByBlankLines(List<String> urls) {
        List<List<String>> groups = new ArrayList<List<String>>();
        List<String> current = new ArrayList<String>();
        Set<String> seen = new HashSet<String>();
        boolean hasSeparator = false;
        for (String raw : urls) {
            if (CommonUtils.isNull(raw) || raw.trim().length() == 0) {
                if (!current.isEmpty()) {
                    groups.add(current);
                    current = new ArrayList<String>();
                    hasSeparator = true;
                }
                continue;
            }
            String url = raw.trim();
            if (seen.contains(url)) {
                continue;
            }
            seen.add(url);
            current.add(url);
        }
        if (!current.isEmpty()) {
            groups.add(current);
        }
        if (groups.isEmpty()) {
            throw new SysException(ResponseCode.FAIL_PARA, "请输入资源URL");
        }
        return hasSeparator ? groups : singleGroup(normalizeUrls(urls));
    }

    private String buildGroupNoPrefix() {
        return "ITG" + new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String buildGroupNo(String prefix, int index) {
        return prefix
                + String.format("%03d", index);
    }

    private boolean existsByUrl(String url) {
        return internalTestResourceRepository.findByUrl(url) != null;
    }

    private String defaultStatus(String status) {
        return CommonUtils.isNotNull(status) ? status : InternalTestResourceStatus.enabled.value;
    }

    private void validateUsedResourceImmutable(InternalTestResourceQo qo, InternalTestResource resource) {
        if (!Integer.valueOf(1).equals(resource.getUsedFlag())) {
            return;
        }
        if (fieldChanged(resource.getUrl(), qo.getUrl())
                || fieldChanged(resource.getUsageType(), qo.getUsageType())
                || fieldChanged(resource.getMediaType(), qo.getMediaType())
                || fieldChanged(resource.getGroupNo(), qo.getGroupNo())) {
            throw new SysException(ResponseCode.FAIL_PARA, "已占用资源不能修改URL、用途、媒体类型或作品组");
        }
    }

    private boolean fieldChanged(String oldValue, String newValue) {
        return CommonUtils.isNotNull(newValue) && !safeEquals(oldValue, newValue);
    }

    private boolean safeEquals(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private void validateUsageContext(String batchNo, String targetType, Long targetId) {
        if (CommonUtils.isNull(batchNo) || CommonUtils.isNull(targetType) || targetId == null) {
            throw new SysException(ResponseCode.FAIL_PARA, "资源占用参数不能为空");
        }
    }

    private List<Long> collectResourceIds(List<InternalTestResource> resources) {
        Set<Long> uniqueIds = new HashSet<Long>();
        for (InternalTestResource resource : resources) {
            if (resource == null || resource.getId() == null) {
                throw new SysException(ResponseCode.FAIL_PARA, "资源不能为空");
            }
            uniqueIds.add(resource.getId());
        }
        if (uniqueIds.isEmpty()) {
            throw new SysException(ResponseCode.FAIL_PARA, "资源不能为空");
        }
        return new ArrayList<Long>(uniqueIds);
    }

    private Map<String, List<InternalTestResource>> groupResourcesByGroupNo(List<InternalTestResource> resources) {
        Map<String, List<InternalTestResource>> groups = new LinkedHashMap<String, List<InternalTestResource>>();
        if (CommonUtils.isNull(resources)) {
            return groups;
        }
        for (InternalTestResource resource : resources) {
            if (resource == null || CommonUtils.isNull(resource.getGroupNo())) {
                continue;
            }
            if (!groups.containsKey(resource.getGroupNo())) {
                groups.put(resource.getGroupNo(), new ArrayList<InternalTestResource>());
            }
            groups.get(resource.getGroupNo()).add(resource);
        }
        return groups;
    }

    private boolean isCompleteAvailableGroup(List<InternalTestResource> fullGroup,
                                             List<InternalTestResource> candidateGroup,
                                             Integer usedFlag) {
        if (CommonUtils.isNull(fullGroup) || CommonUtils.isNull(candidateGroup) || fullGroup.size() != candidateGroup.size()) {
            return false;
        }
        Set<String> candidateKeys = new HashSet<String>();
        for (InternalTestResource resource : candidateGroup) {
            candidateKeys.add(resourceKey(resource));
        }
        for (InternalTestResource resource : fullGroup) {
            if (!isMatchingWorkResource(resource, usedFlag) || !candidateKeys.contains(resourceKey(resource))) {
                return false;
            }
        }
        return true;
    }

    private boolean isMatchingWorkResource(InternalTestResource resource, Integer usedFlag) {
        return resource != null
                && InternalTestResourceUsageType.work.value.equals(resource.getUsageType())
                && InternalTestResourceStatus.enabled.value.equals(resource.getStatus())
                && (usedFlag == null || usedFlag.equals(resource.getUsedFlag()));
    }

    private String resourceKey(InternalTestResource resource) {
        return resource.getId() != null ? "id:" + resource.getId() : "url:" + resource.getUrl();
    }

    private List<InternalTestResource> singleResourceList(InternalTestResource resource) {
        List<InternalTestResource> resources = new ArrayList<InternalTestResource>();
        resources.add(resource);
        return resources;
    }

    private List<List<String>> singleGroup(List<String> urls) {
        List<List<String>> groups = new ArrayList<List<String>>();
        groups.add(urls);
        return groups;
    }

    private List<InternalTestResourceQo> copyResourceGroup(List<InternalTestResource> resources) {
        List<InternalTestResourceQo> group = new ArrayList<InternalTestResourceQo>();
        for (InternalTestResource resource : resources) {
            group.add(CopyUtil.copy(resource, InternalTestResourceQo.class));
        }
        return group;
    }

    private void sortResourceGroup(List<InternalTestResource> resources) {
        Collections.sort(resources, new Comparator<InternalTestResource>() {
            @Override
            public int compare(InternalTestResource left, InternalTestResource right) {
                int byGroupSort = compareNullable(left.getGroupSortNo(), right.getGroupSortNo());
                if (byGroupSort != 0) {
                    return byGroupSort;
                }
                int bySort = compareNullable(left.getSortNo(), right.getSortNo());
                if (bySort != 0) {
                    return bySort;
                }
                return compareNullable(left.getId(), right.getId());
            }
        });
    }

    private <T extends Comparable<T>> int compareNullable(T left, T right) {
        if (left == null) {
            return right == null ? 0 : 1;
        }
        return right == null ? -1 : left.compareTo(right);
    }

    private String normalizeOptional(String value) {
        if (CommonUtils.isNull(value)) {
            return null;
        }
        String normalized = value.trim();
        return normalized.length() == 0 ? null : normalized;
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private int calculateTotalPages(long totalElements, int size) {
        return totalElements == 0 ? 0 : (int) ((totalElements + size - 1) / size);
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
