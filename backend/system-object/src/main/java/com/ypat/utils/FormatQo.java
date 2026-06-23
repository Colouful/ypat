package com.ypat.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

public class FormatQo {
    public static String replaceNum(String str) {
        if(StringUtils.isEmpty(str)) {
            return "";
        }
        Pattern p1 = compile("[0-9A-Za-z]");
        Matcher m = p1.matcher(str);
        String result = m.replaceAll("*").trim();
        return result;
    }

    /**
     * 过滤手机号
     */
    public static String replacePhoneNumber(String text) {
        if (text == null){
            return "";
        }
        Pattern pattern = compile("(?<!\\d)(?:(?:1[35689]\\d{9})|(?:861[35689]\\d{9}))(?!\\d)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            text = text.replace(matcher.group(), matcher.group().substring(0, 3) + "********");
        }
        return text;
    }

    /**
     * 过滤qq号微信号
     */
    public static String replaceQqOrWxNumber(String text) {
        if (text == null){
            return "";
        }
        Pattern pattern = compile("(微信|QQ|qq|weixin|1[0-9]{10}|[a-zA-Z0-9\\-\\_]{6,16}|[0-9]\n" +
                "{6,11})+");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            text = text.replace(matcher.group(), "******");
        }
        return text;
    }

}
