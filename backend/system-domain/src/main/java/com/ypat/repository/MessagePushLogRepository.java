package com.ypat.repository;

import com.ypat.entity.MessagePushLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MessagePushLogRepository extends JpaRepository<MessagePushLog, Long>, JpaSpecificationExecutor<MessagePushLog> {
}
