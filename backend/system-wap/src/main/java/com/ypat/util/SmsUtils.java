package com.ypat.util;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.ypat.third.baidu.ai.GsonUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class SmsUtils {

    // 阿里云 AccessKey 只能通过环境变量注入，禁止写入仓库。
    public static final String accessKeyId = System.getenv("ALIYUN_ACCESS_KEY_ID"); // "your-access-key-id";
    public static final String accessKeySecret = System.getenv("ALIYUN_ACCESS_KEY_SECRET"); // "your-access-key-secret";
    public static final String SignName = "爱去拍";
    public static final String TemplateCode = "SMS_190271093";

    public static boolean sendLoginCode(String phoneNumbers, String code) {
        if (StringUtils.isEmpty(phoneNumbers) || StringUtils.isEmpty(code)) {
            return false;
        }
        if (StringUtils.isEmpty(accessKeyId) || StringUtils.isEmpty(accessKeySecret)) {
            return false;
        }
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        return sendSms(phoneNumbers, params);
    }

    public static void sendMsg(String PhoneNumbers, String profess) {
        if(StringUtils.isEmpty(PhoneNumbers)) {
            return;
        }
        Map<String,String> params = new HashMap<>();
        params.put("code",profess);

        sendSms(PhoneNumbers, params);
    }

    private static boolean sendSms(String phoneNumbers, Map<String, String> params) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("PhoneNumbers", phoneNumbers);
        request.putQueryParameter("SignName", SignName);
        request.putQueryParameter("TemplateCode", TemplateCode);
        request.putQueryParameter("TemplateParam", GsonUtils.toJson(params));


        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            return true;
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void queryMsg(String PhoneNumbers) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("QuerySendDetails");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumber", PhoneNumbers);
        request.putQueryParameter("SendDate", "20200516");
        request.putQueryParameter("PageSize", "1");
        request.putQueryParameter("CurrentPage", "10");
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        new SmsUtils().sendMsg("18500271832", "约摄影师");
//        {"Message":"OK","RequestId":"96BE29D2-0B43-4421-A16F-580C5D3FC6BD","BizId":"627923289636601374^0","Code":"OK"}
//        new SmsUtils().queryMsg("13269825756");
    }
}
