package com.ypat.repository;

import com.ypat.entity.MemberBenefitRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberBenefitRuleRepository extends JpaRepository<MemberBenefitRule, Long>, JpaSpecificationExecutor<MemberBenefitRule> {
    MemberBenefitRule findByLevelCodeAndSceneAndBenefitType(@Param("levelCode") String levelCode,
                                                            @Param("scene") String scene,
                                                            @Param("benefitType") String benefitType);
    List<MemberBenefitRule> findByLevelCodeOrderBySceneAsc(@Param("levelCode") String levelCode);
}
