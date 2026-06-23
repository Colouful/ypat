package com.ypat.service;

import com.ypat.BannerQo;
import com.ypat.entity.Banner;
import com.ypat.repository.BannerRepository;
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
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    public void save(BannerQo bannerQo){
        Banner banner = CopyUtil.copy(bannerQo, Banner.class);
        banner.setCredate(new Date());
        bannerRepository.save(banner);
    }

    public void upDown(BannerQo bannerQo){
        Banner banner = get(bannerQo.getId());
        banner.setStatus(bannerQo.getStatus());
        bannerRepository.save(banner);
    }

    public Banner get(Long id){
        return bannerRepository.findById(id);
    }

    public BannerQo findById(Long id){
        Banner banner = get(id);
        return CopyUtil.copy(banner, BannerQo.class);
    }

    public Map<String, Object> findPage(BannerQo queryQo) {
        Page<Banner> bannerPage = findPageByPredicate(queryQo);
        List<BannerQo> content = new ArrayList<>();
        if(!CollectionUtils.isEmpty(bannerPage.getContent())){
            for (Banner banner : bannerPage.getContent()) {
                BannerQo qo = CopyUtil.copy(banner, BannerQo.class);
                content.add(qo);
            }
        }

        Map<String, Object> page = new HashMap<>();
        page.put("content", content);
        page.put("totalPages", bannerPage.getTotalPages());
        page.put("totalElements", bannerPage.getTotalElements());
        return page;
    }

    /**
     * 分页
     * @param queryQo
     * @return
     */
    public Page<Banner> findPageByPredicate(BannerQo queryQo){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable  = new PageRequest(queryQo.getPage(), queryQo.getSize(), sort);
        return bannerRepository.findAll(new Specification<Banner>() {
            @Override
            public Predicate toPredicate(Root<Banner> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicatesList = new ArrayList<Predicate>();
                if(CommonUtils.isNotNull(queryQo.getStatus())){
                    predicatesList.add(criteriaBuilder.equal(root.get("status"), queryQo.getStatus()));
                }
                query.where(predicatesList.toArray(new Predicate[predicatesList.size()]));
                return query.getRestriction();
            }
        }, pageable);
    }
}
