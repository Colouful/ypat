package com.ypat.storage;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CosObjectKeyFactoryTest {

    @Test
    public void createsEnvironmentAndBusinessScopedObjectKey() {
        CosObjectKeyFactory factory = new CosObjectKeyFactory("pro");
        String key = factory.createKey(StorageBizPath.WORK, "photo.jpeg", dateAtNoon(2026, Calendar.JULY, 6), "fixeduuid");

        assertEquals("pro/work/2026/07/06/fixeduuid.jpeg", key);
    }

    @Test
    public void fallsBackToJpgWhenOriginalFileHasNoExtension() {
        CosObjectKeyFactory factory = new CosObjectKeyFactory("dev");
        String key = factory.createKey(StorageBizPath.AVATAR, "avatar", dateAtNoon(2026, Calendar.JULY, 6), "abc");

        assertEquals("dev/avatar/2026/07/06/abc.jpg", key);
    }

    @Test
    public void extractsObjectKeyFromMatchingPublicUrl() {
        CosObjectKeyFactory factory = new CosObjectKeyFactory("pre");

        assertEquals(
                "pre/work/2026/07/06/a.jpg",
                factory.extractObjectKey("https://cdn.example.test/files/", "https://cdn.example.test/files/pre/work/2026/07/06/a.jpg")
        );
    }

    @Test
    public void returnsNullWhenPublicUrlDoesNotMatchBaseUrl() {
        CosObjectKeyFactory factory = new CosObjectKeyFactory("pre");

        assertNull(factory.extractObjectKey("https://cdn.example.test/files", "https://other.example.test/pre/work/a.jpg"));
    }

    @Test
    public void supportsMatchingPublicUrl() {
        CosObjectKeyFactory factory = new CosObjectKeyFactory("pre");

        assertTrue(factory.supportsUrl("https://cdn.example.test/files/", "https://cdn.example.test/files/pre/work/a.jpg"));
    }

    @Test
    public void rejectsBackupPathThatOnlySharesBaseUrlPrefix() {
        CosObjectKeyFactory factory = new CosObjectKeyFactory("pre");

        assertFalse(factory.supportsUrl(
                "https://cdn.example.test/files",
                "https://cdn.example.test/files-backup/pre/work/a.jpg"
        ));
    }

    @Test
    public void stripsQueryStringAndFragmentFromObjectKey() {
        CosObjectKeyFactory factory = new CosObjectKeyFactory("pre");

        assertEquals(
                "pre/work/2026/07/06/a.jpg",
                factory.extractObjectKey(
                        "https://cdn.example.test/files",
                        "https://cdn.example.test/files/pre/work/2026/07/06/a.jpg?sign=abc#preview"
                )
        );
    }

    private Date dateAtNoon(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, dayOfMonth, 12, 0, 0);
        return calendar.getTime();
    }
}
