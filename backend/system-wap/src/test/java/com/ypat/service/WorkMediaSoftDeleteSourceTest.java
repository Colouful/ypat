package com.ypat.service;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkMediaSoftDeleteSourceTest {

    @Test
    public void workMediaEntityHasDeletedAtTimestamp() throws Exception {
        String source = read("backend/system-domain/src/main/java/com/ypat/entity/WorkMedia.java");

        assertTrue(source.contains("@Column(name = \"deleted_at\")"));
        assertTrue(source.contains("private Date deletedAt"));
        assertTrue(source.contains("getDeletedAt()"));
        assertTrue(source.contains("setDeletedAt(Date deletedAt)"));
    }

    @Test
    public void repositoryFiltersAndSoftDeletesMedia() throws Exception {
        String source = read("backend/system-domain/src/main/java/com/ypat/repository/WorkMediaRepository.java");

        assertTrue(source.contains("findByIdAndDeletedAtIsNull"));
        assertTrue(source.contains("findByWorkIdAndDeletedAtIsNullOrderBySortNoAsc"));
        assertTrue(source.contains("findByIdInAndUserIdAndDeletedAtIsNull"));
        assertTrue(source.contains("m.deletedAt is null"));
        assertTrue(source.contains("set m.deletedAt = :deletedAt"));
        assertTrue(source.contains("int softDeleteById"));
        assertFalse(source.contains("delete from WorkMedia"));
    }

    @Test
    public void workMediaDeleteUsesSoftDeleteOnly() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/service/WorkMediaService.java");

        assertTrue(source.contains("workMediaRepository.findByIdAndDeletedAtIsNull(mediaId)"));
        assertTrue(source.contains("workMediaRepository.softDeleteById(mediaId, new java.util.Date())"));
        assertFalse(source.contains("workMediaRepository.delete(media)"));
        assertFalse(source.contains("deleteFile("));
    }

    @Test
    public void workServiceOnlyUsesNonDeletedMedia() throws Exception {
        String source = read("backend/system-domain/src/main/java/com/ypat/service/WorkService.java");

        assertTrue(source.contains("findByIdInAndUserIdAndDeletedAtIsNull(mediaIds, userId)"));
        assertFalse(source.contains("findByIdInAndUserId(mediaIds, userId)"));
        assertTrue(source.contains("findByWorkIdAndDeletedAtIsNullOrderBySortNoAsc"));
        assertFalse(source.contains("findByWorkIdOrderBySortNoAsc"));
    }

    @Test
    public void migrationAddsDeletedAtColumn() throws Exception {
        String source = read("docs/sql/pending/V_cos_storage_switch_work_media_soft_delete.sql");

        assertTrue(source.contains("ALTER TABLE t_work_media"));
        assertTrue(source.contains("deleted_at"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get("../" + file);
        if (!Files.exists(path)) path = Paths.get("../../" + file);
        if (!Files.exists(path)) path = Paths.get("../../../" + file);
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
