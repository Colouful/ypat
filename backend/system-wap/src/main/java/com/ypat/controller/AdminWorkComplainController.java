package com.ypat.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.WorkComplainQo;
import com.ypat.service.WorkComplainAdminServiceClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/work/complain")
public class AdminWorkComplainController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    @Autowired
    private WorkComplainAdminServiceClient workComplainAdminServiceClient;

    @GetMapping("/list")
    public ResponseApiBody list(@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
                                @RequestParam(value = "status", required = false) String status,
                                @RequestParam(value = "workId", required = false) Long workId,
                                @RequestParam(value = "userId", required = false) Long userId) {
        if (workId != null && workId <= 0) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (userId != null && userId <= 0) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        String json = workComplainAdminServiceClient.list(
                normalizePage(page),
                normalizeSize(size),
                status,
                workId,
                userId);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @GetMapping("/detail")
    public ResponseApiBody detail(@RequestParam("id") Long id) {
        validateId(id);
        String json = workComplainAdminServiceClient.detail(id);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @PostMapping("/handle")
    public ResponseApiBody handle(@RequestParam("id") Long id,
                                  @RequestParam("status") String status,
                                  @RequestParam(value = "reason", required = false) String reason,
                                  @RequestParam(value = "offlineWork", required = false) Boolean offlineWork) {
        validateId(id);
        if (StringUtils.isBlank(status)) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }

        WorkComplainQo qo = new WorkComplainQo();
        qo.setId(id);
        qo.setStatus(status);
        qo.setReason(reason);
        qo.setOfflineWork(offlineWork);

        String json = workComplainAdminServiceClient.handle(qo);
        return ResponseApiBody.success(parseResponseRes(json));
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
