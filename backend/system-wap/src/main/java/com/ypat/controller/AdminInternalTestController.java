package com.ypat.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ypat.InternalTestGenerateQo;
import com.ypat.InternalTestResourceQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.InternalTestServiceClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 新版管理后台 - 内测数据工厂 Controller。
 */
@RestController
@RequestMapping("/admin/internal-test")
public class AdminInternalTestController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
    private static final String BAD_RESPONSE_MSG = "服务响应格式错误";

    @Autowired
    private InternalTestServiceClient internalTestServiceClient;

    @GetMapping("/resources")
    public ResponseApiBody resources(InternalTestResourceQo qo) {
        if (qo == null) {
            qo = new InternalTestResourceQo();
        }
        qo.setPage(normalizePage(qo.getPage()));
        qo.setSize(normalizeSize(qo.getSize()));

        String json = internalTestServiceClient.resources(
                qo.getMediaType(),
                qo.getUsageType(),
                qo.getStyleCode(),
                qo.getProfession(),
                qo.getProvince(),
                qo.getCity(),
                qo.getArea(),
                qo.getStatus(),
                qo.getUsedFlag(),
                qo.getGroupNo(),
                qo.getKeyword(),
                qo.getPage(),
                qo.getSize());
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @PostMapping("/resources")
    public ResponseApiBody saveResource(@RequestBody InternalTestResourceQo qo) {
        String json = internalTestServiceClient.saveResource(qo);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @PostMapping("/resources/batch")
    public ResponseApiBody batchResources(@RequestBody InternalTestResourceQo qo) {
        String json = internalTestServiceClient.batchResources(qo);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @GetMapping("/resource-groups")
    public ResponseApiBody resourceGroups(InternalTestResourceQo qo) {
        if (qo == null) {
            qo = new InternalTestResourceQo();
        }
        qo.setPage(normalizePage(qo.getPage()));
        qo.setSize(normalizeSize(qo.getSize()));

        String json = internalTestServiceClient.resourceGroups(
                qo.getMediaType(),
                qo.getUsageType(),
                qo.getStyleCode(),
                qo.getProfession(),
                qo.getProvince(),
                qo.getCity(),
                qo.getArea(),
                qo.getStatus(),
                qo.getUsedFlag(),
                qo.getGroupNo(),
                qo.getKeyword(),
                qo.getPage(),
                qo.getSize());
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @PostMapping("/resources/update")
    public ResponseApiBody updateResource(@RequestBody InternalTestResourceQo qo) {
        String json = internalTestServiceClient.updateResource(qo);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @PostMapping("/resources/status")
    public ResponseApiBody updateResourceStatus(@RequestParam("id") Long id,
                                                @RequestParam("status") String status) {
        String json = internalTestServiceClient.updateResourceStatus(id, status);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @GetMapping("/users")
    public ResponseApiBody users(InternalTestGenerateQo qo) {
        if (qo == null) {
            qo = new InternalTestGenerateQo();
        }
        String json = internalTestServiceClient.users(
                qo.getBatchNo(),
                qo.getCity(),
                qo.getArea(),
                qo.getProfess(),
                qo.getGender());
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @PostMapping("/users/create")
    public ResponseApiBody createUsers(@RequestBody InternalTestGenerateQo qo) {
        String json = internalTestServiceClient.createUsers(qo);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @PostMapping("/generate")
    public ResponseApiBody generate(@RequestBody InternalTestGenerateQo qo) {
        String json = internalTestServiceClient.generate(qo);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @GetMapping("/batches")
    public ResponseApiBody batches(InternalTestGenerateQo qo) {
        String batchNo = qo == null ? null : qo.getBatchNo();
        String json = internalTestServiceClient.batches(batchNo);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    @PostMapping("/cleanup")
    public ResponseApiBody cleanup(@RequestBody InternalTestGenerateQo qo) {
        String json = internalTestServiceClient.cleanup(qo);
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

    private JsonElement parseResponseRes(String json) {
        if (StringUtils.isBlank(json)) {
            throw new SysException(ResponseCode.FAIL_SER, BAD_RESPONSE_MSG);
        }

        JsonElement element;
        try {
            element = JsonParser.parseString(json);
        } catch (RuntimeException e) {
            throw new SysException(ResponseCode.FAIL_SER, BAD_RESPONSE_MSG);
        }
        if (element == null || !element.isJsonObject()) {
            throw new SysException(ResponseCode.FAIL_SER, BAD_RESPONSE_MSG);
        }

        JsonObject object = element.getAsJsonObject();
        JsonElement codeElement = object.get("code");
        if (codeElement == null
                || codeElement.isJsonNull()
                || !codeElement.isJsonPrimitive()
                || !codeElement.getAsJsonPrimitive().isNumber()) {
            throw new SysException(ResponseCode.FAIL_SER, BAD_RESPONSE_MSG);
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
        if (resElement == null || resElement.isJsonNull() || !resElement.isJsonObject()) {
            throw new SysException(ResponseCode.FAIL_SER, BAD_RESPONSE_MSG);
        }
        return resElement;
    }
}
