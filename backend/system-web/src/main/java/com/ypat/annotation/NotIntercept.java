package com.ypat.annotation;

import java.lang.annotation.*;

/**
 * @Auther dingyinxin
 * @Date 2021/12/6 16:52
 * @Version 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotIntercept {
}
