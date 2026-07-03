package com.ypat.auth.internal;

import com.ypat.auth.api.Principal;
import com.ypat.auth.api.Principal.TokenKind;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * PR-14: legacy HS256 JWT translator.
 *
 * The legacy {@code com.ypat.util.JwtTokenUtil} (in system-wap)
 * is a self-rolled JWT library. During the cut-over window we
 * still need to accept these tokens, because clients have not
 * yet migrated to the new Authorization Server. After
 * RS256_CUTOFF_DATE this class refuses to translate them and
 * the caller treats them as 401.
 *
 * Hard rules:
 *   - alg MUST be HS256. RS256 tokens take the new path.
 *   - nbf MUST be present AND before RS256_CUTOFF_DATE.
 *   - The jti claim (legacy_jti in our rename) is recorded for
 *     the Redis blacklist to revoke.
 *
 * Verification: this class is small and pure (no Spring, no IO),
 * so unit tests cover all branches without infrastructure.
 */
public class JwtTokenMigrator {

    /** Hard cutover. Tokens with nbf >= this are rejected. */
    public static final Instant RS256_CUTOFF_DATE =
            Instant.parse("2026-09-01T00:00:00Z");

    /**
     * @return translated principal, or empty if the token is
     *     HS256-but-past-cutover, malformed, or otherwise invalid.
     */
    public Principal translate(LegacyJwt jwt) {
        if (!"HS256".equals(jwt.algorithm())) {
            return null;
        }
        if (jwt.notBefore() == null || jwt.notBefore().after(Date.from(RS256_CUTOFF_DATE))) {
            // Past the cutover; no more legacy tokens accepted.
            return null;
        }
        if (jwt.expiration() == null || jwt.expiration().before(new Date())) {
            return null;
        }
        if (jwt.userId() == null || jwt.userId() <= 0) {
            return null;
        }

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        if (Boolean.TRUE.equals(jwt.isAdmin())) {
            roles.add("ROLE_ADMIN");
        }

        return new Principal(
                jwt.userId(),
                jwt.username() == null ? "" : jwt.username(),
                "legacy:" + (jwt.jti() == null ? "" : jwt.jti()),
                TokenKind.LEGACY_HS256,
                roles,
                jwt.expiration().toInstant().getEpochSecond());
    }

    /**
     * Minimum-viable parser shape. The real parser is Nimbus
     * JOSE; this record is what {@link #translate} consumes
     * after parsing.
     */
    public record LegacyJwt(
            String algorithm,
            Date notBefore,
            Date expiration,
            Long userId,
            String username,
            Boolean isAdmin,
            String jti) {}
}