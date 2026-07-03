/**
 * auth module.
 *
 * Owner: v2, write: true, read: true, migrated_at: TBD
 *
 * Public API:
 *   - com.ypat.auth.api        (controllers, DTOs)
 *   - com.ypat.auth.application (login / token issuance / refresh)
 *
 * Everything else is internal until PR-14 lifts the auth layer.
 *
 * The {@code @NamedInterface("api")} annotation marks the
 * {@code com.ypat.auth.api} sub-package as the module's published
 * surface, so other modules (user, work) can depend on
 * {@code com.ypat.auth.api.Principal} and
 * {@code com.ypat.auth.api.TokenBridge} without Modulith's
 * verify() failing.
 */
@org.springframework.modulith.NamedInterface
@org.springframework.lang.NonNullApi
package com.ypat.auth;