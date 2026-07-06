package com.ypat.storage;

import java.text.SimpleDateFormat;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Locale;

public class CosObjectKeyFactory {
    private final String envPrefix;

    public CosObjectKeyFactory(String envPrefix) {
        this.envPrefix = trimSlash(envPrefix);
    }

    public String createKey(StorageBizPath bizPath, String originalFilename, Date now, String uuid) {
        String datePath = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(now);
        return envPrefix + "/" + bizPath.path() + "/" + datePath + "/" + uuid + "." + extension(originalFilename);
    }

    public boolean supportsUrl(String publicBaseUrl, String url) {
        return extractObjectKey(publicBaseUrl, url) != null;
    }

    public String extractObjectKey(String publicBaseUrl, String url) {
        String base = trimTrailingSlash(publicBaseUrl);
        if (base == null || url == null) return null;
        String text = url.trim();
        if (!text.startsWith(base + "/")) return null;
        String key = stripQueryAndFragment(text.substring(base.length() + 1));
        if (!isSafeObjectPath(key)) return null;
        return key.startsWith(envPrefix + "/") ? key : null;
    }

    private String stripQueryAndFragment(String key) {
        int query = key.indexOf('?');
        int fragment = key.indexOf('#');
        int end = key.length();
        if (query >= 0) end = Math.min(end, query);
        if (fragment >= 0) end = Math.min(end, fragment);
        return key.substring(0, end);
    }

    private String extension(String originalFilename) {
        if (originalFilename == null) return "jpg";
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) return "jpg";
        String ext = originalFilename.substring(dot + 1).toLowerCase(Locale.ENGLISH);
        if ("jpeg".equals(ext) || "jpg".equals(ext) || "png".equals(ext) || "webp".equals(ext) || "gif".equals(ext) || "mp4".equals(ext) || "mov".equals(ext)) {
            return ext;
        }
        return "jpg";
    }

    private String trimSlash(String value) {
        if (value == null || value.trim().isEmpty()) return "dev";
        String text = value.trim();
        while (text.startsWith("/")) text = text.substring(1);
        while (text.endsWith("/")) text = text.substring(0, text.length() - 1);
        return text.isEmpty() ? "dev" : text;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String text = value.trim();
        while (text.endsWith("/")) text = text.substring(0, text.length() - 1);
        return text.isEmpty() ? null : text;
    }

    private boolean isSafeObjectPath(String key) {
        if (key == null || key.isEmpty() || key.indexOf('\\') >= 0) return false;
        String[] segments = key.split("/", -1);
        for (String segment : segments) {
            if (segment.isEmpty()) return false;
            String decoded;
            try {
                decoded = URLDecoder.decode(segment, "UTF-8");
            } catch (Exception e) {
                return false;
            }
            if (decoded.isEmpty()
                    || ".".equals(decoded)
                    || "..".equals(decoded)
                    || decoded.indexOf('/') >= 0
                    || decoded.indexOf('\\') >= 0) {
                return false;
            }
        }
        return true;
    }
}
