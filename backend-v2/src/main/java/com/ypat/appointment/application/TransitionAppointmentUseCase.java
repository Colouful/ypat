package com.ypat.appointment.application;

import com.ypat.appointment.application.CreateAppointmentUseCase.State;

/**
 * PR-16: state transition UseCase.
 *
 * The legacy appointment flow lets either side cancel or accept
 * freely. v2 narrows the contract: only the allowed transitions
 * land, every transition is recorded with an actor (the userId
 * making the call), and conflicting transitions are rejected
 * with {@link Conflict}.
 */
public interface TransitionAppointmentUseCase {

    Result transition(long appointmentId, long actorUserId, Event event);

    enum Event {
        CONFIRM, CANCEL, COMPLETE, EXPIRE
    }

    final class Result {
        public final State from;
        public final State to;
        public final long appointmentId;

        public Result(long appointmentId, State from, State to) {
            this.appointmentId = appointmentId;
            this.from = from;
            this.to = to;
        }
    }

    /**
     * Thrown when the requested transition is not allowed from
     * the current state, or the actor is not authorised for it.
     */
    final class Conflict extends RuntimeException {
        public final State currentState;
        public final Event requestedEvent;
        public Conflict(State currentState, Event requestedEvent, String msg) {
            super(msg);
            this.currentState = currentState;
            this.requestedEvent = requestedEvent;
        }
    }
}