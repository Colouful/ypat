package com.ypat.controller;

import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.third.wxpay.sdk.WXPayClient;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;

public class LoginControllerWxCodeTest {

    @Test
    public void codeThrowsBusinessErrorWhenWechatRejectsJsCode() {
        LoginController controller = new LoginController();
        ReflectionTestUtils.setField(controller, "wxPayClient", new FakeWXPayClient(
                "{\"errcode\":40029,\"errmsg\":\"invalid code\"}"
        ));

        ResponseApiBody response = controller.code("expired-code");

        assertEquals(ResponseCode.FAIL_WX.getCode(), response.getCode());
        assertEquals("微信登录失败：invalid code", response.getMsg());
    }

    @Test
    public void codeReturnsWechatSessionWhenWechatAcceptsJsCode() {
        LoginController controller = new LoginController();
        String wxResponse = "{\"openid\":\"openid-1\",\"session_key\":\"session-1\"}";
        ReflectionTestUtils.setField(controller, "wxPayClient", new FakeWXPayClient(wxResponse));

        ResponseApiBody response = controller.code("valid-code");

        assertEquals(ResponseCode.SUCCESS.getCode(), response.getCode());
        assertEquals(wxResponse, response.getRes());
    }

    private static class FakeWXPayClient extends WXPayClient {
        private final String response;

        FakeWXPayClient(String response) {
            this.response = response;
        }

        @Override
        public String code2Session(String code) {
            return response;
        }
    }
}
