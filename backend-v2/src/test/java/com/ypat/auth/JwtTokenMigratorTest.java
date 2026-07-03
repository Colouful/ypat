package com.ypat.auth;

import com.ypat.auth.api.Principal;
import com.ypat.auth.internal.JwtTokenMigrator;
import com.ypat.auth.internal.JwtTokenMigrator.LegacyJwt;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PR-14: pure-Java test for the legacy HS256 translator.
 *
 * No Spring, no Redis, no JWT library — this only covers the
 * decision branches in {@link JwtTokenMigrator#translate}.
 * The Nimbus JOSE parser behind it has its own tests.
 */
class JwtTokenMigratorTest {

    private final JwtTokenMigrator migrator = new JwtTokenMigrator();

    @Test
    void translatesValidLegacyToken() {
        LegacyJwt jwt = new LegacyJwt(
                "HS256",
                Date.from(JwtTokenMigrator.RS256_CUTOFF_DATE.minusSeconds(60)),
                Date.from(Instant.now().plus(1, ChronoUnit.HOURS)),
                42L,
                "alice",
                false,
                "abc123");

        Principal p = migrator.translate(jwt);
        assertThat(p).isNotNull();
        assertThat(p.userId()).isEqualTo(42L);
        assertThat(p.username()).isEqualTo("alice");
        assertThat(p.kind()).isEqualTo(Principal.TokenKind.LEGACY_HS256);
        assertThat(p.hasRole("ROLE_USER")).isTrue();
        assertThat(p.isAdmin()).isFalse();
        assertThat(p.tokenId()).isEqualTo("legacy:abc123");
    }

    @Test
    void marksAdminFromClaim() {
        LegacyJwt jwt = new LegacyJwt(
                "HS256",
                Date.from(JwtTokenMigrator.RS256_CUTOFF_DATE.minusSeconds(60)),
                Date.from(Instant.now().plus(1, ChronoUnit.HOURS)),
                7L, "admin", true, null);

        Principal p = migrator.translate(jwt);
        assertThat(p).isNotNull();
        assertThat(p.isAdmin()).isTrue();
        assertThat(p.hasRole("ROLE_ADMIN")).isTrue();
    }

    @Test
    void rejectsRs256Token() {
        LegacyJwt jwt = new LegacyJwt(
                "RS256",
                Date.from(JwtTokenMigrator.RS256_CUTOFF_DATE.minusSeconds(60)),
                Date.from(Instant.now().plus(1, ChronoUnit.HOURS)),
                1L, "alice", false, null);
        assertThat(migrator.translate(jwt)).isNull();
    }

    @Test
    void rejectsTokenPastCutover() {
        // nbf = cutover + 1s = past the cutoff
        LegacyJwt jwt = new LegacyJwt(
                "HS256",
                Date.from(JwtTokenMigrator.RS256_CUTOFF_DATE.plusSeconds(1)),
                Date.from(Instant.now().plus(1, ChronoUnit.HOURS)),
                1L, "alice", false, null);
        assertThat(migrator.translate(jwt)).isNull();
    }

    @Test
    void rejectsExpiredToken() {
        LegacyJwt jwt = new LegacyJwt(
                "HS256",
                Date.from(JwtTokenMigrator.RS256_CUTOFF_DATE.minusSeconds(60)),
                Date.from(Instant.now().minusSeconds(1)),
                1L, "alice", false, null);
        assertThat(migrator.translate(jwt)).isNull();
    }

    @Test
    void rejectsMissingNotBefore() {
        LegacyJwt jwt = new LegacyJwt(
                "HS256",
                null,
                Date.from(Instant.now().plus(1, ChronoUnit.HOURS)),
                1L, "alice", false, null);
        assertThat(migrator.translate(jwt)).isNull();
    }

    @Test
    void rejectsMissingOrInvalidUserId() {
        LegacyJwt nullId = new LegacyJwt(
                "HS256",
                Date.from(JwtTokenMigrator.RS256_CUTOFF_DATE.minusSeconds(60)),
                Date.from(Instant.now().plus(1, ChronoUnit.HOURS)),
                null, "alice", false, null);
        LegacyJwt zeroId = new LegacyJwt(
                "HS256",
                Date.from(JwtTokenMigrator.RS256_CUTOFF_DATE.minusSeconds(60)),
                Date.from(Instant.now().plus(1, ChronoUnit.HOURS)),
                0L, "alice", false, null);
        assertThat(migrator.translate(nullId)).isNull();
        assertThat(migrator.translate(zeroId)).isNull();
    }

    @Test
    void principalEqualityByUserId() {
        Principal a = new Principal(
                42L, "alice", "t1", Principal.TokenKind.LEGACY_HS256,
                java.util.Set.of("ROLE_USER"), 0L);
        Principal b = new Principal(
                42L, "ALICE", "t2", Principal.TokenKind.OAUTH2_RS256,
                java.util.Set.of("ROLE_ADMIN"), 0L);
        Principal c = new Principal(
                99L, "bob", "t3", Principal.TokenKind.LEGACY_HS256,
                java.util.Set.of("ROLE_USER"), 0L);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
    }
}