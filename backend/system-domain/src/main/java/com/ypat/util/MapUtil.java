package com.ypat.util;

import java.util.*;

public class MapUtil {
    public static void sort(Map<String,String> data){
        List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(data.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1,
                               Map.Entry<String, String> o2) {
                return o2.getKey().compareTo(o1.getKey());
            }
        });
    }
}
