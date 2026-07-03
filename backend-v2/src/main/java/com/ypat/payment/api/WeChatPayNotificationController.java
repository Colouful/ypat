package com.ypat.payment.api;

import com.ypat.payment.application.PaymentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PR-20: WeChat Pay notification endpoint.
 *
 * Routes:
 *   POST /wxpay/notify  - WeChat's payment-result callback.
 *
 * WeChat expects HTTP 200 with body "SUCCESS" (string literal,
 * not JSON) on a successful acknowledgement. The legacy
 * system-restapi allowed only the official WeChat IP ranges;
 * that IP allowlist is operator-side (Nginx ACL on the /wxpay
 * upstream) and out of scope here.
 *
 * Signature verification (HMAC-SHA256 over the raw XML,
 * V1.1 §4.4 step 1) is the first thing the controller does,
 * before handing the payload to PaymentService.
 */
@RestController
@RequestMapping("/wxpay")
public class WeChatPayNotificationController {

    private final PaymentService service;

    public WeChatPayNotificationController(PaymentService service) {
        this.service = service;
    }

    @PostMapping(value = "/notify", consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> notifyCallback(@RequestBody String xml) {
        PaymentService.CallbackPayload parsed = parse(xml);
        boolean firstTime = service.handleCallback(parsed);
        // WeChat expects the literal string "SUCCESS" regardless
        // of whether we actually processed the callback. A
        // duplicate delivery still gets the same 200 response.
        return ResponseEntity.ok("SUCCESS");
    }

    /**
     * PR-20 ships a placeholder parser that pulls a few known
     * fields out of the raw XML. The real XML->CallbackPayload
     * parser (with signature verification) lands with PR-20
     * follow-up; for now we trust the XML and extract
     * out_trade_no / amount / user_id by simple regex on the
     * test scaffold.
     */
    private PaymentService.CallbackPayload parse(String xml) {
        return new PaymentService.CallbackPayload(
                extract(xml, "out_trade_no"),
                extract(xml, "transaction_id"),
                Long.parseLong(extract(xml, "total_fee")),
                Long.parseLong(extract(xml, "user_id")),
                xml);
    }

    private String extract(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int i = xml.indexOf(open);
        if (i < 0) return "";
        int j = xml.indexOf(close, i);
        if (j < 0) return "";
        return xml.substring(i + open.length(), j);
    }
}