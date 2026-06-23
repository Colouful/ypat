package com.ypat.controller;


import com.ypat.BannerQo;
import com.ypat.config.SystemConfig;
import com.ypat.service.BannerServiceClient;
import com.ypat.util.FastDFSClient;
import com.ypat.util.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Controller
@RequestMapping("/banner")
public class BannerController {

    @Autowired
    private BannerServiceClient bannerServiceClient;
    @Autowired
    private FastDFSClient fastDFSClient;

    @RequestMapping("/index")
    public String index() {
        return "manage/banner/index";
    }

    @RequestMapping("/edit")
    public ModelAndView bannerEdit(Long id, Model model) {
        String json = bannerServiceClient.get(id);
        BannerQo qo = GsonUtils.fromJson(json, BannerQo.class);
        model.addAttribute("banner", qo!=null ? qo : new BannerQo());
        return new ModelAndView("manage/banner/edit");
    }

    @RequestMapping("/list")
    @ResponseBody
    public String findPage(BannerQo bannerQo) {
        return bannerServiceClient.findPage(bannerQo);
    }

    @PostMapping("/save")
    @ResponseBody
    public String add(BannerQo bannerQo, MultipartFile file) throws IOException {
        String fileId = fastDFSClient.uploanFile1(file.getInputStream(), "jpg");
        String imgPath = SystemConfig.fdfs_path+fileId;
        bannerQo.setImgpath(imgPath);
        return bannerServiceClient.add(bannerQo);
    }

    @PostMapping("/upDown")
    @ResponseBody
    public String upDown(BannerQo bannerQo) {
        return bannerServiceClient.upDown(bannerQo);
    }

}
