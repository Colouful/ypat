package com.ypat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SysExceptionTest {

    @Test
    public void customMessageIsExposedToCrossServiceErrorResponse() {
        SysException exception = new SysException(ResponseCode.FAIL_PARA, "启用资源不足：ypat");

        assertEquals("{code=1002, msg='启用资源不足：ypat'}", exception.getMessage());
        assertEquals("启用资源不足：ypat", exception.getMsg());
    }
}
