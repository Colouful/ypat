package com.ypat.controller;

import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.YpatInfoQo;
import com.ypat.comm.ImageConst;
import com.ypat.service.UserServiceClient;
import com.ypat.service.YpatServiceClient;
import com.ypat.storage.StorageBizPath;
import com.ypat.storage.StorageService;
import com.ypat.storage.StoredObject;
import com.ypat.third.wxmess.WxMessClient;
import com.ypat.third.baidu.ai.GsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ypat.util.ImageMarkUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端 - 约拍申请/作品 Controller。
 *
 * <p>对应旧后台：审核系统-申请列表、发布作品。</p>
 */
@RestController
@RequestMapping("/admin/ypat")
public class AdminYpatController {

    private static final Logger logger = LoggerFactory.getLogger(AdminYpatController.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    @Autowired
    private YpatServiceClient ypatServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ImageMarkUtil imageMarkUtil;

    @Autowired(required = false)
    private WxMessClient wxMessClient;

    /**
     * 申请列表分页查询。
     *
     * <p>对应旧后台：GET /manage/list</p>
     */
    @GetMapping("/list")
    public ResponseApiBody list(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "recomflag", required = false) String recomflag,
            @RequestParam(value = "target", required = false) String target,
            @RequestParam(value = "patstyle", required = false) String patstyle,
            @RequestParam(value = "chargeway", required = false) String chargeway,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "workId", required = false) String workId,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        YpatInfoQo qo = new YpatInfoQo();
        qo.setPage(normalizePage(page));
        qo.setSize(normalizeSize(size));
        if (StringUtils.isNotBlank(status)) {
            qo.setStatus(status);
        }
        if (StringUtils.isNotBlank(nickname)) {
            qo.setNickname(nickname);
        }
        if (StringUtils.isNotBlank(mobile)) {
            qo.setMobile(mobile);
        }
        if (StringUtils.isNotBlank(recomflag)) {
            qo.setRecomflag(recomflag);
        }
        if (StringUtils.isNotBlank(target)) {
            qo.setTarget(target);
        }
        if (StringUtils.isNotBlank(patstyle)) {
            qo.setPatstyle(patstyle);
        }
        if (StringUtils.isNotBlank(chargeway)) {
            qo.setChargeway(chargeway);
        }
        if (StringUtils.isNotBlank(city)) {
            qo.setCity(city);
        }
        if (StringUtils.isNotBlank(workId)) {
            qo.setWorkId(workId);
        }

