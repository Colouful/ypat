package com.ypat.util;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FastDFSClientStorageServerConfigTest {

    @Test
    public void parsesExplicitStorageServerOverride() {
        Properties properties = new Properties();
        properties.setProperty("fastdfs.storage_server", "47.116.6.104:23000");
        properties.setProperty("fastdfs.storage_path_index", "0");

        FastDFSClient.StorageServerConfig config = FastDFSClient.parseStorageServerConfig(properties);

        assertEquals("47.116.6.104", config.getHost());
        assertEquals(23000, config.getPort());
        assertEquals(0, config.getStorePathIndex());
    }

    @Test
    public void ignoresBlankStorageServerOverride() {
        Properties properties = new Properties();
        properties.setProperty("fastdfs.storage_server", " ");

        assertNull(FastDFSClient.parseStorageServerConfig(properties));
    }
}
