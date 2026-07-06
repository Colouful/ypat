package com.ypat.storage;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CosObjectKeyFactoryTest {

    @Test
    public void createsEnvironmentAndBusinessScopedObjectKey() {
        CosObjectKeyFactory factory = new CosObjectKeyFactory("pro");
        String key = factory.createKey(StorageBizPath.WORK, "photo.jpeg", new Date(1783296000000L), "fixeduuid");

        assertEquals("pro/work/2026/07/06/fixeduuid.jpeg", key);
    }

    @Test
    public void fallsBackToJpgWhenOriginalFileHasNoExtension() {
        CosObjectKeyFactory factory = new CosObjectKeyFactory("dev");
        String key = factory.createKey(StorageBizPath.AVATAR, "avatar", new Date(1783296000000L), "abc");

        assertEquals("dev/avatar/2026/07/06/abc.jpg", key);
    }

    @Test
    public void stripsPublicBaseUrlToObjectKey() {
        CosObjectKeyFactory factory = new CosObjectKeyFactory("pre");

        assertEquals(
                "pre/work/2026/07/06/a.jpg",
                factory.extractObjectKey("https://cdn.example.test/files/", "https://cdn.example.test/files/pre/work/2026/07/06/a.jpg")
        );
        assertNull(factory.extractObjectKey("https://cdn.example.test/files", "https://other.example.test/pre/work/a.jpg"));
        assertTrue(factory.supportsUrl("https://cdn.example.test/files/", "https://cdn.example.test/files/pre/work/a.jpg"));
    }
}
