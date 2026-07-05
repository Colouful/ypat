package com.ypat.controller;

import com.ypat.MemberOrderCreateResult;
import com.ypat.MemberBenefitQuoteQo;
import com.ypat.MemberOrderQo;
import com.ypat.MemberPlanQo;
import com.ypat.MemberStatusQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.config.SystemConfig;
import com.ypat.third.wxpay.sdk.WXPayClient;
import com.ypat.third.wxpay.sdk.WXPayConstants;
import com.ypat.third.wxpay.sdk.WXPayUtil;
import com.ypat.service.MemberServiceClient;
import com.ypat.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员对外接口。下单流程：
 *   1. MemberServiceClient.createPendingOrder 拿到待支付订单 Qo
 *   2. WXPayClient.unifiedOrder(Map) 调微信统一下单
 *   3. 拼装 {appId, timeStamp, nonceStr, package, signType, paySign, outTradeNo} 给前端
 */
@RestController
@Validated
public class MemberController {

    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberServiceClient memberServiceClient;
    @Autowired
    private WXPayClient wxPayClient;
    @Autowired
    private SystemConfig systemConfig;

    @GetMapping("/member/plans")
    public List<MemberPlanQo> plans() {
        return memberServiceClient.plans();
    }

    @GetMapping("/member/status")
    public MemberStatusQo status() {
        Long userId = requireUserId();
        return memberServiceClient.status(userId);
    }

    @GetMapping("/member/benefit/quote")
    public MemberBenefitQuoteQo quote(@RequestParam String scene) {
        Long userId = requireUserId();
        return memberServiceClient.quote(userId, scene);
    }

    @PostMapping("/member/order/create")
    public MemberOrderCreateResult createOrder(@RequestParam @NotNull Long planId) {
        Long userId = requireUserId();
        MemberOrderQo order = memberServiceClient.createOrder(userId, planId);
        if (order == null) throw new SysException(ResponseCode.FAIL_ORDER);

        MemberOrderCreateResult result = new MemberOrderCreateResult();
        result.setOutTradeNo(order.getOutTradeNo());
        try {
            Map<String, String> req = new HashMap<>();
            req.put("body", "会员充值-" + (order.getPlanCode() == null ? "" : order.getPlanCode()));
            req.put("out_trade_no", order.getOutTradeNo());
            req.put("total_fee", String.valueOf(order.getPriceFen()));
            req.put("spbill_create_ip", "112.126.103.244");
            req.put("notify_url", "https://www.91qupaier.com/wxpay/notify");
            req.put("trade_type", "JSAPI");
            Map<String, String> resp = wxPayClient.unifiedOrder(req);
            if (WXPayConstants.FAIL.equals(resp.get("return_code"))) {
                throw new SysException(ResponseCode.FAIL_WX);
            }
            if (WXPayConstants.FAIL.equals(resp.get("result_code"))) {
                throw new SysException(ResponseCode.FAIL_ORDER);
            }
            result.setAppId(systemConfig.getWx_appid());
            result.setTimeStamp(System.currentTimeMillis() / 1000 + "");
            result.setNonceStr(WXPayUtil.generateNonceStr());
            result.setPackageValue("prepay_id=" + resp.get("prepay_id"));
            result.setSignType(WXPayConstants.HMACSHA256);
            Map<String, String> retMap = new HashMap<>();
            retMap.put("appId", result.getAppId());
            retMap.put("timeStamp", result.getTimeStamp());
            retMap.put("nonceStr", result.getNonceStr());
            retMap.put("package", result.getPackageValue());
            retMap.put("signType", result.getSignType());
            result.setPaySign(WXPayUtil.generateSignature(retMap, systemConfig.getWx_key(), WXPayConstants.SignType.HMACSHA256));
            return result;
        } catch (SysException se) {
            throw se;
        } catch (Exception e) {
            logger.error("member.create.wxpay_fail user={} plan={} outTradeNo={}", userId, planId, order.getOutTradeNo(), e);
            throw new SysException(ResponseCode.FAIL_WX);
        }
    }

    @GetMapping("/member/order/status")
    public MemberOrderQo orderStatus(@RequestParam String out_trade_no) {
        Long userId = requireUserId();
        MemberOrderQo qo = memberServiceClient.getOrder(out_trade_no, userId);
        if (qo == null) throw new SysException(ResponseCode.FAIL_NOT);
        if (!userId.equals(qo.getUserId())) throw new SysException(ResponseCode.FAIL_VAL);
        return qo;
    }

    @GetMapping("/member/orders")
    public Map<String, Object> orders(MemberOrderQo qo) {
        Long userId = requireUserId();
        qo.setUserId(userId);
        return memberServiceClient.findOrders(qo);
    }

    @PostMapping("/member/order/cancel")
    public Object cancel(@RequestParam String out_trade_no) {
        Long userId = requireUserId();
        MemberOrderQo existing = memberServiceClient.getOrder(out_trade_no, userId);
        if (existing == null || !userId.equals(existing.getUserId())) {
            throw new SysException(ResponseCode.FAIL_VAL);
        }
        return memberServiceClient.cancelOrder(out_trade_no);
    }

    private Long requireUserId() {
        String raw = UserUtil.getUserId();
        if (raw == null || raw.isEmpty()) throw new SysException(ResponseCode.FAIL_VAL);
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            logger.warn("member.user_id_parse_fail raw={}", raw);
            throw new SysException(ResponseCode.FAIL_VAL);
        }
    }
}
