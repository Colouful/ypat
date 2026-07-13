/**
 * notification module — SMS, push, in-app message center.
 *
 * Owner: v2, write: true, read: true, migrated_at: TBD
 *
 * Public API:
 *   - com.ypat.notification.api
 *   - com.ypat.notification.application
 *
 * Notes:
 *   - Async-driven. Can ship in parallel with strongly-consistent
 *     modules (PR-18).
 */
@org.springframework.lang.NonNullApi
package com.ypat.notification;