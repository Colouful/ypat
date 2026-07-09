package com.ypat.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ypat.MessagePushLogQo;
import com.ypat.ResponseApiBody;
import com.ypat.service.MessServiceClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/message-push-log")
public class AdminMessagePushLogController {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    @Autowired
    private MessServiceClient messServiceClient;

    @GetMapping("/list")
    public ResponseApiBody list(
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "success", required = false) String success,
            @RequestParam(value = "messageId", required = false) Long messageId,
            @RequestParam(value = "ypatid", required = false) Long ypatid,
            @RequestParam(value = "sendperid", required = false) Long sendperid,
            @RequestParam(value = "recperid", required = false) Long recperid,
            @RequestParam(value = "touserOpenid", required = false) String touserOpenid,
            @RequestParam(value = "dateStart", required = false) String dateStart,
            @RequestParam(value = "dateEnd", required = false) String dateEnd,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        MessagePushLogQo qo = buildQuery(eventType, businessType, success, messageId, ypatid,
                sendperid, recperid, touserOpenid, dateStart, dateEnd, page, size);
        String json = messServiceClient.findPushLogPage(qo);
        JsonElement pageData = JsonParser.parseString(json);
        return ResponseApiBody.success(pageData);
    }

    @GetMapping("/stats")
    public ResponseApiBody stats(
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "success", required = false) String success,
            @RequestParam(value = "messageId", required = false) Long messageId,
            @RequestParam(value = "ypatid", required = false) Long ypatid,
            @RequestParam(value = "sendperid", required = false) Long sendperid,
            @RequestParam(value = "recperid", required = false) Long recperid,
            @RequestParam(value = "touserOpenid", required = false) String touserOpenid,
            @RequestParam(value = "dateStart", required = false) String dateStart,
            @RequestParam(value = "dateEnd", required = false) String dateEnd) {

        MessagePushLogQo qo = buildQuery(eventType, businessType, success, messageId, ypatid,
                sendperid, recperid, touserOpenid, dateStart, dateEnd, DEFAULT_PAGE, DEFAULT_SIZE);
        return ResponseApiBody.success(messServiceClient.pushLogStats(qo));
    }

    private MessagePushLogQo buildQuery(String eventType, String businessType, String success,
                                        Long messageId, Long ypatid, Long sendperid, Long recperid,
                                        String touserOpenid, String dateStart, String dateEnd,
                                        Integer page, Integer size) {
        MessagePushLogQo qo = new MessagePushLogQo();
        qo.setPage(normalizePage(page));
        qo.setSize(normalizeSize(size));
        if (StringUtils.isNotBlank(eventType)) {
            qo.setEventType(eventType);
        }
        if (StringUtils.isNotBlank(businessType)) {
            qo.setBusinessType(businessType);
        }
        if (StringUtils.isNotBlank(success)) {
            qo.setSuccess(success);
        }
        qo.setMessageId(messageId);
        qo.setYpatid(ypatid);
        qo.setSendperid(sendperid);
        qo.setRecperid(recperid);
        if (StringUtils.isNotBlank(touserOpenid)) {
            qo.setTouserOpenid(touserOpenid);
        }
        if (StringUtils.isNotBlank(dateStart)) {
            qo.setDateStart(dateStart);
        }
        if (StringUtils.isNotBlank(dateEnd)) {
            qo.setDateEnd(dateEnd);
        }
        return qo;
    }

    private int normalizePage(Integer page) {
        return page == null || page < 0 ? DEFAULT_PAGE : page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
