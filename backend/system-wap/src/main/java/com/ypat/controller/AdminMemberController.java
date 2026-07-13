package com.ypat.controller;

import com.ypat.MemberBenefitRuleQo;
import com.ypat.MemberBenefitConfigQo;
import com.ypat.MemberOperationLogQo;
import com.ypat.MemberOrderQo;
import com.ypat.MemberPlanQo;
import com.ypat.MemberUserAdminQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.service.MemberServiceClient;
import com.ypat.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/member")
public class AdminMemberController {

    @Autowired
    private MemberServiceClient memberServiceClient;

    @GetMapping("/plans")
    public ResponseApiBody plans(MemberPlanQo qo) {
        return ResponseApiBody.success(memberServiceClient.adminPlans(qo));
    }

    @PostMapping("/plans")
    public ResponseApiBody savePlan(@RequestBody MemberPlanQo qo) {
        return ResponseApiBody.success(memberServiceClient.savePlan(qo));
    }

    @PutMapping("/plans/{id}")
    public ResponseApiBody updatePlan(@PathVariable Long id, @RequestBody MemberPlanQo qo) {
        if (qo == null) qo = new MemberPlanQo();
        qo.setId(id);
        return ResponseApiBody.success(memberServiceClient.savePlan(qo));
    }

    @GetMapping("/benefit-rules")
    public ResponseApiBody rules(MemberBenefitRuleQo qo) {
        return ResponseApiBody.success(memberServiceClient.adminRules(qo));
    }

    @PutMapping("/benefit-rules/{id}")
    public ResponseApiBody updateRule(@PathVariable Long id, @RequestBody MemberBenefitRuleQo qo) {
        if (qo == null) qo = new MemberBenefitRuleQo();
        qo.setId(id);
        return ResponseApiBody.success(memberServiceClient.saveRule(qo));
    }

    @GetMapping("/benefit-configs")
    public ResponseApiBody benefitConfigs() {
        return ResponseApiBody.success(memberServiceClient.adminBenefitConfigs());
    }

    @PutMapping("/benefit-configs/{scene}")
    public ResponseApiBody updateBenefitConfig(@PathVariable String scene,
                                               @RequestBody MemberBenefitConfigQo qo) {
        if (qo == null) qo = new MemberBenefitConfigQo();
        qo.setScene(scene);
        qo.setOperatorId(currentOperatorId());
        return ResponseApiBody.success(memberServiceClient.saveBenefitConfig(qo));
    }

    @GetMapping("/users")
    public ResponseApiBody users(MemberUserAdminQo qo) {
        return ResponseApiBody.success(memberServiceClient.adminUsers(qo));
    }

    @PostMapping("/users/{userId}/grant")
    public ResponseApiBody grant(@PathVariable Long userId, @RequestBody MemberUserAdminQo qo) {
        validateAction(qo);
        return ResponseApiBody.success(memberServiceClient.adminGrant(userId, qo.getDays(), currentOperatorId(), qo.getReason()));
    }

    @PostMapping("/users/{userId}/extend")
    public ResponseApiBody extend(@PathVariable Long userId, @RequestBody MemberUserAdminQo qo) {
        validateAction(qo);
        return ResponseApiBody.success(memberServiceClient.adminExtend(userId, qo.getDays(), currentOperatorId(), qo.getReason()));
    }

    @PostMapping("/users/{userId}/cancel")
    public ResponseApiBody cancel(@PathVariable Long userId, @RequestBody MemberUserAdminQo qo) {
        if (qo == null || StringUtils.isBlank(qo.getReason())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        return ResponseApiBody.success(memberServiceClient.adminCancel(userId, currentOperatorId(), qo.getReason()));
    }

    @GetMapping("/orders")
    public ResponseApiBody orders(MemberOrderQo qo) {
        return ResponseApiBody.success(memberServiceClient.adminOrders(qo));
    }

    @GetMapping("/logs")
    public ResponseApiBody logs(MemberOperationLogQo qo) {
        return ResponseApiBody.success(memberServiceClient.adminLogs(qo));
    }

    private void validateAction(MemberUserAdminQo qo) {
        if (qo == null || qo.getDays() == null || qo.getDays() <= 0 || StringUtils.isBlank(qo.getReason())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
    }

    private Long currentOperatorId() {
        String raw = UserUtil.getUserId();
        if (StringUtils.isBlank(raw)) return null;
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
