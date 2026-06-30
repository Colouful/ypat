package com.ypat.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class InviteCodeCodecTest {

    @Test
    public void encode_returnsPrefixedBase36() {
        String code = InviteCodeCodec.encode(123L);
        assertTrue("code should start with IV: " + code, code.startsWith("IV"));
        assertEquals("IV3F", code);
    }

    @Test
    public void encodeDecode_roundTrip() {
        for (long id : new long[]{1L, 42L, 1000L, 99999L, Long.MAX_VALUE}) {
            String code = InviteCodeCodec.encode(id);
            assertEquals(Long.valueOf(id), InviteCodeCodec.decode(code));
        }
    }

    @Test
    public void encode_returnsNullForNullOrNonPositive() {
        assertNull(InviteCodeCodec.encode(null));
        assertNull(InviteCodeCodec.encode(0L));
        assertNull(InviteCodeCodec.encode(-1L));
    }

    @Test
    public void decode_returnsNullForGarbage() {
        assertNull(InviteCodeCodec.decode(null));
        assertNull(InviteCodeCodec.decode(""));
        assertNull(InviteCodeCodec.decode("IV"));            // 只有前缀
        assertNull(InviteCodeCodec.decode("XX42"));          // 前缀错
        assertNull(InviteCodeCodec.decode("IV!!"));          // 非法 base36
    }

    @Test
    public void decode_handlesLowercaseAndWhitespace() {
        assertEquals(Long.valueOf(123L), InviteCodeCodec.decode("iv3f"));
        assertEquals(Long.valueOf(123L), InviteCodeCodec.decode("  IV3F  "));
    }
}
