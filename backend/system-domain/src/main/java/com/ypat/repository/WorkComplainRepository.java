package com.ypat.repository;

import com.ypat.entity.WorkComplain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkComplainRepository extends JpaRepository<WorkComplain, Long>, JpaSpecificationExecutor<WorkComplain> {

    long countByWorkIdAndUserIdAndCreatedAtAfter(Long workId, Long userId, java.util.Date after);

    @Modifying
    @Query("update WorkComplain c set c.status = :status where c.id = :id and c.status = '0'")
    int updatePendingStatus(@Param("id") Long id, @Param("status") String status);
}
