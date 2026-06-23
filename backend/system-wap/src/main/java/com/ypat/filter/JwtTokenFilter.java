package com.ypat.filter;

import com.ypat.comm.Const;
import com.ypat.model.SecurityUserDetails;
import com.ypat.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
/**
 * @Auther dingyinxin
 * @Date 2021/12/6 16:52
 * @Version 1.0
 */
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal ( HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String authHeader = request.getHeader( Const.HEADER_STRING );
        String getRequestURL =request.getRequestURL().toString();
        logger.info("请求URL："+getRequestURL);
        if (authHeader != null ) {
            final String authToken = authHeader;
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtTokenUtil.validateToken(authToken)) {
                    String userId = jwtTokenUtil.getUserFromToken(authToken);
                    logger.debug("token正确："+userId);
                    SecurityUserDetails userDetails = new SecurityUserDetails();
                    userDetails.setUserId(userId);
                    UsernamePasswordAuthenticationToken authentication  = new UsernamePasswordAuthenticationToken( userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }else{
                    logger.error("token校验错误：");
                }
            }else{
                logger.error("token错误：");
            }
        }
        ParamRequestWrapper paramRequestWrapper = new ParamRequestWrapper(request);
        chain.doFilter(paramRequestWrapper, response);
    }
}

