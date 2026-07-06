package com.ypat.storage;

import com.ypat.SysException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Base64ImagePayloadTest {
    private static final String ONE_PIXEL_PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=";

    @Test
    public void keepsDetectedPngMetadataForBase64Images() {
        Base64ImagePayload payload = Base64ImagePayload.fromBytes(Base64.decodeBase64(ONE_PIXEL_PNG));

        assertEquals("png", payload.getFilename());
        assertEquals("image/png", payload.getContentType());
    }

    @Test(expected = SysException.class)
    public void rejectsNonImageBase64Payloads() {
        Base64ImagePayload.fromBytes(Base64.decodeBase64("YXZhdGFy"));
    }
}
