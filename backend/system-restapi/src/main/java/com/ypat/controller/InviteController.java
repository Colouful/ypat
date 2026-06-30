package com.ypat.controller;

import com.ypat.InviteRelationQo;
import com.ypat.InviteSummaryQo;
import com.ypat.service.InviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * system-restapi 内部接口：被 system-wap / system-web 的 Feign 客户端调用。
 * 路径前缀 /service/invite/ 与现有 RecordController 等保持一致。
 */
@RestController
public class InviteController {

    @Autowired
    private InviteService inviteService;

    @GetMapping("/service/invite/summary")
    public InviteSummaryQo summary(@RequestParam("userid") Long userid) {
        return inviteService.getSummary(userid);
    }

    @PostMapping("/service/invite/findPage")
    public Map<String, Object> findPage(@RequestBody InviteRelationQo qo) {
        return inviteService.findPage(qo);
    }
}
