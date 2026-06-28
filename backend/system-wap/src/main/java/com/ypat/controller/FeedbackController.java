package com.ypat.controller;

import com.ypat.FeedbackQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.FeedbackServiceClient;
import com.ypat.util.RedisClient;
import com.ypat.util.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeedbackController {
    private static final int MIN_CONTENT_LENGTH = 10;
    private static final int MAX_CONTENT_LENGTH = 500;
    private static final int MAX_CONTACT_LENGTH = 100;
    private static final long RATE_LIMIT_SECONDS = 60L;

    @Autowired
    private FeedbackServiceClient feedbackServiceClient;
    @Autowired
    private RedisClient redisClient;

    @PostMapping("/feedback/add")
    public String add(String content, String contact) {
        String userId = UserUtil.getUserId();
        if (StringUtils.isBlank(userId)) {
            throw new SysException(ResponseCode.FAIL_AUTH);
        }
        String normalizedContent = sanitize(StringUtils.trimToEmpty(content));
        String normalizedContact = sanitize(StringUtils.trimToEmpty(contact));
        if (normalizedContent.length() < MIN_CONTENT_LENGTH || normalizedContent.length() > MAX_CONTENT_LENGTH) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (normalizedContact.length() > MAX_CONTACT_LENGTH) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }

        String rateKey = "feedback:add:" + userId;
        if (redisClient.get(rateKey) != null) {
            throw new SysException(1006, "提交过于频繁，请稍后再试");
        }
        redisClient.put(rateKey, "1", RATE_LIMIT_SECONDS);

        FeedbackQo feedbackQo = new FeedbackQo();
        feedbackQo.setUserid(Long.parseLong(userId));
        feedbackQo.setContent(normalizedContent);
        feedbackQo.setContact(normalizedContact);
        return feedbackServiceClient.add(feedbackQo);
    }

    private String sanitize(String value) {
        return value.replace("<", "＜").replace(">", "＞");
    }
}

