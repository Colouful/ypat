package com.ypat.controller;

import com.ypat.OrderQo;
import com.ypat.PaymentCreateResult;
import com.ypat.PaymentOrderQo;
import com.ypat.ProductQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.enums.OrderType;
import com.ypat.enums.PaymentBusinessType;
import com.ypat.enums.PaymentChannel;
import com.ypat.payment.WechatPaymentReconcileService;
import com.ypat.payment.WechatPaymentService;
import com.ypat.service.OrderServiceClient;
import com.ypat.service.PaymentOrderServiceClient;
import com.ypat.service.PpdOrderServiceClient;
import com.ypat.service.ProductServiceClient;
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
public class PpdController {

    @Autowired
    private ProductServiceClient productServiceClient;
    @Autowired
    private OrderServiceClient orderServiceClient;
    @Autowired
    private PpdOrderServiceClient ppdOrderServiceClient;
    @Autowired
    private PaymentOrderServiceClient paymentOrderServiceClient;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private WechatPaymentService wechatPaymentService;
    @Autowired
    private WechatPaymentReconcileService wechatPaymentReconcileService;

    @PostMapping("/ppd/order/create")
    public PaymentCreateResult createOrder(@RequestParam("productId") Long productId,
                                           @RequestParam(defaultValue = "MINIAPP") String channel,
                                           HttpServletRequest request) {
        if (productId == null || !PaymentChannel.supportedForCreate(channel)) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        ProductQo product = GsonUtils.fromJson(productServiceClient.get(productId), ProductQo.class);
        if (product == null || product.getOldval() == null || product.getOldval() <= 0
                || product.getCurrval() == null || product.getCurrval() <= 0) {
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        if (!isProductUpStatus(product.getStatus())) throw new SysException(ResponseCode.FAIL_VAL);

        Long userId = requireUserId();
        String outTradeNo = generateOutTradeNo(userId);
        OrderQo order = new OrderQo();
        order.setType(OrderType.PPD.value);
        order.setProductid(product.getId());
        order.setTotal_fee(product.getOldval());
        order.setUserid(userId);
        order.setOut_trade_no(outTradeNo);
        ppdOrderServiceClient.addPpdPayment(order);

        ensurePaymentOrder(outTradeNo, userId, channel, product.getOldval());
        WechatPaymentService.WechatPaymentCommand command = new WechatPaymentService.WechatPaymentCommand();
        command.setBusinessType(PaymentBusinessType.PPD.value);
        command.setChannel(channel);
        command.setDescription("拍豆充值-" + product.getName());
        command.setOutTradeNo(outTradeNo);
        command.setAmountFen(product.getOldval());
        command.setClientIp(clientIp(request));
        if (PaymentChannel.MINIAPP.value.equals(channel)) command.setOpenid(requireOpenid(userId));

        PaymentCreateResult result = wechatPaymentService.create(command);
        paymentOrderServiceClient.prepared(outTradeNo, channel, extractPrepayId(result), result.getH5Url());
        return result;
    }

    @GetMapping("/ppd/order/status")
    public String status(@RequestParam("out_trade_no") String outTradeNo) {
        Long userId = requireUserId();
        OrderQo query = statusQuery(outTradeNo, userId);
        if (wechatPaymentReconcileService.syncPaidIfWechatSuccess(outTradeNo)) {
            query = statusQuery(outTradeNo, userId);
        }
        return orderServiceClient.findPage(query);
    }

    private OrderQo statusQuery(String outTradeNo, Long userId) {
        if (outTradeNo == null || outTradeNo.trim().isEmpty()) throw new SysException(ResponseCode.FAIL_PARA);
        OrderQo query = new OrderQo();
        query.setUserid(userId);
        query.setOut_trade_no(outTradeNo);
        query.setPage(0);
        query.setSize(1);
        return query;
    }

    private void ensurePaymentOrder(String outTradeNo, Long userId, String channel, Integer amountFen) {
        PaymentOrderQo existing = paymentOrderServiceClient.get(outTradeNo);
        if (existing != null) return;
        paymentOrderServiceClient.createPending(PaymentBusinessType.PPD.value, outTradeNo, outTradeNo,
                userId, channel, amountFen);
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

    private String generateOutTradeNo(Long userId) {
        return "P" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + userId
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    private boolean isProductUpStatus(String status) {
        return "0".equals(status) || "up".equalsIgnoreCase(status);
    }
}
