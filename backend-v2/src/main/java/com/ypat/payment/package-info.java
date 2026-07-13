/**
 * payment module — WeChat Pay callback, idempotency, pay log.
 *
 * Owner: v2, write: true, read: true, migrated_at: TBD
 *
 * Public API:
 *   - com.ypat.payment.api
 *   - com.ypat.payment.application
 *
 * Notes:
 *   - Strongly consistent + external integration. Last to migrate
 *     (PR-20).
 *   - Three-side sign-off required: 风控 / 法务 / 财务.
 */
@org.springframework.lang.NonNullApi
package com.ypat.payment;