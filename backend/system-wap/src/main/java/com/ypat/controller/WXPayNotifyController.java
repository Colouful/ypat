package com.ypat.controller;

import com.ypat.BillQo;
import com.ypat.PubEventQo;
import com.ypat.service.BillServiceClient;
import com.ypat.service.MemberServiceClient;
import com.ypat.service.OrderServiceClient;
import com.ypat.service.PubEventServiceClient;
import com.ypat.third.wxpay.sdk.WXPayClient;
import com.ypat.third.wxpay.sdk.WXPayConstants;
import com.ypat.third.wxpay.sdk.WXPayUtil;
import com.ypat.util.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WXPayNotifyController {

    private static Logger logger = LoggerFactory.getLogger(WXPayNotifyController.class);
    @Autowired
    private WXPayClient wxPayClient;
    @Autowired
    private BillServiceClient billServiceClient;
    @Autowired
    private PubEventServiceClient pubEventServiceClient;
    @Autowired
    private MemberServiceClient memberServiceClient;

    @PostMapping(value = "/wxpay/notify", produces = {"application/xml;charset=UTF-8"})
    public void wxpayNotify(@RequestBody String data, HttpServletResponse response) throws Exception{
        String responseXml = null;
        Map<String, String> reps = new HashMap<>();
        String RETURN_CODE = "return_code";
        try {
            String requestXml = data;
            logger.info("微信支付回调参数："+requestXml);
            Map<String, String> req = wxPayClient.processResponseXml(requestXml);
            String return_code = req.get(RETURN_CODE);
            if(return_code.equals(WXPayConstants.SUCCESS)) {
                String outTradeNo = req.get("out_trade_no");
                if (outTradeNo != null && outTradeNo.startsWith("M")) {
                    // 切片 3 会员订单：通过 Feign 走 Feign 内部 markPaid，幂等
                    String transactionId = req.get("transaction_id");
                    Boolean granted = memberServiceClient.markPaid(outTradeNo, transactionId, System.currentTimeMillis());
                    logger.info("会员支付回调 outTradeNo={} granted={}", outTradeNo, granted);
                } else {
                    BillQo billQo = MapUtils.map2Java(BillQo.class, req);
                    billServiceClient.add(billQo);
                }
                reps.put(RETURN_CODE, WXPayConstants.SUCCESS);
            } else {
                throw new Exception("接收数据异常");
            }
        } catch (Exception e){
            logger.error("接收结果失败：", e);
            reps.put(RETURN_CODE, WXPayConstants.FAIL);
        } finally {
            try {
                responseXml = WXPayUtil.mapToXml(reps);
            } catch (Exception e1) {
                logger.error("mapToXml异常：", e1);
                reps.put(RETURN_CODE, WXPayConstants.FAIL);
            }
            logger.info("响应微信结果: "+ responseXml);
        }
        response.getWriter().println(responseXml);
        response.getWriter().flush();
    }

    private String readRequestData(HttpServletRequest request) throws IOException {
        StringBuffer sb = new StringBuffer() ;
        InputStream is = request.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String s = "" ;
        while((s=br.readLine())!=null){
            sb.append(s) ;
        }
        return sb.toString();
    }

    @GetMapping(value = "/wxpub/notify", produces = {"application/xml;charset=UTF-8"})
    public void authorize(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String echostr = request.getParameter("echostr");
        logger.info("微信公众号校验："+echostr);
        response.getWriter().println(echostr);
        response.getWriter().flush();
    }

    @PostMapping(value = "/wxpub/notify", produces = {"application/xml;charset=UTF-8"})
    public void wxpubNotify(@RequestBody String data, HttpServletRequest request, HttpServletResponse response) throws Exception{
        Map<String, String> reps = new HashMap<>();
        try {
            String requestXml = data;
            logger.info("微信公众号接收参数："+requestXml);
            Map<String, String> reqMap = MapUtils.xml2Map(requestXml);
            Map<String, String> req = new HashMap<>();
            for (Map.Entry<String, String> entry : reqMap.entrySet()) {
                req.put(StringUtils.uncapitalize(entry.getKey()), entry.getValue());
            }
            PubEventQo pubEventQo = MapUtils.map2Java(PubEventQo.class, req);
            pubEventServiceClient.add(pubEventQo);
        } catch (Exception e){
            logger.error("微信公众号接收失败：", e);
        }
    }
}
