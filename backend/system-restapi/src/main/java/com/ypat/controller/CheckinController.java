package com.ypat.controller;

import com.ypat.CheckinRecordQo;
import com.ypat.CheckinResultQo;
import com.ypat.CheckinRuleQo;
import com.ypat.CheckinTodayQo;
import com.ypat.service.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CheckinController {
    @Autowired
    private CheckinService checkinService;

    @GetMapping("/service/checkin/today")
    public CheckinTodayQo today(@RequestParam("userId") Long userId) {
        return checkinService.today(userId);
    }

    @PostMapping("/service/checkin/do")
    public CheckinResultQo doCheckin(@RequestParam("userId") Long userId) {
        return checkinService.doCheckin(userId);
    }

    @GetMapping("/service/checkin/rule")
    public CheckinRuleQo rule() {
        return checkinService.getRule();
    }

    @PostMapping("/service/checkin/rule/save")
    public CheckinRuleQo saveRule(@RequestBody CheckinRuleQo qo) {
        return checkinService.saveRule(qo);
    }

    @PostMapping("/service/checkin/records")
    public Map<String, Object> records(@RequestBody CheckinRecordQo qo) {
        return checkinService.findRecords(qo);
    }
}
