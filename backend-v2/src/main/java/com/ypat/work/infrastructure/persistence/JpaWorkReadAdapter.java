package com.ypat.work.infrastructure.persistence;

import com.ypat.work.domain.WorkEntity;
import com.ypat.work.infrastructure.JpaWorkReadRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * PR-11 follow-up: real adapter implementing the
 * {@link StubWorkReadRepository} contract on top of the JPA
 * repository. Replaces InMemoryWorkReadRepository as the
 * default binding for QueryWorkListUseCase / QueryWorkDetailUseCase.
 *
 * The legacy schema is not perfectly clean (V1.0 §2.3.6); this
 * adapter does the minimum mapping so the controllers can serve
 * real data without waiting on a follow-up PR.
 */
@Repository
public class JpaWorkReadAdapter implements StubWorkReadRepository {

    private final JpaWorkReadRepository jpa;

    public JpaWorkReadAdapter(JpaWorkReadRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Page list(String city, List<String> tags,
                     Date cursorPublishTime, Long cursorId, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        // First page: cursorTime = now + 1 day, cursorId = Long.MAX_VALUE
        // so the < / < comparison returns all rows. Subsequent pages
        // pass the actual cursor from the previous Page.
        Date cursor = cursorPublishTime != null
                ? cursorPublishTime
                : new Date(System.currentTimeMillis() + 86_400_000L);
        Long id = cursorId != null ? cursorId : Long.MAX_VALUE;
        List<WorkEntity> rows = jpa.findList(city, cursor, id, PageRequest.of(0, safeSize));
        List<WorkRow> items = new ArrayList<>(rows.size());
        Date nextCursorTime = null;
        Long nextCursorId = null;
        for (WorkEntity w : rows) {
            items.add(new WorkRow(
                    w.getId() == null ? 0L : w.getId(),
                    w.getUserId() == null ? 0L : w.getUserId(),
                    "", "",
                    w.getDescription(), w.getStatus(), w.getPublishTime(),
                    w.getCity(), w.getArea(),
                    w.getLikeCount(), w.getFavoriteCount(),
                    false, false,
                    new ArrayList<>(), new ArrayList<>()));
            nextCursorTime = w.getPublishTime();
            nextCursorId = w.getId();
        }
        return new Page(items,
                rows.size() < safeSize ? null : nextCursorTime,
                rows.size() < safeSize ? null : nextCursorId);
    }

    @Override
    public Optional<WorkRow> detail(long workId, Long viewerId) {
        WorkEntity w = jpa.findByIdAndDeletedFlag(workId, 0);
        if (w == null) return Optional.empty();
        return Optional.of(new WorkRow(
                w.getId() == null ? 0L : w.getId(),
                w.getUserId() == null ? 0L : w.getUserId(),
                "", "",
                w.getDescription(), w.getStatus(), w.getPublishTime(),
                w.getCity(), w.getArea(),
                w.getLikeCount(), w.getFavoriteCount(),
                false, false,
                new ArrayList<>(), new ArrayList<>()));
    }
}