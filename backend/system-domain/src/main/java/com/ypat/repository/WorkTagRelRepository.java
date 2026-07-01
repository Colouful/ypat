package com.ypat.repository;

import com.ypat.entity.WorkTagRel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkTagRelRepository extends JpaRepository<WorkTagRel, Long> {

    List<WorkTagRel> findByWorkId(Long workId);

    @Modifying
    @Query("delete from WorkTagRel r where r.workId = :workId")
    int deleteByWorkId(@Param("workId") Long workId);

    /** 按 workId 列表查 tagId 列表（用于筛选） */
    @Query("select r.tagId from WorkTagRel r where r.workId in :workIds")
    List<Long> findTagIdsByWorkIds(@Param("workIds") List<Long> workIds);
}
