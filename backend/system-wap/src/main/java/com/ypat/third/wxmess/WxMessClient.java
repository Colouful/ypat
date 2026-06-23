package com.ypat.third.wxmess;

import com.google.gson.JsonObject;
import com.ypat.config.SystemConfig;
import com.ypat.enums.MessType;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.third.wxpay.sdk.WXPayConfigImpl;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Map;

@Component
public class WxMessClient {
    private static Logger logger = LoggerFactory.getLogger(WxMessClient.class);

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private SystemConfig systemConfig;

    public String getAccessToken() throws JSONException {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
                + "&appid=" + systemConfig.getWx_appid()
                + "&secret=" + systemConfig.getWx_appsecret();
        ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);
        logger.info("获取用户token：" + entity);
        if (entity.getStatusCode().equals(HttpStatus.OK)){
            JSONObject jsonObject = new JSONObject(entity.getBody());
            String access_token = (String)jsonObject.get("access_token");
            return access_token;
        }
        return null;
    }

    public String getAccessTokenPub() throws JSONException {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
                + "&appid=" + systemConfig.getWx_pub_appid()
                + "&secret=" + systemConfig.getWx_pub_appsecret();
        ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);
        logger.info("获取公众号token：" + entity);
        if (entity.getStatusCode().equals(HttpStatus.OK)){
            JSONObject jsonObject = new JSONObject(entity.getBody());
            String access_token = (String)jsonObject.get("access_token");
            return access_token;
        }
        return null;
    }

    public String sendMsg(String access_token, String touser, MessType messType, String page, Map<String,String> contentMap) {
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + access_token;
        WxMess wxMess = WxMess.build(touser, messType, page, contentMap);
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

    public byte[] qrCode(String access_token, String scene, String page) throws Exception{
        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="+access_token;
        JsonObject jsonParams = new JsonObject();
        if(StringUtils.isEmpty(scene)) {
            scene = "0";//0-小程序；1-百度小程序；2-PC小程序
        }
        jsonParams.addProperty("page",  page);
        jsonParams.addProperty("scene", scene);
        ResponseEntity<byte[]> entity = new RestTemplate().postForEntity(url,jsonParams.toString(), byte[].class);
        if (entity.getStatusCode().equals(HttpStatus.OK)){
            return entity.getBody();
        }
        return null;
    }

    public String pubQrCode(String access_token, Integer scene_id) throws Exception{
        String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token="+access_token;
        JsonObject jsonParams = new JsonObject();
        JsonObject action_info = new JsonObject();
        JsonObject scene = new JsonObject();
        scene.addProperty("scene_id", scene_id);
        action_info.add("scene", scene);

        jsonParams.addProperty("action_name", "QR_LIMIT_SCENE");
        jsonParams.add("action_info", action_info);
        ResponseEntity<String> entity = new RestTemplate().postForEntity(url,jsonParams.toString(), String.class);
        logger.info("获取公众号二维码返回值：" + entity);
        if (entity.getStatusCode().equals(HttpStatus.OK)){
            return entity.getBody();
        }
        return null;
    }
}
