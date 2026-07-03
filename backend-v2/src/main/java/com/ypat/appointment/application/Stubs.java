package com.ypat.appointment.application;

import com.ypat.appointment.application.CreateAppointmentUseCase.State;
import org.springframework.stereotype.Service;

/**
 * PR-16: stub implementations for the appointment UseCases.
 *
 * Stubs always succeed. The real state-machine wiring lands with
 * PR-16 follow-up once the appointment JPA entity is in place.
 */
@Service
public class Stubs {

    @Service
    public static class CreateAppointmentStub implements CreateAppointmentUseCase {
        @Override
        public Result create(Command cmd) {
            return new Result(System.currentTimeMillis(), State.PENDING);
        }
    }

    @Service
    public static class TransitionAppointmentStub implements TransitionAppointmentUseCase {
        @Override
        public Result transition(long appointmentId, long actorUserId, Event event) {
            State from = State.PENDING;
            State to = switch (event) {
                case CONFIRM  -> State.CONFIRMED;
                case CANCEL   -> State.CANCELLED;
                case COMPLETE -> State.COMPLETED;
                case EXPIRE   -> State.EXPIRED;
            };
            return new Result(appointmentId, from, to);
        }
    }
}