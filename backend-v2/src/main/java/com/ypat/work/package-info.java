/**
 * work module — photo work submission, detail, list, like,
 * favorite, complaint, and quick-apply. Migrates first because
 * it's the highest-traffic read path.
 *
 * Owner: v2, write: true, read: true, migrated_at: TBD
 *
 * Public API:
 *   - com.ypat.work.api
 *   - com.ypat.work.application
 *   - com.ypat.work.domain.{Work, WorkMedia}
 *
 * Internal (must NOT be referenced from another module):
 *   - com.ypat.work.internal
 *
 * The 591-line WorkService (system-domain) is split into 8
 * UseCases in PR-13. Until then this module is empty.
 */
@org.springframework.lang.NonNullApi
package com.ypat.work;