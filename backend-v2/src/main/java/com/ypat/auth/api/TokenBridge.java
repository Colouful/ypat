package com.ypat.auth.api;

import java.util.Optional;

/**
 * PR-14: the contract every authentication path goes through.
 *
 * v2 controllers depend only on this. They never see
 * {@code OAuth2Authentication} or {@code JwtTokenUtil} directly.
 * Implementations sit in {@code com.ypat.auth.internal} and
 * compose:
 *
 *   - Spring Authorization Server's JWT decoder (RS256, kid=current)
 *   - JwtTokenMigrator for the legacy HS256 path (kid=legacy)
 *   - RedisTokenBlacklist for revocation (any token kind)
 *   - Spring Session fallback for the system-sso cookie path
 *     (only active during the cut-over window)
 *
 * Hard rules enforced here, not at the call site:
 *
 *   - LegacyToken only accepted when its nbf is before
 *     RS256_CUTOFF_DATE (V1.1 §1.2 row 3).
 *   - Any token that has been written to the Redis blacklist
 *     is rejected with the same response as 'never issued'.
 *   - Token kind SESSION_COOKIE is only accepted when the
 *     caller comes from a host that the legacy /api/sso/*
 *     prefix is still routed to. After PR-22 prep completes
 *     this returns Optional.empty() unconditionally.
 */
public interface TokenBridge {

    /**
     * @return the resolved principal, or empty if the token is
     *     missing / malformed / blacklisted / past its kind's
     *     cut-over date.
     */
    Optional<Principal> resolve(String authorizationHeader);

    /**
     * Force-revoke a token. Writes to the Redis blacklist with
     * a TTL matching the token's exp.
     *
     * Used by the logout flow and by admin-initiated session
     * kills.
     */
    void revoke(String tokenId, long ttlSeconds);
}