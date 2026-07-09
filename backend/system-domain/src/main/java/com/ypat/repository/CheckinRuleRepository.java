package com.ypat.repository;

import com.ypat.entity.CheckinRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CheckinRuleRepository extends JpaRepository<CheckinRule, Long>, JpaSpecificationExecutor<CheckinRule> {
    CheckinRule findTopByOrderByIdAsc();
}
