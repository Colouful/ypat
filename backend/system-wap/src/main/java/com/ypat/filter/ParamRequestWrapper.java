package com.ypat.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class ParamRequestWrapper extends HttpServletRequestWrapper {
    private static Logger logger = LoggerFactory.getLogger(ParamRequestWrapper.class);
    public ParamRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String name) {
        String value = super.getParameter(name);
        if(value!=null && !"pics".equals(name) && !"cardfront".equals(name)){
            logger.info("输入：" + name + " = " + value);
            if(value.contains("undefined") || value.contains("null")) {
                return null;
            }
        }else if("pics".equals(name)){

        }
        return super.getParameterValues(name);
    }
}
