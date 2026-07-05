package com.ypat.repository;

import com.ypat.entity.MemberOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MemberOperationLogRepository extends JpaRepository<MemberOperationLog, Long>, JpaSpecificationExecutor<MemberOperationLog> {
}
