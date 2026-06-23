package com.ypat.controller;

import com.ypat.BannerQo;
import com.ypat.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class BannerController {
    @Autowired
    private BannerService bannerService;

    @GetMapping("/service/banner/get")
    public BannerQo get(Long id) {
        return bannerService.findById(id);
    }

    @PostMapping("/service/banner/add")
    public void add(@RequestBody BannerQo bannerQo){
        bannerService.save(bannerQo);
    }

    @PostMapping("/service/banner/upDown")
    public void upDown(@RequestBody BannerQo bannerQo){
        bannerService.upDown(bannerQo);
    }

    @PostMapping("/service/banner/findPage")
    public Map<String, Object> findPage(@RequestBody BannerQo bannerQo) {
        return bannerService.findPage(bannerQo);
    }
}
