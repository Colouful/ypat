package com.ypat.repository;

import com.ypat.entity.PubEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PubEventRepository extends JpaRepository<PubEvent, Long>, JpaSpecificationExecutor<PubEvent> {

    public PubEvent findByDateStrAndEventAndEventKey(@Param("dateStr") String dateStr,
                                          @Param("event") String event,
                                          @Param("eventKey") String eventKey);
}
