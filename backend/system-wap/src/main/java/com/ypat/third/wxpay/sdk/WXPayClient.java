package com.ypat.third.wxpay.sdk;

import com.ypat.OrderQo;
import com.ypat.UserQo;
import com.ypat.config.SystemConfig;
import com.ypat.enums.OrderType;
import com.ypat.service.UserServiceClient;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.util.TradeGenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class WXPayClient {

    private static final Logger logger = LoggerFactory.getLogger(WXPayClient.class);
    private WXPayConfigImpl config = null;
    private WXPay wxpay = null;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private SystemConfig systemConfig;

    @PostConstruct
    public void init() {
        try {
            config = new WXPayConfigImpl(systemConfig);
            wxpay = new WXPay(config, true, false);
        } catch (Exception e) {
            logger.error("wxclient初始化失败", e);
        }
    }

    public String code2Session(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + config.getAppID()
                + "&secret=" + config.getAppSecret()
                + "&js_code=" + code
                + "&grant_type=authorization_code";
        ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);
        logger.info("微信 code2Session 请求完成，status={}", entity.getStatusCodeValue());
        if (entity.getStatusCode().equals(HttpStatus.OK)) {
            return entity.getBody();
        }
        return null;
    }

    public Map<String, String> processResponseXml(String xml) throws Exception {
        return wxpay.processResponseXml(xml);
    }

    public Map<String, String> generateSignature() throws Exception {
        Map<String, String> retMap = new HashMap<>();
        retMap.put("appId", config.getAppID());
        retMap.put("timeStamp", System.currentTimeMillis() / 1000 + "");
        retMap.put("nonceStr", WXPayUtil.generateNonceStr());
        retMap.put("package", "prepay_id=wx10221310881700ff04e621d01896363200");
        retMap.put("signType", WXPayConstants.HMACSHA256);
        retMap.put("paySign", WXPayUtil.generateSignature(retMap, config.getKey(), WXPayConstants.SignType.HMACSHA256));
        return retMap;
    }

    public Map<String, String> unifiedOrder(OrderQo orderQo) throws Exception {
        Map<String, String> data = new HashMap<>();
        String userStr = userServiceClient.get(orderQo.getUserid());
        UserQo userQo = GsonUtils.fromJson(userStr, UserQo.class);
        data.put("body", OrderType.getNameByCode(orderQo.getType()));
        data.put("out_trade_no", TradeGenUtil.genTradeSerialNo());
        // total_fee 在前后端统一使用“分”，禁止再次乘以 100。
        data.put("total_fee", String.valueOf(orderQo.getTotal_fee()));
        data.put("spbill_create_ip", "112.126.103.244");
        data.put("notify_url", "https://www.91qupaier.com/wxpay/notify");
        data.put("trade_type", "JSAPI");
        data.put("openid", userQo.getOpenid());
        Map<String, String> resp = wxpay.unifiedOrder(data);
        resp.put("out_trade_no", data.get("out_trade_no"));
        logger.info("微信统一下单完成，outTradeNo={}, returnCode={}, resultCode={}",
                data.get("out_trade_no"), resp.get("return_code"), resp.get("result_code"));
        return resp;
    }

    public String getSandboxSignKey() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("mch_id", config.getMchID());
            params.put("nonce_str", WXPayUtil.generateNonceStr());
            params.put("sign", WXPayUtil.generateSignature(params, config.getKey()));
            String strXML = wxpay.requestWithoutCert("/sandboxnew/pay/getsignkey",
                    params, config.getHttpConnectTimeoutMs(), config.getHttpReadTimeoutMs());
            Map<String, String> result = WXPayUtil.xmlToMap(strXML);
            if ("SUCCESS".equals(result.get("return_code"))) {
                return result.get("sandbox_signkey");
            }
            return null;
        } catch (Exception e) {
            logger.warn("获取微信沙箱签名密钥失败", e);
            return null;
        }
    }

    public Map<String, String> unifiedOrder(Map<String, String> data) throws Exception {
        Map<String, String> resp = wxpay.unifiedOrder(data);
        logger.info("微信统一下单完成，returnCode={}, resultCode={}", resp.get("return_code"), resp.get("result_code"));
        return resp;
    }
}
