package com.ypat.member.application;

import java.time.LocalDate;

/**
 * PR-17: Membership tier — sub-domain UseCase.
 *
 * V1.1 §2.2 folds member into user. This is the user-facing
 * representation of the member tier; it lives under
 * com.ypat.member because that's where the legacy code keeps
 * the tier / plan / expiry logic. It is reached from the user
 * UseCase (GetCurrentUserUseCase) — that UseCase reads this
 * Tier to populate the `memberTier` and `memberExpiry` fields
 * in CurrentUser.
 *
 * Future: when the database merge lands, this whole sub-domain
 * disappears and the tier fields become columns on t_user.
 */
public interface MembershipTier {

    /**
     * Returns the current tier and expiry for a user.
     * @return Tier.value: 0 = free, 1 = bronze, 2 = silver, 3 = gold
     */
    Tier current(long userId);

    /**
     * Extends (or downgrades) a user's membership.
     */
    void applyChange(long userId, Change change);

    /** Plain view-model: tier + expiry + last-change timestamp. */
    final class Tier {
        public final int value;            // 0/1/2/3
        public final String name;           // "free" / "bronze" / ...
        public final LocalDate expiresAt;
        public final LocalDate lastChanged;

        public Tier(int value, String name, LocalDate expiresAt, LocalDate lastChanged) {
            this.value = value;
            this.name = name;
            this.expiresAt = expiresAt;
            this.lastChanged = lastChanged;
        }
    }

    final class Change {
        public final long userId;
        public final int newTier;        // 0/1/2/3
        public final LocalDate newExpiresAt;
        public final String reason;      // "purchase" / "refund" / "admin"

        public Change(long userId, int newTier, LocalDate newExpiresAt, String reason) {
            this.userId = userId;
            this.newTier = newTier;
            this.newExpiresAt = newExpiresAt;
            this.reason = reason;
        }
    }
}