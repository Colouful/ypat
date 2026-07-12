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

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 身份证识别
 */
@Component
public class Idcard {

    protected Log log = LogFactory.getLog(getClass());
    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    private RedisClient redisClient;
    /**
     * 重要提示代码中所需工具类
     * FileUtil,Base64Util,HttpUtil,GsonUtils请从
     * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
     * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
     * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
     * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3
     */
    public IdcardResponse idcard(InputStream inputStream, String id_card_side) {
        // 身份证识别url
        String url = systemConfig.getBd_api_idcard();
        String ak = systemConfig.getBd_ak_idcard();
        String sk = systemConfig.getBd_sk_idcard();
        try {
            byte[] imgData = FileUtil.readFileByBytes(inputStream);
            String imgStr = Base64Util.encode(imgData);
            // 识别身份证正面id_card_side=front;
            // 识别身份证背面id_card_side=back;
            String params = "id_card_side="+id_card_side+"&" + URLEncoder.encode("image", "UTF-8") + "="
                    + URLEncoder.encode(imgStr, "UTF-8");
            /**
             * "#####调用鉴权接口获取的token#####";
             */
            String accessToken = (String)redisClient.get(Const.ACCESS_TOKEN_CARD);
            if(StringUtils.isEmpty(accessToken)){
                accessToken = AuthService.getAuth(ak, sk);
                redisClient.put(Const.ACCESS_TOKEN_CARD, accessToken, Const.ACCESS_TOKEN_EXP_TIME);
            }
            String result = HttpUtil.post(url, accessToken, params);
            log.info("百度识别结果:"+result);
            return responseJson(result);
        } catch (Exception e) {
            log.error("百度识别接口异常", e);
        }
        return null;
    }

    public IdcardResponse idcard(String imgBase64, String id_card_side) {
        // 身份证识别url
        String url = systemConfig.getBd_api_idcard();
        String ak = systemConfig.getBd_ak_idcard();
        String sk = systemConfig.getBd_sk_idcard();
        try {
            String params = "id_card_side="+id_card_side+"&" + URLEncoder.encode("image", "UTF-8") + "="
                    + URLEncoder.encode(imgBase64, "UTF-8");

            String accessToken = (String)redisClient.get(Const.ACCESS_TOKEN_CARD);
            if(StringUtils.isEmpty(accessToken)){
                accessToken = AuthService.getAuth(ak, sk);
                redisClient.put(Const.ACCESS_TOKEN_CARD, accessToken, Const.ACCESS_TOKEN_EXP_TIME);
            }
            String result = HttpUtil.post(url, accessToken, params);
            log.info("百度识别结果:"+result);
            return responseJson(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private IdcardResponse responseJson(String json){
        if(StringUtils.isEmpty(json)) {
            return null;
        }
        Map map = GsonUtils.fromJson(json, Map.class);
        IdcardResponse result = new IdcardResponse();
        Number errorCode = (Number) map.get("error_code");
        if (errorCode != null) {
            result.setError_code(errorCode.intValue());
            result.setError_msg((String) map.get("error_msg"));
            return result;
        }
        String image_status = (String) map.get("image_status");
        Map words_result = (Map) map.get("words_result");
        Map cert = (Map) words_result.get("公民身份号码");
        Map name = (Map) words_result.get("姓名");
        String cert_words = (String) cert.get("words");
        String name_words = (String) name.get("words");
        result.setImage_status(image_status);
        result.setCert_words(cert_words);
        result.setName_words(name_words);
        return result;
    }
}
