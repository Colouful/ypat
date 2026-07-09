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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        List<String> missing = new ArrayList<>();
        collectMissing(missing, "YPAT_WX_APP_ID", systemConfig.getWx_appid());
        collectMissing(missing, "YPAT_WX_MCH_ID", systemConfig.getWx_mchid());
        collectMissing(missing, "YPAT_WX_MCH_SERIAL_NO", systemConfig.getWx_mch_serial_no());
        collectMissing(missing, "YPAT_WX_MCH_PRIVATE_KEY_PATH", systemConfig.getWx_mch_private_key_path());
        collectMissing(missing, "YPAT_WX_API_V3_KEY", systemConfig.getWx_api_v3_key());
        collectMissing(missing, "YPAT_WX_PAY_PUBLIC_KEY_ID", systemConfig.getWx_pay_public_key_id());
        collectMissing(missing, "YPAT_WX_PAY_PUBLIC_KEY_PATH", systemConfig.getWx_pay_public_key_path());
        collectMissing(missing, "YPAT_WX_NOTIFY_URL", systemConfig.getWx_notify_url());
        if (!missing.isEmpty()) {
            throw new SysException(ResponseCode.FAIL_PAY_CONFIG, String.join(", ", missing) + " 未配置");
        }
        if (shouldValidatePaymentKeyFiles()) {
            List<String> unreadable = new ArrayList<>();
            collectUnreadable(unreadable, "YPAT_WX_MCH_PRIVATE_KEY_PATH", systemConfig.getWx_mch_private_key_path());
            collectUnreadable(unreadable, "YPAT_WX_PAY_PUBLIC_KEY_PATH", systemConfig.getWx_pay_public_key_path());
            if (!unreadable.isEmpty()) {
                throw new SysException(ResponseCode.FAIL_PAY_CONFIG,
                        String.join(", ", unreadable) + " 文件不存在或不可读");
            }
        }
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

    private void collectMissing(List<String> missing, String name, String value) {
        if (!hasText(value)) {
            missing.add(name);
        }
    }

    private void collectUnreadable(List<String> unreadable, String name, String path) {
        File file = new File(path);
        if (!file.isFile() || !file.canRead()) {
            unreadable.add(name);
        }
    }

    protected boolean shouldValidatePaymentKeyFiles() {
        return true;
    }

    static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
