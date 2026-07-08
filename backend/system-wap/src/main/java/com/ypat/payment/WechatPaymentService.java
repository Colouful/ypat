package com.ypat.payment;

import com.ypat.PaymentCreateResult;
import com.ypat.PaymentPayParams;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.enums.PaymentChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WechatPaymentService {

    private final WechatPayV3Client client;

    @Autowired
    public WechatPaymentService(WechatPayV3Client client) {
        this.client = client;
    }

    public PaymentCreateResult create(WechatPaymentCommand command) {
        if (command == null || !PaymentChannel.supportedForCreate(command.getChannel())) {
            throw new SysException(ResponseCode.FAIL_PARA);
        }
        PaymentCreateResult result = new PaymentCreateResult();
        result.setOutTradeNo(command.getOutTradeNo());
        result.setBusinessType(command.getBusinessType());
        result.setChannel(command.getChannel());
        result.setAmountFen(command.getAmountFen());

        WechatPayV3Client.PrepayCommand prepay = new WechatPayV3Client.PrepayCommand();
        prepay.setDescription(command.getDescription());
        prepay.setOutTradeNo(command.getOutTradeNo());
        prepay.setAmountFen(command.getAmountFen());
        prepay.setOpenid(command.getOpenid());
        prepay.setClientIp(command.getClientIp());

        if (PaymentChannel.MINIAPP.value.equals(command.getChannel())) {
            PaymentPayParams params = client.prepayMiniapp(prepay);
            result.setPayParams(params);
        } else if (PaymentChannel.H5.value.equals(command.getChannel())) {
            result.setH5Url(client.prepayH5(prepay));
        }
        return result;
    }

    public static class WechatPaymentCommand {
        private String businessType;
        private String channel;
        private String description;
        private String outTradeNo;
        private Integer amountFen;
        private String openid;
        private String clientIp;

        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getOutTradeNo() { return outTradeNo; }
        public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
        public Integer getAmountFen() { return amountFen; }
        public void setAmountFen(Integer amountFen) { this.amountFen = amountFen; }
        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
        public String getClientIp() { return clientIp; }
        public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    }
}
