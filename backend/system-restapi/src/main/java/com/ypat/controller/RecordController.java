package com.ypat.controller;

import com.ypat.RecordQo;
import com.ypat.entity.Record;
import com.ypat.service.RecordService;
import com.ypat.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class RecordController {
    @Autowired
    private RecordService recordService;

    @GetMapping("/service/record/get")
    public RecordQo get(Long id) {
        return recordService.findById(id);
    }

    @PutMapping("/service/record/add")
    public void add(@RequestBody RecordQo recordQo){
        Record record = CopyUtil.copy(recordQo, Record.class);
        recordService.save(record);
    }

    @PostMapping("/service/record/findPage")
    public Map<String, Object> findPage(@RequestBody RecordQo recordQo) {
        return recordService.findPage(recordQo);
    }
}
