package com.ypat.controller;


import com.ypat.BannerQo;
import com.ypat.enums.ArticleStatus;
import com.ypat.service.BannerServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BannerController {

    @Autowired
    private BannerServiceClient bannerServiceClient;

    @GetMapping("/banner/get")
    public String get(Long id) {
        return bannerServiceClient.get(id);
    }

    @GetMapping("/banner/list")
    public String findPage(BannerQo bannerQo) {
        bannerQo.setStatus(ArticleStatus.fb.value);
        return bannerServiceClient.findPage(bannerQo);
    }

}
