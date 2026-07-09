package com.ypat.service;

import com.ypat.CheckinRecordQo;
import com.ypat.CheckinResultQo;
import com.ypat.CheckinRuleQo;
import com.ypat.CheckinTodayQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("SYSTEM-API")
public interface CheckinServiceClient {
    @GetMapping("/service/checkin/today")
    CheckinTodayQo today(@RequestParam("userId") Long userId);

    @PostMapping("/service/checkin/do")
    CheckinResultQo doCheckin(@RequestParam("userId") Long userId);

    @GetMapping("/service/checkin/rule")
    CheckinRuleQo rule();

    @PostMapping("/service/checkin/rule/save")
    CheckinRuleQo saveRule(@RequestBody CheckinRuleQo qo);

    @PostMapping("/service/checkin/records")
    Map<String, Object> records(@RequestBody CheckinRecordQo qo);
}
