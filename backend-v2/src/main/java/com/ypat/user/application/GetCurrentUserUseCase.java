package com.ypat.user.application;

import java.util.Date;
import java.util.Set;

/**
 * PR-15: GetCurrentUserUseCase.
 *
 * Replaces the legacy {@code UserService.getCurrentUser(Long)}
 * pattern from system-wap. Returns the view-model shape that
 * frontends render, not the entity.
 *
 * The contract takes a {@code Principal} (from PR-14) instead of
 * a raw userId. That's how we collapse the three legacy auth
 * shapes (OAuth2 / HS256 JWT / Session cookie) into one entry
 * point: whoever resolves the token hands us a Principal, and
 * this UseCase trusts the Principal rather than re-validating.
 */
public interface GetCurrentUserUseCase {

    CurrentUser get(com.ypat.auth.api.Principal principal);

    final class CurrentUser {
        public final long id;
        public final String nickname;
        public final String avatar;
        public final String phoneMasked;        // 13****8888
        public final String idNoMasked;         // first + last char only
        public final String city;
        public final Integer memberTier;        // 0/1/2/3 (legacy mapping)
        public final Date memberExpiry;
        public final Set<String> roles;
        public final boolean isIdentityVerified;

        public CurrentUser(long id, String nickname, String avatar,
                           String phoneMasked, String idNoMasked,
                           String city, Integer memberTier, Date memberExpiry,
                           Set<String> roles, boolean isIdentityVerified) {
            this.id = id;
            this.nickname = nickname;
            this.avatar = avatar;
            this.phoneMasked = phoneMasked;
            this.idNoMasked = idNoMasked;
            this.city = city;
            this.memberTier = memberTier;
            this.memberExpiry = memberExpiry;
            this.roles = roles;
            this.isIdentityVerified = isIdentityVerified;
        }
    }
}