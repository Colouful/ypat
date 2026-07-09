package com.ypat.repository;

import com.ypat.entity.CheckinRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

public interface CheckinRecordRepository extends JpaRepository<CheckinRecord, Long>, JpaSpecificationExecutor<CheckinRecord> {
    CheckinRecord findByUseridAndCheckinDate(@Param("userid") Long userid, @Param("checkinDate") String checkinDate);
}
