package com.ypat.repository;

import com.ypat.entity.YpatInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YpatInfoRepository extends JpaRepository<YpatInfo, Long>, JpaSpecificationExecutor<YpatInfo> {

    YpatInfo findById(@Param("id") Long id);

    @Override
    @EntityGraph(value = "YpatInfo.all")
    Page<YpatInfo> findAll(Specification<YpatInfo> var1, Pageable var2);

    @Modifying
    @Query("update YpatInfo i set i.readtimes=i.readtimes+1 where i.id in :ids ")
    void updateByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query("update YpatInfo y set y.status = :status, y.reason = :reason where y.dataFlag = 'internal_test' and (:batchNo is null or y.internalBatchNo = :batchNo)")
    int updateInternalTestYpatStatus(@Param("batchNo") String batchNo, @Param("status") String status, @Param("reason") String reason);
}
