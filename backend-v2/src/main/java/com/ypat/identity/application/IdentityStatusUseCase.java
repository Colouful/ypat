package com.ypat.identity.application;

/**
 * PR-15: Identity (实名) status query.
 *
 * V1.1 §4.4 mandates that the identity card number is stored
 * encrypted via KMS envelope encryption. v2's API surface
 * therefore exposes a {@link Status} that says "verified" /
 * "not verified" / "pending" without leaking the ID itself.
 * The full ID never leaves the server. The verification flow
 * (encrypted upload + decryption for review) is PR-21.
 */
public interface IdentityStatusUseCase {

    Status status(long userId);

    final class Status {
        public final long userId;
        public final State state;

        public Status(long userId, State state) {
            this.userId = userId;
            this.state = state;
        }
    }

    enum State {
        /** No identity verification on file. */
        UNVERIFIED,
        /** Real-name auth submitted, awaiting review. */
        PENDING,
        /** Real-name auth passed; full ID stored encrypted server-side. */
        VERIFIED,
        /** Real-name auth rejected; user must re-submit. */
        REJECTED
    }
}