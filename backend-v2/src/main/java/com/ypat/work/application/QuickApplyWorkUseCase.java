package com.ypat.work.application;

/**
 * PR-13: Quick-apply (立即约拍) use case.
 *
 * Replaces {@code WorkService.quickApply(WorkQuickApplyQo)}.
 *
 * The legacy method branches on the work's author profession
 * (UserProfess) to pick a target type (YpatTarget). The exact
 * branch table:
 *   - sm.ts  -> YpatTarget.smtx  (私模 - 私聊)
 *   - sm.ms  -> YpatTarget.smtx
 *   - sm.py  -> YpatTarget.smzyp (私约 - 私约拍)
 *   - ps     -> YpatTarget.ptbd  (普通 - 拼团报单)
 *   - default -> YpatTarget.wysf (摄影师 - 玩约私房)
 *
 * We keep the same logic; PR-15 will replace the magic strings
 * with a typed enum and add unit tests for the matrix.
 */
public interface QuickApplyWorkUseCase {

    QuickApplyResult quickApply(QuickApplyCommand cmd);

    final class QuickApplyCommand {
        public final Long fromUserId;
        public final Long workId;

        public QuickApplyCommand(Long fromUserId, Long workId) {
            this.fromUserId = fromUserId;
            this.workId = workId;
        }
    }

    final class QuickApplyResult {
        public final Long targetId;     // ID of the new appointment/event/...
        public final String targetType; // YpatTarget.value

        public QuickApplyResult(Long targetId, String targetType) {
            this.targetId = targetId;
            this.targetType = targetType;
        }
    }
}