package com.ypat.controller;

import com.ypat.MemberBenefitQuoteQo;
import com.ypat.MemberOrderQo;
import com.ypat.MemberPlanQo;
import com.ypat.MemberStatusQo;
import com.ypat.PaymentCreateResult;
import com.ypat.PaymentOrderQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.enums.PaymentBusinessType;
import com.ypat.enums.PaymentChannel;
import com.ypat.payment.WechatPaymentReconcileService;
import com.ypat.payment.WechatPaymentService;
import com.ypat.service.MemberServiceClient;
import com.ypat.service.PaymentOrderServiceClient;
import com.ypat.service.UserServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
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
    private PaymentOrderServiceClient paymentOrderServiceClient;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private WechatPaymentService wechatPaymentService;
    @Autowired
    private WechatPaymentReconcileService wechatPaymentReconcileService;

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
    public PaymentCreateResult createOrder(@RequestParam @NotNull Long planId,
                                           @RequestParam(defaultValue = "MINIAPP") String channel,
                                           HttpServletRequest request) {
        if (!PaymentChannel.supportedForCreate(channel)) throw new SysException(ResponseCode.FAIL_PARA);
        Long userId = requireUserId();
        MemberOrderQo order = memberServiceClient.createOrder(userId, planId);
        if (order == null) throw new SysException(ResponseCode.FAIL_ORDER);

        ensurePaymentOrder(PaymentBusinessType.MEMBER.value, order.getOutTradeNo(), userId, channel, order.getPriceFen());

        WechatPaymentService.WechatPaymentCommand command = new WechatPaymentService.WechatPaymentCommand();
        command.setBusinessType(PaymentBusinessType.MEMBER.value);
        command.setChannel(channel);
        command.setDescription("会员充值-" + (order.getPlanCode() == null ? "" : order.getPlanCode()));
        command.setOutTradeNo(order.getOutTradeNo());
        command.setAmountFen(order.getPriceFen());
        command.setClientIp(clientIp(request));
        if (PaymentChannel.MINIAPP.value.equals(channel)) {
            command.setOpenid(requireOpenid(userId));
        }

        PaymentCreateResult result = wechatPaymentService.create(command);
        String prepayId = extractPrepayId(result);
        paymentOrderServiceClient.prepared(order.getOutTradeNo(), channel, prepayId, result.getH5Url());
        memberServiceClient.prepared(order.getOutTradeNo(), channel, prepayId);
        return result;
    }

    @GetMapping("/member/order/status")
    public MemberOrderQo orderStatus(@RequestParam String out_trade_no) {
        Long userId = requireUserId();
        MemberOrderQo qo = memberServiceClient.getOrder(out_trade_no, userId);
        if (qo == null) throw new SysException(ResponseCode.FAIL_NOT);
        if (!userId.equals(qo.getUserId())) throw new SysException(ResponseCode.FAIL_VAL);
        if ("0".equals(qo.getStatus()) && wechatPaymentReconcileService.syncPaidIfWechatSuccess(out_trade_no)) {
            qo = memberServiceClient.getOrder(out_trade_no, userId);
        }
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

    private void ensurePaymentOrder(String businessType, String outTradeNo, Long userId,
                                    String channel, Integer amountFen) {
        PaymentOrderQo existing = paymentOrderServiceClient.get(outTradeNo);
        if (existing != null) return;
        paymentOrderServiceClient.createPending(businessType, outTradeNo, outTradeNo, userId, channel, amountFen);
    }

    private String extractPrepayId(PaymentCreateResult result) {
        if (result == null || result.getPayParams() == null) return null;
        String pkg = result.getPayParams().getPackageValue();
        if (pkg != null && pkg.startsWith("prepay_id=")) return pkg.substring("prepay_id=".length());
        return null;
    }

    private String requireOpenid(Long userId) {
        UserQo user = GsonUtils.fromJson(userServiceClient.get(userId), UserQo.class);
        if (user == null || user.getOpenid() == null || user.getOpenid().trim().isEmpty()) {
            throw new SysException(ResponseCode.FAIL_PARA, "用户 openid 缺失");
        }
        return user.getOpenid();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
