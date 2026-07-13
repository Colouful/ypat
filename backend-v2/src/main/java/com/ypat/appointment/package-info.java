/**
 * appointment module — scheduled photography sessions between
 * users. Migrates after user (PR-15) because both endpoints of
 * the appointment must resolve to a real Principal.
 *
 * Owner: v2, write: true, read: true, migrated_at: TBD
 *
 * Public API:
 *   - com.ypat.appointment.api
 *   - com.ypat.appointment.application
 *
 * State machine lives in the implementation (PR-16 follow-up):
 *   PENDING -> CONFIRMED / CANCELLED / EXPIRED
 *   CONFIRMED -> COMPLETED / CANCELLED
 * The contract enforces the allowed transitions; the
 * implementation enforces them atomically against the DB.
 *
 * @NamedInterface marks com.ypat.appointment.api as published.
 */
@org.springframework.modulith.NamedInterface
@org.springframework.lang.NonNullApi
package com.ypat.appointment;