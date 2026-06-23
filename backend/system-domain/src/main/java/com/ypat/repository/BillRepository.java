package com.ypat.repository;


import com.ypat.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface BillRepository extends JpaRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {
    Bill findById(@Param("id") Long id);
}
