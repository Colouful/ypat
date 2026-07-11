package com.ypat.service;

import com.ypat.OrderQo;
import com.ypat.entity.Order;
import com.ypat.entity.Product;
import com.ypat.entity.Record;
import com.ypat.entity.User;
import com.ypat.enums.OrderType;
import com.ypat.enums.RecordType;
import com.ypat.enums.YesNo;
import com.ypat.repository.OrderRepository;
import com.ypat.repository.ProductRepository;
import com.ypat.repository.RecordRepository;
import com.ypat.repository.UserRepository;
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
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RecordRepository recordRepository;

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

    public void savePpdPaymentOrder(Order order) {
        if (order == null || !OrderType.PPD.value.equals(order.getType())) {
            throw new com.ypat.SysException(com.ypat.ResponseCode.FAIL_PARA);
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

    public boolean markPpdPaid(String outTradeNo) {
        Order order = orderRepository.findByOut_trade_no(outTradeNo);
        if (order == null) throw new com.ypat.SysException(com.ypat.ResponseCode.FAIL_NOT);
        if (!OrderType.PPD.value.equals(order.getType())) {
            throw new com.ypat.SysException(com.ypat.ResponseCode.FAIL_PARA);
        }
        if (YesNo.yes.value.equals(order.getStatus())) return false;

        Product product = productRepository.findById(order.getProductid());
        if (product == null || product.getCurrval() == null || product.getCurrval() <= 0) {
            throw new com.ypat.SysException(com.ypat.ResponseCode.FAIL_NOT);
        }
        User user = userRepository.findByIdForUpdate(order.getUserid());
        if (user == null) throw new com.ypat.SysException(com.ypat.ResponseCode.FAIL_NOT);

        order.setStatus(YesNo.yes.value);
        order.setReturn_code("SUCCESS");
        order.setResult_code("SUCCESS");
        orderRepository.save(order);

        user.setPpd((user.getPpd() == null ? 0 : user.getPpd()) + product.getCurrval());
        userRepository.save(user);

        Record record = new Record();
        record.setCredate(new Date());
        record.setPpd(product.getCurrval());
        record.setUserid(order.getUserid());
        record.setType(RecordType.PAY.value);
        recordRepository.save(record);
        return true;
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
        page.put("number", orderPage.getNumber());
        page.put("size", orderPage.getSize());
        return page;
    }

    /**
     * 分页查询。所有非空条件必须真正进入 Specification，尤其是 userid 和 out_trade_no，
     * 否则上层按当前用户查询订单状态时会产生越权和串单风险。
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
                if(CommonUtils.isNotNull(queryQo.getUserid())){
                    predicatesList.add(criteriaBuilder.equal(root.get("userid"), queryQo.getUserid()));
                }
                if(CommonUtils.isNotNull(queryQo.getOut_trade_no())){
                    predicatesList.add(criteriaBuilder.equal(root.get("out_trade_no"), queryQo.getOut_trade_no()));
                }
                if(CommonUtils.isNotNull(queryQo.getType())){
                    predicatesList.add(criteriaBuilder.equal(root.get("type"), queryQo.getType()));
                }
                query.where(predicatesList.toArray(new Predicate[predicatesList.size()]));
                return query.getRestriction();
            }
        }, pageable);
    }
}
