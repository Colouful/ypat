package com.ypat.service;

import com.ypat.MemberOrderCreateResult;
import com.ypat.MemberBenefitQuoteQo;
import com.ypat.MemberBenefitConfigQo;
import com.ypat.MemberBenefitRuleQo;
import com.ypat.MemberOperationLogQo;
import com.ypat.MemberOrderQo;
import com.ypat.MemberPlanQo;
import com.ypat.MemberStatusQo;
import com.ypat.MemberUserAdminQo;
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

    @GetMapping("/service/member/benefit/quote")
    MemberBenefitQuoteQo quote(@RequestParam("userId") Long userId,
                               @RequestParam("scene") String scene);

    @PostMapping("/service/member/order/create")
    MemberOrderQo createOrder(@RequestParam("userId") Long userId,
                              @RequestParam("planId") Long planId);

    @PostMapping("/service/member/order/cancel")
    Boolean cancelOrder(@RequestParam("outTradeNo") String outTradeNo);

    @PostMapping("/service/member/order/prepared")
    MemberOrderQo prepared(@RequestParam("outTradeNo") String outTradeNo,
                           @RequestParam("channel") String channel,
                           @RequestParam(value = "prepayId", required = false) String prepayId);

    @GetMapping("/service/member/order/get")
    MemberOrderQo getOrder(@RequestParam("outTradeNo") String outTradeNo,
                           @RequestParam("userId") Long userId);

    @PostMapping("/service/member/order/findPage")
    Map<String, Object> findOrders(@RequestBody MemberOrderQo qo);

    @PostMapping("/service/member/admin/plans")
    Map<String, Object> adminPlans(@RequestBody MemberPlanQo qo);

    @PostMapping("/service/member/admin/plan/save")
    MemberPlanQo savePlan(@RequestBody MemberPlanQo qo);

    @PostMapping("/service/member/admin/rules")
    Map<String, Object> adminRules(@RequestBody MemberBenefitRuleQo qo);

    @PostMapping("/service/member/admin/rule/save")
    MemberBenefitRuleQo saveRule(@RequestBody MemberBenefitRuleQo qo);

    @GetMapping("/service/member/admin/benefit-configs")
    List<MemberBenefitConfigQo> adminBenefitConfigs();

    @PostMapping("/service/member/admin/benefit-config/save")
    MemberBenefitConfigQo saveBenefitConfig(@RequestBody MemberBenefitConfigQo qo);

    @PostMapping("/service/member/admin/users")
    Map<String, Object> adminUsers(@RequestBody MemberUserAdminQo qo);

    @PostMapping("/service/member/admin/user/grant")
    Boolean adminGrant(@RequestParam("userId") Long userId,
                       @RequestParam("days") Integer days,
                       @RequestParam(value = "operatorId", required = false) Long operatorId,
                       @RequestParam("reason") String reason);

    @PostMapping("/service/member/admin/user/extend")
    Boolean adminExtend(@RequestParam("userId") Long userId,
                        @RequestParam("days") Integer days,
                        @RequestParam(value = "operatorId", required = false) Long operatorId,
                        @RequestParam("reason") String reason);

    @PostMapping("/service/member/admin/user/cancel")
    Boolean adminCancel(@RequestParam("userId") Long userId,
                        @RequestParam(value = "operatorId", required = false) Long operatorId,
                        @RequestParam("reason") String reason);

    @PostMapping("/service/member/admin/orders")
    Map<String, Object> adminOrders(@RequestBody MemberOrderQo qo);

    @PostMapping("/service/member/admin/logs")
    Map<String, Object> adminLogs(@RequestBody MemberOperationLogQo qo);

    @PostMapping("/service/member/markPaid")
    Boolean markPaid(@RequestParam("outTradeNo") String outTradeNo,
                     @RequestParam("wxTransactionId") String wxTransactionId,
                     @RequestParam(value = "paidAtMs", required = false) Long paidAtMs);
}
