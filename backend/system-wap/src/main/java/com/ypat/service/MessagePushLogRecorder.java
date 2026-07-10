package com.ypat.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ypat.MessagePushLogQo;
import com.ypat.comm.Const;
import com.ypat.enums.MessType;
import com.ypat.enums.MessagePushEventType;
import com.ypat.enums.YesNo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MessagePushLogRecorder {
    private static final Logger logger = LoggerFactory.getLogger(MessagePushLogRecorder.class);

    @Autowired
    private MessServiceClient messServiceClient;

    public void recordWechat(MessType messType, Long ypatid, Long sendperid, Long recperid,
                             String touserOpenid, String pageUrl, String responseBody, Exception error) {
        try {
            MessagePushLogQo qo = new MessagePushLogQo();
            qo.setEventType(MessagePushEventType.WECHAT_SUBSCRIBE_SENT.value);
            qo.setBusinessType(messType == null ? null : messType.value);
            qo.setYpatid(ypatid);
            qo.setSendperid(sendperid);
            qo.setRecperid(recperid);
            qo.setTouserOpenid(touserOpenid);
            qo.setTemplateId(templateId(messType));
            qo.setPageUrl(pageUrl);
            qo.setResponseBody(responseBody);

            WechatResult result = parseWechatResult(responseBody);
            qo.setSuccess(error == null && result.success ? YesNo.yes.value : YesNo.no.value);
            qo.setWechatErrcode(result.errcode);
            qo.setWechatErrmsg(error == null ? result.errmsg : error.getMessage());
            qo.setRemark("微信订阅消息发送");
            messServiceClient.recordPushLog(qo);
        } catch (Exception e) {
            logger.error("微信订阅推送日志记录失败：", e);
        }
    }

    private String templateId(MessType messType) {
        if (messType == null) {
            return null;
        }
        switch (messType) {
            case send:
                return Const.TEMP_0;
            case oauth:
                return Const.TEMP_1;
            case audit:
                return Const.TEMP_2;
            case order:
                return Const.TEMP_3;
            default:
                return null;
        }
    }

    private WechatResult parseWechatResult(String responseBody) {
        WechatResult result = new WechatResult();
        if (StringUtils.isEmpty(responseBody)) {
            result.success = false;
            result.errmsg = "empty response";
            return result;
        }
        try {
            JsonElement element = JsonParser.parseString(responseBody);
            if (element != null && element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("errcode") && !object.get("errcode").isJsonNull()) {
                    result.errcode = object.get("errcode").getAsString();
                }
                if (object.has("errmsg") && !object.get("errmsg").isJsonNull()) {
                    result.errmsg = object.get("errmsg").getAsString();
                }
                result.success = "0".equals(result.errcode);
            }
        } catch (Exception e) {
            result.success = false;
            result.errmsg = "parse response failed";
        }
        return result;
    }

    private static class WechatResult {
        private boolean success;
        private String errcode;
        private String errmsg;
    }
}
