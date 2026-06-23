package com.ypat.service;

import com.ypat.OrderQo;
import com.ypat.entity.Order;
import com.ypat.enums.OrderType;
import com.ypat.enums.YesNo;
import com.ypat.repository.OrderRepository;
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
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public void save(Order order){
        //创建订单时所有未支付的订单
        List<Order> histories = orderRepository.findByUseridAndStatus(order.getUserid(), YesNo.no.value);
        for (Order history : histories) {
            orderRepository.delete(history.getId());
        }
        //如果是拍拍豆
        if(order.getType().equals(OrderType.PPD.value)){

        }
        order.setCredate(new Date());
        order.setStatus(YesNo.no.value);
        orderRepository.save(order);
    }

    public Order get(Long id){
        return orderRepository.findById(id);
    }

    public OrderQo findById(Long id){
        Order order = get(id);
        return CopyUtil.copy(order, OrderQo.class);
    }

    public int countByUseridAndType(Long userid, String type) {
        return orderRepository.countByUseridAndTypeAndStatus(userid, type, YesNo.yes.value);
    }

    public Map<String, Object> findPage(OrderQo queryQo) {
        Page<Order> orderPage = findPageByPredicate(queryQo);
        List<OrderQo> content = new ArrayList<>();
        if(!CollectionUtils.isEmpty(orderPage.getContent())){
            for (Order order : orderPage.getContent()) {
                OrderQo qo = CopyUtil.copy(order, OrderQo.class);
                content.add(qo);
            }
        }

        Map<String, Object> page = new HashMap<>();
        page.put("content", content);
        page.put("totalPages", orderPage.getTotalPages());
        page.put("totalElements", orderPage.getTotalElements());
        return page;
    }

    /**
     * 分页
     * @param queryQo
     * @return
     */
    public Page<Order> findPageByPredicate(OrderQo queryQo){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable  = new PageRequest(queryQo.getPage(), queryQo.getSize(), sort);
        return orderRepository.findAll(new Specification<Order>() {
            @Override
            public Predicate toPredicate(Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
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
