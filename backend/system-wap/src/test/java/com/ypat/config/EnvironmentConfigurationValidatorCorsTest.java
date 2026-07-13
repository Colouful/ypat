package com.ypat.config;

import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * PR-02: lock down CORS configuration.
 *
 * YPAT_CORS_ORIGINS used to default to "*" in WebSecurityConfig,
 * which means a misconfigured deploy silently allowed every origin.
 * This test exercises EnvironmentConfigurationValidator directly
 * — no Spring boot, no MySQL, no Eureka — so it stays fast and
 * deterministic.
 */
public class EnvironmentConfigurationValidatorCorsTest {

    private final EnvironmentConfigurationValidator validator =
            new EnvironmentConfigurationValidator();

    @Test
    public void devProfileAcceptsMissingCorsOrigins() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        // No YPAT_CORS_ORIGINS set.
        validator.postProcessEnvironment(env, new SpringApplication());
        // No throw = pass.
    }

    @Test
    public void devProfileAcceptsWildcardCorsOrigins() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        env.setProperty("YPAT_CORS_ORIGINS", "*");
        validator.postProcessEnvironment(env, new SpringApplication());
    }

    @Test
    public void stagingProfileRejectsMissingCorsOrigins() {
        MockEnvironment env = stagingBaseline();
        try {
            validator.postProcessEnvironment(env, new SpringApplication());
            fail("staging must reject missing YPAT_CORS_ORIGINS");
        } catch (IllegalStateException e) {
            assertEquals(
                    "YPAT EnvironmentConfigurationValidator: [staging] requires YPAT_CORS_ORIGINS "
                            + "to be set. Aborting startup to prevent misconfiguration.",
                    e.getMessage());
        }
    }

    @Test
    public void stagingProfileRejectsWildcardCorsOrigins() {
        MockEnvironment env = stagingBaseline();
        env.setProperty("YPAT_CORS_ORIGINS", "*");
        try {
            validator.postProcessEnvironment(env, new SpringApplication());
            fail("staging must reject wildcard CORS");
        } catch (IllegalStateException e) {
            assertNotNull(e.getMessage());
            if (!e.getMessage().contains("YPAT_CORS_ORIGINS='*'")) {
                fail("Expected message about wildcard, got: " + e.getMessage());
            }
        }
    }

    @Test
    public void stagingProfileRejectsWildcardInList() {
        MockEnvironment env = stagingBaseline();
        env.setProperty("YPAT_CORS_ORIGINS", "https://app.example.com,*");
        try {
            validator.postProcessEnvironment(env, new SpringApplication());
            fail("staging must reject wildcard inside a comma list");
        } catch (IllegalStateException e) {
            if (!e.getMessage().contains("wildcard")) {
                fail("Expected message about wildcard token, got: " + e.getMessage());
            }
        }
    }

    @Test
    public void productionProfileRejectsWildcardCorsOrigins() {
        MockEnvironment env = productionBaseline();
        env.setProperty("YPAT_CORS_ORIGINS", "*");
        try {
            validator.postProcessEnvironment(env, new SpringApplication());
            fail("production must reject wildcard CORS");
        } catch (IllegalStateException e) {
            if (!e.getMessage().contains("[production]")) {
                fail("Expected [production] in message, got: " + e.getMessage());
            }
        }
    }

    @Test
    public void stagingProfileAcceptsExplicitOriginList() {
        MockEnvironment env = stagingBaseline();
        env.setProperty("YPAT_CORS_ORIGINS", "https://staging.example.com,https://admin.example.com");
        validator.postProcessEnvironment(env, new SpringApplication());
    }

    @Test
    public void productionProfileAcceptsExplicitOriginList() {
        MockEnvironment env = productionBaseline();
        env.setProperty("YPAT_CORS_ORIGINS", "https://app.example.com");
        validator.postProcessEnvironment(env, new SpringApplication());
    }

    // ── helpers ────────────────────────────────────────────────

    /**
     * staging baseline = every required key filled with a safe non-staging,
     * non-placeholder value. Validator will only complain about whatever
     * the test deliberately leaves out or breaks.
     */
    private MockEnvironment stagingBaseline() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("pre");
        env.setProperty("YPAT_MYSQL_URL", "jdbc:mysql://mysql:3306/ypat");
        env.setProperty("YPAT_MYSQL_USERNAME", "ypat");
        env.setProperty("YPAT_MYSQL_PASSWORD", "ypat-pass");
        env.setProperty("YPAT_REDIS_HOST", "redis");
        env.setProperty("YPAT_REDIS_PASSWORD", "redis-pass");
        env.setProperty("YPAT_EUREKA_DEFAULT_ZONE", "http://eureka:8761/eureka/");
        env.setProperty("YPAT_SSO_JWT_SIGNING_KEY", "staging-signing-key-32-chars-min");
        env.setProperty("YPAT_FDFS_PUBLIC_BASE_URL", "https://files.staging.example.com");
        env.setProperty("YPAT_LOCAL_MYSQL_ROOT_PASSWORD", "root");
        env.setProperty("YPAT_LOCAL_REDIS_PASSWORD", "redis-pass");
        return env;
    }

    /**
     * production baseline = staging baseline + the four production-only
     * keys. Same approach: fill the safe default, leave the test
     * variable free to break.
     */
    private MockEnvironment productionBaseline() {
        MockEnvironment env = stagingBaseline();
        env.setActiveProfiles("pro");
        env.setProperty("YPAT_DB_NAME", "ypat");
        env.setProperty("YPAT_DB_USERNAME", "ypat");
        env.setProperty("YPAT_DB_PASSWORD", "ypat-pass");
        env.setProperty("YPAT_FDFS_TRACKER_SERVERS", "fdfs-tracker-1:22122");
        env.setProperty("YPAT_FASTDFS_IMAGE", "fastdfs-image:1.0");
        env.setProperty("YPAT_FASTDFS_TRACKER_DATA_DIR", "/var/fdfs/tracker");
        env.setProperty("YPAT_FASTDFS_STORAGE_DATA_DIR", "/var/fdfs/storage");
        return env;
    }
}