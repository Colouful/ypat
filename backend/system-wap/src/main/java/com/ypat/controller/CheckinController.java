package com.ypat.controller;

import com.ypat.CheckinResultQo;
import com.ypat.CheckinTodayQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.CheckinServiceClient;
import com.ypat.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckinController {
    @Autowired
    private CheckinServiceClient checkinServiceClient;

    @GetMapping("/checkin/today")
    public ResponseApiBody today() {
        Long userId = currentUserId();
        CheckinTodayQo qo = checkinServiceClient.today(userId);
        return ResponseApiBody.success(qo);
    }

    @PostMapping("/checkin/do")
    public ResponseApiBody doCheckin() {
        Long userId = currentUserId();
        CheckinResultQo qo = checkinServiceClient.doCheckin(userId);
        return ResponseApiBody.success(qo);
    }

    private Long currentUserId() {
        String raw = UserUtil.getUserId();
        if (StringUtils.isBlank(raw)) throw new SysException(ResponseCode.FAIL_AUTH);
        return Long.parseLong(raw);
    }
}
