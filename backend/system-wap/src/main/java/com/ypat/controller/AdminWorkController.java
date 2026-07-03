package com.ypat.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.WorkListQo;
import com.ypat.service.WorkServiceClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 作品治理 Controller。
 */
@RestController
@RequestMapping("/admin/work")
public class AdminWorkController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    @Autowired
    private WorkServiceClient workServiceClient;

    @GetMapping("/list")
    public ResponseApiBody list(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "mediaType", required = false) String mediaType,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "tagIds", required = false) String tagIds,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "profession", required = false) String profession,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        WorkListQo qo = new WorkListQo();
        qo.setPage(normalizePage(page));
        qo.setSize(normalizeSize(size));
        if (StringUtils.isNotBlank(status)) qo.setStatus(status);
        if (StringUtils.isNotBlank(city)) qo.setCity(city);
        if (StringUtils.isNotBlank(mediaType)) qo.setMediaType(mediaType);
        if (StringUtils.isNotBlank(nickname)) qo.setNickname(nickname);
        if (StringUtils.isNotBlank(mobile)) qo.setMobile(mobile);
        if (StringUtils.isNotBlank(tagIds)) qo.setTagIds(tagIds);
        if (StringUtils.isNotBlank(category)) qo.setCategory(category);
        if (StringUtils.isNotBlank(gender)) qo.setGender(gender);
        if (StringUtils.isNotBlank(profession)) qo.setProfession(profession);

        String json = workServiceClient.adminList(qo);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @GetMapping("/detail")
    public ResponseApiBody detail(@RequestParam("id") Long id) {
        validateId(id);
        String json = workServiceClient.adminDetail(id);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @PostMapping("/audit")
    public ResponseApiBody audit(
            @RequestParam("id") Long id,
            @RequestParam("flag") String flag,
            @RequestParam(value = "reason", required = false) String reason) {

        validateId(id);
        if (StringUtils.isBlank(flag)) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }

        String res = workServiceClient.adminAudit(id, flag, reason);
        return ResponseApiBody.success(parseResponseRes(res));
    }

    @PostMapping("/offline")
    public ResponseApiBody offline(
            @RequestParam("id") Long id,
            @RequestParam(value = "reason", required = false) String reason) {

        validateId(id);
        String res = workServiceClient.adminOffline(id, reason);
        return ResponseApiBody.success(parseResponseRes(res));
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
        JsonElement element = JsonParser.parseString(json);
        if (element != null && element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("code")) {
                int code = object.get("code").getAsInt();
                if (code != ResponseCode.SUCCESS.getCode()) {
                    String msg = object.has("msg") && !object.get("msg").isJsonNull()
                            ? object.get("msg").getAsString()
                            : ResponseCode.FAIL_SER.getMsg();
                    throw new SysException(code, msg);
                }
            }
            if (object.has("res")) {
                return object.get("res");
            }
        }
        return element;
    }
}
