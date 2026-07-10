package com.ypat.service;

import com.ypat.InternalTestUserActionQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.entity.User;
import com.ypat.enums.InternalTestDataFlag;
import com.ypat.enums.YesNo;
import com.ypat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class InternalTestUserActionService {

    private static final int DEFAULT_MEMBER_DAYS = 365;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private DepositService depositService;

    public boolean grantMember(InternalTestUserActionQo qo) {
        User user = requireInternalUser(qo);
        int days = qo.getDays() == null || qo.getDays() <= 0 ? DEFAULT_MEMBER_DAYS : qo.getDays();
        return memberService.adminGrant(user.getId(), days, qo.getOperatorId(), reason(qo, "内测数据一键会员"));
    }

    public boolean verifyUser(InternalTestUserActionQo qo) {
        User user = requireInternalUser(qo);
        String beforeValue = "realnameflag=" + user.getRealnameflag() + ",creditflag=" + user.getCreditflag();
        user.setRealnameflag(YesNo.yes.value);
        user.setCreditflag(YesNo.yes.value);
        userRepository.save(user);
        memberService.recordAdminOperation(user.getId(), qo.getOperatorId(), "INTERNAL_TEST_VERIFY",
                reason(qo, "内测数据一键认证"), beforeValue, "realnameflag=1,creditflag=1");
        return true;
    }

    public boolean markDepositPaid(InternalTestUserActionQo qo) {
        User user = requireInternalUser(qo);
        com.ypat.DepositOrderQo order = depositService.createInternalTestPaidOrder(user.getId());
        memberService.recordAdminOperation(user.getId(), qo.getOperatorId(), "INTERNAL_TEST_DEPOSIT",
                reason(qo, "内测数据一键保证金"), null, "outTradeNo=" + order.getOutTradeNo());
        return true;
    }

    private User requireInternalUser(InternalTestUserActionQo qo) {
        if (qo == null || qo.getUserId() == null) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        User user = userRepository.findOne(qo.getUserId());
        if (user == null) {
            throw new SysException(ResponseCode.FAIL_NOT);
        }
        if (!InternalTestDataFlag.internalTest.value.equals(user.getDataFlag())) {
            throw new SysException(ResponseCode.FAIL_PARA, "只能操作内测用户");
        }
        return user;
    }

    private String reason(InternalTestUserActionQo qo, String fallback) {
        if (qo == null || qo.getReason() == null || qo.getReason().trim().isEmpty()) {
            return fallback;
        }
        return qo.getReason().trim();
    }
}
