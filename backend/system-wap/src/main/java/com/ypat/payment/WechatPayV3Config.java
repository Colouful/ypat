package com.ypat.payment;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.config.SystemConfig;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RSAPublicKeyNotificationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WechatPayV3Config {

    public static final String MODE_PUBLIC_KEY = "PUBLIC_KEY";

    private final SystemConfig systemConfig;

    @Autowired
    public WechatPayV3Config(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    public Config sdkConfig() {
        assertConfigured();
        return new RSAPublicKeyConfig.Builder()
                .merchantId(systemConfig.getWx_mchid())
                .merchantSerialNumber(systemConfig.getWx_mch_serial_no())
                .privateKeyFromPath(systemConfig.getWx_mch_private_key_path())
                .apiV3Key(systemConfig.getWx_api_v3_key())
                .publicKeyFromPath(systemConfig.getWx_pay_public_key_path())
                .publicKeyId(systemConfig.getWx_pay_public_key_id())
                .build();
    }

    public NotificationParser notificationParser() {
        assertConfigured();
        RSAPublicKeyNotificationConfig notifyConfig = new RSAPublicKeyNotificationConfig.Builder()
                .apiV3Key(systemConfig.getWx_api_v3_key())
                .publicKeyFromPath(systemConfig.getWx_pay_public_key_path())
                .publicKeyId(systemConfig.getWx_pay_public_key_id())
                .build();
        return new NotificationParser(notifyConfig);
    }

    public void assertConfigured() {
        String mode = systemConfig.getWx_pay_mode();
        if (hasText(mode) && !MODE_PUBLIC_KEY.equalsIgnoreCase(mode.trim())) {
            throw new SysException(ResponseCode.FAIL_PAY_CONFIG, "当前仅支持微信支付公钥模式");
        }
        require("YPAT_WX_APP_ID", systemConfig.getWx_appid());
        require("YPAT_WX_MCH_ID", systemConfig.getWx_mchid());
        require("YPAT_WX_MCH_SERIAL_NO", systemConfig.getWx_mch_serial_no());
        require("YPAT_WX_MCH_PRIVATE_KEY_PATH", systemConfig.getWx_mch_private_key_path());
        require("YPAT_WX_API_V3_KEY", systemConfig.getWx_api_v3_key());
        require("YPAT_WX_PAY_PUBLIC_KEY_ID", systemConfig.getWx_pay_public_key_id());
        require("YPAT_WX_PAY_PUBLIC_KEY_PATH", systemConfig.getWx_pay_public_key_path());
        require("YPAT_WX_NOTIFY_URL", systemConfig.getWx_notify_url());
    }

    public String miniappAppId() {
        require("YPAT_WX_APP_ID", systemConfig.getWx_appid());
        return systemConfig.getWx_appid();
    }

    public String h5AppId() {
        return hasText(systemConfig.getWx_h5_appid()) ? systemConfig.getWx_h5_appid() : miniappAppId();
    }

    public String mchId() {
        require("YPAT_WX_MCH_ID", systemConfig.getWx_mchid());
        return systemConfig.getWx_mchid();
    }

    public String notifyUrl() {
        require("YPAT_WX_NOTIFY_URL", systemConfig.getWx_notify_url());
        return systemConfig.getWx_notify_url();
    }

    public String h5SceneInfo() {
        return systemConfig.getWx_h5_scene_info();
    }

    private void require(String name, String value) {
        if (!hasText(value)) {
            throw new SysException(ResponseCode.FAIL_PAY_CONFIG, name + " 未配置");
        }
    }

    static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
