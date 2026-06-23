package com.ypat.controller;

import com.ypat.ArticleQo;
import com.ypat.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @GetMapping("/service/article/get")
    public ArticleQo get(Long id) {
        return articleService.findById(id);
    }

    @PostMapping("/service/article/add")
    public void add(@RequestBody ArticleQo articleQo){
        articleService.save(articleQo);
    }

    @PostMapping("/service/article/upDown")
    public void upDown(@RequestBody ArticleQo articleQo){
        articleService.upDown(articleQo);
    }

    @PostMapping("/service/article/read")
    public void readAdd(Long id){
        articleService.readAdd(id);
    }

    @PostMapping("/service/article/findPage")
    public Map<String, Object> findPage(@RequestBody ArticleQo articleQo) {
        return articleService.findPage(articleQo);
    }
}
