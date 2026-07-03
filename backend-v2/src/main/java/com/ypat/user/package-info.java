/**
 * user module — accounts, profiles, and member tier.
 *
 * Owner: v2, write: true, read: true, migrated_at: TBD
 *
 * Public API:
 *   - com.ypat.user.api
 *   - com.ypat.user.application
 *   - com.ypat.user.domain.User
 *
 * Notes:
 *   - The legacy code keeps "user" and "member" as separate
 *     modules. Per V1.1 §2.2 we merge them into a single user
 *     module in PR-17 (member becomes a sub-domain).
 *   - This package-info claims the whole "user" space; member/
 *     is reserved for the legacy imports that we will re-route.
 *   - The {@code @NamedInterface("api")} annotation marks
 *     {@code com.ypat.user.api} as the module's published
 *     surface; UserController lives there.
 */
@org.springframework.modulith.NamedInterface
@org.springframework.lang.NonNullApi
package com.ypat.user;