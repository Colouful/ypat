package com.ypat.repository;


import com.ypat.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface RecordRepository extends JpaRepository<Record, Long>, JpaSpecificationExecutor<Record> {
    Record findById(@Param("id") Long id);
}
