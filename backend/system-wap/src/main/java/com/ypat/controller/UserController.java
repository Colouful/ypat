package com.ypat.controller;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.UserQo;
import com.ypat.comm.ImageConst;
import com.ypat.service.UserServiceClient;
import com.ypat.storage.StorageBizPath;
import com.ypat.storage.StorageService;
import com.ypat.storage.StoredObject;
import com.ypat.util.UserUtil;
import org.apache.tomcat.util.codec.binary.Base64;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
public class UserController {
    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserServiceClient systemServiceClient;
    @Autowired
    private StorageService storageService;

    @GetMapping("/user/get")
    public String get(Long id) {
        if(id==null){
            id = Long.parseLong(UserUtil.getUserId());
        }
        return systemServiceClient.get(id);
    }

    @GetMapping("/user/linkway/get")
    public String linkway(@NotEmpty Long userid, @NotEmpty Long messid) {
        Long id = Long.parseLong(UserUtil.getUserId());
        return systemServiceClient.linkway(id, userid, messid);
    }

    @PostMapping("/user/upd2")
    public String upd2(UserQo userQo, MultipartFile pics) throws IOException {
        Long id = Long.parseLong(UserUtil.getUserId());
        userQo.setId(id);
        if (pics != null) {
            StoredObject storedObject = storageService.upload(pics.getInputStream(), pics.getOriginalFilename(), pics.getContentType(), StorageBizPath.AVATAR);
            if (storedObject == null || storedObject.getUrl() == null) {
                throw new SysException(ResponseCode.FAIL_MARK);
            }
            userQo.setImgpath(storedObject.getUrl());
        }
        return systemServiceClient.upd(userQo);
    }

    @PostMapping("/user/upd")
    public String upd(@Valid UserQo userQo, String pics) throws IOException {
        logger.info("用户修改输入："+userQo);
        Long id = Long.parseLong(UserUtil.getUserId());
        userQo.setId(id);
        if (!StringUtils.isEmpty(pics)) {
            // 兼容 "data:image/xxx;base64,<data>" 格式：只切第一个逗号
            final String[] picsArr = pics.split(",", 2);
            if (picsArr[0].indexOf("data:image") >= 0 && picsArr.length == 2) {
                byte[] bytes = Base64.decodeBase64(picsArr[1]);
                StoredObject storedObject = storageService.upload(new ByteArrayInputStream(bytes), ImageConst.IMAGE_TYPE, "image/jpeg", StorageBizPath.AVATAR);
                if (storedObject == null || storedObject.getUrl() == null) {
                    throw new SysException(ResponseCode.FAIL_MARK);
                }
                userQo.setImgpath(storedObject.getUrl());
            } else {
                userQo.setImgpath(pics.trim());
            }
        }
        return systemServiceClient.upd(userQo);
    }

    @GetMapping("/user/findPage")
    public String findPage(UserQo userQo) {
        return systemServiceClient.findPage(userQo);
    }

}
