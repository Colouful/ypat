package com.ypat.util;

import com.ypat.enums.YpatPatstyle;

import java.text.SimpleDateFormat;
import java.util.Random;

public class TradeGenUtil {
    public static synchronized String  genTradeSerialNo(){
        SimpleDateFormat smt=new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String dateStr = smt.format(System.currentTimeMillis());
        Random r=new Random();
        String randomStr =String.format("%06d",  r.nextInt(999999));
        return dateStr+randomStr;
    }

    public static String genNum(int len) {
        Random r = new Random();
        StringBuilder rs = new StringBuilder();
        for (int i = 0; i < len; i++) {
            rs.append(r.nextInt(10));
        }
        return rs.toString();
    }

    public static String gen9Num(){
        Random r=new Random();
        String randomStr =String.format("%10d",  r.nextInt(1000000000));
        return randomStr;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(genNum(11));
        }
    }

}
