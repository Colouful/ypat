package com.mock;

import com.google.gson.JsonObject;
import com.ypat.enums.MessType;
import com.ypat.enums.OrderType;
import com.ypat.third.baidu.ai.Base64Util;
import com.ypat.third.baidu.ai.FileUtil;
import com.ypat.third.baidu.ai.GsonUtils;
import com.ypat.third.wxmess.WxMess;
import com.ypat.third.wxpay.sdk.WXPayClient;
import com.ypat.third.wxpay.sdk.WXPayConfigImpl;
import com.ypat.third.wxpay.sdk.WXPayConstants;
import com.ypat.third.wxpay.sdk.WXPayUtil;
import com.ypat.util.ImageMarkUtil;
import com.ypat.util.JwtTokenUtil;
import com.ypat.util.TradeGenUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.Buffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MockTest {

    @Test
    public void oauth_ocr() throws Exception {
        String url = "http://localhost:8081/oauth/ocr";
        //MediaType type = MediaType.parseMediaType("multipart/form-data");
        HttpHeaders headers = new HttpHeaders();
        //headers.setContentType(type);
        headers.set("Token",getToken());
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<String, Object>();
        form.add("cardfront", getBase64());
        HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, headers);
        ResponseEntity<String> res = new RestTemplate().postForEntity(url, files, String.class);
        System.out.println(res);
    }

    @Test
    public void oauth_add() throws Exception{
        String url = "http://localhost:8081/oauth/add";
        String certcode = "45010319650122253X";
        String name = "蒋林林";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token",getToken());
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<String, Object>();
        form.add("pics", getBase64()+","+getBase64_back()+","+getBase64_hand());
        form.add("certcode", certcode);
        form.add("name", name);
        HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, headers);
        ResponseEntity<String> res = new RestTemplate().postForEntity(url, files, String.class, Map.class);
        System.out.println(res);
    }

    @Test
    public void upd_user() {
        // 上传文件
        String url = "http://localhost:8081/user/upd";
        String base64 = "data:image/jpeg;base64,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";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token",getToken());
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("wx", "wwwww1");
        requestBody.add("wb", "wwwww2");
        requestBody.add("pics", base64);
        HttpEntity<MultiValueMap> entity = new HttpEntity<MultiValueMap>(requestBody, headers);
        ResponseEntity<String> res = new RestTemplate().postForEntity(url,entity,String.class);
        System.out.println(res);
    }

    private String getBase64() throws Exception{
        String fileNameWithPath = "e://card/jianglin.png";
        File file = new File(fileNameWithPath);
        InputStream inputStream = new FileInputStream(file);
        byte[] imgData = FileUtil.readFileByBytes(inputStream);
        String imgStr = Base64Util.encode(imgData);
        return "data:image;base64,"+imgStr;
    }

    private String getBase64_back() throws Exception{
        String fileNameWithPath = "e://card/jianglin2.png";
        File file = new File(fileNameWithPath);
        InputStream inputStream = new FileInputStream(file);
        byte[] imgData = FileUtil.readFileByBytes(inputStream);
        String imgStr = Base64Util.encode(imgData);
        return "data:image;base64,"+imgStr;
    }

    private String getBase64_hand() throws Exception{
        String fileNameWithPath = "e://card/jianglin3.png";
        File file = new File(fileNameWithPath);
        InputStream inputStream = new FileInputStream(file);
        byte[] imgData = FileUtil.readFileByBytes(inputStream);
        String imgStr = Base64Util.encode(imgData);
        return "data:image;base64,"+imgStr;
    }

    private String getToken() {
        return "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI5OSIsImNyZWF0ZWQiOjE1NzU1MjQ5Nzg4ODIsImV4cCI6MTYwNzA2MDk3OH0.Nso6uCRnvFqah8RmcpEwjIx33zCcE9cu4J84QRzFr8QCauxSC42QrPh37L3NjrVngbbdXZg9bGXy0tLncIVYyA";
    }

