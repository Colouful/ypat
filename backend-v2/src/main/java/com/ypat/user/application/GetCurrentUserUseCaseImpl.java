package com.ypat.user.application;

import org.springframework.stereotype.Service;

/**
 * PR-15: stub implementation. Returns a placeholder for any
 * caller. The JPA implementation lands with PR-15 final once
 * the v2 User entity is wired up against MySQL.
 */
@Service
public class GetCurrentUserUseCaseImpl implements GetCurrentUserUseCase {

    @Override
    public CurrentUser get(com.ypat.auth.api.Principal principal) {
        if (principal == null) return null;
        return new CurrentUser(
                principal.userId(),
                "", "", "", "", "",
                0, null,
                principal.roles(),
                false);
    }
}