package com.ypat.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author dingyinxin
 */
public class CopyUtil {

    private static Logger logger = LoggerFactory.getLogger(CopyUtil.class);

    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet<String>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static void copyIgnoreNull(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    /**
     * 复制对象
     *
     * @param src   原对象
     * @param clazz 对象类
     * @param <T>
     * @return 新的对象
     */
    public static <T> T copy(Object src, Class<T> clazz) {
        if (src == null) {
            return null;
        }
        T t = null;
        try {
            t = clazz.newInstance();
            BeanUtils.copyProperties(src, t);
            return t;
        } catch (Exception e) {
            logger.error("复制文件出错", e);
        }
        return t;
    }

    /**
     * 复制对象list集合
     *
     * @param srclist 原对象集合
     * @param clazz   对象类
     * @param <T>
     * @return 新的对象
     */
    public static <T> List<T> copyList(Object srclist, Class<T> clazz) {
        if (srclist == null) {
            return null;
        }
        List<T> destlist = new ArrayList<T>();
        List<Object> srcLists = (List<Object>) srclist;
        for (Object src : srcLists) {
            destlist.add(copy(src, clazz));
        }
        return destlist;
    }

}
