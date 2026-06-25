package com.ypat.controller;

import com.ypat.OrderQo;
import com.ypat.ProductQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.config.SystemConfig;
import com.ypat.enums.OrderType;
import com.ypat.service.OrderServiceClient;
import com.ypat.service.ProductServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.third.wxpay.sdk.WXPayClient;
import com.ypat.third.wxpay.sdk.WXPayConstants;
import com.ypat.third.wxpay.sdk.WXPayUtil;
import com.ypat.util.MapUtils;
import com.ypat.util.UserUtil;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired private OrderServiceClient orderServiceClient;
    @Autowired private ProductServiceClient productServiceClient;
    @Autowired private WXPayClient wxPayClient;
    @Autowired private SystemConfig systemConfig;

    @GetMapping("/order/get")
    public String get(Long id) {
        if (id == null) throw new SysException(ResponseCode.FAIL_PARA);
        Long currentUserId = Long.parseLong(UserUtil.getUserId());
        String json = orderServiceClient.get(id);
        OrderQo order = GsonUtils.fromJson(json, OrderQo.class);
        if (order == null) throw new SysException(ResponseCode.FAIL_NOT);
        if (!currentUserId.equals(order.getUserid())) throw new SysException(ResponseCode.FAIL_VAL);
        return json;
    }

    @GetMapping("/order/status")
    public String status(@NotEmpty String out_trade_no) {
        OrderQo query = new OrderQo();
        query.setUserid(Long.parseLong(UserUtil.getUserId()));
        query.setOut_trade_no(out_trade_no);
        query.setPage(0);
        query.setSize(1);
        return orderServiceClient.findPage(query);
    }

    @GetMapping("/order/count")
    public String count(String type) {
        return orderServiceClient.count(Long.parseLong(UserUtil.getUserId()), type);
    }

    @PostMapping("/order/create")
    public String add(@Valid OrderQo orderQo) throws Exception {
        Long userid = Long.parseLong(UserUtil.getUserId());
        orderQo.setUserid(userid);
        if (OrderType.PPD.value.equals(orderQo.getType())) {
            if (StringUtils.isEmpty(orderQo.getProductid())) throw new SysException(ResponseCode.FAIL_PARA);
            ProductQo product = GsonUtils.fromJson(productServiceClient.get(orderQo.getProductid()), ProductQo.class);
            if (product == null || product.getOldval() == null || product.getOldval() < 1) throw new SysException(ResponseCode.FAIL_NOT);
            if (!"1".equals(product.getStatus())) throw new SysException(ResponseCode.FAIL_VAL);
            orderQo.setTotal_fee(product.getOldval());
        } else if (orderQo.getTotal_fee() == null || orderQo.getTotal_fee() < 1) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }

        Map<String, String> respData = wxPayClient.unifiedOrder(orderQo);
        OrderQo created = MapUtils.map2Java(OrderQo.class, respData);
        if (WXPayConstants.FAIL.equals(created.getReturn_code())) throw new SysException(ResponseCode.FAIL_WX);
        if (WXPayConstants.FAIL.equals(created.getResult_code())) throw new SysException(ResponseCode.FAIL_ORDER);

        created.setTotal_fee(orderQo.getTotal_fee());
        created.setType(orderQo.getType());
        created.setProductid(orderQo.getProductid());
        created.setUserid(userid);
        orderServiceClient.add(created);

        Map<String, String> retMap = new HashMap<>();
        retMap.put("appId", systemConfig.getWx_appid());
        retMap.put("timeStamp", System.currentTimeMillis() / 1000 + "");
        retMap.put("nonceStr", WXPayUtil.generateNonceStr());
        retMap.put("package", "prepay_id=" + created.getPrepay_id());
        retMap.put("signType", WXPayConstants.HMACSHA256);
        retMap.put("paySign", WXPayUtil.generateSignature(retMap, systemConfig.getWx_key(), WXPayConstants.SignType.HMACSHA256));
        retMap.put("out_trade_no", created.getOut_trade_no());
        logger.info("订单创建成功，userid={}, outTradeNo={}", userid, created.getOut_trade_no());
        return GsonUtils.toJson(retMap);
    }

    @GetMapping("/order/findPage")
    public String findPage(OrderQo orderQo) {
        orderQo.setUserid(Long.parseLong(UserUtil.getUserId()));
        return orderServiceClient.findPage(orderQo);
    }
}
