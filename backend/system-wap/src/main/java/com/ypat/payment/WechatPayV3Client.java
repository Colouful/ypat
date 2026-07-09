package com.ypat.payment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ypat.PaymentPayParams;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.h5.model.H5Info;
import com.wechat.pay.java.service.payments.h5.model.PrepayResponse;
import com.wechat.pay.java.service.payments.h5.model.SceneInfo;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.model.TransactionAmount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WechatPayV3Client {

    private final WechatPayV3Config config;
    private final Gateway gateway;

    @Autowired
    public WechatPayV3Client(WechatPayV3Config config) {
        this(config, new SdkGateway());
    }

    WechatPayV3Client(WechatPayV3Config config, Gateway gateway) {
        this.config = config;
        this.gateway = gateway;
    }

    public PaymentPayParams prepayMiniapp(PrepayCommand command) {
        validateBase(command);
        if (!WechatPayV3Config.hasText(command.getOpenid())) {
            throw new SysException(ResponseCode.FAIL_PARA, "openid 未配置");
        }

        Config sdkConfig = config.sdkConfig();
        com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest req =
                new com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest();
        req.setAppid(config.miniappAppId());
        req.setMchid(config.mchId());
        req.setDescription(command.getDescription());
        req.setOutTradeNo(command.getOutTradeNo());
        req.setNotifyUrl(config.notifyUrl());
        com.wechat.pay.java.service.payments.jsapi.model.Amount amount =
                new com.wechat.pay.java.service.payments.jsapi.model.Amount();
        amount.setTotal(command.getAmountFen());
        amount.setCurrency("CNY");
        req.setAmount(amount);
        Payer payer = new Payer();
        payer.setOpenid(command.getOpenid());
        req.setPayer(payer);

        PrepayWithRequestPaymentResponse resp;
        try {
            resp = gateway.prepayMiniapp(sdkConfig, req);
        } catch (RuntimeException ex) {
            throw mapWechatPayException(ex);
        }
        PaymentPayParams params = new PaymentPayParams();
        params.setTimeStamp(resp.getTimeStamp());
        params.setNonceStr(resp.getNonceStr());
        params.setPackageValue(resp.getPackageVal());
        params.setSignType(resp.getSignType());
        params.setPaySign(resp.getPaySign());
        return params;
    }

    public String prepayH5(PrepayCommand command) {
        validateBase(command);
        if (!WechatPayV3Config.hasText(command.getClientIp())) {
            throw new SysException(ResponseCode.FAIL_PARA, "clientIp 未配置");
        }

        Config sdkConfig = config.sdkConfig();
        com.wechat.pay.java.service.payments.h5.model.PrepayRequest req =
                new com.wechat.pay.java.service.payments.h5.model.PrepayRequest();
        req.setAppid(config.h5AppId());
        req.setMchid(config.mchId());
        req.setDescription(command.getDescription());
        req.setOutTradeNo(command.getOutTradeNo());
        req.setNotifyUrl(config.notifyUrl());
        com.wechat.pay.java.service.payments.h5.model.Amount amount =
                new com.wechat.pay.java.service.payments.h5.model.Amount();
        amount.setTotal(command.getAmountFen());
        amount.setCurrency("CNY");
        req.setAmount(amount);
        req.setSceneInfo(h5Scene(command.getClientIp()));

        PrepayResponse resp;
        try {
            resp = gateway.prepayH5(sdkConfig, req);
        } catch (RuntimeException ex) {
            throw mapWechatPayException(ex);
        }
        if (resp == null || !WechatPayV3Config.hasText(resp.getH5Url())) {
            throw new SysException(ResponseCode.FAIL_ORDER, "微信 H5 下单未返回 h5_url");
        }
        return resp.getH5Url();
    }

    private RuntimeException mapWechatPayException(RuntimeException ex) {
        if (ex instanceof SysException) {
            return ex;
        }
        if (isWechatNoAuth(ex)) {
            return new SysException(ResponseCode.FAIL_PAY,
                    "商户号该产品权限未开通，请在微信支付商户平台 > 产品中心开通对应支付产品后重试");
        }
        return ex;
    }

    private boolean isWechatNoAuth(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains("NO_AUTH") && message.contains("产品权限未开通")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    public WechatNotifyPayload parseNotify(String serial, String timestamp, String nonce,
                                           String signature, String body) {
        if (!WechatPayV3Config.hasText(serial) || !WechatPayV3Config.hasText(timestamp)
                || !WechatPayV3Config.hasText(nonce) || !WechatPayV3Config.hasText(signature)
                || !WechatPayV3Config.hasText(body)) {
            throw new SysException(ResponseCode.FAIL_PAY_NOTIFY_SIGN);
        }
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(serial)
                .timestamp(timestamp)
                .nonce(nonce)
                .signature(signature)
                .body(body)
                .build();
        Transaction tx = gateway.parseNotify(config.notificationParser(), requestParam);
        return toNotifyPayload(tx);
    }

    private WechatNotifyPayload toNotifyPayload(Transaction tx) {
        if (tx == null) throw new SysException(ResponseCode.FAIL_PAY);
        WechatNotifyPayload payload = new WechatNotifyPayload();
        payload.setOutTradeNo(tx.getOutTradeNo());
        payload.setTransactionId(tx.getTransactionId());
        payload.setTradeState(tx.getTradeState() == null ? null : tx.getTradeState().name());
        payload.setSuccessTime(tx.getSuccessTime());
        TransactionAmount amount = tx.getAmount();
        if (amount != null) {
            payload.setAmountFen(amount.getTotal());
        }
        return payload;
    }

    private SceneInfo h5Scene(String clientIp) {
        SceneInfo scene = new SceneInfo();
        scene.setPayerClientIp(clientIp);
        H5Info h5 = new H5Info();
        h5.setType("Wap");
        String raw = config.h5SceneInfo();
        if (WechatPayV3Config.hasText(raw)) {
            JsonObject json = JsonParser.parseString(raw).getAsJsonObject();
            h5.setType(text(json, "type", h5.getType()));
            h5.setAppName(text(json, "appName", text(json, "app_name", null)));
            h5.setAppUrl(text(json, "appUrl", text(json, "app_url", null)));
            h5.setBundleId(text(json, "bundleId", text(json, "bundle_id", null)));
            h5.setPackageName(text(json, "packageName", text(json, "package_name", null)));
        }
        scene.setH5Info(h5);
        return scene;
    }

    private String text(JsonObject json, String name, String fallback) {
        if (json != null && json.has(name) && !json.get(name).isJsonNull()) {
            String value = json.get(name).getAsString();
            return WechatPayV3Config.hasText(value) ? value : fallback;
        }
        return fallback;
    }

    private void validateBase(PrepayCommand command) {
        config.assertConfigured();
        if (command == null || !WechatPayV3Config.hasText(command.getDescription())
                || !WechatPayV3Config.hasText(command.getOutTradeNo())
                || command.getAmountFen() == null || command.getAmountFen() <= 0) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
    }

    interface Gateway {
        PrepayWithRequestPaymentResponse prepayMiniapp(Config config,
                com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest request);

        PrepayResponse prepayH5(Config config,
                com.wechat.pay.java.service.payments.h5.model.PrepayRequest request);

        Transaction parseNotify(NotificationParser parser, RequestParam requestParam);
    }

    private static class SdkGateway implements Gateway {
        @Override
        public PrepayWithRequestPaymentResponse prepayMiniapp(Config config,
                com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest request) {
            return new JsapiServiceExtension.Builder().config(config).build().prepayWithRequestPayment(request);
        }

        @Override
        public PrepayResponse prepayH5(Config config,
                com.wechat.pay.java.service.payments.h5.model.PrepayRequest request) {
            return new H5Service.Builder().config(config).build().prepay(request);
        }

        @Override
        public Transaction parseNotify(NotificationParser parser, RequestParam requestParam) {
            return parser.parse(requestParam, Transaction.class);
        }
    }

    public static class PrepayCommand {
        private String description;
        private String outTradeNo;
        private Integer amountFen;
        private String openid;
        private String clientIp;

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getOutTradeNo() { return outTradeNo; }
        public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
        public Integer getAmountFen() { return amountFen; }
        public void setAmountFen(Integer amountFen) { this.amountFen = amountFen; }
        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
        public String getClientIp() { return clientIp; }
        public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    }
}
