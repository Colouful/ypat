package com.ypat.handler;

import com.netflix.client.ClientException;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SysExceptionHandlerTest {

    @Test
    public void netflixClientExceptionIsReportedAsServerError() {
        SysExceptionHandler handler = new SysExceptionHandler();

        ResponseApiBody response = handler.exception(null,
                new ClientException("Load balancer does not have available server for client: SYSTEM-API"));

        assertEquals(ResponseCode.FAIL_SER.getCode(), response.getCode());
        assertEquals("内部服务暂不可用，请稍后重试", response.getMsg());
    }
}
