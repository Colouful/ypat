/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.ypat.third.baidu.ai;

import com.ypat.comm.Const;
import com.ypat.config.SystemConfig;
import com.ypat.util.RedisClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 身份证匹配
 */
@Component
public class Idmatch {

    protected Log log = LogFactory.getLog(getClass());
    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    private RedisClient redisClient;

    public IdmatchResponse idmatch(String id_card_number, String name) {
        // 身份证识别url
        String url = systemConfig.getBd_api_idmatch();
        String ak = systemConfig.getBd_ak_idmatch();
        String sk = systemConfig.getBd_sk_idmatch();
        try {

            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("id_card_number", id_card_number);
            String param = GsonUtils.toJson(map);

            String accessToken = (String)redisClient.get(Const.ACCESS_TOKEN_MATCH);
            if(StringUtils.isEmpty(accessToken)){
                accessToken = AuthService.getAuth(ak, sk);
                redisClient.put(Const.ACCESS_TOKEN_MATCH, accessToken, Const.ACCESS_TOKEN_EXP_TIME);
            }
            String result = HttpUtil.post(url, accessToken, "application/json", param);
            log.info("百度匹配结果:"+result);
            return responseJson(result);
        } catch (Exception e) {
            log.error("百度匹配结果异常", e);
        }
        return null;
    }

    private IdmatchResponse responseJson(String json){
        if(StringUtils.isEmpty(json)) {
            return null;
        }
        Map map = GsonUtils.fromJson(json, Map.class);
        Integer error_code = (Integer) map.get("error_code");
        IdmatchResponse result = new IdmatchResponse();
        result.setError_code(error_code);
        return result;
    }
}