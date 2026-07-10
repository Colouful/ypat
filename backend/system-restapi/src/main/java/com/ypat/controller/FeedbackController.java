package com.ypat.controller;

import com.ypat.FeedbackQo;
import com.ypat.ResponseApiBody;
import com.ypat.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/service/feedback/add")
    public FeedbackQo add(@RequestBody FeedbackQo feedbackQo) {
        return feedbackService.add(feedbackQo);
    }

    @GetMapping("/service/feedback/admin/list")
    public ResponseApiBody adminList(@RequestParam("page") Integer page,
                                     @RequestParam("size") Integer size,
                                     @RequestParam(value = "status", required = false) String status,
                                     @RequestParam(value = "type", required = false) String type,
                                     @RequestParam(value = "userId", required = false) Long userId) {
        return ResponseApiBody.success(feedbackService.adminList(page, size, status, type, userId));
    }

    @GetMapping("/service/feedback/admin/detail")
    public ResponseApiBody adminDetail(@RequestParam("id") Long id) {
        return ResponseApiBody.success(feedbackService.adminDetail(id));
    }

    @PostMapping("/service/feedback/admin/handle")
    public ResponseApiBody adminHandle(@RequestBody FeedbackQo feedbackQo) {
        feedbackService.adminHandle(feedbackQo);
        return ResponseApiBody.success("处理完成");
    }
}
