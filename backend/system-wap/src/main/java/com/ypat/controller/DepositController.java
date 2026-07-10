package com.ypat.controller;

import com.ypat.DepositConfigQo;
import com.ypat.DepositOrderQo;
import com.ypat.PaymentCreateResult;
import com.ypat.PaymentOrderQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.enums.PaymentBusinessType;
import com.ypat.enums.PaymentChannel;
import com.ypat.enums.PaymentStatus;
import com.ypat.payment.WechatPaymentReconcileService;
import com.ypat.payment.WechatPaymentService;
import com.ypat.service.DepositServiceClient;
import com.ypat.service.PaymentOrderServiceClient;
import com.ypat.service.UserServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class DepositController {

    @Autowired
    private DepositServiceClient depositServiceClient;
    @Autowired
    private PaymentOrderServiceClient paymentOrderServiceClient;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private WechatPaymentService wechatPaymentService;
    @Autowired
    private WechatPaymentReconcileService wechatPaymentReconcileService;

    @GetMapping("/deposit/config")
    public DepositConfigQo config() {
        return depositServiceClient.config();
    }

    @PostMapping("/deposit/order/create")
    public PaymentCreateResult createOrder(@RequestParam(defaultValue = "MINIAPP") String channel,
                                           HttpServletRequest request) {
        if (!PaymentChannel.supportedForCreate(channel)) throw new SysException(ResponseCode.FAIL_PARA);
        Long userId = requireUserId();
        DepositConfigQo config = depositServiceClient.config();
        if (config == null || !"1".equals(config.getEnabled())) throw new SysException(ResponseCode.FAIL_VAL);

        DepositOrderQo order = depositServiceClient.createOrder(userId, channel);
        ensurePaymentOrder(PaymentBusinessType.DEPOSIT.value, order.getOutTradeNo(), userId,
                channel, order.getAmountFen());

        WechatPaymentService.WechatPaymentCommand command = new WechatPaymentService.WechatPaymentCommand();
        command.setBusinessType(PaymentBusinessType.DEPOSIT.value);
        command.setChannel(channel);
        command.setDescription("保证金");
        command.setOutTradeNo(order.getOutTradeNo());
        command.setAmountFen(order.getAmountFen());
        command.setClientIp(clientIp(request));
        if (PaymentChannel.MINIAPP.value.equals(channel)) {
            command.setOpenid(requireOpenid(userId));
        }

        PaymentCreateResult result = wechatPaymentService.create(command);
        String prepayId = extractPrepayId(result);
        paymentOrderServiceClient.prepared(order.getOutTradeNo(), channel, prepayId, result.getH5Url());
        depositServiceClient.prepared(order.getOutTradeNo(), channel, prepayId);
        return result;
    }

    @GetMapping("/deposit/order/status")
    public DepositOrderQo status(@RequestParam("out_trade_no") String outTradeNo) {
        DepositOrderQo qo = depositServiceClient.getOrder(outTradeNo, requireUserId());
        if (qo == null) throw new SysException(ResponseCode.FAIL_NOT);
        if (PaymentStatus.PENDING.value.equals(qo.getStatus())
                && wechatPaymentReconcileService.syncPaidIfWechatSuccess(outTradeNo)) {
            qo = depositServiceClient.getOrder(outTradeNo, requireUserId());
        }
        return qo;
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

    private Long requireUserId() {
        String raw = UserUtil.getUserId();
        if (raw == null || raw.trim().isEmpty()) throw new SysException(ResponseCode.FAIL_VAL);
        return Long.parseLong(raw);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
