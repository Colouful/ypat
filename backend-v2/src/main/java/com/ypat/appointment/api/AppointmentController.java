package com.ypat.appointment.api;

import com.ypat.appointment.application.CreateAppointmentUseCase;
import com.ypat.appointment.application.TransitionAppointmentUseCase;
import com.ypat.auth.api.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * PR-16: appointment controller.
 *
 * Routes:
 *   POST /api/appointment                 - create
 *   POST /api/appointment/{id}/transition - confirm / cancel / complete / expire
 */
@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    private final CreateAppointmentUseCase create;
    private final TransitionAppointmentUseCase transition;

    public AppointmentController(CreateAppointmentUseCase create,
                                 TransitionAppointmentUseCase transition) {
        this.create = create;
        this.transition = transition;
    }

    @PostMapping
    public ResponseEntity<?> create(Principal principal, @RequestBody Map<String, Object> body) {
        if (principal == null) return ResponseEntity.status(401).build();
        Long toUserId = ((Number) body.get("toUserId")).longValue();
        Long workId = body.get("workId") == null ? null : ((Number) body.get("workId")).longValue();
        Date scheduledAt = new Date(((Number) body.get("scheduledAt")).longValue());
        String notes = (String) body.getOrDefault("notes", "");

        CreateAppointmentUseCase.Result r = create.create(
                new CreateAppointmentUseCase.Command(principal.userId(), toUserId,
                        workId, scheduledAt, notes));

        Map<String, Object> resp = new HashMap<>();
        resp.put("appointmentId", r.appointmentId);
        resp.put("state", r.state.name());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<?> transition(Principal principal,
                                         @PathVariable long id,
                                         @RequestBody Map<String, Object> body) {
        if (principal == null) return ResponseEntity.status(401).build();
        String eventStr = (String) body.get("event");
        TransitionAppointmentUseCase.Event event;
        try {
            event = TransitionAppointmentUseCase.Event.valueOf(eventStr);
        } catch (IllegalArgumentException | NullPointerException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "INVALID_EVENT"));
        }

        TransitionAppointmentUseCase.Result r =
                transition.transition(id, principal.userId(), event);

        Map<String, Object> resp = new HashMap<>();
        resp.put("appointmentId", r.appointmentId);
        resp.put("from", r.from.name());
        resp.put("to", r.to.name());
        return ResponseEntity.ok(resp);
    }
}