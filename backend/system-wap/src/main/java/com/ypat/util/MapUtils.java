package com.ypat.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;


/**
 * Map工具类
 */
public class MapUtils {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public final static <E> E get(Map map, Object key, E defaultValue) {
        Object o = map.get(key);
        if (o == null){
            return defaultValue;
        }
        return (E) o;
    }

    public static <T> List<T> map2Java(Class clazz, List<Map> mapList) {
        if (mapList == null || mapList.isEmpty()) {
            return null;
        }
        List<T> objectList = new ArrayList<T>();

        T object = null;
        for (Map<String, String> map : mapList) {
            if (map != null) {
                object = map2Java(clazz, map);
                objectList.add(object);
            }
        }
        return objectList;
    }

    public static <T> T map2Java(Class clazz, Map<String,String> map) {
        try {
            // 获取javaBean属性
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            // 创建 JavaBean 对象
            Object obj = clazz.newInstance();

            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            if (propertyDescriptors != null && propertyDescriptors.length > 0) {
                String propertyName = null;
                String propertyValue = null;
                String propertyType = null;
                for (PropertyDescriptor pd : propertyDescriptors) {
                    propertyName = pd.getName();
                    propertyType = pd.getPropertyType().getTypeName();
                    if (map.containsKey(propertyName)) {
                        propertyValue = map.get(propertyName);
                        if("java.lang.String".equals(propertyType)){
                            pd.getWriteMethod().invoke(obj, new Object[]{propertyValue});
                        }else if("java.lang.Integer".equals(propertyType)){
                            pd.getWriteMethod().invoke(obj, Integer.parseInt(propertyValue));
                        }else if("java.lang.Long".equals(propertyType)){
                            pd.getWriteMethod().invoke(obj, Long.parseLong(propertyValue));
                        }
                    }
                }
                return (T) obj;
            }
        } catch (Exception e) {
        }

        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map java2Map(Object javaBean) {
        Map map = new HashMap();

        try {
            // 获取javaBean属性
            BeanInfo beanInfo = Introspector.getBeanInfo(javaBean.getClass());

            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            if (propertyDescriptors != null && propertyDescriptors.length > 0) {
                String propertyName = null;
                Object propertyValue = null;
                for (PropertyDescriptor pd : propertyDescriptors) {
                    propertyName = pd.getName();
                    if (!"class".equals(propertyName)) {
                        Method readMethod = pd.getReadMethod();
                        propertyValue = readMethod.invoke(javaBean, new Object[0]);
                        map.put(propertyName, propertyValue);
                    }
                }
            }

        } catch (Exception e) {

        }
        return map;
    }

    public static Map<String,String> xml2Map(String xml) {
        Map<String, String> map = new HashMap<String, String>();
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(xml);
            Element rootElt = doc.getRootElement();
            List<Element> list = rootElt.elements();
            for (Element element : list) {  //遍历节点
                map.put(element.getName(), element.getText());
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static String mapKey2Str(Map<String,Object> map){
        Set<String> keySet = map.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyArray.length; i++) {
            sb.append(keyArray[i].trim());
            if(i != keyArray.length-1){
                sb.append(",");
            }
        }
        return sb.toString();
    }


    public static Map<String,Object> str2Map(String str) {
        String[] str1 = str.split(",");
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < str1.length; i++) {
            String[] str2 = str1[i].split(":");
            map.put(str2[0], str2[1]);
        }
        return map;
    }

    public static String getImageType(String key) {
        Map<String, String> map = new HashMap<>();
        map.put("data:image/jpg;base64",     "jpg");
        map.put("data:image/jpeg;base64",    "jpeg");
        map.put("data:image/png;base64",     "png");
        map.put("data:image/gif;base64",     "gif");
        map.put("data:image/x-icon;base64", "icon");
        map.put("data:image/bpm;base64",     "bpm");
        String val = map.get(key);
        return val;
    }
}
