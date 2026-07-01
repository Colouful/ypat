package com.ypat.controller;

import com.ypat.ResponseApiBody;
import com.ypat.entity.WorkTag;
import com.ypat.repository.WorkTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service/dict")
public class WorkDictController {

    @Autowired
    private WorkTagRepository workTagRepository;

    @GetMapping("/work-tag")
    public ResponseApiBody listWorkTags() {
        List<WorkTag> tags = workTagRepository.findByStatusOrderBySortNoAsc(1);
        return ResponseApiBody.success(tags);
    }
}
