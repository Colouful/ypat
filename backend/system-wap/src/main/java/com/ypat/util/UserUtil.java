package com.ypat.util;

import com.ypat.model.SecurityUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserUtil {
    public static String getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication!=null){
            Object principal = authentication.getPrincipal();
            if(principal!=null){
                if(principal instanceof SecurityUserDetails){
                    return ((SecurityUserDetails)principal).getUserId();
                }
            }
        }
        return null;
    }
}
