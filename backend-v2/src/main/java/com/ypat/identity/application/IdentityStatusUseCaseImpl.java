package com.ypat.identity.application;

import com.ypat.identity.domain.UserIdentity;
import com.ypat.identity.infrastructure.UserIdentityRepository;
import org.springframework.stereotype.Service;

/**
 * PR-21 follow-up: real implementation.
 *
 * Reads t_user_identity (PR-21 schema) and maps the row's
 * status string to the IdentityStatusUseCase.State enum.
 * Returns UNVERIFIED when the user has no submission row at
 * all (the common case for newly-registered users).
 */
@Service
public class IdentityStatusUseCaseImpl implements IdentityStatusUseCase {

    private final UserIdentityRepository repo;

    public IdentityStatusUseCaseImpl(UserIdentityRepository repo) {
        this.repo = repo;
    }

    @Override
    public Status status(long userId) {
        return repo.findByUserId(userId)
                .map(row -> new Status(userId, mapState(row.getStatus())))
                .orElseGet(() -> new Status(userId, State.UNVERIFIED));
    }

    private static State mapState(String s) {
        if (s == null) return State.UNVERIFIED;
        return switch (s) {
            case "PENDING"  -> State.PENDING;
            case "VERIFIED" -> State.VERIFIED;
            case "REJECTED" -> State.REJECTED;
            default          -> State.UNVERIFIED;
        };
    }
}