        String json = ypatServiceClient.findPage(qo);
        return ResponseApiBody.success(parseResponseRes(json));
    }

    /**
     * 申请详情。
     *
     * <p>对应旧后台：GET /manage/detail</p>
     */
    @GetMapping("/detail")
    public ResponseApiBody detail(@RequestParam("id") Long id) {
        if (id == null) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        String json = ypatServiceClient.get(id, null);
        JsonElement data = JsonParser.parseString(json);
        return ResponseApiBody.success(data);
    }

    /**
     * 审核。
     *
     * <p>对应旧后台：POST /manage/audit</p>
     */
    @PostMapping("/audit")
    public ResponseApiBody audit(
            @RequestParam("id") Long id,
            @RequestParam("flag") String flag,
            @RequestParam(value = "reason", required = false) String reason) {

        if (id == null || StringUtils.isBlank(flag)) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }

        logger.info("管理端约拍审核：id={}, flag={}", id, flag);
        String res = ypatServiceClient.audit(id, flag, null, reason);
        JsonElement resData = JsonParser.parseString(res);

        // 微信消息推送（兼容旧逻辑，失败不影响主流程）
        pushAuditMessage(id, flag, reason);

        Map<String, Object> result = new HashMap<>(4);
        result.put("success", true);
        result.put("data", resData);
        return ResponseApiBody.success(result);
    }

    /**
     * 上推荐 / 取消推荐。
     *
     * <p>对应旧后台：POST /manage/upRecom</p>
     */
    @PostMapping("/recom")
    public ResponseApiBody recom(
            @RequestParam("id") Long id,
            @RequestParam("recomflag") String recomflag) {

        if (id == null || StringUtils.isBlank(recomflag)) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }

        logger.info("管理端约拍推荐：id={}, recomflag={}", id, recomflag);
        String res = ypatServiceClient.upRecom(id, recomflag);
        JsonElement resData = JsonParser.parseString(res);
        return ResponseApiBody.success(resData);
    }

    /**
     * 发布作品（后台代提交）。
     *
     * <p>对应旧后台：POST /ypat/submit</p>
     */
    @PostMapping("/submit")
    public ResponseApiBody submit(
            YpatInfoQo ypatInfoQo,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "files", required = false) MultipartFile[] files) throws IOException {

        if (ypatInfoQo.getPatdate() == null) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "patdate不能为空");
        }

        // 1. 创建临时用户（头像）
        UserQo userQo = new UserQo();
        if (file != null && !file.isEmpty()) {
            StoredObject storedObject = storageService.upload(file.getInputStream(), ImageConst.IMAGE_TYPE, file.getContentType(), StorageBizPath.AVATAR);
            if (storedObject == null || storedObject.getUrl() == null) {
                throw new SysException(ResponseCode.FAIL_UPLOAD);
            }
            userQo.setImgpath(storedObject.getUrl());
        }
        userQo.setNickname(ypatInfoQo.getNickname());
        userQo.setGender(ypatInfoQo.getGender());
        userQo.setProfess(ypatInfoQo.getProfess());
        userQo.setName("wm");
        userQo.setMobile(genTempMobile());
        String userJson = userServiceClient.add(userQo);
        UserQo user = GsonUtils.fromJson(userJson, UserQo.class);

        // 2. 上传作品图片（加水印）
        List<String> picsList = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile multipartFile : files) {
                InputStream inputStream = multipartFile.getInputStream();
                InputStream waterStream = imageMarkUtil.waterMake(inputStream);
                StoredObject storedObject = storageService.upload(waterStream, ImageConst.IMAGE_TYPE, "image/jpeg", StorageBizPath.YPAT);
                if (storedObject == null || storedObject.getUrl() == null) {
                    throw new SysException(ResponseCode.FAIL_UPLOAD);
                }
                picsList.add(storedObject.getUrl());
            }
        }
        if (picsList.isEmpty()) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "请至少上传一张作品图片");
        }

        ypatInfoQo.setUserid(user.getId());
        ypatInfoQo.setPics(picsList);
        String res = ypatServiceClient.submit(ypatInfoQo);
        JsonElement resData = JsonParser.parseString(res);
        return ResponseApiBody.success(resData);
    }

    private String genTempMobile() {
        StringBuilder sb = new StringBuilder("1");
        java.util.Random r = new java.util.Random();
        for (int i = 0; i < 10; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
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
            if (resElement == null || resElement.isJsonNull() || !resElement.isJsonObject()) {
                throw new SysException(ResponseCode.FAIL_SER, "服务响应格式错误");
            }
            return resElement;
        }
        return object;
    }

    private void pushAuditMessage(Long id, String flag, String reason) {
        try {
            if (wxMessClient == null) {
                return;
            }
            String accessToken = wxMessClient.getAccessToken();
            if (accessToken == null) {
                return;
            }
            String ypatJson = ypatServiceClient.get(id, null);
            YpatInfoQo ypatInfoQo = GsonUtils.fromJson(ypatJson, YpatInfoQo.class);
            if (ypatInfoQo == null || ypatInfoQo.getUserQo() == null) {
                return;
            }
            Map<String, String> contentMap = new HashMap<>();
            contentMap.put("time", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            String content = ypatInfoQo.getDescrib();
            if (StringUtils.isNotEmpty(content) && content.length() >= 10) {
                content = content.substring(0, 10) + "...";
            }
            contentMap.put("content", content);
            // 消息模板字段简化为通用结果
            contentMap.put("result", "2".equals(flag) ? "审核通过" : "审核未通过");
            contentMap.put("note", StringUtils.isEmpty(reason) ? "无" : reason);
            wxMessClient.sendMsg(accessToken, ypatInfoQo.getUserQo().getOpenid(), com.ypat.enums.MessType.audit, "", contentMap);
        } catch (Exception e) {
            logger.error("约拍审核消息推送失败：", e);
        }
    }
}
