package com.ypat.controller;

import com.ypat.MemberOrderCreateResult;
import com.ypat.MemberOrderQo;
import com.ypat.MemberPlanQo;
import com.ypat.MemberStatusQo;
import com.ypat.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * system-restapi 内部接口：被 system-wap / system-web 的 Feign 客户端调用。
 * 入参 / 出参全部用 system-object 里的 Qo 类型，system-wap 不依赖 system-domain。
 */
@RestController
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/service/member/plans")
    public List<MemberPlanQo> plans() {
        return memberService.listActivePlans();
    }

    @GetMapping("/service/member/plan")
    public MemberPlanQo plan(@RequestParam("planId") Long planId) {
        return memberService.getPlan(planId);
    }

    @GetMapping("/service/member/status")
    public MemberStatusQo status(@RequestParam("userId") Long userId) {
        return memberService.getStatus(userId);
    }

    /**
     * 创建会员订单：返回 Qo（不带 Entity），Feign 调用方需要 outTradeNo / priceFen / durationDays / planId。
     * 微信统一下单在调用方（system-wap MemberController）完成，避免跨模块依赖。
     */
    @PostMapping("/service/member/order/create")
    public MemberOrderQo createOrder(@RequestParam("userId") Long userId,
                                     @RequestParam("planId") Long planId) {
        return memberService.createPendingOrderQo(userId, planId);
    }

    @PostMapping("/service/member/order/cancel")
    public Boolean cancelOrder(@RequestParam("outTradeNo") String outTradeNo) {
        return memberService.cancelOrder(outTradeNo);
    }

    @GetMapping("/service/member/order/get")
    public MemberOrderQo getOrder(@RequestParam("outTradeNo") String outTradeNo,
                                  @RequestParam(value = "userId", required = false) Long userId) {
        return memberService.getOrder(outTradeNo, userId);
    }

    @PostMapping("/service/member/order/findPage")
    public Map<String, Object> findOrders(@RequestBody MemberOrderQo qo) {
        if (qo == null || qo.getUserId() == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("content", java.util.Collections.emptyList());
            empty.put("totalElements", 0L);
            empty.put("totalPages", 0);
            empty.put("number", 0);
            empty.put("size", qo == null || qo.getSize() == null ? 10 : qo.getSize());
            return empty;
        }
        return memberService.findUserOrders(qo.getUserId(), qo);
    }

    /**
     * 内部接口：微信支付回调命中会员订单后调此端点。
     */
    @PostMapping("/service/member/markPaid")
    public boolean markPaid(@RequestParam("outTradeNo") String outTradeNo,
                            @RequestParam("wxTransactionId") String wxTransactionId,
                            @RequestParam(value = "paidAtMs", required = false) Long paidAtMs) {
        java.util.Date paidAt = paidAtMs != null ? new java.util.Date(paidAtMs) : new java.util.Date();
        return memberService.markPaid(outTradeNo, wxTransactionId, paidAt);
    }
}