package com.ypat.interceptor;

import com.ypat.comm.Const;
import com.ypat.handler.SysExceptionHandler;
import com.ypat.model.SecurityUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        SecurityUserDetails user = (SecurityUserDetails) request.getSession().getAttribute(Const.USER_SESSION_KEY);
        String getRequestURL =request.getRequestURL().toString();
        logger.debug("getRequestURL====="+getRequestURL+"=====user====="+user);
        if (user == null){
            response.sendRedirect(request.getContextPath()+"/manage/");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
