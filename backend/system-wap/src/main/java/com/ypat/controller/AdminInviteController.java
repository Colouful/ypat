package com.ypat.controller;

import com.ypat.InviteConfigQo;
import com.ypat.InviteRelationQo;
import com.ypat.ResponseApiBody;
import com.ypat.service.InviteServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/invite")
public class AdminInviteController {

    @Autowired
    private InviteServiceClient inviteServiceClient;

    @GetMapping("/config")
    public ResponseApiBody config() {
        return ResponseApiBody.success(inviteServiceClient.config());
    }

    @PutMapping("/config")
    public ResponseApiBody saveConfig(@RequestBody InviteConfigQo qo) {
        return ResponseApiBody.success(inviteServiceClient.saveConfig(qo));
    }

    @GetMapping("/records")
    public ResponseApiBody records(InviteRelationQo qo) {
        return ResponseApiBody.success(inviteServiceClient.adminFindPage(qo));
    }
}
