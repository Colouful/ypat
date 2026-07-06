package com.ypat.repository;

import com.ypat.entity.Work;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkRepository extends JpaRepository<Work, Long>, JpaSpecificationExecutor<Work> {

    /**
     * 用户作品列表（按 ID 倒序）
     */
    Page<Work> findByUseridAndDeletedFlagOrderByIdDesc(Long userid, Integer deletedFlag, Pageable pageable);

    /**
     * 我的作品（按状态过滤）
     */
    Page<Work> findByUseridAndStatusAndDeletedFlagOrderByIdDesc(Long userid, String status, Integer deletedFlag, Pageable pageable);

    /**
     * 详情查询：仅展示未下架未删除 + 已通过
     */
    Work findByIdAndStatusAndDeletedFlag(Long id, String status, Integer deletedFlag);

    /**
     * 任意状态（用户查看自己作品时）
     */
    Work findByIdAndUseridAndDeletedFlag(Long id, Long userid, Integer deletedFlag);

    /**
     * 原子 +1 阅读量
     */
    @Modifying
    @Query("update Work w set w.readCount = w.readCount + 1 where w.id = :id and w.deletedFlag = 0")
    int incrReadCount(@Param("id") Long id);

    /**
     * 原子 +1 点赞数（不低于 0）
     */
    @Modifying
    @Query("update Work w set w.likeCount = w.likeCount + 1 where w.id = :id and w.deletedFlag = 0")
    int incrLikeCount(@Param("id") Long id);

    // Hibernate 5.0 update HQL 不支持 case 里做算术，改用 where 条件保护：
    // likeCount > 0 时才减，为 0 时 update 影响 0 行，逻辑等价
    @Modifying
    @Query("update Work w set w.likeCount = w.likeCount - 1 where w.id = :id and w.likeCount > 0 and w.deletedFlag = 0")
    int decrLikeCount(@Param("id") Long id);

    /**
     * 原子 +1 收藏数
     */
    @Modifying
    @Query("update Work w set w.favoriteCount = w.favoriteCount + 1 where w.id = :id and w.deletedFlag = 0")
    int incrFavoriteCount(@Param("id") Long id);

    // 见 decrLikeCount 说明
    @Modifying
    @Query("update Work w set w.favoriteCount = w.favoriteCount - 1 where w.id = :id and w.favoriteCount > 0 and w.deletedFlag = 0")
    int decrFavoriteCount(@Param("id") Long id);

    /**
     * 修改状态（下架 / 审核）
     */
    @Modifying
    @Query("update Work w set w.status = :status, w.updatedAt = CURRENT_TIMESTAMP where w.id = :id and w.deletedFlag = 0")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Modifying
    @Query("update Work w set w.status = :status, w.auditReason = :auditReason, w.updatedAt = CURRENT_TIMESTAMP where w.id = :id and w.deletedFlag = 0")
    int updateStatusAndAuditReason(@Param("id") Long id, @Param("status") String status, @Param("auditReason") String auditReason);

    /**
     * 公开可见数量（统计）
     */
    long countByStatusAndDeletedFlag(String status, Integer deletedFlag);

    @Modifying
    @Query("update Work w set w.status = :status, w.auditReason = :reason, w.updatedAt = CURRENT_TIMESTAMP where w.dataFlag = 'internal_test' and w.deletedFlag = 0 and (:batchNo is null or w.internalBatchNo = :batchNo)")
    int updateInternalTestWorkStatus(@Param("batchNo") String batchNo, @Param("status") String status, @Param("reason") String reason);

    @Modifying
    @Query("update Work w set w.status = :status, w.auditReason = :reason, w.updatedAt = CURRENT_TIMESTAMP where w.dataFlag = 'internal_test' and w.deletedFlag = 0 and w.userid in :userIds and (:batchNo is null or w.internalBatchNo = :batchNo)")
    int updateInternalTestWorkStatusByUserIds(@Param("userIds") java.util.List<Long> userIds, @Param("batchNo") String batchNo, @Param("status") String status, @Param("reason") String reason);
}
