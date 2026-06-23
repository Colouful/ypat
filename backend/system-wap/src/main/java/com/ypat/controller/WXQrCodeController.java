package com.ypat.controller;

import com.ypat.annotation.NotIntercept;
import com.ypat.third.wxmess.WxMessClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class WXQrCodeController {

    private static Logger logger = LoggerFactory.getLogger(YpatInfoController.class);

    @Autowired
    private WxMessClient wxMessClient;

    @NotIntercept
    @GetMapping(value = "/qr/code", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] qrCode(String scene, String page, HttpServletResponse response) {
        try {
            String accessToken = wxMessClient.getAccessToken();
            if(accessToken != null) {
                byte[] bytes = wxMessClient.qrCode(accessToken, scene, page);
                return bytes;
            }
        } catch (Exception e) {
            logger.error("二维码生成失败：", e);
        }
        return null;
    }

    @GetMapping(value = "/pub/qr/code")
    public String pubQrCode(Integer scene) {
        try {
            String accessToken = wxMessClient.getAccessTokenPub();
            if(accessToken != null) {
                return wxMessClient.pubQrCode(accessToken, scene);
            }
        } catch (Exception e) {
            logger.error("二维码生成失败：", e);
        }
        return null;
    }
}
