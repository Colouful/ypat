package com.ypat.controller;

import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.WorkComplainQo;
import com.ypat.service.WorkComplainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service/work")
public class WorkComplainController {

    @Autowired
    private WorkComplainService workComplainService;

    @PostMapping("/complain")
    public ResponseApiBody complain(@RequestBody WorkComplainQo qo) {
        if (qo == null) throw new SysException(ResponseCode.FAIL_PARA);
        if (qo.getUserId() == null) throw new SysException(ResponseCode.FAIL_AUTH);
        workComplainService.complain(qo);
        return ResponseApiBody.success("投诉已提交");
    }
}
