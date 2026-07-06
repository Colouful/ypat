package com.ypat.controller;

import com.ypat.YpatInfoQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.comm.Const;
import com.ypat.comm.ImageConst;
import com.ypat.enums.MessType;
import com.ypat.enums.YpatStatus;
import com.ypat.storage.Base64ImagePayload;
import com.ypat.service.YpatServiceClient;
import com.ypat.storage.StorageBizPath;
import com.ypat.storage.StorageService;
import com.ypat.storage.StorageUrlPolicy;
import com.ypat.storage.StoredObject;
import com.ypat.third.wxmess.WxMessClient;
import com.ypat.util.*;
import org.apache.tomcat.util.codec.binary.Base64;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class YpatInfoController {

    private static Logger logger = LoggerFactory.getLogger(YpatInfoController.class);
    @Autowired
    private YpatServiceClient ypatServiceClient;
    @Autowired
    private StorageService storageService;
    @Autowired
    private StorageUrlPolicy storageUrlPolicy;
    @Autowired
    private WxMessClient wxMessClient;
    @Autowired
    private ImageMarkUtil imageMarkUtil;

    @GetMapping("/ypat/get")
    public String get(@NotEmpty Long id) {
        return ypatServiceClient.get(id, UserUtil.getUserId()!=null?Long.parseLong(UserUtil.getUserId()):null);
    }

    @GetMapping(value = {"ypat/tc/list","ypat/tj/list","ypat/zx/list"})
    public String findPage(YpatInfoQo ypatInfoQo) {
        ypatInfoQo.setStatus(YpatStatus.shtg.value);
        return ypatServiceClient.findPage(ypatInfoQo);
    }


    @GetMapping(value = {"ypat/audit/list"})
    public String auditList(YpatInfoQo ypatInfoQo) {
        throw new SysException(ResponseCode.FAIL_VAL);
    }

    @PostMapping(value = {"/ypat/add","/ypat/upd"})
    public String add(@Valid YpatInfoQo ypatInfoQo, MultipartFile[] pics) throws IOException {
        List<String> picsList = new ArrayList<>();
        if (pics != null && pics.length > 0) {
            for (int i = 0; i < pics.length; i++) {
                MultipartFile uploadfile = pics[i];
                StoredObject storedObject = storageService.upload(uploadfile.getInputStream(), uploadfile.getOriginalFilename(), uploadfile.getContentType(), StorageBizPath.YPAT);
                if (storedObject == null || storedObject.getUrl() == null) {
                    throw new SysException(ResponseCode.FAIL_UPLOAD);
                }
                picsList.add(storedObject.getUrl());
            }
        }
        ypatInfoQo.setUserid(Long.parseLong(UserUtil.getUserId()));
        ypatInfoQo.setPics(picsList);
        return ypatServiceClient.add(ypatInfoQo);
    }

    @PostMapping("/ypat/submit")
    public String submit(@Valid YpatInfoQo ypatInfoQo) throws IOException {
        logger.info("约拍申请输入："+ypatInfoQo);
        if(ypatInfoQo.getPatdate()==null){
            throw new RuntimeException("patdate不能为空");
        }
        List<String> picsList = new ArrayList<>();
        List<String> pics = ypatInfoQo.getPics();
        if (pics != null && pics.size() > 0) {
            for (int i = 0; i < pics.size(); i++) {
                String fileBase64 = pics.get(i);
                if (isHttpUrl(fileBase64)) {
                    picsList.add(storageUrlPolicy.requireSupported(fileBase64));
                } else {
                    byte[] bytes = Base64.decodeBase64(stripBase64Prefix(fileBase64));
                    Base64ImagePayload image = Base64ImagePayload.fromBytes(bytes);
                    InputStream inputStream = image.inputStream();
                    InputStream waterStream = imageMarkUtil.waterMake(inputStream);
                    StoredObject storedObject = storageService.upload(waterStream, ImageConst.IMAGE_TYPE, "image/jpeg", StorageBizPath.YPAT);
                    if (storedObject == null || storedObject.getUrl() == null) {
                        throw new SysException(ResponseCode.FAIL_UPLOAD);
                    }
                    picsList.add(storedObject.getUrl());
                }
            }
        } else {
            throw new RuntimeException("未上传图片");
        }
        if(picsList.size()<=0) {
            throw new RuntimeException("上传图片格式错误");
        }

        ypatInfoQo.setUserid(Long.parseLong(UserUtil.getUserId()));
        ypatInfoQo.setPics(picsList);
        String res = ypatServiceClient.submit(ypatInfoQo);
        //推送消息
        try {
            String accessToken = wxMessClient.getAccessToken();
            if(accessToken != null) {
                String page = "";
                Map<String,String> contentMap = new HashMap<>();
                contentMap.put("type", "发布待审批");
                contentMap.put("per",  "去拍");
                wxMessClient.sendMsg(accessToken, Const.SYS_ADMIN, MessType.order, page, contentMap);
            }
        } catch (Exception e) {
            logger.error("消息推送失败：", e);
        }
        return res;
    }

    @RequestMapping(value = "/ypat/yd/add", method = {RequestMethod.POST, RequestMethod.PUT})
    public String readAdd(Long ypatid){
        return ypatServiceClient.readAdd(ypatid);
    }

    @PostMapping("/ypat/audit")
    public String audit(Long id, String flag, String recomflag, String reason, String messflag){
        throw new SysException(ResponseCode.FAIL_VAL);
    }


    @PostMapping("/ypat/upRecom")
    @ResponseBody
    public String upRecom(Long id, String recomflag) {
        throw new SysException(ResponseCode.FAIL_VAL);
    }

    private boolean isHttpUrl(String value) {
        if (value == null) return false;
        String text = value.trim().toLowerCase();
        return text.startsWith("http://") || text.startsWith("https://");
    }

    private String stripBase64Prefix(String value) {
        if (value == null) return "";
        int comma = value.indexOf(',');
        return comma >= 0 ? value.substring(comma + 1) : value;
    }
}
