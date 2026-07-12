package com.ypat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SysExceptionTest {
    @Test
    public void numericCodeConstructorBuildsEscapedJsonMessage() {
        SysException exception = new SysException(1002, "参数\"错误\\请重试\n");

        assertEquals("{\"code\":1002,\"msg\":\"参数\\\"错误\\\\请重试\\n\"}", exception.getMessage());
        assertEquals("参数\"错误\\请重试\n", exception.getMsg());
    }

    @Test
    public void responseCodeConstructorBuildsJsonMessage() {
        SysException exception = new SysException(ResponseCode.FAIL_PARA);

        assertEquals("{\"code\":1002,\"msg\":\"参数错误\"}", exception.getMessage());
        assertEquals("参数错误", exception.getMsg());
    }

    @Test
    public void customMessageIsPreservedForCrossServiceErrors() {
        SysException exception = new SysException(ResponseCode.FAIL_PARA, "约拍资源已被占用，请重新选择");

        assertEquals("{\"code\":1002,\"msg\":\"约拍资源已被占用，请重新选择\"}", exception.getMessage());
        assertEquals("约拍资源已被占用，请重新选择", exception.getMsg());
    }

    @Test
    public void customMessageEscapesJsonSpecialCharactersAndControls() {
        String msg = "引号\"反斜杠\\退格\b换页\f换行\n回车\r制表\t控制\037";

        SysException exception = new SysException(ResponseCode.FAIL_PARA, msg);

        assertEquals("{\"code\":1002,\"msg\":\"引号\\\"反斜杠\\\\退格\\b换页\\f换行\\n回车\\r制表\\t控制\\u001F\"}", exception.getMessage());
        assertEquals(msg, exception.getMsg());
    }

    @Test
    public void nullCustomMessageRemainsJsonNull() {
        SysException exception = new SysException(ResponseCode.FAIL_PARA, null);

        assertEquals("{\"code\":1002,\"msg\":null}", exception.getMessage());
        assertNull(exception.getMsg());
    }
}
