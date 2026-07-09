package com.ypat.service;

import com.ypat.InviteConfigQo;
import com.ypat.InviteRelationQo;
import com.ypat.InviteSummaryQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("SYSTEM-API")
public interface InviteServiceClient {

    @GetMapping("/service/invite/summary")
    InviteSummaryQo summary(@RequestParam("userid") Long userid);

    @PostMapping("/service/invite/findPage")
    Map<String, Object> findPage(@RequestBody InviteRelationQo qo);

    @GetMapping("/service/invite/config")
    InviteConfigQo config();

    @PostMapping("/service/invite/config/save")
    InviteConfigQo saveConfig(@RequestBody InviteConfigQo qo);

    @PostMapping("/service/invite/admin/findPage")
    Map<String, Object> adminFindPage(@RequestBody InviteRelationQo qo);
}
