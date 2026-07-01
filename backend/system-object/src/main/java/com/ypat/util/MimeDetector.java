package com.ypat.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * MIME 检测工具（基于 magic number，不依赖扩展名）
 * 读取文件前 16 字节做格式判断
 */
public final class MimeDetector {

    private MimeDetector() {}

    public static final Set<String> ALLOWED_IMAGE_MIME = new HashSet<>(Arrays.asList(
        "image/jpeg", "image/png", "image/webp"
    ));

    public static final Set<String> ALLOWED_VIDEO_MIME = new HashSet<>(Arrays.asList(
        "video/mp4", "video/quicktime"
    ));

    /** 最大图片总大小 100MB */
    public static final long MAX_IMAGE_TOTAL_SIZE = 100L * 1024L * 1024L;
    /** 单视频最大 200MB */
    public static final long MAX_VIDEO_SIZE = 200L * 1024L * 1024L;
    /** 单图片最大 100MB（与总大小一致） */
    public static final long MAX_IMAGE_SIZE = 100L * 1024L * 1024L;
    /** 图片最多 9 张 */
    public static final int MAX_IMAGE_COUNT = 9;
    /** 视频最多 1 个 */
    public static final int MAX_VIDEO_COUNT = 1;

    /**
     * 检测文件真实 MIME
     * 注意：会消费 InputStream 的前 16 字节
     * 调用前请确保 stream 支持 mark/reset，或在调用方自己处理
     */
    public static String detect(InputStream is) throws IOException {
        if (is == null) return "application/octet-stream";
        if (!is.markSupported()) {
            throw new IOException("InputStream must support mark/reset");
        }
        is.mark(16);
        byte[] header = new byte[16];
        int read = 0;
        while (read < 16) {
            int n = is.read(header, read, 16 - read);
            if (n < 0) break;
            read += n;
        }
        is.reset();
        if (read < 4) return "application/octet-stream";

        // JPEG: FF D8 FF
        if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (read >= 8
            && (header[0] & 0xFF) == 0x89
            && header[1] == 'P' && header[2] == 'N' && header[3] == 'G'
            && header[4] == 0x0D && header[5] == 0x0A && header[6] == 0x1A && header[7] == 0x0A) {
            return "image/png";
        }
        // WebP: RIFF .... WEBP
        if (read >= 12
            && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
            && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "image/webp";
        }
        // MP4: 00 00 00 ?? 66 74 79 70 (ftyp)
        if (read >= 12
            && header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p') {
            return "video/mp4";
        }
        // MOV/QuickTime: 同 MP4（ftyp 标识） → 视作 video/quicktime
        if (read >= 12 && header[4] == 'q' && header[5] == 't') {
            return "video/quicktime";
        }
        return "application/octet-stream";
    }

    public static boolean isImage(String mime) {
        return mime != null && ALLOWED_IMAGE_MIME.contains(mime.toLowerCase());
    }

    public static boolean isVideo(String mime) {
        return mime != null && ALLOWED_VIDEO_MIME.contains(mime.toLowerCase());
    }

    /**
     * 校验文件名（拒绝路径穿越）
     * 拒绝包含以下字符: / \ : * ? " < > |
     * 不允许 . 开头
     */
    public static boolean isValidFileName(String name) {
        if (name == null || name.isEmpty()) return false;
        if (name.startsWith(".")) return false;
        if (name.contains("..")) return false;
        for (char c : name.toCharArray()) {
            if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?'
                || c == '"' || c == '<' || c == '>' || c == '|') {
                return false;
            }
        }
        return true;
    }

    /**
     * 校验图片扩展名
     */
    public static boolean isImageExt(String filename) {
        if (filename == null) return false;
        String low = filename.toLowerCase();
        return low.endsWith(".jpg") || low.endsWith(".jpeg") || low.endsWith(".png") || low.endsWith(".webp");
    }

    public static boolean isVideoExt(String filename) {
        if (filename == null) return false;
        String low = filename.toLowerCase();
        return low.endsWith(".mp4") || low.endsWith(".mov");
    }
}
