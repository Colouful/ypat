package com.ypat.repository;

import com.ypat.entity.WorkFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkFavoriteRepository extends JpaRepository<WorkFavorite, Long> {

    boolean existsByWorkIdAndUserId(Long workId, Long userId);

    long countByWorkId(Long workId);

    Page<WorkFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying
    @Query("delete from WorkFavorite f where f.workId = :workId and f.userId = :userId")
    int deleteByWorkIdAndUserId(@Param("workId") Long workId, @Param("userId") Long userId);
}
