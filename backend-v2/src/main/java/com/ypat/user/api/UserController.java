package com.ypat.user.api;

import com.ypat.auth.api.Principal;
import com.ypat.user.application.GetCurrentUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * PR-15: read-only User controller, takes a {@link Principal}
 * (PR-14) directly. The TokenBridge filter (PR-15 follow-up)
 * resolves the Authorization header into a Principal before this
 * controller sees the request; the controller never touches the
 * raw token.
 *
 * Routes:
 *   GET /api/user/me  -> the current user's view-model shape
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final GetCurrentUserUseCase getCurrentUser;

    public UserController(GetCurrentUserUseCase getCurrentUser) {
        this.getCurrentUser = getCurrentUser;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(com.ypat.auth.api.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "UNAUTHENTICATED"));
        }
        GetCurrentUserUseCase.CurrentUser u = getCurrentUser.get(principal);
        if (u == null) {
            return ResponseEntity.status(404).build();
        }
        // Map to the wire shape. PR-15 final replaces this with
        // a typed DTO once the OpenAPI baseline is locked.
        Map<String, Object> body = new HashMap<>();
        body.put("id", u.id);
        body.put("nickname", u.nickname);
        body.put("avatar", u.avatar);
        body.put("phoneMasked", u.phoneMasked);
        body.put("idNoMasked", u.idNoMasked);
        body.put("city", u.city);
        body.put("memberTier", u.memberTier);
        body.put("memberExpiry", u.memberExpiry);
        body.put("roles", u.roles);
        body.put("isIdentityVerified", u.isIdentityVerified);
        return ResponseEntity.ok(body);
    }
}