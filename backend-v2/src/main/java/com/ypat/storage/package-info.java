/**
 * storage module — abstract file storage interface.
 *
 * Owner: v2, write: true, read: true, migrated_at: TBD
 *
 * Public API:
 *   - com.ypat.storage.api.StorageService
 *   - com.ypat.storage.api.MediaMetadata
 *
 * Internal (must NOT be referenced from another module):
 *   - com.ypat.storage.internal.{CosAdapter, FastdfsAdapter, ...}
 *
 * Notes:
 *   - Per V1.1 §2.2 storage is *infrastructure*, not a business
 *     module. It depends on persistence and external SDKs but
 *     no business module may depend on its internals.
 *   - COS first-write lands in PR-10. FastDFS stays as a
 *     read-fallback only.
 */
@org.springframework.lang.NonNullApi
package com.ypat.storage;