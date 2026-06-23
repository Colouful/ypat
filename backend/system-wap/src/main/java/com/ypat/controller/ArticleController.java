package com.ypat.controller;


import com.ypat.ArticleQo;
import com.ypat.enums.ArticleStatus;
import com.ypat.service.ArticleServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticleController {

    @Autowired
    private ArticleServiceClient articleServiceClient;

    @GetMapping("/article/get")
    public String get(Long id) {
        return articleServiceClient.get(id);
    }

    @GetMapping("/article/list")
    public String findPage(ArticleQo articleQo) {
        articleQo.setStatus(ArticleStatus.fb.value);
        return articleServiceClient.findPage(articleQo);
    }

}
