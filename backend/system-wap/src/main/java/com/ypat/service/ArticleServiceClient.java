package com.ypat.service;

import com.ypat.ArticleQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("SYSTEM-API")
public interface ArticleServiceClient {

    @GetMapping("/service/article/get")
    String get(@RequestParam("id") Long id);

    @PostMapping("/service/article/findPage")
    String findPage(@RequestBody ArticleQo articleQo);

    @PostMapping("/service/article/add")
    void add(@RequestBody ArticleQo articleQo);

    @PostMapping("/service/article/upDown")
    void upDown(@RequestBody ArticleQo articleQo);

}
