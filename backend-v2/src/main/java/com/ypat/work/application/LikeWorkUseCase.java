package com.ypat.work.application;

/**
 * PR-13: Like use case (idempotent).
 *
 * Replaces {@code WorkService.likeIdempotent(...)} /
 * {@code WorkService.unlikeIdempotent(...)} from PR-09.
 *
 * The non-idempotent legacy methods (WorkService.like / unlike)
 * are deliberately NOT modelled here. They throw on duplicate,
 * which is the wrong contract for HTTP clients behind a flaky
 * network.
 *
 * The DB-side UNIQUE KEY uk_work_like_user_work from PR-09 is
 * what makes this safe under concurrent calls.
 */
public interface LikeWorkUseCase {

    /** Idempotent like. Only the first call increments the counter. */
    void like(Long workId, Long userId);

    /** Idempotent unlike. No-op if the user never liked the work. */
    void unlike(Long workId, Long userId);
}