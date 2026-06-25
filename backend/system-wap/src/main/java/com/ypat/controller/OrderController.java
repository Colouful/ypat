package com.ypat.controller;

import com.ypat.OrderQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.config.SystemConfig;
import com.ypat.enums.OrderType;
import com.ypat.service.OrderServiceClient;
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
    private static Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private OrderServiceClient orderServiceClient;
    @Autowired
    private WXPayClient wxPayClient;
    @Autowired
    private SystemConfig systemConfig;

    @GetMapping("/order/get")
    public String get(Long id) {
        return orderServiceClient.get(id);
    }

    /**
     * 按当前登录用户和微信商户订单号查询订单状态。
     * userid 始终从 Token 中解析，避免前端越权查询他人订单。
     */
    @GetMapping("/order/status")
    public String status(@NotEmpty String out_trade_no) {
        Long userid = Long.parseLong(UserUtil.getUserId());
        OrderQo orderQo = new OrderQo();
        orderQo.setUserid(userid);
        orderQo.setOut_trade_no(out_trade_no);
        orderQo.setPage(0);
        orderQo.setSize(1);
        return orderServiceClient.findPage(orderQo);
    }

    @GetMapping("/order/count")
    public String count(String type) {
        Long userid = Long.parseLong(UserUtil.getUserId());
        return orderServiceClient.count(userid, type);
    }

    @PostMapping("/order/create")
    public String add(@Valid OrderQo orderQo) throws Exception {
        logger.info("创建订单输入："+orderQo);
        if(orderQo.getTotal_fee()==null){
            throw new RuntimeException("total_fee不能为空");
        }
        //如果是拍拍豆
        if(orderQo.getType().equals(OrderType.PPD.value)){
            if(StringUtils.isEmpty(orderQo.getProductid())){
                throw new RuntimeException("类型为拍拍充值时,productid不能为空");
            }
        }
        //微信下单
        Long userid = Long.parseLong(UserUtil.getUserId());
        orderQo.setUserid(userid);
        Map<String, String> respData = wxPayClient.unifiedOrder(orderQo);
        OrderQo orderQo1 = MapUtils.map2Java(OrderQo.class,respData);
        String return_code = orderQo1.getReturn_code();
        if(return_code.equals(WXPayConstants.FAIL)){
            throw new SysException(ResponseCode.FAIL_WX);
        }
        String result_code = orderQo1.getResult_code();
        if(result_code.equals(WXPayConstants.FAIL)){
            throw new SysException(ResponseCode.FAIL_ORDER);
        }

        //生成系统自己的订单
        orderQo1.setTotal_fee(orderQo.getTotal_fee());
        orderQo1.setType(orderQo.getType());
        orderQo1.setProductid(orderQo.getProductid());
        orderQo1.setUserid(orderQo.getUserid());
        orderServiceClient.add(orderQo1);

        //返回给小程序的支付参数以及商户订单号，供前端进行服务端状态确认
        Map<String, String> retMap = new HashMap<>();
        retMap.put("appId", systemConfig.getWx_appid());
        retMap.put("timeStamp", System.currentTimeMillis()/1000+"");
        retMap.put("nonceStr", WXPayUtil.generateNonceStr());
        retMap.put("package", "prepay_id="+orderQo1.getPrepay_id());
        retMap.put("signType",WXPayConstants.HMACSHA256);
        retMap.put("paySign", WXPayUtil.generateSignature(retMap, systemConfig.getWx_key(), WXPayConstants.SignType.HMACSHA256));
        retMap.put("out_trade_no", orderQo1.getOut_trade_no());
        logger.info("创建订单成功后再次签名输出："+retMap);
        return GsonUtils.toJson(retMap);
    }

    @GetMapping("/order/findPage")
    public String findPage(OrderQo orderQo) {
        return orderServiceClient.findPage(orderQo);
    }
}
