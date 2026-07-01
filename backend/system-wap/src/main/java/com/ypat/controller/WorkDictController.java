package com.ypat.controller;

import com.ypat.service.WorkDictServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkDictController {

    @Autowired
    private WorkDictServiceClient workDictServiceClient;

    @GetMapping("/dict/work-tag")
    public String listWorkTags() {
        return workDictServiceClient.listWorkTags();
    }
}
