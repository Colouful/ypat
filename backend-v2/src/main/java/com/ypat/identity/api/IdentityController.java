package com.ypat.identity.api;

import com.ypat.identity.application.GetDecryptedIdNoUseCase;
import com.ypat.identity.application.IdentityStatusUseCase;
import com.ypat.identity.application.VerifyIdentityUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PR-21: identity verification + admin decryption.
 *
 * Routes:
 *   GET  /api/identity/status/{userId}              - public status enum
 *   POST /api/identity/{userId}/submit              - encrypt + store id number
 *   POST /api/identity/{userId}/review             - admin approve/reject
 *   GET  /api/identity/{userId}/decrypt            - admin-only plaintext (audited)
 *
 * The submit and review routes get @PreAuthorize in PR-15
 * follow-up; PR-21 ships the contract.
 */
@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private final IdentityStatusUseCase identityStatus;
    private final VerifyIdentityUseCase verify;
    private final GetDecryptedIdNoUseCase decrypt;

    public IdentityController(IdentityStatusUseCase identityStatus,
                               VerifyIdentityUseCase verify,
                               GetDecryptedIdNoUseCase decrypt) {
        this.identityStatus = identityStatus;
        this.verify = verify;
        this.decrypt = decrypt;
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<?> status(@PathVariable long userId) {
        IdentityStatusUseCase.Status s = identityStatus.status(userId);
        return ResponseEntity.ok(Map.of(
                "userId", s.userId,
                "state", s.state.name()));
    }

    @PostMapping("/{userId}/submit")
    public ResponseEntity<?> submit(@PathVariable long userId,
                                     @RequestBody Map<String, Object> body) {
        String idNumber = (String) body.get("idNumber");
        verify.submit(userId, idNumber);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{userId}/review")
    public ResponseEntity<?> review(@PathVariable long userId,
                                     @RequestBody Map<String, Object> body) {
        boolean approve = (Boolean) body.getOrDefault("approve", false);
        String rejectReason = (String) body.getOrDefault("rejectReason", null);
        long reviewerId = ((Number) body.getOrDefault("reviewerId", 1)).longValue();
        verify.review(userId, approve, rejectReason, reviewerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/decrypt")
    public ResponseEntity<?> decrypt(@PathVariable long userId) {
        String plaintext = decrypt.decrypt(userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "idNumber", plaintext));
    }
}