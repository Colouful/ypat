package com.ypat.repository;

import com.ypat.entity.MemberPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberPlanRepository extends JpaRepository<MemberPlan, Long>, JpaSpecificationExecutor<MemberPlan> {

    List<MemberPlan> findByStatusOrderBySortNoAsc(@Param("status") String status);

    MemberPlan findById(@Param("id") Long id);
}