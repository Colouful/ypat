package com.ypat.controller;

import com.ypat.OrderQo;
import com.ypat.PaymentCreateResult;
import com.ypat.PaymentOrderQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.enums.OrderType;
import com.ypat.enums.PaymentBusinessType;
import com.ypat.enums.PaymentChannel;
import com.ypat.enums.UserStatus;
import com.ypat.enums.YesNo;
import com.ypat.payment.WechatPaymentReconcileService;
import com.ypat.payment.WechatPaymentService;
import com.ypat.service.OrderServiceClient;
import com.ypat.service.PaymentOrderServiceClient;
import com.ypat.service.RealnameOrderServiceClient;
import com.ypat.service.UserServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
public class RealnamePaymentController {

    private static final int REALNAME_AUDIT_FEE_FEN = 2900;

    @Autowired
    private OrderServiceClient orderServiceClient;
    @Autowired
    private RealnameOrderServiceClient realnameOrderServiceClient;
    @Autowired
    private PaymentOrderServiceClient paymentOrderServiceClient;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private WechatPaymentService wechatPaymentService;
    @Autowired
    private WechatPaymentReconcileService wechatPaymentReconcileService;

    @PostMapping("/realname/order/create")
    public PaymentCreateResult createOrder(@RequestParam(defaultValue = "MINIAPP") String channel,
                                           HttpServletRequest request) {
        if (!PaymentChannel.supportedForCreate(channel)) throw new SysException(ResponseCode.FAIL_PARA);

        Long userId = requireUserId();
        UserQo user = requireUnpaidUser(userId);
        String outTradeNo = generateOutTradeNo(userId);

        OrderQo order = new OrderQo();
        order.setType(OrderType.REAL.value);
        order.setTotal_fee(REALNAME_AUDIT_FEE_FEN);
        order.setUserid(userId);
        order.setOut_trade_no(outTradeNo);
        realnameOrderServiceClient.addRealnamePayment(order);

        ensurePaymentOrder(outTradeNo, userId, channel);
        WechatPaymentService.WechatPaymentCommand command = new WechatPaymentService.WechatPaymentCommand();
        command.setBusinessType(PaymentBusinessType.REALNAME.value);
        command.setChannel(channel);
        command.setDescription("实名认证审核费");
        command.setOutTradeNo(outTradeNo);
        command.setAmountFen(REALNAME_AUDIT_FEE_FEN);
        command.setClientIp(clientIp(request));
        if (PaymentChannel.MINIAPP.value.equals(channel)) command.setOpenid(requireOpenid(user));

        PaymentCreateResult result = wechatPaymentService.create(command);
        paymentOrderServiceClient.prepared(outTradeNo, channel, extractPrepayId(result), result.getH5Url());
        return result;
    }

    @GetMapping("/realname/order/status")
    public String status(@RequestParam("out_trade_no") String outTradeNo) {
        Long userId = requireUserId();
        OrderQo query = statusQuery(outTradeNo, userId);
        if (wechatPaymentReconcileService.syncPaidIfWechatSuccess(outTradeNo)) {
            query = statusQuery(outTradeNo, userId);
        }
        return orderServiceClient.findPage(query);
    }

    private UserQo requireUnpaidUser(Long userId) {
        UserQo user = GsonUtils.fromJson(userServiceClient.get(userId), UserQo.class);
        if (user == null) throw new SysException(ResponseCode.FAIL_NOT);
        String status = user.getStatus();
        if (YesNo.yes.value.equals(user.getRealnameflag())
                || UserStatus.ytj.value.equals(status)
                || UserStatus.shtg.value.equals(status)
                || UserStatus.shbtg.value.equals(status)
                || UserStatus.zfcg.value.equals(status)) {
            throw new SysException(ResponseCode.FAIL_VAL, "当前实名认证状态无需重复支付");
        }
        return user;
    }

    private OrderQo statusQuery(String outTradeNo, Long userId) {
        if (outTradeNo == null || outTradeNo.trim().isEmpty()) throw new SysException(ResponseCode.FAIL_PARA);
        OrderQo query = new OrderQo();
        query.setUserid(userId);
        query.setType(OrderType.REAL.value);
        query.setOut_trade_no(outTradeNo);
        query.setPage(0);
        query.setSize(1);
        return query;
    }

    private void ensurePaymentOrder(String outTradeNo, Long userId, String channel) {
        PaymentOrderQo existing = paymentOrderServiceClient.get(outTradeNo);
        if (existing != null) return;
        paymentOrderServiceClient.createPending(PaymentBusinessType.REALNAME.value, outTradeNo, outTradeNo,
                userId, channel, REALNAME_AUDIT_FEE_FEN);
    }

    private String extractPrepayId(PaymentCreateResult result) {
        if (result == null || result.getPayParams() == null) return null;
        String pkg = result.getPayParams().getPackageValue();
        if (pkg != null && pkg.startsWith("prepay_id=")) return pkg.substring("prepay_id=".length());
        return null;
    }

    private String requireOpenid(UserQo user) {
        if (user.getOpenid() == null || user.getOpenid().trim().isEmpty()) {
            throw new SysException(ResponseCode.FAIL_PARA, "用户 openid 缺失");
        }
        return user.getOpenid();
    }

    private Long requireUserId() {
        String raw = UserUtil.getUserId();
        if (raw == null || raw.trim().isEmpty()) throw new SysException(ResponseCode.FAIL_VAL);
        return Long.parseLong(raw);
    }

    private String generateOutTradeNo(Long userId) {
        return "R" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + userId
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
