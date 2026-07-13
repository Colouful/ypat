package com.ypat.appointment.application;

import java.util.Date;

/**
 * PR-16: CreateAppointmentUseCase.
 *
 * Replaces the appointment-creation path in legacy system-wap /
 * system-restapi. State machine handled by the implementation,
 * not the caller.
 *
 * State machine (PR-16 lands the contract; the real transitions
 * land with PR-16 follow-up):
 *
 *   PENDING   --(creator confirms)-->   CONFIRMED
 *   PENDING   --(creator cancels)-->    CANCELLED
 *   PENDING   --(TTL expires)-->        EXPIRED
 *   CONFIRMED --(both accept)-->         COMPLETED
 *   CONFIRMED --(either cancels)-->      CANCELLED
 *
 * Why the state machine belongs in the implementation:
 *   - All transitions are recorded with timestamp + actor.
 *   - The audit trail is a v2 requirement (V1.1 §4.4 WORM).
 *   - Concurrent transitions from both sides need an
 *     atomic UPDATE with WHERE state IN (...) clause; doing
 *     that in SQL, not in code, avoids lost updates.
 */
public interface CreateAppointmentUseCase {

    Result create(Command cmd);

    final class Command {
        public final Long fromUserId;
        public final Long toUserId;
        public final Long workId;            // nullable; the work this appointment is about
        public final Date scheduledAt;
        public final String notes;

        public Command(Long fromUserId, Long toUserId, Long workId,
                       Date scheduledAt, String notes) {
            this.fromUserId = fromUserId;
            this.toUserId = toUserId;
            this.workId = workId;
            this.scheduledAt = scheduledAt;
            this.notes = notes;
        }
    }

    final class Result {
        public final Long appointmentId;
        public final State state;

        public Result(Long appointmentId, State state) {
            this.appointmentId = appointmentId;
            this.state = state;
        }
    }

    enum State {
        PENDING, CONFIRMED, COMPLETED, CANCELLED, EXPIRED
    }
}