package com.ypat.wallet.api;

import com.ypat.wallet.application.WalletService;
import com.ypat.wallet.domain.LedgerEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PR-19: read + write endpoints for the wallet.
 *
 * Routes:
 *   GET  /api/wallet/{userId}             - balance + recent history
 *   POST /api/wallet/{userId}/adjust      - admin / system adjust
 *
 * Authentication: deferred. PR-14's TokenBridge hands the
 * controller a Principal; PR-15 follow-up wires @PreAuthorize
 * for the admin-only /adjust endpoint.
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService service;

    public WalletController(WalletService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> view(@PathVariable long userId) {
        long balance = service.balance(userId);
        List<LedgerEntry> history = service.history(userId);
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("balance", balance);
        body.put("history", history.stream().map(e -> Map.of(
                "id", e.getId(),
                "delta", e.getDelta(),
                "balanceAfter", e.getBalanceAfter(),
                "reason", e.getReason(),
                "refType", e.getRefType() == null ? "" : e.getRefType(),
                "refId", e.getRefId() == null ? "" : e.getRefId(),
                "actorUserId", e.getActorUserId() == null ? "" : e.getActorUserId(),
                "note", e.getNote() == null ? "" : e.getNote(),
                "createdAt", e.getCreatedAt()
        )).collect(Collectors.toList()));
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{userId}/adjust")
    public ResponseEntity<?> adjust(@PathVariable long userId,
                                     @RequestBody Map<String, Object> body) {
        long delta = ((Number) body.get("delta")).longValue();
        String reason = (String) body.getOrDefault("reason", "ADMIN_ADJUST");
        String refType = (String) body.getOrDefault("refType", null);
        String refId = (String) body.getOrDefault("refId", null);
        String note = (String) body.getOrDefault("note", null);

        LedgerEntry e = service.apply(userId, delta, reason, refType, refId,
                null /* PR-14 follow-up pulls this from Principal */,
                note);
        return ResponseEntity.ok(Map.of(
                "ledgerId", e.getId(),
                "balanceAfter", e.getBalanceAfter()));
    }
}