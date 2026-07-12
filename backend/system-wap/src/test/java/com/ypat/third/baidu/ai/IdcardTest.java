package com.ypat.third.baidu.ai;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IdcardTest {

    @Test
    public void parsesBaiduTopLevelErrorResponse() throws Exception {
        Idcard idcard = new Idcard();
        Method responseJson = Idcard.class.getDeclaredMethod("responseJson", String.class);
        responseJson.setAccessible(true);

        IdcardResponse response = (IdcardResponse) responseJson.invoke(
                idcard,
                "{\"error_code\":18,\"error_msg\":\"Open api qps request limit reached\"}"
        );

        assertEquals(18, response.getError_code());
        assertEquals("Open api qps request limit reached", response.getError_msg());
        assertNull(response.getImage_status());
    }
}
