package com.ypat.repository;

import com.ypat.entity.WorkComplain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkComplainRepository extends JpaRepository<WorkComplain, Long> {

    long countByWorkIdAndUserIdAndCreatedAtAfter(Long workId, Long userId, java.util.Date after);
}
