package com.ypat.third.wxmess;

import com.ypat.enums.MessType;
import com.ypat.util.GsonUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class WxMessClient {
    private static Logger logger = LoggerFactory.getLogger(WxMessClient.class);

    @Autowired
    private RestTemplate restTemplate;

    public String getAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
                + "&appid=" + WXConfig.appID
                + "&secret=" + WXConfig.appSecret;
        ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);
        logger.info("获取用户token：" + entity);
        if (entity.getStatusCode().equals(HttpStatus.OK)){
            JSONObject jsonObject = new JSONObject(entity.getBody());
            String access_token = (String)jsonObject.get("access_token");
            return access_token;
        }
        return null;
    }

    public String sendMsg(String access_token, String touser, MessType type, String page, Map<String,String> contentMap) {
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + access_token;
        WxMess wxMess = WxMess.build(touser, type, page, contentMap);
        String json = GsonUtils.toJson(wxMess);
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(mediaType);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> formEntity = new HttpEntity<String>(json, headers);
        ResponseEntity<String> entity = restTemplate.postForEntity(url, formEntity, String.class);
        logger.info("获取发送消息返回值：" + entity);
        if (entity.getStatusCode().equals(HttpStatus.OK)){
            return entity.getBody();
        }
        return null;
    }
}
