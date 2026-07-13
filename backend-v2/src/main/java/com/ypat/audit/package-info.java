/**
 * audit module — admin action log, WORM storage sink.
 *
 * Owner: v2, write: true, read: true, migrated_at: TBD
 *
 * Public API:
 *   - com.ypat.audit.api.AuditLogger
 *
 * Notes:
 *   - Cross-cutting concern. P2 in the upgrade plan, but the
 *     package reservation is created now so other modules can
 *     start emitting audit events from their PR.
 */
@org.springframework.lang.NonNullApi
package com.ypat.audit;