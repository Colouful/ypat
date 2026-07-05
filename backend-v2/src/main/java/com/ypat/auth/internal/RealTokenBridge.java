package com.ypat.auth.internal;

import com.ypat.auth.api.Principal;
import com.ypat.auth.api.Principal.TokenKind;
import com.ypat.auth.api.TokenBridge;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;

/**
 * PR-14 follow-up: real TokenBridge implementation.
 *
 * Resolves the Authorization: Bearer token into a Principal
 * by:
 *   1. Reading the JOSE header to identify the algorithm
 *      and the kid (RS256 = current, HS256 = legacy).
 *   2. Looking up the blacklist (Redis SET bl:{kind}:{tokenId})
 *      before any crypto work; a hit is "token revoked".
 *   3. Verifying the signature with the matching key.
 *   4. Parsing the claim set into a Principal.
 *
 * The hard cutover (V1.1 §1.2 row 3) lives in
 * {@link JwtTokenMigrator#translate}: legacy HS256 tokens are
 * rejected after 2026-09-01T00:00:00Z.
 *
 * For dev / docker compose the RS256 public key is a
 * fixed dev value; production swaps it for the Authorization
 * Server's published JWKS via {@code YPAT_AUTH_JWKS_URL}.
 */
@Service
public class RealTokenBridge implements TokenBridge {

    private static final Instant RS256_CUTOFF_DATE =
            Instant.parse("2026-09-01T00:00:00Z");

    private final StringRedisTemplate redis;
    private final JWSVerifier rs256Verifier;
    private final JWSVerifier hs256Verifier;
    private final String redisPrefix;

    public RealTokenBridge(StringRedisTemplate redis,
                            @Value("${ypat.auth.rs256-public-key:}") String rs256Pem,
                            @Value("${ypat.auth.legacy-hs-key:}") String legacyHs,
                            @Value("${ypat.auth.redis-prefix:bl:}") String redisPrefix)
            throws Exception {
        this.redis = redis;
        this.redisPrefix = redisPrefix;
        this.rs256Verifier = rs256Pem == null || rs256Pem.isBlank()
                ? null
                : new RSASSAVerifier(rsaPublicKeyFromPem(rs256Pem));
        this.hs256Verifier = legacyHs == null || legacyHs.isBlank()
                ? null
                : new MACVerifier(legacyHs.getBytes());
    }

    @Override
    public Optional<Principal> resolve(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWSAlgorithm alg = jwt.getHeader().getAlgorithm();
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            System.out.println("[DEBUG] JWT alg=" + alg + " kid=" + jwt.getHeader().getKeyID());

            // step 2: blacklist check
            String jti = claims.getJWTID();
            if (jti != null) {
                String kind = alg == JWSAlgorithm.HS256 ? "legacy" : "oauth2";
                Boolean revoked = redis.hasKey(redisPrefix + kind + ":" + jti);
                if (Boolean.TRUE.equals(revoked)) return Optional.empty();
            }

            // step 3: signature
            boolean sigOk;
            if (alg == JWSAlgorithm.RS256) {
                if (rs256Verifier == null) return Optional.empty();
                sigOk = jwt.verify(rs256Verifier);
            } else if (alg == JWSAlgorithm.HS256) {
                if (hs256Verifier == null) return Optional.empty();
                sigOk = jwt.verify(hs256Verifier);
            } else {
                return Optional.empty();
            }
            if (!sigOk) return Optional.empty();

            // step 4: build Principal
            TokenKind kind = alg == JWSAlgorithm.HS256
                    ? TokenKind.LEGACY_HS256
                    : TokenKind.OAUTH2_RS256;

            // legacy cutover guard
            if (kind == TokenKind.LEGACY_HS256) {
                Date nbf = claims.getNotBeforeTime();
                if (nbf == null || nbf.toInstant().isAfter(RS256_CUTOFF_DATE)) {
                    return Optional.empty();
                }
            }

            Long userId = claims.getLongClaim("user_id");
            if (userId == null) userId = claims.getLongClaim("uid");
            if (userId == null) return Optional.empty();

            String username = claims.getStringClaim("preferred_username");
            if (username == null) username = claims.getSubject();
            Boolean isAdmin = claims.getBooleanClaim("is_admin");
            if (isAdmin == null) isAdmin = false;

            Set<String> roles = new HashSet<>();
            roles.add("ROLE_USER");
            if (Boolean.TRUE.equals(isAdmin)) roles.add("ROLE_ADMIN");
            Object scope = claims.getClaim("scope");
            if (scope instanceof String s) {
                for (String tok : s.split(" ")) {
                    if (!tok.isBlank()) roles.add("SCOPE_" + tok.toUpperCase());
                }
            }

            long exp = claims.getExpirationTime() == null
                    ? 0L
                    : claims.getExpirationTime().toInstant().getEpochSecond();
            return Optional.of(new Principal(
                    userId, username, jti, kind, roles, exp));
        } catch (Exception e) {
            System.out.println("[DEBUG] TokenBridge.resolve EXCEPTION: " + e);
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void revoke(String tokenId, long ttlSeconds) {
        if (tokenId == null || ttlSeconds <= 0) return;
        // best-effort: we don't know whether the token is RS256 or
        // HS256 here, so we revoke under both kinds. A hit on the
        // wrong kind is a no-op on the read path.
        redis.opsForValue().set(redisPrefix + "oauth2:" + tokenId, "1");
        redis.opsForValue().set(redisPrefix + "legacy:" + tokenId, "1");
    }

    private static RSAPublicKey rsaPublicKeyFromPem(String pem) throws Exception {
        // Strip PEM headers/footers AND any non-base64 character
        // (whitespace, stray YAML special chars, embedded dashes).
        // Then trim the body so its length is a multiple of 4 —
        // a stray character at the end otherwise triggers
        // "Last unit does not have enough valid bits" from
        // Base64.getDecoder().decode().
        String body = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("[^A-Za-z0-9+/=]", "");
        int rem = body.length() % 4;
        if (rem != 0) {
            body = body.substring(0, body.length() - rem);
        }
        byte[] der = java.util.Base64.getDecoder().decode(body);
        java.security.spec.X509EncodedKeySpec spec =
                new java.security.spec.X509EncodedKeySpec(der);
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }
}