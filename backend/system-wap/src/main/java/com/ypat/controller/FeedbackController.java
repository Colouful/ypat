package com.ypat.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ypat.FeedbackQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.FeedbackServiceClient;
import com.ypat.storage.StorageBizPath;
import com.ypat.storage.StorageService;
import com.ypat.storage.StoredObject;
import com.ypat.util.RedisClient;
import com.ypat.util.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FeedbackController {
    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);
    private static final String TYPE_FUNCTION = "function";
    private static final String TYPE_EXPERIENCE = "experience";
    private static final String TYPE_ACCOUNT = "account";
    private static final String TYPE_PAYMENT = "payment";
    private static final String TYPE_CONTENT = "content";
    private static final String TYPE_OTHER = "other";
    private static final int MIN_CONTENT_LENGTH = 10;
    private static final int MAX_CONTENT_LENGTH = 500;
    private static final int MAX_CONTACT_LENGTH = 100;
    private static final int MAX_PICS_LENGTH = 1000;
    private static final int MAX_PIC_COUNT = 3;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
    private static final long RATE_LIMIT_SECONDS = 60L;

    @Autowired
    private FeedbackServiceClient feedbackServiceClient;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private StorageService storageService;

    @PostMapping("/feedback/add")
    public String add(String type, String content, String contact, String pics) {
        String userId = UserUtil.getUserId();
        if (StringUtils.isBlank(userId)) {
            throw new SysException(ResponseCode.FAIL_AUTH);
        }
        String normalizedType = normalizeType(type);
        String normalizedContent = sanitize(StringUtils.trimToEmpty(content));
        String normalizedContact = sanitize(StringUtils.trimToEmpty(contact));
        String normalizedPics = sanitize(StringUtils.trimToEmpty(pics));
        if (normalizedContent.length() < MIN_CONTENT_LENGTH || normalizedContent.length() > MAX_CONTENT_LENGTH) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (normalizedContact.length() > MAX_CONTACT_LENGTH) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (normalizedPics.length() > MAX_PICS_LENGTH || countPics(normalizedPics) > MAX_PIC_COUNT) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }

        String rateKey = "feedback:add:" + userId;
        applyRateLimit(rateKey, userId);

        FeedbackQo feedbackQo = new FeedbackQo();
        feedbackQo.setUserid(Long.parseLong(userId));
        feedbackQo.setType(normalizedType);
        feedbackQo.setContent(normalizedContent);
        feedbackQo.setContact(normalizedContact);
        feedbackQo.setPics(normalizedPics);
        return feedbackServiceClient.add(feedbackQo);
    }

    @PostMapping("/feedback/upload/image")
    public ResponseApiBody uploadImage(@RequestPart("file") MultipartFile file) throws IOException {
        String userId = UserUtil.getUserId();
        if (StringUtils.isBlank(userId)) {
            throw new SysException(ResponseCode.FAIL_AUTH);
        }
        if (file == null || file.isEmpty()) {
            throw new SysException(ResponseCode.FAIL_PARA, "请选择要上传的文件");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new SysException(ResponseCode.FAIL_PARA, "仅允许上传图片文件");
        }
        StoredObject storedObject = storageService.upload(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                StorageBizPath.FEEDBACK);
        Map<String, Object> res = new HashMap<>();
        res.put("url", storedObject.getUrl());
        return ResponseApiBody.success(res);
    }

    @GetMapping("/admin/feedback/list")
    public ResponseApiBody adminList(@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                     @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
                                     @RequestParam(value = "status", required = false) String status,
                                     @RequestParam(value = "type", required = false) String type,
                                     @RequestParam(value = "userId", required = false) Long userId) {
        if (userId != null && userId <= 0) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        String json = feedbackServiceClient.adminList(normalizePage(page), normalizeSize(size), status, type, userId);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @GetMapping("/admin/feedback/detail")
    public ResponseApiBody adminDetail(@RequestParam("id") Long id) {
        validateId(id);
        String json = feedbackServiceClient.adminDetail(id);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @PostMapping("/admin/feedback/handle")
    public ResponseApiBody adminHandle(@RequestParam("id") Long id,
                                       @RequestParam("status") String status,
                                       @RequestParam(value = "reason", required = false) String reason,
                                       @RequestParam(value = "handleReason", required = false) String handleReason) {
        validateId(id);
        if (StringUtils.isBlank(status)) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        FeedbackQo feedbackQo = new FeedbackQo();
        feedbackQo.setId(id);
        feedbackQo.setStatus(status);
        feedbackQo.setHandleReason(StringUtils.isBlank(handleReason) ? reason : handleReason);

        String json = feedbackServiceClient.adminHandle(feedbackQo);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    private void applyRateLimit(String rateKey, String userId) {
        try {
            if (redisClient.get(rateKey) != null) {
                throw new SysException(1006, "提交过于频繁，请稍后再试");
            }
            redisClient.put(rateKey, "1", RATE_LIMIT_SECONDS);
        } catch (SysException e) {
            throw e;
        } catch (RuntimeException e) {
            logger.warn("反馈限频 Redis 不可用，userid={}, error={}", userId, e.toString());
        }
    }

    private String sanitize(String value) {
        return value.replace("<", "＜").replace(">", "＞");
    }

    private String normalizeType(String type) {
        if (StringUtils.isBlank(type)) {
            return TYPE_OTHER;
        }
        String normalized = StringUtils.trim(type);
        if (TYPE_FUNCTION.equals(normalized)
                || TYPE_EXPERIENCE.equals(normalized)
                || TYPE_ACCOUNT.equals(normalized)
                || TYPE_PAYMENT.equals(normalized)
                || TYPE_CONTENT.equals(normalized)
                || TYPE_OTHER.equals(normalized)) {
            return normalized;
        }
        throw new SysException(ResponseCode.FAIL_PARA);
    }

    private int countPics(String pics) {
        if (StringUtils.isBlank(pics)) {
            return 0;
        }
        int count = 0;
        String[] parts = pics.split(",");
        for (String part : parts) {
            if (StringUtils.isNotBlank(part)) {
                count++;
            }
        }
        return count;
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

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
    }

    private JsonElement parseResponseRes(String json) {
        if (StringUtils.isBlank(json)) {
            throw new SysException(ResponseCode.FAIL_SER, "服务响应格式错误");
        }
        JsonElement element;
        try {
            element = JsonParser.parseString(json);
        } catch (RuntimeException e) {
            throw new SysException(ResponseCode.FAIL_SER, "服务响应格式错误");
        }
        if (element == null || !element.isJsonObject()) {
            throw new SysException(ResponseCode.FAIL_SER, "服务响应格式错误");
        }

        JsonObject object = element.getAsJsonObject();
        if (object.has("code")) {
            JsonElement codeElement = object.get("code");
            if (codeElement == null
                    || codeElement.isJsonNull()
                    || !codeElement.isJsonPrimitive()
                    || !codeElement.getAsJsonPrimitive().isNumber()) {
                throw new SysException(ResponseCode.FAIL_SER, "服务响应格式错误");
            }
            int code = codeElement.getAsInt();
            if (code != ResponseCode.SUCCESS.getCode()) {
                JsonElement msgElement = object.get("msg");
                String msg = msgElement != null
                        && !msgElement.isJsonNull()
                        && msgElement.isJsonPrimitive()
                        && msgElement.getAsJsonPrimitive().isString()
                        ? msgElement.getAsString()
                        : ResponseCode.FAIL_SER.getMsg();
                throw new SysException(code, msg);
            }
            JsonElement resElement = object.get("res");
            if (resElement == null || resElement.isJsonNull()) {
                throw new SysException(ResponseCode.FAIL_SER, "服务响应格式错误");
            }
            return resElement;
        }
        return object;
    }
}
