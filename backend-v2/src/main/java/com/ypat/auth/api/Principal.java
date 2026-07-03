package com.ypat.auth.api;

import java.util.Set;

/**
 * PR-14: unified principal across OAuth2 access tokens and
 * legacy HS256 JWT.
 *
 * The legacy code (system-wap / system-sso / system-security)
 * speaks three different principal shapes:
 *
 *   - Spring Security OAuth2 access_token -> {@code Authentication}
 *     with {@code OAuth2AuthenticationDetails} on it
 *   - Self-rolled {@code JwtTokenUtil} -> a UserDetails-style
 *     object built from a hand-parsed JWT claim set
 *   - Spring Session (system-sso) -> {@code session.getAttribute("user")}
 *     holding a different UserDetails shape again
 *
 * v2 picks one shape and translates the others into it. This
 * is it.
 *
 * Equality / hashCode follow the userId only — two principals
 * representing the same backend user are the same principal
 * regardless of which token they arrived on.
 */
public final class Principal {

    private final long userId;
    private final String username;
    private final String tokenId;            // jti / legacy_jti
    private final TokenKind kind;
    private final Set<String> roles;        // e.g. {"ROLE_USER", "ROLE_ADMIN"}
    private final long expiresAtEpochSecond;

    public Principal(long userId,
                     String username,
                     String tokenId,
                     TokenKind kind,
                     Set<String> roles,
                     long expiresAtEpochSecond) {
        this.userId = userId;
        this.username = username;
        this.tokenId = tokenId;
        this.kind = kind;
        this.roles = roles == null ? Set.of() : Set.copyOf(roles);
        this.expiresAtEpochSecond = expiresAtEpochSecond;
    }

    public long userId() { return userId; }
    public String username() { return username; }
    public String tokenId() { return tokenId; }
    public TokenKind kind() { return kind; }
    public Set<String> roles() { return roles; }
    public long expiresAtEpochSecond() { return expiresAtEpochSecond; }

    public boolean isAdmin() {
        return roles.contains("ROLE_ADMIN");
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Principal p)) return false;
        return userId == p.userId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(userId);
    }

    public enum TokenKind {
        /** Spring Authorization Server RS256 access_token */
        OAUTH2_RS256,
        /** Legacy self-rolled HS256 JWT, only accepted before RS256_CUTOFF_DATE */
        LEGACY_HS256,
        /** Spring Session cookie (system-sso only, dies with the legacy tree) */
        SESSION_COOKIE
    }
}