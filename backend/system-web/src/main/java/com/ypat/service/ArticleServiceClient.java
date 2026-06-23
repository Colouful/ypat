package com.ypat.service;

import com.ypat.ArticleQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("SYSTEM-API")
public interface ArticleServiceClient {

    /**
     * get
     * @param id
     * @return
     */
    @GetMapping("/service/article/get")
    String get(@RequestParam("id") Long id);

    /**
     * findPage
     * @param articleQo
     * @return
     */
    @PostMapping("/service/article/findPage")
    String findPage(@RequestBody ArticleQo articleQo);

    /**
     * add
     * @param articleQo
     * @return
     */
    @PostMapping("/service/article/add")
    String add(@RequestBody ArticleQo articleQo);

    /**
     * upDown
     * @param articleQo
     * @return
     */
    @PostMapping("/service/article/upDown")
    String upDown(@RequestBody ArticleQo articleQo);
}
