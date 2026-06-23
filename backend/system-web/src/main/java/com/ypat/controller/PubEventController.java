package com.ypat.controller;


import com.ypat.ArticleQo;
import com.ypat.PubEventQo;
import com.ypat.config.SystemConfig;
import com.ypat.service.ArticleServiceClient;
import com.ypat.service.PubEventServiceClient;
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
@RequestMapping("/pubevent")
public class PubEventController {

    @Autowired
    private PubEventServiceClient pubEventServiceClient;

    @RequestMapping("/index")
    public String index() {
        return "manage/pubevent/index";
    }

    @RequestMapping("/list")
    @ResponseBody
    public String findPage(PubEventQo pubEventQo) {
        pubEventQo.setEvent("SCAN");
        return pubEventServiceClient.findPage(pubEventQo);
    }

}
