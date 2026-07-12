package com.ypat.controller;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.WorkDetailQo;
import com.ypat.WorkListQo;
import com.ypat.WorkQuickApplyQo;
import com.ypat.WorkSubmitQo;
import com.ypat.service.WorkServiceClient;
import com.ypat.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作品 Controller
 */
@RestController
public class WorkController {

    @Autowired
    private WorkServiceClient workServiceClient;

    @PostMapping("/work/submit")
    public String submit(WorkSubmitQo qo) {
        String userId = UserUtil.getUserId();
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        qo.setUserid(userId);
        return workServiceClient.submit(qo);
    }

    @GetMapping("/work/get")
    public String get(@RequestParam("id") String id) {
        if (id == null) throw new SysException(ResponseCode.FAIL_PARA);
        String viewerUserId = UserUtil.getUserId();
        return workServiceClient.get(id, viewerUserId);
    }

    @PostMapping("/work/list")
    public String list(WorkListQo qo) {
        if (qo == null) qo = new WorkListQo();
        qo.setViewerUserId(UserUtil.getUserId());
        return workServiceClient.list(qo);
    }

    @GetMapping("/work/my")
    public String my(@RequestParam(value = "page", defaultValue = "1") Integer page,
                     @RequestParam(value = "size", defaultValue = "10") Integer size,
                     @RequestParam(value = "status", required = false) String status) {
        String userId = UserUtil.getUserId();
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        return workServiceClient.my(userId, page, size, status);
    }

    @GetMapping("/work/favorites")
    public String favorites(@RequestParam(value = "page", defaultValue = "1") Integer page,
                            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        String userId = UserUtil.getUserId();
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        return workServiceClient.favorites(userId, page, size);
    }

    @PutMapping("/work/offline")
    public String offline(@RequestParam("id") String id) {
        String userId = UserUtil.getUserId();
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        return workServiceClient.offline(id, userId);
    }

    @PutMapping("/work/like/add")
    public String like(@RequestParam("workId") String workId) {
        String userId = UserUtil.getUserId();
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        return workServiceClient.like(workId, userId);
    }

    @PutMapping("/work/like/cancel")
    public String unlike(@RequestParam("workId") String workId) {
        String userId = UserUtil.getUserId();
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        return workServiceClient.unlike(workId, userId);
    }

    @PutMapping("/work/sc/add")
    public String favorite(@RequestParam("workId") String workId) {
        String userId = UserUtil.getUserId();
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        return workServiceClient.favorite(workId, userId);
    }

    @PutMapping("/work/sc/cancel")
    public String unfavorite(@RequestParam("workId") String workId) {
        String userId = UserUtil.getUserId();
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        return workServiceClient.unfavorite(workId, userId);
    }

    @PostMapping("/work/quick-apply")
    public String quickApply(WorkQuickApplyQo qo) {
        String userId = UserUtil.getUserId();
        if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
        qo.setViewerUserId(userId);
        return workServiceClient.quickApply(qo);
    }
}
