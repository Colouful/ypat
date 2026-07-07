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
import java.util.List;
import java.util.Map;

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
                if (CommonUtils.isNotNull(qo.getCity())) {
                    predicates.add(cb.equal(root.get("city"), qo.getCity()));
                }
                if (CommonUtils.isNotNull(qo.getStatus())) {
                    predicates.add(cb.equal(root.get("status"), qo.getStatus()));
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

    private void validateSaveQo(InternalTestResourceQo qo) {
        if (qo == null || CommonUtils.isNull(qo.getUrl())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (!InternalTestResourceMediaType.isValid(qo.getMediaType())
                || !InternalTestResourceUsageType.isValid(qo.getUsageType())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (CommonUtils.isNotNull(qo.getStatus()) && !InternalTestResourceStatus.isValid(qo.getStatus())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
    }
}
