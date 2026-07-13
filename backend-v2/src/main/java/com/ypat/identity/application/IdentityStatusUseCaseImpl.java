package com.ypat.identity.application;

import org.springframework.stereotype.Service;

/**
 * PR-15: stub implementation. Always reports UNVERIFIED.
 * PR-21 brings the real flow + KMS envelope encryption.
 */
@Service
public class IdentityStatusUseCaseImpl implements IdentityStatusUseCase {

    @Override
    public Status status(long userId) {
        return new Status(userId, State.UNVERIFIED);
    }
}