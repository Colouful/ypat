package com.ypat.controller;


import com.ypat.ArticleQo;
import com.ypat.config.SystemConfig;
import com.ypat.service.ArticleServiceClient;
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

/**
 * @author dingyinxin
 */
@Controller
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleServiceClient articleServiceClient;
    @Autowired
    private FastDFSClient fastDFSClient;

    @RequestMapping("/index")
    public String index() {
        return "manage/article/index";
    }

    @RequestMapping("/edit")
    public ModelAndView articleEdit(Long id, Model model) {
        ArticleQo qo = new ArticleQo();
        if(id!=null) {
            String json = articleServiceClient.get(id);
            qo = GsonUtils.fromJson(json, ArticleQo.class);
        }
        model.addAttribute("article", qo);
        return new ModelAndView("manage/article/edit");
    }

    @RequestMapping("/list")
    @ResponseBody
    public String findPage(ArticleQo articleQo) {
        return articleServiceClient.findPage(articleQo);
    }

    @PostMapping("/save")
    @ResponseBody
    public String add(ArticleQo articleQo, MultipartFile file) throws IOException {
        articleQo.setContent(articleQo.getEditorValue());
        if(file!=null && !file.isEmpty()) {
            String fileId = fastDFSClient.uploanFile1(file.getInputStream(), "jpg");
            String imgPath = SystemConfig.fdfs_path+fileId;
            articleQo.setImgpath(imgPath);
        }
        return articleServiceClient.add(articleQo);
    }

    @PostMapping("/upDown")
    @ResponseBody
    public String upDown(ArticleQo articleQo) {
        return articleServiceClient.upDown(articleQo);
    }

}
