package com.ypat.comm;

import java.text.DateFormat;
import java.util.Date;
/**
 * @Auther dingyinxin
 * @Date 2021/12/6 16:52
 * @Version 1.0
 */
public class Const {

    public static final long EXPIRATION_TIME = 432_000;         // 5天(以毫秒ms计)
    public static final String SECRET = "WNFSIDHFIOWEF#$%&*9984334SecretSOFOJWNFOWIJFSLSIJDF";      // JWT密码
    public static final String TOKEN_PREFIX  = "Tgbnhy";        // Token前缀
    public static final String HEADER_STRING = "Token";         // wap存放Token的Header Key
    public static final String USER_SESSION_KEY="USER";         // web存放用户session
    public static final String PAGE_INDEX = "pages/home/home/index";
    public static final String PAGE_PUB_TG  = "pages/home/success/index?status=2";//发布成功
    public static final String PAGE_PUB_BTG = "pages/home/success/index?status=3";//发布失败
    public static final String PAGE_REALNAME_TG  = "pages/home/success/index?status=95";//实名成功
    public static final String PAGE_REALNAME_BTG = "pages/home/success/index?status=96";//实名失败
    public static final String PAGE_MESS = "pages/mine/message/index?type=rectimesCount&name=收到的约拍";

    public static final String TEMP_0 = "bcu2sYUoDPwIcB-1Jyx_HAoJVZtzgvuEyR8d0qf-dXE";//拍摄模板
    public static final String TEMP_1 = "_uTkXi5VM9BOAXOMMUdibQPioXMwNfngC90CJgynSTg";//实名认证审核
    public static final String TEMP_2 = "i6ydNmF4EKyAQomQuopOeCWHKmMytHDjP3W0anbEq4w";//发布信息审核
    public static final String TEMP_3 = "Bv1tvnuGZeKpwRxTz-QtOt_btN0tlkDjMgyeP-Iz16s";//新订单通知

    public static final String SYSFLAG="sysflag";
    public static final String SYSFLAG_AUDIT="1";
    public static final String SYSFLAG_QUERY="2";
    public static final String SYSFLAG_ORDER="3";

    public static void main(String[] args) {
        System.out.println(Const.EXPIRATION_TIME * 1000);
        System.out.println(DateFormat.getInstance().format(new Date(System.currentTimeMillis())));
        System.out.println(DateFormat.getInstance().format(new Date(System.currentTimeMillis() + Const.EXPIRATION_TIME)));
        System.out.println(DateFormat.getInstance().format(new Date(System.currentTimeMillis() + Const.EXPIRATION_TIME * 1000)));
    }
}
