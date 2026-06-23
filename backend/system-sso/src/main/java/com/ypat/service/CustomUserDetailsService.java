package com.ypat.service;

import com.google.gson.Gson;
import com.ypat.UserQo;
import com.ypat.entity.User;
import com.ypat.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User json = userService.get(1L);
        UserQo userQo = new UserQo();
        userQo.setName(userName);
        userQo.setPassword(new BCryptPasswordEncoder().encode("123456"));
        userQo.setId(123L);
        return new SecurityUser(userQo);
    }
}
