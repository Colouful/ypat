package com.ypat.storage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CosStoragePropertiesTest {

    @Test
    public void providerDefaultsToFastdfsWhenBlank() {
        CosStorageProperties properties = new CosStorageProperties();
        assertEquals("fastdfs", properties.normalizedProvider());
        assertFalse(properties.isCosEnabled());
    }

    @Test
    public void cosProviderRequiresAllConnectionFields() {
        CosStorageProperties properties = new CosStorageProperties();
        properties.setProvider("cos");
        properties.setSecretId("sid");
        properties.setSecretKey("skey");
        properties.setRegion("ap-guangzhou");
        properties.setBucket("ypat-1250000000");
        properties.setPublicBaseUrl("https://cdn.example.test");
        properties.setEnvPrefix("pro");

        assertTrue(properties.isCosEnabled());
        properties.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void cosProviderFailsWhenSecretKeyMissing() {
        CosStorageProperties properties = new CosStorageProperties();
        properties.setProvider("cos");
        properties.setSecretId("sid");
        properties.setRegion("ap-guangzhou");
        properties.setBucket("ypat-1250000000");
        properties.setPublicBaseUrl("https://cdn.example.test");
        properties.setEnvPrefix("pro");

        properties.validate();
    }
}
