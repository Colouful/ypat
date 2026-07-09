package com.ypat.service;

import com.ypat.ProductQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.Product;
import com.ypat.repository.ProductRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public void save(ProductQo productQo){
        if (productQo == null || StringUtils.isBlank(productQo.getName())) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "产品名称不能为空");
        }
        if (productQo.getCurrval() == null || productQo.getCurrval() <= 0) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "充值数量必须大于0");
        }
        if (productQo.getOldval() == null || productQo.getOldval() <= 0) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "支付金额必须大于0");
        }
        if (StringUtils.isBlank(productQo.getStatus())) {
            productQo.setStatus("0");
        }
        if (StringUtils.isBlank(productQo.getRecommended())) {
            productQo.setRecommended("0");
        }
        Product product = CopyUtil.copy(productQo, Product.class);
        productRepository.save(product);
    }

    public void upDown(ProductQo productQo){
        Product product = get(productQo.getId());
        product.setStatus(productQo.getStatus());
        productRepository.save(product);
    }

    public Product get(Long id){
        return productRepository.findById(id);
    }

    public ProductQo findById(Long id){
        Product product = get(id);
        return CopyUtil.copy(product, ProductQo.class);
    }

    public Map<String, Object> findPage(ProductQo queryQo) {
        Page<Product> productPage = findPageByPredicate(queryQo);
        List<ProductQo> content = new ArrayList<>();
        if(!CollectionUtils.isEmpty(productPage.getContent())){
            for (Product product : productPage.getContent()) {
                ProductQo qo = CopyUtil.copy(product, ProductQo.class);
                content.add(qo);
            }
        }

        Map<String, Object> page = new HashMap<>();
        page.put("content", content);
        page.put("pages", productPage.getTotalPages());
        page.put("totals", productPage.getTotalElements());
        return page;
    }

    /**
     * 分页
     * @param queryQo
     * @return
     */
    public Page<Product> findPageByPredicate(ProductQo queryQo){
        Sort sort = new Sort(Sort.Direction.DESC, "recommended").and(new Sort(Sort.Direction.DESC, "id"));
        Pageable pageable  = new PageRequest(queryQo.getPage(), queryQo.getSize(), sort);
        return productRepository.findAll(new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicatesList = new ArrayList<Predicate>();
                if(CommonUtils.isNotNull(queryQo.getStatus())){
                    if ("0".equals(queryQo.getStatus())) {
                        predicatesList.add(root.get("status").in("0", "up"));
                    } else if ("1".equals(queryQo.getStatus())) {
                        predicatesList.add(root.get("status").in("1", "down"));
                    } else {
                        predicatesList.add(criteriaBuilder.equal(root.get("status"), queryQo.getStatus()));
                    }
                }
                if(StringUtils.isNotBlank(queryQo.getName())){
                    predicatesList.add(criteriaBuilder.like(root.get("name"), "%" + queryQo.getName().trim() + "%"));
                }
                query.where(predicatesList.toArray(new Predicate[predicatesList.size()]));
                return query.getRestriction();
            }
        }, pageable);
    }
}
