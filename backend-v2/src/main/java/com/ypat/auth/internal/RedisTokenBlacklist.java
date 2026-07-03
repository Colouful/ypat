package com.ypat.auth.internal;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * PR-14: Redis-backed revocation list for any token kind.
 *
 * Key shape: {@code bl:{kind}:{tokenId}} where kind is one of
 * {@code oauth2}, {@code legacy}, {@code session}. Storing the
 * kind in the key avoids cross-kind collisions (a legacy_jti
 * could collide with an OAuth2 jti at the format level).
 *
 * TTL matches the token's exp. Once the token would have
 * expired anyway, the blacklist entry is allowed to expire
 * too — no need for a separate sweeper.
 *
 * Why {@code StringRedisTemplate} and not {@code RedisTemplate} :
 *   the value is always the literal string "1" (presence is the
 *   signal). Smaller payload, no Jackson overhead, debuggable
 *   via plain {@code redis-cli}.
 */
public class RedisTokenBlacklist {

    private static final String PREFIX = "bl:";

    private final StringRedisTemplate redis;

    public RedisTokenBlacklist(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void revoke(String kind, String tokenId, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            // Already expired, nothing to do.
            return;
        }
        redis.opsForValue().set(
                PREFIX + kind + ":" + tokenId,
                "1",
                Duration.ofSeconds(ttlSeconds));
    }

    public boolean isRevoked(String kind, String tokenId) {
        Boolean has = redis.hasKey(PREFIX + kind + ":" + tokenId);
        return Boolean.TRUE.equals(has);
    }
}