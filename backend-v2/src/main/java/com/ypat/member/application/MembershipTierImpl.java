package com.ypat.member.application;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * PR-17: stub implementation. Returns tier=0 (free) with no
 * expiry. The real implementation (PR-17 follow-up) reads
 * t_user_member + t_member_plan and writes through the same
 * Flyway-managed transaction as the wallet ledger.
 */
@Service
public class MembershipTierImpl implements MembershipTier {

    @Override
    public Tier current(long userId) {
        return new Tier(0, "free", null, null);
    }

    @Override
    public void applyChange(long userId, Change change) {
        // No-op until PR-17 follow-up wires the t_user_member
        // + t_member_plan tables through Flyway.
    }
}