package com.ypat.controller;

import com.ypat.CheckinRecordQo;
import com.ypat.CheckinRuleQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.CheckinServiceClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminCheckinController {
    @Autowired
    private CheckinServiceClient checkinServiceClient;

    @GetMapping("/admin/checkin/rule")
    public ResponseApiBody rule() {
        return ResponseApiBody.success(checkinServiceClient.rule());
    }

    @PutMapping("/admin/checkin/rule")
    public ResponseApiBody saveRule(@RequestBody CheckinRuleQo qo) {
        if (qo == null || StringUtils.isBlank(qo.getEnabled()) || qo.getRewardPpd() == null
                || qo.getRewardPpd() < 0 || StringUtils.isBlank(qo.getConfirmTitle())
                || StringUtils.isBlank(qo.getConfirmContent())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        return ResponseApiBody.success(checkinServiceClient.saveRule(qo));
    }

    @GetMapping("/admin/checkin/records")
    public ResponseApiBody records(CheckinRecordQo qo) {
        return ResponseApiBody.success(checkinServiceClient.records(qo));
    }
}
