/**
 * identity module — real-name authentication, ID card encryption.
 *
 * Owner: v2, write: true, read: true, migrated_at: TBD
 *
 * Public API:
 *   - com.ypat.identity.api
 *   - com.ypat.identity.application
 *
 * Notes:
 *   - ID numbers are stored encrypted via KMS envelope encryption.
 *   - Last to migrate (PR-21).
 *   - The {@code @NamedInterface("api")} annotation marks
 *     {@code com.ypat.identity.api} as the module's published
 *     surface; IdentityController lives there.
 */
@org.springframework.modulith.NamedInterface
@org.springframework.lang.NonNullApi
package com.ypat.identity;