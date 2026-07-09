package com.ypat.service;

import com.ypat.BillQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.*;
import com.ypat.entity.Record;
import com.ypat.enums.OrderType;
import com.ypat.enums.RecordType;
import com.ypat.enums.UserStatus;
import com.ypat.enums.YesNo;
import com.ypat.repository.*;
import com.ypat.util.CommonUtils;
import com.ypat.util.Constant;
import com.ypat.util.CopyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class BillService {
    private static Logger logger = LoggerFactory.getLogger(BillService.class);
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private RecordRepository recordRepository;


    public void save(Bill bill){
        String out_trade_no = bill.getOut_trade_no();
        Order order = orderRepository.findByOut_trade_no(out_trade_no);
        if(order!=null && order.getStatus().equals(YesNo.no.value)){
            if(bill.getTotal_fee()>=100 && (bill.getTotal_fee()!=order.getTotal_fee()*100)){
                logger.error(String.format("与订单金额不一致: %s,%d,%d", out_trade_no, bill.getTotal_fee(), order.getTotal_fee()));
                throw new SysException(ResponseCode.FAIL_PAY);
            }
            //生成流水
            bill.setCredate(new Date());
            bill.setType(order.getType());
            billRepository.save(bill);

            //更新订单
            order.setStatus(YesNo.yes.value);
            order.setReturn_code(bill.getReturn_code());
            order.setReturn_msg(bill.getReturn_msg());
            order.setResult_code(bill.getResult_code());
            order.setErr_code(bill.getErr_code());
            order.setErr_code_des(bill.getErr_code_des());
            orderRepository.save(order);

            //充值成功的处理
            if(Constant.SUCCESS.equals(bill.getResult_code())){
                User user = userRepository.findById(order.getUserid());
                if(order.getType().equals(OrderType.PPD.value)) {
                    //充值拍拍豆
                    Product product = productRepository.findById(order.getProductid());
                    Integer rechargePpd = product == null ? 0 : product.getCurrval();
                    user.setPpd((user.getPpd() == null ? 0 : user.getPpd()) + rechargePpd);
                    //增加收支记录
                    Record record = new Record();
                    record.setCredate(new Date());
                    record.setPpd(rechargePpd);
                    record.setUserid(order.getUserid());
                    record.setType(RecordType.PAY.value);
                    recordRepository.save(record);
                }else if(order.getType().equals(OrderType.REAL.value)) {
                    //实名认证充值
                    user.setStatus(UserStatus.zfcg.value);
                }else if(order.getType().equals(OrderType.CRED.value)){
                    //保证金充值
                    user.setCreditflag(YesNo.yes.value);
                }
                userRepository.save(user);
            }
        }
    }

    public Bill get(Long id){
        return billRepository.findById(id);
    }

    public BillQo findById(Long id){
        Bill bill = get(id);
        return CopyUtil.copy(bill, BillQo.class);
    }

    public Map<String, Object> findPage(BillQo queryQo) {
        Page<Bill> orderPage = findPageByPredicate(queryQo);
        List<BillQo> content = new ArrayList<>();
        if(!CollectionUtils.isEmpty(orderPage.getContent())){
            for (Bill bill : orderPage.getContent()) {
                BillQo qo = CopyUtil.copy(bill, BillQo.class);
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
    public Page<Bill> findPageByPredicate(BillQo queryQo){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable  = new PageRequest(queryQo.getPage(), queryQo.getSize(), sort);
        return billRepository.findAll(new Specification<Bill>() {
            @Override
            public Predicate toPredicate(Root<Bill> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicatesList = new ArrayList<Predicate>();
                if(CommonUtils.isNotNull(queryQo.getType())){
                    predicatesList.add(criteriaBuilder.greaterThan(root.get("type"), queryQo.getType()));
                }
                if(CommonUtils.isNotNull(queryQo.getId())){
                    predicatesList.add(criteriaBuilder.equal(root.get("id"), queryQo.getId()));
                }
                if(CommonUtils.isNotNull(queryQo.getUserid())){
                    javax.persistence.criteria.Subquery<String> orderSubquery = query.subquery(String.class);
                    Root<Order> orderRoot = orderSubquery.from(Order.class);
                    orderSubquery.select(orderRoot.get("out_trade_no"));
                    orderSubquery.where(criteriaBuilder.equal(orderRoot.get("userid"), queryQo.getUserid()));
                    predicatesList.add(root.get("out_trade_no").in(orderSubquery));
                }
                query.where(predicatesList.toArray(new Predicate[predicatesList.size()]));
                return query.getRestriction();
            }
        }, pageable);
    }
}
