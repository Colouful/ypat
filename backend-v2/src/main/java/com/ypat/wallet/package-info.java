/**
 * wallet module — beans (in-app currency), ledger, balance.
 *
 * Owner: v2, write: true, read: true, migrated_at: TBD
 *
 * Public API:
 *   - com.ypat.wallet.api
 *   - com.ypat.wallet.application
 *
 * Notes:
 *   - Strongly consistent ledger. Last to migrate (PR-19).
 *   - Per V1.1 §2.2 wallet is its own module, not a sub-domain
 *     of payment. Fault domain is independent.
 */
@org.springframework.lang.NonNullApi
package com.ypat.wallet;