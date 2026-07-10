package com.ypat.repository;

import com.ypat.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long>, JpaSpecificationExecutor<Feedback> {
    @Modifying
    @Query("update Feedback f set f.status = :status, f.handleReason = :handleReason, f.handledBy = :handledBy, f.handledAt = :handledAt, f.upddate = :updatedAt where f.id = :id and f.status = '0'")
    int updatePendingStatus(@Param("id") Long id,
                            @Param("status") String status,
                            @Param("handleReason") String handleReason,
                            @Param("handledBy") Long handledBy,
                            @Param("handledAt") Date handledAt,
                            @Param("updatedAt") Date updatedAt);
}
