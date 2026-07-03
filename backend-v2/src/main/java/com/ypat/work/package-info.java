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
 * The {@code @NamedInterface("api")} annotation marks
 * {@code com.ypat.work.api} as the module's published surface.
 * WorkController lives there and is the entry point other
 * modules (and external clients) hit.
 */
@org.springframework.modulith.NamedInterface
@org.springframework.lang.NonNullApi
package com.ypat.work;