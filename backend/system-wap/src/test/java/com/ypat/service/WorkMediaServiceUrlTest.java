package com.ypat.service;

import com.ypat.storage.FastDfsStorageService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class WorkMediaServiceUrlTest {

    @Test
    public void joinsPublicBaseUrlAndFastDfsFileIdWithSingleSlash() {
        assertEquals(
                "https://panghu.work/files/group1/M00/00/00/a.jpg",
                FastDfsStorageService.joinPublicFileUrl("https://panghu.work/files", "group1/M00/00/00/a.jpg")
        );
        assertEquals(
                "https://panghu.work/files/group1/M00/00/00/a.jpg",
                FastDfsStorageService.joinPublicFileUrl("https://panghu.work/files/", "/group1/M00/00/00/a.jpg")
        );
        assertEquals(
                "https://fastdfs.panghu.work/group1/M00/00/00/a.jpg",
                FastDfsStorageService.joinPublicFileUrl("https://fastdfs.panghu.work/", "group1/M00/00/00/a.jpg")
        );
    }

    @Test
    public void extractsFastDfsFileIdFromNormalizedPublicUrl() {
        assertEquals(
                "group1/M00/00/00/a.jpg",
                FastDfsStorageService.extractFastDfsFileId(
                        "https://panghu.work/files/",
                        "https://panghu.work/files/group1/M00/00/00/a.jpg"
                )
        );
        assertEquals(
                "group1/M00/00/00/a.jpg",
                FastDfsStorageService.extractFastDfsFileId(
                        "https://panghu.work/files/",
                        "https://panghu.work/files/group1/M00/00/00/a.jpg?token=abc#preview"
                )
        );
        assertNull(FastDfsStorageService.extractFastDfsFileId(
                "https://panghu.work/files",
                "https://cdn.example.test/group1/M00/00/00/a.jpg"
        ));
        assertNull(FastDfsStorageService.extractFastDfsFileId(
                "https://panghu.work/files",
                "https://panghu.work/files-backup/group1/M00/00/00/a.jpg"
        ));
    }
}
