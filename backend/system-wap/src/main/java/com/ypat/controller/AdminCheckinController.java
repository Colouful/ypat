package com.ypat.controller;

import com.ypat.CheckinRecordQo;
import com.ypat.CheckinRuleQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.enums.YesNo;
import com.ypat.service.CheckinServiceClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/checkin")
public class AdminCheckinController {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    @Autowired
    private CheckinServiceClient checkinServiceClient;

    @GetMapping("/rule")
    public ResponseApiBody rule() {
        return ResponseApiBody.success(checkinServiceClient.rule());
    }

    @PutMapping("/rule")
    public ResponseApiBody saveRule(@RequestBody CheckinRuleQo qo) {
        if (qo == null || !isYesNo(qo.getEnabled()) || qo.getRewardPpd() == null || qo.getRewardPpd() < 0
                || StringUtils.isBlank(qo.getConfirmTitle()) || StringUtils.isBlank(qo.getConfirmContent())
                || qo.getConfirmTitle().trim().length() > 64 || qo.getConfirmContent().trim().length() > 256) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        return ResponseApiBody.success(checkinServiceClient.saveRule(qo));
    }

    @GetMapping("/records")
    public ResponseApiBody records(CheckinRecordQo qo) {
        return ResponseApiBody.success(checkinServiceClient.records(normalizePage(qo)));
    }

    private boolean isYesNo(String value) {
        return YesNo.yes.value.equals(value) || YesNo.no.value.equals(value);
    }

    private CheckinRecordQo normalizePage(CheckinRecordQo qo) {
        if (qo == null) {
            qo = new CheckinRecordQo();
        }
        if (qo.getPage() == null || qo.getPage() < 0) {
            qo.setPage(DEFAULT_PAGE);
        }
        if (qo.getSize() == null || qo.getSize() <= 0) {
            qo.setSize(DEFAULT_SIZE);
        }
        if (qo.getSize() > MAX_SIZE) {
            qo.setSize(MAX_SIZE);
        }
        return qo;
    }
}
