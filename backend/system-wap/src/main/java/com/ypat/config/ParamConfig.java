package com.ypat.config;

import com.ypat.comm.Const;
import com.ypat.util.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ParamConfig {

    @Autowired
    private RedisClient redisClient;

    private String real_name_flag;

    public String getRealName() {
        if(StringUtils.isEmpty(real_name_flag)) {
            String realName = (String)redisClient.get(Const.PARAM_REAL_NAME);
            if(StringUtils.isEmpty(realName)){
                real_name_flag = "0";
                redisClient.putNoExpire(Const.PARAM_REAL_NAME, real_name_flag);
            }else{
                real_name_flag = realName;
            }
        }
        return real_name_flag;
    }

    public void setRealName(String flag) {
        real_name_flag = flag;
        redisClient.putNoExpire(Const.PARAM_REAL_NAME, real_name_flag);
    }

}
