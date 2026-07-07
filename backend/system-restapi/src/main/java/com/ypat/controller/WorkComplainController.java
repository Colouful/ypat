package com.ypat.controller;

import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.WorkComplainQo;
import com.ypat.service.WorkComplainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    @GetMapping("/complain/admin/list")
    public ResponseApiBody adminList(@RequestParam("page") Integer page,
                                     @RequestParam("size") Integer size,
                                     @RequestParam(value = "status", required = false) String status,
                                     @RequestParam(value = "workId", required = false) Long workId,
                                     @RequestParam(value = "userId", required = false) Long userId) {
        if (page == null || page < 0 || size == null || size <= 0) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (workId != null && workId <= 0) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (userId != null && userId <= 0) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        Map<String, Object> res = workComplainService.adminList(page, size, status, workId, userId);
        return ResponseApiBody.success(res);
    }

    @GetMapping("/complain/admin/detail")
    public ResponseApiBody adminDetail(@RequestParam("id") Long id) {
        if (id == null || id <= 0) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        return ResponseApiBody.success(workComplainService.adminDetail(id));
    }

    @PostMapping("/complain/admin/handle")
    public ResponseApiBody adminHandle(@RequestBody WorkComplainQo qo) {
        if (qo == null || qo.getId() == null || qo.getId() <= 0) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        if (qo.getStatus() == null || qo.getStatus().trim().isEmpty()) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        workComplainService.adminHandle(qo);
        return ResponseApiBody.success("处理完成");
    }
}
