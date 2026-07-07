package com.ypat.service;

import com.ypat.WorkComplainQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("SYSTEM-API")
public interface WorkComplainAdminServiceClient {

    @GetMapping("/service/work/complain/admin/list")
    String list(@RequestParam("page") Integer page,
                @RequestParam("size") Integer size,
                @RequestParam(value = "status", required = false) String status,
                @RequestParam(value = "workId", required = false) Long workId,
                @RequestParam(value = "userId", required = false) Long userId);

    @GetMapping("/service/work/complain/admin/detail")
    String detail(@RequestParam("id") Long id);

    @PostMapping("/service/work/complain/admin/handle")
    String handle(@RequestBody WorkComplainQo qo);
}
