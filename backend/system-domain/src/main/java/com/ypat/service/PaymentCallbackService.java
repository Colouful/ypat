package com.ypat.service;

import com.ypat.PaymentOrderQo;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.enums.PaymentBusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional(rollbackFor = Exception.class)
public class PaymentCallbackService {

    @Autowired
    private PaymentOrderService paymentOrderService;
    @Autowired
    private DepositService depositService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private OrderService orderService;

    public boolean markPaid(String outTradeNo, String txId, Integer amountFen,
                            Date paidAt, String eventId, String digest) {
        PaymentOrderQo payment = paymentOrderService.findByOutTradeNo(outTradeNo);
        if (payment == null) throw new SysException(ResponseCode.FAIL_NOT);
        if (amountFen == null || !amountFen.equals(payment.getAmountFen())) {
            throw new SysException(ResponseCode.FAIL_PAY_AMOUNT);
        }

        boolean first = paymentOrderService.markPaidIfPending(outTradeNo, txId, paidAt, eventId, digest);
        if (!first) return false;

        String businessNo = payment.getBusinessOrderNo();
        if (PaymentBusinessType.DEPOSIT.value.equals(payment.getBusinessType())) {
            depositService.markPaid(businessNo, txId, paidAt);
        } else if (PaymentBusinessType.MEMBER.value.equals(payment.getBusinessType())) {
            memberService.markPaid(businessNo, txId, paidAt);
        } else if (PaymentBusinessType.PPD.value.equals(payment.getBusinessType())) {
            orderService.markPpdPaid(businessNo);
        } else {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        return true;
    }
}
