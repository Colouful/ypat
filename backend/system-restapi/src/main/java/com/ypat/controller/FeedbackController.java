package com.ypat.controller;

import com.ypat.FeedbackQo;
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
}