//    @Test
    public String getAccessToken() throws Exception{
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
                + "&appid=wx94b432e0db7c29be" //+ WXConfig.appID
                + "&secret=ca31c3ffd70aa4ea286066deffa93517" ;//+ WXConfig.appSecret;
        ResponseEntity<String> entity = new RestTemplate().getForEntity(url, String.class);
        if (entity.getStatusCode().equals(HttpStatus.OK)){
            JSONObject jsonObject = new JSONObject(entity.getBody());
            String access_token = (String)jsonObject.get("access_token");
            System.out.println("获取用户token：" + access_token);
            return access_token;
        }
        return null;
    }

    public String getAccessTokenPub() throws Exception{
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
                + "&appid=wx80daa0a43275379f" //+ WXConfig.appID
                + "&secret=3afab63ce1a9c98294df423fa6b976bc" ;//+ WXConfig.appSecret;
        ResponseEntity<String> entity = new RestTemplate().getForEntity(url, String.class);
        if (entity.getStatusCode().equals(HttpStatus.OK)){
            System.out.println(entity.getBody());
            JSONObject jsonObject = new JSONObject(entity.getBody());
            String access_token = (String)jsonObject.get("access_token");
            System.out.println("获取用户token：" + access_token);
            return access_token;
        }
        return null;
    }

    @Test
    public void sendMsg() {
        String access_token="29_fHZNfy9-279LGy1p8HuKXpdxftL41XIBk0PGZMJWsmwuJQ2zHRtuh47RY7_DsItfAH5xvZ-H-6uABC-xnY2MCn_p0qMYXEjqGg_u6e17DvV3vi9tJhv2QD45Y79U5BPkcI7HRS0-VJdYdTD1BLCeAEARBS";
        MessType type = MessType.send;
        Map<String,String> contentMap = new HashMap<>();
        contentMap.put("area", "北京");
        contentMap.put("time", DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
        contentMap.put("note", "丁 向您发起了约拍");
        String touser = "o5ZmB4kQQTKpaM2zMdQMNKONve-k";
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + access_token;
        WxMess wxMess = WxMess.build(touser, type, null, contentMap);
        String json = GsonUtils.toJson(wxMess);
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(mediaType);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> formEntity = new HttpEntity<String>(json, headers);
        ResponseEntity<String> entity = new RestTemplate().postForEntity(url, formEntity, String.class);
        System.out.println("获取发送消息返回值：" + entity);
        if (entity.getStatusCode().equals(HttpStatus.OK)){
            System.out.println(entity.getBody());
        }
    }

    @Test
    public void unifiedOrder() throws Exception{
        WXPayClient client = new WXPayClient();
        //OrderQo orderQo = new OrderQo();
        //client.unifiedOrder(orderQo);
        //client.getSandboxSignKey();
        //client.code2Session("011ImFv21miuMQ1kYBw21z4wv21ImFvx");

        /**
         * 签名
         Map<String, String> retMap = new HashMap<>();
         retMap.put("appId", WXPayConfigImpl.appID);
         retMap.put("timeStamp", 1576308638+"");
         retMap.put("nonceStr", "cZQTP1bpw1jZeenia2rTVGdXwISA33pw");
         retMap.put("package", "prepay_id=wx141530387649432e0428d6a51332861300");
         retMap.put("signType",WXPayConstants.HMACSHA256);
         String sign = WXPayUtil.generateSignature(retMap, WXPayConfigImpl.key, WXPayConstants.SignType.HMACSHA256);
         retMap.put("paySign", sign);
         */

        /**
         * 订单
         */
        Map<String, String> data = new HashMap<String, String>();
        data.put("body", OrderType.getNameByCode("2"));
        data.put("out_trade_no", TradeGenUtil.genTradeSerialNo());
        data.put("total_fee", String.valueOf(1));//test
        data.put("spbill_create_ip", "112.126.103.244");//终端IP
        data.put("notify_url", "http://112.126.103.244:8081/wxpay/notify");//不能带参数
        data.put("trade_type", "JSAPI");  //NATIVE为扫码支付 JSAPI为小程序
        data.put("openid", "o5ZmB4kQQTKpaM2zMdQMNKONve-k");
        Map<String, String> resp = client.unifiedOrder(data);

        Map<String, String> retMap = new HashMap<>();
        retMap.put("appId", "WXPayConfigImpl.appID");
        retMap.put("timeStamp", System.currentTimeMillis()/1000+"");
        retMap.put("nonceStr", WXPayUtil.generateNonceStr());
        retMap.put("package", "prepay_id=wx150025542144110a9d76144a1025290900");
        retMap.put("signType", WXPayConstants.HMACSHA256);
        retMap.put("paySign", WXPayUtil.generateSignature(retMap, "WXPayConfigImpl.key", WXPayConstants.SignType.HMACSHA256));
    }


    @Test
    public void imageMark() throws Exception{
        File file = new File("d://temp//abc.jpg");
        InputStream fileInputStream = new FileInputStream(file);
        new ImageMarkUtil().waterMake(fileInputStream);
    }

    /**
     * 小程序二维码
     * @throws Exception
     */
    @Test
    public void createQRCode() throws Exception{
        String access_token = getAccessToken();
        String url = "https://api.weixin.qq.com/wxa/getwxacode?access_token="+access_token;
        JsonObject jsonParams = new JsonObject();
        int i = 2;//0-小程序；1-百度小程序；2-PC小程序
        jsonParams.addProperty("path", "pages/home/home/index?channel="+i);
        ResponseEntity<byte[]> entity = new RestTemplate().postForEntity(url,jsonParams.toString(), byte[].class);
        if (entity.getStatusCode().equals(HttpStatus.OK)){
            System.out.println(entity);
            File file = new File("d://temp//qr"+i+".png");
            FileUtils.writeByteArrayToFile(file, entity.getBody());
        }
    }

    /**
     * 小程序二维码-无限制
     * @throws Exception
     */
    @Test
    public void getUnlimited() throws Exception{
        String access_token = getAccessToken();
        String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="+access_token;
        JsonObject jsonParams = new JsonObject();
        int i = 2;//0-小程序；1-百度小程序；2-PC小程序
        jsonParams.addProperty("path", "pages/home/home/index?channel="+i);
        jsonParams.addProperty("scene", "s123");
        ResponseEntity<byte[]> entity = new RestTemplate().postForEntity(url,jsonParams.toString(), byte[].class);
        if (entity.getStatusCode().equals(HttpStatus.OK)){
            System.out.println(entity);
            File file = new File("d://temp//un"+i+".png");
            FileUtils.writeByteArrayToFile(file, entity.getBody());
        }
    }

    /**
     * 公众号二维码-永久
     */
    @Test
    public void createPubQRCode() throws Exception{
        String access_token = getAccessTokenPub();
        String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token="+access_token;
        JsonObject jsonParams = new JsonObject();
        JsonObject action_info = new JsonObject();
        JsonObject scene = new JsonObject();
        scene.addProperty("scene_id", 1);//最小值1
        action_info.add("scene", scene);

        jsonParams.addProperty("action_name", "QR_LIMIT_SCENE");
        jsonParams.add("action_info", action_info);
        ResponseEntity<String> entity = new RestTemplate().postForEntity(url,jsonParams.toString(), String.class);
        if (entity.getStatusCode().equals(HttpStatus.OK)){
            System.out.println(entity);
        }
    }

    @Test
    public void createToken() {
        String instantToken = JwtTokenUtil.getInstantToken();
        System.out.println(instantToken);
        Boolean aBoolean = new JwtTokenUtil().validateToken(instantToken);
        System.out.println(aBoolean);
    }

}
