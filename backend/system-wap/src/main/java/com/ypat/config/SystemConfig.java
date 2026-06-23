package com.ypat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource(value = "classpath:conf/sys_conf.properties")
@ConfigurationProperties(prefix = "system.third")
@Component
public class SystemConfig {
    private String bd_api_idcard;
    private String bd_ak_idcard;
    private String bd_sk_idcard;

    private String bd_api_idmatch;
    private String bd_ak_idmatch;
    private String bd_sk_idmatch;

    private String wx_appid;
    private String wx_appsecret;
    private String wx_mchid;
    private String wx_key;
    private String fdfs_path;

    private String bd_key;
    private String bd_secret;

    private String wx_pub_appid;
    private String wx_pub_appsecret;

    public String getBd_key() {
        return bd_key;
    }

    public void setBd_key(String bd_key) {
        this.bd_key = bd_key;
    }

    public String getBd_secret() {
        return bd_secret;
    }

    public void setBd_secret(String bd_secret) {
        this.bd_secret = bd_secret;
    }

    public String getBd_api_idcard() {
        return bd_api_idcard;
    }

    public void setBd_api_idcard(String bd_api_idcard) {
        this.bd_api_idcard = bd_api_idcard;
    }

    public String getBd_api_idmatch() {
        return bd_api_idmatch;
    }

    public void setBd_api_idmatch(String bd_api_idmatch) {
        this.bd_api_idmatch = bd_api_idmatch;
    }

    public String getBd_ak_idcard() {
        return bd_ak_idcard;
    }

    public void setBd_ak_idcard(String bd_ak_idcard) {
        this.bd_ak_idcard = bd_ak_idcard;
    }

    public String getBd_sk_idcard() {
        return bd_sk_idcard;
    }

    public void setBd_sk_idcard(String bd_sk_idcard) {
        this.bd_sk_idcard = bd_sk_idcard;
    }

    public String getBd_ak_idmatch() {
        return bd_ak_idmatch;
    }

    public void setBd_ak_idmatch(String bd_ak_idmatch) {
        this.bd_ak_idmatch = bd_ak_idmatch;
    }

    public String getBd_sk_idmatch() {
        return bd_sk_idmatch;
    }

    public void setBd_sk_idmatch(String bd_sk_idmatch) {
        this.bd_sk_idmatch = bd_sk_idmatch;
    }

    public String getWx_appid() {
        return wx_appid;
    }

    public void setWx_appid(String wx_appid) {
        this.wx_appid = wx_appid;
    }

    public String getWx_appsecret() {
        return wx_appsecret;
    }

    public void setWx_appsecret(String wx_appsecret) {
        this.wx_appsecret = wx_appsecret;
    }

    public String getWx_mchid() {
        return wx_mchid;
    }

    public void setWx_mchid(String wx_mchid) {
        this.wx_mchid = wx_mchid;
    }

    public String getWx_key() {
        return wx_key;
    }

    public void setWx_key(String wx_key) {
        this.wx_key = wx_key;
    }

    public String getFdfs_path() {
        return fdfs_path;
    }

    public void setFdfs_path(String fdfs_path) {
        this.fdfs_path = fdfs_path;
    }

    public String getWx_pub_appid() {
        return wx_pub_appid;
    }

    public void setWx_pub_appid(String wx_pub_appid) {
        this.wx_pub_appid = wx_pub_appid;
    }

    public String getWx_pub_appsecret() {
        return wx_pub_appsecret;
    }

    public void setWx_pub_appsecret(String wx_pub_appsecret) {
        this.wx_pub_appsecret = wx_pub_appsecret;
    }

    @Override
    public String toString() {
        return "SystemConfig{" +
                "bd_api_idcard='" + bd_api_idcard + '\'' +
                ", bd_ak_idcard='" + bd_ak_idcard + '\'' +
                ", bd_sk_idcard='" + bd_sk_idcard + '\'' +
                ", bd_api_idmatch='" + bd_api_idmatch + '\'' +
                ", bd_ak_idmatch='" + bd_ak_idmatch + '\'' +
                ", bd_sk_idmatch='" + bd_sk_idmatch + '\'' +
                ", wx_appid='" + wx_appid + '\'' +
                ", wx_appsecret='" + wx_appsecret + '\'' +
                ", wx_mchid='" + wx_mchid + '\'' +
                ", wx_key='" + wx_key + '\'' +
                ", fdfs_path='" + fdfs_path + '\'' +
                ", bd_key='" + bd_key + '\'' +
                ", bd_secret='" + bd_secret + '\'' +
                ", wx_pub_appid='" + wx_pub_appid + '\'' +
                ", wx_pub_appsecret='" + wx_pub_appsecret + '\'' +
                '}';
    }
}
