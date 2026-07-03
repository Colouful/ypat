package com.ypat.controller;

import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.WorkDetailQo;
import com.ypat.WorkListQo;
import com.ypat.WorkQuickApplyQo;
import com.ypat.WorkSubmitQo;
import com.ypat.entity.Work;
import com.ypat.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 作品 Controller（Feign 目标）
 */
@RestController
@RequestMapping("/service/work")
public class WorkController {

    @Autowired
    private WorkService workService;

    @PostMapping("/submit")
    public ResponseApiBody submit(@RequestBody WorkSubmitQo qo) {
        if (qo == null) throw new SysException(ResponseCode.FAIL_PARA);
        if (qo.getUserid() == null) throw new SysException(ResponseCode.FAIL_AUTH);
        Work w = workService.submit(qo);
        return ResponseApiBody.success(w);
    }

    @GetMapping("/get")
    public ResponseApiBody get(@RequestParam("id") String id, @RequestParam(value = "viewerUserId", required = false) String viewerUserId) {
        WorkDetailQo qo = new WorkDetailQo();
        qo.setId(id);
        qo.setViewerUserId(viewerUserId);
        Map<String, Object> detail = workService.getDetail(qo);
        return ResponseApiBody.success(detail);
    }

    @PostMapping("/list")
    public ResponseApiBody list(@RequestBody WorkListQo qo) {
        Map<String, Object> page = workService.pageList(qo);
        return ResponseApiBody.success(page);
    }

    @PostMapping("/admin/list")
    public ResponseApiBody adminList(@RequestBody WorkListQo qo) {
        Map<String, Object> page = workService.adminPageList(qo);
        return ResponseApiBody.success(page);
    }

    @GetMapping("/admin/detail")
    public ResponseApiBody adminDetail(@RequestParam("id") Long id) {
        return ResponseApiBody.success(workService.adminDetail(id));
    }

    @PostMapping("/admin/audit")
    public ResponseApiBody adminAudit(@RequestParam("id") Long id,
                                      @RequestParam("flag") String flag,
                                      @RequestParam(value = "reason", required = false) String reason) {
        workService.adminAudit(id, flag, reason);
        return ResponseApiBody.success("审核完成");
    }

    @PostMapping("/admin/offline")
    public ResponseApiBody adminOffline(@RequestParam("id") Long id,
                                        @RequestParam(value = "reason", required = false) String reason) {
        workService.adminOffline(id, reason);
        return ResponseApiBody.success("已下架");
    }

    @GetMapping("/my")
    public ResponseApiBody my(@RequestParam("userId") String userId,
                              @RequestParam("page") Integer page,
                              @RequestParam("size") Integer size,
                              @RequestParam(value = "status", required = false) String status) {
        Long uid = Long.parseLong(userId);
        Map<String, Object> res = workService.myWorks(uid, page, size, status);
        return ResponseApiBody.success(res);
    }

    @PutMapping("/offline")
    public ResponseApiBody offline(@RequestParam("id") String id, @RequestParam("userId") String userId) {
        Long wid = Long.parseLong(id);
        Long uid = Long.parseLong(userId);
        workService.offline(wid, uid);
        return ResponseApiBody.success("已下架");
    }

    @PutMapping("/like/add")
    public ResponseApiBody like(@RequestParam("workId") String workId, @RequestParam("userId") String userId) {
        workService.like(Long.parseLong(workId), Long.parseLong(userId));
        return ResponseApiBody.success("已点赞");
    }

    @PutMapping("/like/cancel")
    public ResponseApiBody unlike(@RequestParam("workId") String workId, @RequestParam("userId") String userId) {
        workService.unlike(Long.parseLong(workId), Long.parseLong(userId));
        return ResponseApiBody.success("已取消点赞");
    }

    @PutMapping("/sc/add")
    public ResponseApiBody favorite(@RequestParam("workId") String workId, @RequestParam("userId") String userId) {
        workService.favorite(Long.parseLong(workId), Long.parseLong(userId));
        return ResponseApiBody.success("已收藏");
    }

    @PutMapping("/sc/cancel")
    public ResponseApiBody unfavorite(@RequestParam("workId") String workId, @RequestParam("userId") String userId) {
        workService.unfavorite(Long.parseLong(workId), Long.parseLong(userId));
        return ResponseApiBody.success("已取消收藏");
    }

    @PostMapping("/quick-apply")
    public ResponseApiBody quickApply(@RequestBody WorkQuickApplyQo qo) {
        Map<String, Object> res = workService.quickApply(qo);
        return ResponseApiBody.success(res);
    }
}
