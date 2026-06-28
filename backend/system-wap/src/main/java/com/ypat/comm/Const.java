package com.ypat.comm;

public class Const {

    public static final long EXPIRATION_TIME = 31536000; //432_000;// 5天(以毫秒ms计) //1year
    public static final String SECRET = env("YPAT_WAP_JWT_SECRET", "development-only-jwt-key-change-me");      // JWT密码
    public static final String TOKEN_PREFIX  = "Tgbnhy";        // Token前缀
    public static final String HEADER_STRING = "Token";         // 存放Token的Header Key
    public static final String ACCESS_TOKEN_CARD  = "accessTokenCard";      // 百度accesstoken
    public static final String ACCESS_TOKEN_MATCH  = "accessTokenMatch";   // 百度accesstoken
    public static final String PARAM_REAL_NAME  = "paramRealName";   // 实名参数
    public static final long ACCESS_TOKEN_EXP_TIME  = 1296000;  // 百度accesstoken 15天 (以秒)
    public static final String PAGE_INDEX = "pages/home/home/index";
    public static final String PAGE_PUB_TG  = "pages/home/success/index?status=2";//发布成功
    public static final String PAGE_PUB_BTG = "pages/home/success/index?status=3";//发布失败
    public static final String PAGE_REALNAME_TG  = "pages/home/success/index?status=95";//实名成功
    public static final String PAGE_REALNAME_BTG = "pages/home/success/index?status=96";//实名失败
    public static final String PAGE_MESS = "pages/mine/message/index?type=rectimesCount&name=收到的约拍";
    public static final String SYS_ADMIN = "o5ZmB4kyCVPskEOaO0PK1He0Kl7w";

    public static final String TEMP_0 = "bcu2sYUoDPwIcB-1Jyx_HAoJVZtzgvuEyR8d0qf-dXE";//拍摄模板
    public static final String TEMP_1 = "_uTkXi5VM9BOAXOMMUdibQPioXMwNfngC90CJgynSTg";//实名认证审核
    public static final String TEMP_2 = "i6ydNmF4EKyAQomQuopOeCWHKmMytHDjP3W0anbEq4w";//发布信息审核
    public static final String TEMP_3 = "Bv1tvnuGZeKpwRxTz-QtOt_btN0tlkDjMgyeP-Iz16s";//新订单通知

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }
}
