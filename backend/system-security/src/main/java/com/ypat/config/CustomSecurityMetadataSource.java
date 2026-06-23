package com.ypat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.ArrayList;
import java.util.Collection;

public class CustomSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    private static final Logger logger = LoggerFactory.getLogger(CustomSecurityMetadataSource.class);
    public static final String MERCHANT_CENTER_ROLES_ALL = "MERCHANT_CENTER_ROLES_ALL_";
    private PathMatcher pathMatcher = new AntPathMatcher();
    private CacheComponent cacheComponent;

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    public CustomSecurityMetadataSource(/**RoleFuture roleFuture*/Object roleFuture, CacheComponent cacheComponent) {
        super();
        //this.roleFuture = roleFuture;
        this.cacheComponent = cacheComponent;
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object)
            throws IllegalArgumentException {
        String url = ((FilterInvocation) object).getRequestUrl();
        Collection<ConfigAttribute> roles = new ArrayList<>();//有权限的角色列表
        return roles;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
