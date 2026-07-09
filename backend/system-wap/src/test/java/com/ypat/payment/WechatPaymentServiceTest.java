package com.ypat.payment;

import com.ypat.PaymentCreateResult;
import com.ypat.PaymentPayParams;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.config.SystemConfig;
import com.ypat.enums.PaymentBusinessType;
import com.ypat.enums.PaymentChannel;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.h5.model.PrepayResponse;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class WechatPaymentServiceTest {

    @Test
    public void miniappPaymentMapsSdkPayParamsWithoutCallingWechatNetwork() {
        FakeGateway gateway = new FakeGateway();
        WechatPayV3Client client = new WechatPayV3Client(config(), gateway);
        WechatPaymentService service = new WechatPaymentService(client);

        PaymentCreateResult result = service.create(command(PaymentChannel.MINIAPP.value));

        assertEquals("D202607080001", result.getOutTradeNo());
        assertEquals(PaymentBusinessType.DEPOSIT.value, result.getBusinessType());
        assertEquals(PaymentChannel.MINIAPP.value, result.getChannel());
        assertEquals(Integer.valueOf(1), result.getAmountFen());
        assertEquals("wx-test-appid", gateway.lastMiniappRequest.getAppid());
        assertEquals("1900000001", gateway.lastMiniappRequest.getMchid());
        assertEquals("D202607080001", gateway.lastMiniappRequest.getOutTradeNo());
        assertEquals(Integer.valueOf(1), gateway.lastMiniappRequest.getAmount().getTotal());
        assertEquals("openid-1", gateway.lastMiniappRequest.getPayer().getOpenid());

        PaymentPayParams params = result.getPayParams();
        assertNotNull(params);
        assertEquals("1783499261", params.getTimeStamp());
        assertEquals("prepay_id=wx-prepay", params.getPackageValue());
        assertEquals("RSA", params.getSignType());
        assertEquals("pay-sign", params.getPaySign());
    }

    @Test
    public void h5PaymentMapsSceneAndH5Url() {
        FakeGateway gateway = new FakeGateway();
        WechatPayV3Client client = new WechatPayV3Client(config(), gateway);
        WechatPaymentService service = new WechatPaymentService(client);

        PaymentCreateResult result = service.create(command(PaymentChannel.H5.value));

        assertEquals("https://wx.tenpay.com/h5", result.getH5Url());
        assertEquals("127.0.0.1", gateway.lastH5Request.getSceneInfo().getPayerClientIp());
        assertEquals("Wap", gateway.lastH5Request.getSceneInfo().getH5Info().getType());
        assertEquals("Ypat", gateway.lastH5Request.getSceneInfo().getH5Info().getAppName());
    }

    @Test
    public void miniappPaymentNoAuthReturnsSafeBusinessMessage() {
        FakeGateway gateway = new FakeGateway();
        gateway.failWithNoAuth = true;
        WechatPaymentService service = new WechatPaymentService(new WechatPayV3Client(config(), gateway));

        try {
            service.create(command(PaymentChannel.MINIAPP.value));
            fail("expected wechat no auth to be mapped");
        } catch (SysException ex) {
            assertEquals(ResponseCode.FAIL_PAY.getCode(), ex.getCode());
            assertTrue(ex.getMsg().contains("商户号该产品权限未开通"));
            assertTrue(!ex.getMsg().contains("Authorization"));
            assertTrue(!ex.getMsg().contains("signature"));
            assertTrue(!ex.getMsg().contains("HttpRequest"));
        }
    }

    @Test
    public void missingV3ConfigListsEveryMissingPaymentSetting() {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setWx_appid("wx-test-appid");
        WechatPayV3Config cfg = new WechatPayV3Config(systemConfig);

        try {
            new WechatPaymentService(new WechatPayV3Client(cfg, new FakeGateway()))
                    .create(command(PaymentChannel.MINIAPP.value));
            fail("expected missing v3 config to fail");
        } catch (SysException ex) {
            assertTrue(ex.getMsg().contains("YPAT_WX_MCH_ID"));
            assertTrue(ex.getMsg().contains("YPAT_WX_MCH_SERIAL_NO"));
            assertTrue(ex.getMsg().contains("YPAT_WX_MCH_PRIVATE_KEY_PATH"));
            assertTrue(ex.getMsg().contains("YPAT_WX_API_V3_KEY"));
            assertTrue(ex.getMsg().contains("YPAT_WX_PAY_PUBLIC_KEY_ID"));
            assertTrue(ex.getMsg().contains("YPAT_WX_PAY_PUBLIC_KEY_PATH"));
            assertTrue(ex.getMsg().contains("YPAT_WX_NOTIFY_URL"));
        }
    }

    @Test
    public void configuredV3PaymentFailsFastWhenMerchantPrivateKeyFileMissing() throws Exception {
        File publicKey = File.createTempFile("wechat-pay-public-key", ".pem");
        publicKey.deleteOnExit();

        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setWx_pay_mode("PUBLIC_KEY");
        systemConfig.setWx_appid("wx-test-appid");
        systemConfig.setWx_mchid("1900000001");
        systemConfig.setWx_mch_serial_no("merchant-serial");
        systemConfig.setWx_mch_private_key_path(publicKey.getAbsolutePath() + ".missing");
        systemConfig.setWx_api_v3_key("12345678901234567890123456789012");
        systemConfig.setWx_pay_public_key_id("PUB_KEY_ID_TEST");
        systemConfig.setWx_pay_public_key_path(publicKey.getAbsolutePath());
        systemConfig.setWx_notify_url("https://example.com/payment/wechat/notify");
        WechatPayV3Config cfg = new WechatPayV3Config(systemConfig);

        try {
            new WechatPaymentService(new WechatPayV3Client(cfg, new FakeGateway()))
                    .create(command(PaymentChannel.MINIAPP.value));
            fail("expected missing merchant private key file to fail");
        } catch (SysException ex) {
            assertTrue(ex.getMsg().contains("YPAT_WX_MCH_PRIVATE_KEY_PATH"));
            assertTrue(ex.getMsg().contains("文件不存在或不可读"));
        }
    }

    private static WechatPaymentService.WechatPaymentCommand command(String channel) {
        WechatPaymentService.WechatPaymentCommand command = new WechatPaymentService.WechatPaymentCommand();
        command.setBusinessType(PaymentBusinessType.DEPOSIT.value);
        command.setChannel(channel);
        command.setDescription("保证金");
        command.setOutTradeNo("D202607080001");
        command.setAmountFen(1);
        command.setOpenid("openid-1");
        command.setClientIp("127.0.0.1");
        return command;
    }

    private static WechatPayV3Config config() {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setWx_pay_mode("PUBLIC_KEY");
        systemConfig.setWx_appid("wx-test-appid");
        systemConfig.setWx_h5_appid("wx-test-appid");
        systemConfig.setWx_mchid("1900000001");
        systemConfig.setWx_mch_serial_no("merchant-serial");
        systemConfig.setWx_mch_private_key_path("/tmp/not-used-by-fake-gateway.pem");
        systemConfig.setWx_api_v3_key("12345678901234567890123456789012");
        systemConfig.setWx_pay_public_key_id("PUB_KEY_ID_TEST");
        systemConfig.setWx_pay_public_key_path("/tmp/not-used-by-fake-gateway.pem");
        systemConfig.setWx_notify_url("https://example.com/payment/wechat/notify");
        systemConfig.setWx_h5_scene_info("{\"type\":\"Wap\",\"app_name\":\"Ypat\"}");
        return new TestWechatPayV3Config(systemConfig);
    }

    private static class TestWechatPayV3Config extends WechatPayV3Config {
        TestWechatPayV3Config(SystemConfig systemConfig) {
            super(systemConfig);
        }

        @Override
        public Config sdkConfig() {
            assertConfigured();
            return null;
        }

        @Override
        protected boolean shouldValidatePaymentKeyFiles() {
            return false;
        }
    }

    private static class FakeGateway implements WechatPayV3Client.Gateway {
        com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest lastMiniappRequest;
        com.wechat.pay.java.service.payments.h5.model.PrepayRequest lastH5Request;
        boolean failWithNoAuth;

        @Override
        public PrepayWithRequestPaymentResponse prepayMiniapp(Config config,
                com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest request) {
            lastMiniappRequest = request;
            if (failWithNoAuth) {
                throw new RuntimeException("Wrong HttpStatusCode[403]\n"
                        + "httpResponseBody[{\"code\":\"NO_AUTH\",\"message\":\"商户号该产品权限未开通，请前往商户平台>产品中心检查后重试\"}]"
                        + "\tHttpRequest[{\"headers\":{\"Authorization\":\"secret\",\"signature\":\"secret\"}}]");
            }
            PrepayWithRequestPaymentResponse response = new PrepayWithRequestPaymentResponse();
            response.setAppId("wx-test-appid");
            response.setTimeStamp("1783499261");
            response.setNonceStr("nonce");
            response.setPackageVal("prepay_id=wx-prepay");
            response.setSignType("RSA");
            response.setPaySign("pay-sign");
            return response;
        }

        @Override
        public PrepayResponse prepayH5(Config config,
                com.wechat.pay.java.service.payments.h5.model.PrepayRequest request) {
            lastH5Request = request;
            PrepayResponse response = new PrepayResponse();
            response.setH5Url("https://wx.tenpay.com/h5");
            return response;
        }

        @Override
        public Transaction parseNotify(NotificationParser parser, RequestParam requestParam) {
            return null;
        }
    }
}
