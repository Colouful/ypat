package com.ypat.repository;

import com.ypat.entity.WorkTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkTagRepository extends JpaRepository<WorkTag, Long> {

    /** 启用状态的标签（按 sort_no 升序） */
    List<WorkTag> findByStatusOrderBySortNoAsc(Integer status);

    WorkTag findByCode(String code);
}
