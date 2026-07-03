package com.ypat.work.infrastructure;

import com.ypat.work.domain.WorkEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * PR-11 follow-up: real JPA repository for the work list /
 * detail path. Replaces the InMemoryWorkReadRepository stub
 * (PR-11) once the JPA entity is in place.
 */
public interface JpaWorkReadRepository extends JpaRepository<WorkEntity, Long> {

    /**
     * Cursor-paged list. The cursor is the (publish_time, id)
     * tuple from the previous page's last item; null for the
     * first page. Combined with the PR-08 covering index
     * idx_city_status_pub, this is an O(log n) range scan.
     */
    @Query("SELECT w FROM WorkEntity w " +
           "WHERE w.deletedFlag = 0 " +
           "AND w.status = 'PUBLISHED' " +
           "AND (:city IS NULL OR w.city = :city) " +
           "AND (w.publishTime < :cursorTime " +
           "     OR (w.publishTime = :cursorTime AND w.id < :cursorId)) " +
           "ORDER BY w.publishTime DESC, w.id DESC")
    List<WorkEntity> findList(@Param("city") String city,
                              @Param("cursorTime") Date cursorTime,
                              @Param("cursorId") Long cursorId,
                              PageRequest page);

    /** Single work by id, only if not soft-deleted. */
    WorkEntity findByIdAndDeletedFlag(Long id, Integer deletedFlag);
}