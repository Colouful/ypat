package com.ypat.repository;

import com.ypat.entity.InternalTestResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternalTestResourceRepository extends JpaRepository<InternalTestResource, Long>, JpaSpecificationExecutor<InternalTestResource> {
    List<InternalTestResource> findByIdInAndStatus(List<Long> ids, String status);

    InternalTestResource findByUrl(String url);

    List<InternalTestResource> findByGroupNoIn(List<String> groupNos);

    List<InternalTestResource> findByGroupNoInAndStatus(List<String> groupNos, String status);

    List<InternalTestResource> findByUsedBatchNo(String usedBatchNo);
}
