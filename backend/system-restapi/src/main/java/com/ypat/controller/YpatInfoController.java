package com.ypat.controller;

import com.google.gson.Gson;
import com.ypat.YpatInfoQo;
import com.ypat.entity.YpatInfo;
import com.ypat.service.YpatInfoService;
import com.ypat.util.CopyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class YpatInfoController {
    private static Logger logger = LoggerFactory.getLogger(YpatInfoController.class);

    @Autowired
    private YpatInfoService ypatInfoService;

    @GetMapping("/service/ypat/get")
    public YpatInfoQo findById(Long id, Long userid) {
        return ypatInfoService.findById(id, userid);
    }

    @PostMapping("/service/ypat/findPage")
    public Map<String, Object> findPage(@RequestBody YpatInfoQo ypatInfoQo) {
        return ypatInfoService.findPage(ypatInfoQo);
    }

    @PostMapping("/service/ypat/add")
    public YpatInfoQo add(@RequestBody YpatInfoQo ypatInfoQo){
        return ypatInfoService.save(ypatInfoQo);
    }

    @PostMapping("/service/ypat/submit")
    public void submit(@RequestBody YpatInfoQo ypatInfoQo){
        ypatInfoService.submit(ypatInfoQo);
    }

    @PostMapping("/service/ypat/audit")
    public void audit(Long id, String flag, String recomflag, String reason){
        ypatInfoService.audit(id, flag, recomflag, reason);
    }

    @DeleteMapping("/service/ypat/del")
    public void del(Long id){
        ypatInfoService.delete(id);
    }

    @PutMapping("/service/ypat/yd/add")
    public void readAdd(Long id){
        ypatInfoService.readAdd(id);
    }

    @PostMapping("/service/ypat/upRecom")
    public void upRecom(Long id, String recomflag){
        ypatInfoService.upRecom(id, recomflag);
    }
    /**
     *
    @PutMapping(value = "/service/ypat/yp/add")
    public void ypatAdd(Long id){
        ypatInfoService.ypatAdd(id);
    }

    @PutMapping(value = "/service/ypat/sc/add")
    public void collAdd(Long id){
        ypatInfoService.collAdd(id);
    }
     */
}
