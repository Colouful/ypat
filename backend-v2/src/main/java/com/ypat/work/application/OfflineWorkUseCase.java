package com.ypat.work.application;

/**
 * PR-13: Offline (下架) use case.
 *
 * Replaces {@code WorkService.offline(Long, Long)}.
 *
 * Ownership check stays: only the work's author can take it
 * offline. The status transition PUBLISHED -> XJ (下架)
 * matches the legacy {@code WorkStatus.xj.value}.
 *
 * The actual status update is a single SQL UPDATE; the only
 * reason this is its own UseCase instead of a one-liner
 * repository call is so the audit trail (V1.1 §4.4 WORM audit)
 * can hang off it later.
 */
public interface OfflineWorkUseCase {

    void offline(Long workId, Long userId);
}