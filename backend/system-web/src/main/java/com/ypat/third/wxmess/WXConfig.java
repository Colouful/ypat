package com.ypat.third.wxmess;

public class WXConfig {
    public static final String appID = env("YPAT_WEB_WX_APP_ID");
    public static final String appSecret = env("YPAT_WEB_WX_APP_SECRET");
    public static final String mchID = env("YPAT_WEB_WX_MCH_ID");
    public static final String key = env("YPAT_WEB_WX_PAY_KEY");

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value;
    }
}
