package com.ypat.work.application;

import com.ypat.work.infrastructure.persistence.StubWorkReadRepository;
import com.ypat.work.infrastructure.persistence.StubWorkReadRepository.WorkRow;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * PR-11: stub implementation of {@link QueryWorkDetailUseCase}.
 *
 * Translates a {@link WorkRow} into the typed {@link WorkDetail}.
 * Media list and tags are passed through; the actual URL signing
 * and storage resolution lands with PR-15.
 */
@Service
public class QueryWorkDetailUseCaseImpl implements QueryWorkDetailUseCase {

    private final StubWorkReadRepository repo;

    public QueryWorkDetailUseCaseImpl(StubWorkReadRepository repo) {
        this.repo = repo;
    }

    @Override
    public WorkDetail detail(DetailQuery q) {
        Optional<WorkRow> row = repo.detail(q.workId, q.viewerId);
        if (row.isEmpty()) {
            return null;            // controller maps to 404
        }
        WorkRow r = row.get();
        return new WorkDetail(
                r.id, r.userId, r.nickname, r.avatar,
                r.description, r.status, r.publishTime,
                r.city, r.area, null,
                null, r.likeCount, r.favoriteCount,
                r.isLikedByViewer, r.isFavoritedByViewer,
                new java.util.ArrayList<>(), r.tags);
    }
}