package com.ypat.repository;

import com.ypat.entity.WorkMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkMediaRepository extends JpaRepository<WorkMedia, Long> {

    /** 按 workId 升序查全部媒体 */
    List<WorkMedia> findByWorkIdAndDeletedAtIsNullOrderBySortNoAsc(Long workId);

    /** 按 ID 查未删除媒体 */
    WorkMedia findByIdAndDeletedAtIsNull(Long id);

    /** 按 workId 软删全部媒体（提交时清理） */
    @Modifying
    @Query("update WorkMedia m set m.deletedAt = :deletedAt where m.workId = :workId and m.deletedAt is null")
    int deleteByWorkId(@Param("workId") Long workId, @Param("deletedAt") java.util.Date deletedAt);

    /** 按 ID 软删除媒体 */
    @Modifying
    @Query("update WorkMedia m set m.deletedAt = :deletedAt where m.id = :id and m.deletedAt is null")
    int softDeleteById(@Param("id") Long id, @Param("deletedAt") java.util.Date deletedAt);

    /** 绑定 workId 到媒体（孤儿媒体在提交时绑定） */
    @Modifying
    @Query("update WorkMedia m set m.workId = :workId where m.id in :ids and m.userId = :userId and m.workId is null and m.deletedAt is null")
    int bindWorkId(@Param("workId") Long workId, @Param("ids") List<Long> ids, @Param("userId") Long userId);

    /** 累计文件大小（提交时校验） */
    @Query("select coalesce(sum(m.fileSize),0) from WorkMedia m where m.id in :ids and m.deletedAt is null")
    Long sumFileSizeByIds(@Param("ids") List<Long> ids);

    /** 按 ID 列表查（校验归属） */
    List<WorkMedia> findByIdInAndUserIdAndDeletedAtIsNull(List<Long> ids, Long userId);

    /** 孤儿媒体清理（24h 前） */
    @Query("select m from WorkMedia m where m.workId is null and m.createdAt < :threshold and m.deletedAt is null")
    List<WorkMedia> findOrphansOlderThan(@Param("threshold") java.util.Date threshold);
}
