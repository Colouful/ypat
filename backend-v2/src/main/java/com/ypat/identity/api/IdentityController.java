package com.ypat.identity.api;

import com.ypat.identity.application.IdentityStatusUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * PR-15: read-only Identity status controller.
 *
 * Routes:
 *   GET /api/identity/status/{userId}
 *
 * Returns the verification state without exposing the ID number.
 * The encryption + decryption flow lands with PR-21.
 */
@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private final IdentityStatusUseCase identityStatus;

    public IdentityController(IdentityStatusUseCase identityStatus) {
        this.identityStatus = identityStatus;
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<?> status(@PathVariable long userId) {
        IdentityStatusUseCase.Status s = identityStatus.status(userId);
        return ResponseEntity.ok(Map.of(
                "userId", s.userId,
                "state", s.state.name()));
    }
}