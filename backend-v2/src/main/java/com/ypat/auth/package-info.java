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
 */
@org.springframework.lang.NonNullApi
package com.ypat.auth;