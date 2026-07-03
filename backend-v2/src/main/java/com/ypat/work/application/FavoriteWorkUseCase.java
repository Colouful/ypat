package com.ypat.work.application;

/**
 * PR-13: Favorite use case (idempotent).
 *
 * Replaces {@code WorkService.favoriteIdempotent(...)} /
 * {@code WorkService.unfavoriteIdempotent(...)} from PR-09.
 *
 * Mirrors {@link LikeWorkUseCase} contract: idempotent under
 * concurrent calls, DB-side UNIQUE KEY uk_work_favorite_user_work
 * from PR-09 is the safety net.
 */
public interface FavoriteWorkUseCase {

    void favorite(Long workId, Long userId);

    void unfavorite(Long workId, Long userId);
}