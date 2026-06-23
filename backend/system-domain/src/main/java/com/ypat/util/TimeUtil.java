package com.ypat.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class TimeUtil {

    public static String getTimeStr(Long timeMillis) {
        String timeStr = "";
        if(timeMillis/(1000*60)<60){
            timeStr = timeMillis/(1000*60)+"分钟前";
        }else if(timeMillis/(1000*60*60)<24){
            timeStr = timeMillis/(1000*60*60)+"小时前";
        }else {
            timeStr = timeMillis/(1000*60*60*24)+"天前";
        }
        return timeStr;
    }

    /**
     * 获得某天最大时间23:59:59
     * @param date
     * @return
     */
    public static Date getEndDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());;
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        return Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获得某天最小时间00:00:00
     * @param date
     * @return
     */
    public static Date getStartDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }
}
