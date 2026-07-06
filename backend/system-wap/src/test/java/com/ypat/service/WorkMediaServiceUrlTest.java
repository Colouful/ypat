package com.ypat.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class WorkMediaServiceUrlTest {

    @Test
    public void joinsPublicBaseUrlAndFastDfsFileIdWithSingleSlash() {
        assertEquals(
                "https://panghu.work/files/group1/M00/00/00/a.jpg",
                WorkMediaService.joinPublicFileUrl("https://panghu.work/files", "group1/M00/00/00/a.jpg")
        );
        assertEquals(
                "https://panghu.work/files/group1/M00/00/00/a.jpg",
                WorkMediaService.joinPublicFileUrl("https://panghu.work/files/", "/group1/M00/00/00/a.jpg")
        );
        assertEquals(
                "https://fastdfs.panghu.work/group1/M00/00/00/a.jpg",
                WorkMediaService.joinPublicFileUrl("https://fastdfs.panghu.work/", "group1/M00/00/00/a.jpg")
        );
    }

    @Test
    public void extractsFastDfsFileIdFromNormalizedPublicUrl() {
        assertEquals(
                "group1/M00/00/00/a.jpg",
                WorkMediaService.extractFastDfsFileId(
                        "https://panghu.work/files/",
                        "https://panghu.work/files/group1/M00/00/00/a.jpg"
                )
        );
        assertNull(WorkMediaService.extractFastDfsFileId(
                "https://panghu.work/files",
                "https://cdn.example.test/group1/M00/00/00/a.jpg"
        ));
        assertNull(WorkMediaService.extractFastDfsFileId(
                "https://panghu.work/files",
                "https://panghu.work/files-backup/group1/M00/00/00/a.jpg"
        ));
    }
}
