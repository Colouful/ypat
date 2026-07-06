package com.ypat.storage;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.util.MimeDetector;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Base64ImagePayload {
    private final byte[] bytes;
    private final String filename;
    private final String contentType;

    private Base64ImagePayload(byte[] bytes, String filename, String contentType) {
        this.bytes = bytes;
        this.filename = filename;
        this.contentType = contentType;
    }

    public static Base64ImagePayload fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new SysException(ResponseCode.FAIL_UPLOAD);
        }
        try {
            String mime = MimeDetector.detect(new ByteArrayInputStream(bytes));
            if (!MimeDetector.isImage(mime)) {
                throw new SysException(ResponseCode.FAIL_FILE_TYPE, "图片格式不支持（仅 JPEG/PNG/WebP）");
            }
            return new Base64ImagePayload(bytes, extensionFor(mime), mime);
        } catch (IOException e) {
            throw new SysException(ResponseCode.FAIL_UPLOAD);
        }
    }

    private static String extensionFor(String mime) {
        if ("image/png".equalsIgnoreCase(mime)) return "png";
        if ("image/webp".equalsIgnoreCase(mime)) return "webp";
        return "jpg";
    }

    public ByteArrayInputStream inputStream() {
        return new ByteArrayInputStream(bytes);
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }
}
