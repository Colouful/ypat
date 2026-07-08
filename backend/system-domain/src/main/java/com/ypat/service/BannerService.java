package com.ypat.service;

import com.ypat.BannerQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.Banner;
import com.ypat.repository.BannerRepository;
import com.ypat.util.CommonUtils;
import com.ypat.util.CopyUtil;
import org.apache.commons.lang3.StringUtils;
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

    private static final String JUMP_DISABLED = "0";
    private static final String JUMP_ENABLED = "1";
    private static final String JUMP_TYPE_MINIAPP = "miniapp";
    private static final String JUMP_TYPE_WEB = "web";
    private static final int MAX_JUMP_URL_LENGTH = 500;

    @Autowired
    private BannerRepository bannerRepository;

    public void save(BannerQo bannerQo){
        normalizeAndValidateJump(bannerQo);
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

    private void normalizeAndValidateJump(BannerQo bannerQo) {
        String jumpflag = StringUtils.defaultIfBlank(bannerQo.getJumpflag(), JUMP_DISABLED).trim();
        bannerQo.setJumpflag(jumpflag);

        if (!JUMP_ENABLED.equals(jumpflag)) {
            bannerQo.setJumpflag(JUMP_DISABLED);
            bannerQo.setJumptype(null);
            bannerQo.setJumpurl(null);
            return;
        }

        String jumptype = StringUtils.trimToEmpty(bannerQo.getJumptype());
        String jumpurl = StringUtils.trimToEmpty(bannerQo.getJumpurl());

        if (!JUMP_TYPE_MINIAPP.equals(jumptype) && !JUMP_TYPE_WEB.equals(jumptype)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "跳转类型不正确");
        }
        if (StringUtils.isBlank(jumpurl)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请输入跳转目标");
        }
        if (jumpurl.length() > MAX_JUMP_URL_LENGTH) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "跳转目标不能超过500个字符");
        }
        if (JUMP_TYPE_MINIAPP.equals(jumptype) && !isMiniappPath(jumpurl)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请输入 /pages 或 /pages-sub 开头的小程序页面路径");
        }
        if (JUMP_TYPE_WEB.equals(jumptype) && !isHttpUrl(jumpurl)) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请输入 http 或 https 开头的外部地址");
        }

        bannerQo.setJumptype(jumptype);
        bannerQo.setJumpurl(jumpurl);
    }

    private boolean isMiniappPath(String value) {
        return value.startsWith("/pages/") || value.startsWith("/pages-sub/");
    }

    private boolean isHttpUrl(String value) {
        return StringUtils.startsWithIgnoreCase(value, "http://")
                || StringUtils.startsWithIgnoreCase(value, "https://");
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
