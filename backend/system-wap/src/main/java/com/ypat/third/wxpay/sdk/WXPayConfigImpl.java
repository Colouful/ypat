package com.ypat.third.wxpay.sdk;

import com.ypat.config.SystemConfig;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class WXPayConfigImpl extends WXPayConfig {
    private byte[] certData;
    private String appID = "";
    private String appSecret = "";
    private String mchID = "";
    private String key = "";

    public WXPayConfigImpl(SystemConfig systemConfig) {
        this.appID = systemConfig.getWx_appid();
        this.appSecret = systemConfig.getWx_appsecret();
        this.mchID = systemConfig.getWx_mchid();
        this.key = systemConfig.getWx_key();
        /**
         * 下单接口 不需要证书
         String certPath = "/path/to/apiclient_cert.p12";
         File file = new File(certPath);
         InputStream certStream = new FileInputStream(file);
         this.certData = new byte[(int) file.length()];
         certStream.read(this.certData);
         certStream.close();
         */
    }

    @Override
    public String getAppID() {
        return this.appID;
    }

    @Override
    public String getMchID() {
        return this.mchID;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    public String getAppSecret() {
        return appSecret;
    }

    @Override
    public InputStream getCertStream() {
        ByteArrayInputStream certBis = new ByteArrayInputStream(this.certData);
        return certBis;
    }

    @Override
    public IWXPayDomain getWXPayDomain() {
        IWXPayDomain iwxPayDomain = new IWXPayDomain() {
            @Override
            public void report(String domain, long elapsedTimeMillis, Exception ex) {
            }
            @Override
            public DomainInfo getDomain(WXPayConfig config) {
                return new IWXPayDomain.DomainInfo(WXPayConstants.DOMAIN_API, true);
            }
        };
        return iwxPayDomain;
    }
}
