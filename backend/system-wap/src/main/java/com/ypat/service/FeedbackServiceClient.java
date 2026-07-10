package com.ypat.service;

import com.ypat.FeedbackQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("SYSTEM-API")
public interface FeedbackServiceClient {
    @PostMapping("/service/feedback/add")
    String add(@RequestBody FeedbackQo feedbackQo);

    @GetMapping("/service/feedback/admin/list")
    String adminList(@RequestParam("page") Integer page,
                     @RequestParam("size") Integer size,
                     @RequestParam(value = "status", required = false) String status,
                     @RequestParam(value = "type", required = false) String type,
                     @RequestParam(value = "userId", required = false) Long userId);

    @GetMapping("/service/feedback/admin/detail")
    String adminDetail(@RequestParam("id") Long id);

    @PostMapping("/service/feedback/admin/handle")
    String adminHandle(@RequestBody FeedbackQo feedbackQo);
}
