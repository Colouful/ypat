package com.ypat.service;

import com.ypat.MemberOrderCreateResult;
import com.ypat.MemberOrderQo;
import com.ypat.MemberPlanQo;
import com.ypat.MemberStatusQo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("SYSTEM-API")
public interface MemberServiceClient {

    @GetMapping("/service/member/plans")
    List<MemberPlanQo> plans();

    @GetMapping("/service/member/plan")
    MemberPlanQo plan(@RequestParam("planId") Long planId);

    @GetMapping("/service/member/status")
    MemberStatusQo status(@RequestParam("userId") Long userId);

    @PostMapping("/service/member/order/create")
    MemberOrderQo createOrder(@RequestParam("userId") Long userId,
                              @RequestParam("planId") Long planId);

    @PostMapping("/service/member/order/cancel")
    Boolean cancelOrder(@RequestParam("outTradeNo") String outTradeNo);

    @GetMapping("/service/member/order/get")
    MemberOrderQo getOrder(@RequestParam("outTradeNo") String outTradeNo,
                           @RequestParam("userId") Long userId);

    @PostMapping("/service/member/order/findPage")
    Map<String, Object> findOrders(@RequestBody MemberOrderQo qo);

    @PostMapping("/service/member/markPaid")
    Boolean markPaid(@RequestParam("outTradeNo") String outTradeNo,
                     @RequestParam("wxTransactionId") String wxTransactionId,
                     @RequestParam(value = "paidAtMs", required = false) Long paidAtMs);
}