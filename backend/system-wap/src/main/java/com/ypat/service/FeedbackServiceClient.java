package com.ypat.service;

import com.ypat.FeedbackQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("SYSTEM-API")
public interface FeedbackServiceClient {
    @PostMapping("/service/feedback/add")
    String add(@RequestBody FeedbackQo feedbackQo);
}
