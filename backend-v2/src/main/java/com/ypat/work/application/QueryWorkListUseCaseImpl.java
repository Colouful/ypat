package com.ypat.work.application;

import com.ypat.work.infrastructure.persistence.StubWorkReadRepository;
import com.ypat.work.infrastructure.persistence.StubWorkReadRepository.WorkRow;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * PR-11: stub implementation of {@link QueryWorkListUseCase}.
 *
 * Reads from {@link StubWorkReadRepository} (interface, not the
 * concrete in-memory class) and translates the row shape into the
 * UseCase's typed result. The translation lives here so the
 * {@code application} package does not import {@code infrastructure.persistence}
 * directly — Modulith boundary discipline.
 *
 * The real implementation lands with PR-15 once the JPA backend
 * is wired in.
 */
@Service
public class QueryWorkListUseCaseImpl implements QueryWorkListUseCase {

    private final StubWorkReadRepository repo;

    public QueryWorkListUseCaseImpl(StubWorkReadRepository repo) {
        this.repo = repo;
    }

    @Override
    public Page list(ListQuery q) {
        StubWorkReadRepository.Page row = repo.list(q.city, q.tags,
                q.cursorPublishTime == null ? null : new Date(q.cursorPublishTime),
                q.cursorId,
                Math.min(Math.max(q.size, 1), 50));

        List<Item> items = new ArrayList<>(row.items.size());
        for (WorkRow r : row.items) {
            items.add(new Item(
                    r.id, r.userId, r.nickname, null /* cover objectKey filled by PR-10+11 storage URL resolver */,
                    r.city, r.publishTime, r.likeCount, r.favoriteCount));
        }
        return new Page(items, row.nextCursorPublishTime, row.nextCursorId);
    }
}