package com.ypat.repository;

import com.ypat.entity.WorkLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkLikeRepository extends JpaRepository<WorkLike, Long> {

    boolean existsByWorkIdAndUserId(Long workId, Long userId);

    long countByWorkId(Long workId);

    @Modifying
    @Query("delete from WorkLike l where l.workId = :workId and l.userId = :userId")
    int deleteByWorkIdAndUserId(@Param("workId") Long workId, @Param("userId") Long userId);
}
