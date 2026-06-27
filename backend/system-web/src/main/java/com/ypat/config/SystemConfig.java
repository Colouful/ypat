package com.ypat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "system.web.third")
@Component
public class SystemConfig {
    private String bd_api;
    private String bd_ak;
    private String bd_sk;
    public static final String fdfs_path = "http://127.0.0.1:8888/";

    public String getBd_api() {
        return bd_api;
    }

    public void setBd_api(String bd_api) {
        this.bd_api = bd_api;
    }

    public String getBd_ak() {
        return bd_ak;
    }

    public void setBd_ak(String bd_ak) {
        this.bd_ak = bd_ak;
    }

    public String getBd_sk() {
        return bd_sk;
    }

    public void setBd_sk(String bd_sk) {
        this.bd_sk = bd_sk;
    }
}
