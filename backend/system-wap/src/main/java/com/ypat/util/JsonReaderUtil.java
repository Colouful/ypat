package com.ypat.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ypat.third.baidu.ai.GsonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public class JsonReaderUtil {
    protected static Log log = LogFactory.getLog(JsonReaderUtil.class);

    public static String readJsonData(String fileName) {
        // 读取文件数据
        StringBuffer strbuffer = new StringBuffer();
        try {

            InputStream inputStream = JsonReaderUtil.class.getClassLoader().getResourceAsStream("conf/"+fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader in  = new BufferedReader(inputStreamReader);
            String str;
            while ((str = in.readLine()) != null) {
                strbuffer.append(str);
            }
            in.close();
        } catch (IOException e) {
            log.error("读取文件异常", e);
        }
        return strbuffer.toString();
    }

    public static void main(String[] args) {
        String provinceData = JsonReaderUtil.readJsonData("province.json");
        String cityData = JsonReaderUtil.readJsonData("city.json");
        String areaData = JsonReaderUtil.readJsonData("area.json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("provinceData", GsonUtils.fromJson(provinceData, JsonArray.class));
        jsonObject.add("cityData", GsonUtils.fromJson(cityData, JsonArray.class));
        jsonObject.add("areaData", GsonUtils.fromJson(areaData, JsonArray.class));
        System.out.println( GsonUtils.toJson(jsonObject));
    }
}
