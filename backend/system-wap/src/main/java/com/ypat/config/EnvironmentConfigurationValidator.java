package com.ypat.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * YPAT 环境配置启动时校验器（Spring Boot 1.5.9 兼容）。
 *
 * 规则：
 *   - development：允许本地默认值（如 localhost）
 *   - staging：关键变量为空则启动失败
 *   - production：任何关键变量为空则启动失败；发现 staging 域名则启动失败
 *
 * 启动注册：src/main/resources/META-INF/spring.factories
 *   org.springframework.boot.env.EnvironmentPostProcessor=\
 *     com.ypat.config.EnvironmentConfigurationValidator
 *
 * 设计依据：Spring Boot 1.5.9.RELEASE 不支持直接 ${VAR:?msg} 形式校验空值，
 * 必须在启动时主动读取并校验，避免 prod 启动后才发现关键变量缺失。
 */
public class EnvironmentConfigurationValidator implements EnvironmentPostProcessor {

    private static final String DEV_PROFILE = "dev";
    private static final String STAGING_PROFILE = "pre";
    private static final String PRODUCTION_PROFILE = "pro";

    private static final Set<String> STAGING_HOSTS = new HashSet<>(Arrays.asList(
            "panghu.work",
            "www.panghu.work",
            "82.156.14.216"
    ));

    private static final Set<String> PRODUCTION_FORBIDDEN_HOSTS = new HashSet<>(Arrays.asList(
            "panghu.work",
            "www.panghu.work",
            "82.156.14.216",
            "localhost",
            "127.0.0.1"
    ));

    private static final Set<String> REQUIRED_FOR_STAGING = new HashSet<>(Arrays.asList(
            "YPAT_MYSQL_URL",
            "YPAT_MYSQL_USERNAME",
            "YPAT_MYSQL_PASSWORD",
            "YPAT_REDIS_HOST",
            "YPAT_REDIS_PASSWORD",
            "YPAT_EUREKA_DEFAULT_ZONE",
            "YPAT_SSO_JWT_SIGNING_KEY",
            "YPAT_FDFS_PUBLIC_BASE_URL",
            "YPAT_LOCAL_MYSQL_ROOT_PASSWORD",
            "YPAT_LOCAL_REDIS_PASSWORD"
    ));

    private static final Set<String> REQUIRED_FOR_PRODUCTION = new HashSet<>(REQUIRED_FOR_STAGING);

    static {
        REQUIRED_FOR_PRODUCTION.add("YPAT_DB_NAME");
        REQUIRED_FOR_PRODUCTION.add("YPAT_DB_USERNAME");
        REQUIRED_FOR_PRODUCTION.add("YPAT_DB_PASSWORD");
        REQUIRED_FOR_PRODUCTION.add("YPAT_FDFS_TRACKER_SERVERS");
        REQUIRED_FOR_PRODUCTION.add("YPAT_FASTDFS_IMAGE");
        REQUIRED_FOR_PRODUCTION.add("YPAT_FASTDFS_TRACKER_DATA_DIR");
        REQUIRED_FOR_PRODUCTION.add("YPAT_FASTDFS_STORAGE_DATA_DIR");
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
        String[] activeProfiles = env.getActiveProfiles();
        if (activeProfiles.length == 0) {
            // 兜底：如果没显式 -Dspring.profiles.active=...
            String defaultProfiles = env.getProperty("spring.profiles.active");
            if (defaultProfiles == null || defaultProfiles.isEmpty()) {
                return; // 视为 development
            }
            activeProfiles = defaultProfiles.split(",");
        }

        String profile = activeProfiles[0].trim().toLowerCase();

        if (DEV_PROFILE.equals(profile)) {
            // 开发环境：放行
            return;
        }

        if (STAGING_PROFILE.equals(profile)) {
            validateRequired(env, REQUIRED_FOR_STAGING, "staging");
            validateNoProductionForbidden(env, profile);
            return;
        }

        if (PRODUCTION_PROFILE.equals(profile)) {
            validateRequired(env, REQUIRED_FOR_PRODUCTION, "production");
            validateNoProductionForbidden(env, profile);
            validateNoStagingHosts(env, profile);
            return;
        }

        // 未知 profile 不阻止启动（Spring 会自己报 profile 不存在的错）
    }

    private void validateRequired(ConfigurableEnvironment env, Set<String> keys, String envName) {
        for (String key : keys) {
            String value = env.getProperty(key);
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalStateException(
                        "YPAT EnvironmentConfigurationValidator: [" + envName + "] requires " + key
                                + " to be set. Aborting startup to prevent misconfiguration.");
            }
            if (isPlaceholder(value)) {
                throw new IllegalStateException(
                        "YPAT EnvironmentConfigurationValidator: [" + envName + "] " + key
                                + " is still a placeholder: '" + value + "'. Aborting startup.");
            }
        }
    }

    private void validateNoProductionForbidden(ConfigurableEnvironment env, String profile) {
        // 生产/预发都禁止裸 localhost / 127.0.0.1 / placeholder
        String[] checked = {"YPAT_MYSQL_URL", "YPAT_EUREKA_DEFAULT_ZONE", "YPAT_FDFS_PUBLIC_BASE_URL"};
        for (String key : checked) {
            String value = env.getProperty(key);
            if (value == null) continue;
            if (value.contains("localhost") || value.contains("127.0.0.1")) {
                throw new IllegalStateException(
                        "YPAT EnvironmentConfigurationValidator: [" + profile + "] " + key
                                + " must not reference localhost/127.0.0.1, got: " + value);
            }
        }
    }

    private void validateNoStagingHosts(ConfigurableEnvironment env, String profile) {
        if (!PRODUCTION_PROFILE.equals(profile)) return;
        for (String key : new String[]{"YPAT_FDFS_PUBLIC_BASE_URL", "YPAT_EUREKA_DEFAULT_ZONE", "YPAT_MYSQL_URL"}) {
            String value = env.getProperty(key);
            if (value == null) continue;
            for (String forbidden : STAGING_HOSTS) {
                if (value.contains(forbidden)) {
                    throw new IllegalStateException(
                            "YPAT EnvironmentConfigurationValidator: [production] " + key
                                    + " references forbidden staging host '" + forbidden + "'. Production must be independent.");
                }
            }
        }
        // 直接检查 YPAT_FDFS_TRACKER_SERVERS
        String trackerServers = env.getProperty("YPAT_FDFS_TRACKER_SERVERS");
        if (trackerServers != null) {
            for (String forbidden : PRODUCTION_FORBIDDEN_HOSTS) {
                if (trackerServers.contains(forbidden)) {
                    throw new IllegalStateException(
                            "YPAT EnvironmentConfigurationValidator: [production] YPAT_FDFS_TRACKER_SERVERS references forbidden host '"
                                    + forbidden + "': " + trackerServers);
                }
            }
        }
    }

    private boolean isPlaceholder(String value) {
        if (value == null) return true;
        String trimmed = value.trim();
        return trimmed.isEmpty()
                || trimmed.equalsIgnoreCase("CHANGE_ME")
                || trimmed.equalsIgnoreCase("placeholder")
                || trimmed.equalsIgnoreCase("TODO")
                || trimmed.equalsIgnoreCase("<placeholder>")
                || trimmed.endsWith(".example.invalid");
    }
}