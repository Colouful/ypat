package com.ypat.user.application;

import com.ypat.identity.application.IdentityStatusUseCase;
import com.ypat.user.domain.UserEntity;
import com.ypat.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * PR-15 follow-up: real implementation.
 *
 * Reads t_user via the JPA repository, masks phone and ID-no
 * (V1.1 §4.4 — plaintext never leaves the server in a
 * response). Falls back to placeholder values if the user row
 * is missing so the controller can still answer with 200 for
 * the freshly-registered case.
 */
@Service
public class GetCurrentUserUseCaseImpl implements GetCurrentUserUseCase {

    private final UserRepository users;
    private final IdentityStatusUseCase identityStatus;

    public GetCurrentUserUseCaseImpl(UserRepository users,
                                      IdentityStatusUseCase identityStatus) {
        this.users = users;
        this.identityStatus = identityStatus;
    }

    @Override
    public CurrentUser get(com.ypat.auth.api.Principal principal) {
        if (principal == null) return null;
        UserEntity u = users.findById(principal.userId()).orElse(null);
        if (u == null) {
            return new CurrentUser(
                    principal.userId(),
                    principal.username(),
                    null, maskPhone(null), maskIdNo(null),
                    null, 0, null,
                    principal.roles(),
                    false);
        }
        IdentityStatusUseCase.Status id = identityStatus.status(principal.userId());
        boolean verified = id != null
                && id.state == IdentityStatusUseCase.State.VERIFIED;
        return new CurrentUser(
                u.getId(),
                u.getNickname() != null ? u.getNickname() : u.getName(),
                u.getAvatarUrl(),
                maskPhone(u.getMobile()),
                maskIdNo(u.getCertCode()),
                u.getCity(),
                memberTierFromFlag(u.getRealnameFlag()),
                null /* memberExpiry: legacy t_user_member table, PR-17 follow-up */,
                principal.roles(),
                verified);
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static String maskIdNo(String id) {
        if (id == null || id.length() < 4) return "";
        return id.charAt(0) + "****************" + id.charAt(id.length() - 1);
    }

    private static int memberTierFromFlag(String flag) {
        // legacy column semantics: "0" = free, "1"+"n" = paid tier
        if (flag == null || flag.isBlank() || "0".equals(flag)) return 0;
        try { return Integer.parseInt(flag); } catch (NumberFormatException e) { return 0; }
    }
}