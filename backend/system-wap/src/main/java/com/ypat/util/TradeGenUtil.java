package com.ypat.util;

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
}
