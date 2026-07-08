package com.ypat.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.payment.WechatNotifyPayload;
import com.ypat.payment.WechatPayV3Client;
import com.ypat.service.PaymentOrderServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class PaymentNotifyController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentNotifyController.class);

    @Autowired
    private WechatPayV3Client wechatPayV3Client;
    @Autowired
    private PaymentOrderServiceClient paymentOrderServiceClient;

    @PostMapping("/payment/wechat/notify")
    public Map<String, String> notify(@RequestHeader("Wechatpay-Serial") String serial,
                                      @RequestHeader("Wechatpay-Timestamp") String timestamp,
                                      @RequestHeader("Wechatpay-Nonce") String nonce,
                                      @RequestHeader("Wechatpay-Signature") String signature,
                                      @RequestBody String body) {
        try {
            WechatNotifyPayload payload = wechatPayV3Client.parseNotify(serial, timestamp, nonce, signature, body);
            payload.setEventId(eventId(body));
            Boolean first = paymentOrderServiceClient.markPaid(payload.getOutTradeNo(),
                    payload.getTransactionId(),
                    payload.getAmountFen(),
                    paidAtMs(payload.getSuccessTime()),
                    payload.getEventId(),
                    sha256(body));
            logger.info("wechat.v3.notify.success outTradeNo={} txId={} first={}",
                    payload.getOutTradeNo(), payload.getTransactionId(), first);
            return response("SUCCESS", "成功");
        } catch (SysException ex) {
            logger.warn("wechat.v3.notify.fail code={} msg={}", ex.getCode(), ex.getMsg());
            return response("FAIL", ex.getMsg());
        } catch (Exception ex) {
            logger.error("wechat.v3.notify.error", ex);
            return response("FAIL", ResponseCode.FAIL_SER.getMsg());
        }
    }

    private Long paidAtMs(String successTime) {
        if (successTime == null || successTime.trim().isEmpty()) return System.currentTimeMillis();
        return Date.from(OffsetDateTime.parse(successTime).toInstant()).getTime();
    }

    private String eventId(String body) {
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            return json.has("id") && !json.get("id").isJsonNull() ? json.get("id").getAsString() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String sha256(String body) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest((body == null ? "" : body).getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private Map<String, String> response(String code, String message) {
        Map<String, String> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        return map;
    }
}
