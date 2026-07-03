package com.ypat.work.application;

/**
 * PR-13: Work complaint (投诉) use case.
 *
 * Replaces {@code WorkService.complain(Work, Long, String, String)}.
 *
 * Carries the legacy validation rules forward verbatim until
 * PR-15 implements the controller swap:
 *   - reason must be 10..500 chars
 *   - contact must be <= 100 chars (nullable)
 *
 * Sensitive-word filtering (the MOBILE / WX / QQ regex in
 * WorkService) is part of this use case too — it currently
 * throws if contact details appear in the reason text. We
 * keep that behaviour rather than dropping it; rolling it out
 * later as a separate signal.
 */
public interface ComplainWorkUseCase {

    void complain(Long workId, Long userId, String reason, String contact);
}