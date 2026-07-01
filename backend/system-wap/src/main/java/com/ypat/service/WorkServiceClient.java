package com.ypat.service;

import com.ypat.WorkDetailQo;
import com.ypat.WorkListQo;
import com.ypat.WorkQuickApplyQo;
import com.ypat.WorkSubmitQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("SYSTEM-API")
public interface WorkServiceClient {

    @PostMapping("/service/work/submit")
    String submit(@RequestBody WorkSubmitQo qo);

    @GetMapping("/service/work/get")
    String get(@RequestParam("id") String id, @RequestParam(value = "viewerUserId", required = false) String viewerUserId);

    @PostMapping("/service/work/list")
    String list(@RequestBody WorkListQo qo);

    @GetMapping("/service/work/my")
    String my(@RequestParam("userId") String userId, @RequestParam("page") Integer page, @RequestParam("size") Integer size, @RequestParam(value = "status", required = false) String status);

    @PutMapping("/service/work/offline")
    String offline(@RequestParam("id") String id, @RequestParam("userId") String userId);

    @PutMapping("/service/work/like/add")
    String like(@RequestParam("workId") String workId, @RequestParam("userId") String userId);

    @PutMapping("/service/work/like/cancel")
    String unlike(@RequestParam("workId") String workId, @RequestParam("userId") String userId);

    @PutMapping("/service/work/sc/add")
    String favorite(@RequestParam("workId") String workId, @RequestParam("userId") String userId);

    @PutMapping("/service/work/sc/cancel")
    String unfavorite(@RequestParam("workId") String workId, @RequestParam("userId") String userId);

    @PostMapping("/service/work/quick-apply")
    String quickApply(@RequestBody WorkQuickApplyQo qo);
}
