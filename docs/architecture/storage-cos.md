# Storage Abstraction (PR-10)

**Status**: PR-10 implemented. COS adapter + routing live in
backend-v2; SQL staged in `tools/ddl/`.

## What PR-10 lands

| File | Purpose |
|---|---|
| `backend-v2/src/main/java/com/ypat/storage/api/StorageService.java` | The public interface. upload / read / delete. |
| `backend-v2/src/main/java/com/ypat/storage/api/MediaMetadata.java` | Provider / key / content-type / size. JDK 17 record-equivalent. |
| `backend-v2/src/main/java/com/ypat/storage/internal/CosStorageAdapter.java` | COS implementation. Lazy client init, idempotent delete, 404-as-FileNotFoundException. |
| `backend-v2/src/main/java/com/ypat/storage/internal/RoutingStorageService.java` | The bean callers see. Per-provider routing + read fallback chain. |
| `tools/ddl/V4__work_media_storage_columns.sql` | ADD COLUMN storage_provider / object_key / checksum_sha256 + backfill. |
| `tools/ddl/rollback/V4__work_media_storage_columns.sql` | Paired DROP. |

backend-v2/pom.xml gains `com.qcloud:cos_api:5.6.97`.

## Why this shape

The pre-PR-10 state is `system-wap/util/FastDFSClient.java` —
a static helper that talks to the FastDFS tracker, returns the
full URL, and is called directly from controllers. V1.0 §2.3.5
calls out three problems with that shape:

1. The URL is **stored in the row** (column `url` in
   `t_work_media`). Switching CDN / host later means
   backfilling every row.
2. FastDFS is the **only** target. There's no adapter to slot
   in a replacement.
3. The Client / Service separation is gone — controllers know
   about `FastDFSClient.getUploadToken()` directly.

PR-10 fixes (1) and (2). (3) is PR-11's job: the work-upload
controller moves into backend-v2 and stops importing
`com.ypat.util.FastDFSClient` outright.

## Schema delta

`V4__work_media_storage_columns.sql` adds three columns and
backfills `object_key` from existing `url` values:

```sql
ALTER TABLE t_work_media
    ADD COLUMN storage_provider VARCHAR(16) NOT NULL DEFAULT 'FASTDFS',
    ADD COLUMN object_key VARCHAR(255) NULL,
    ADD COLUMN checksum_sha256 CHAR(64) NULL,
    ALGORITHM = INSTANT;
```

`ALGORITHM = INSTANT` for ADD COLUMN with DEFAULT is supported
on MySQL 8 and means no table rewrite. The follow-up UPDATE is
a normal DML — operators run it in the off-peak window or via
`pt-online-schema-change`.

`url` is kept untouched for now. New code does not write to it;
the eventual `StorageUrlResolver` (PR-11) reads
`storage_provider` + `object_key` and assembles a fresh URL on
demand.

## Configuration

`application.yml`:

```yaml
ypat:
  storage:
    write-provider: COS
    read-fallback: COS,FASTDFS
    cos:
      region: ${YPAT_COS_REGION:ap-guangzhou}
      bucket: ${YPAT_COS_BUCKET:ypat-dev-placeholder}
      secret-id: ${YPAT_COS_SECRET_ID:?YPAT_COS_SECRET_ID is required}
      secret-key: ${YPAT_COS_SECRET_KEY:?YPAT_COS_SECRET_KEY is required}
```

The `?:` fail-fast syntax (PR-02 pattern) means a missing secret
aborts startup rather than failing at first upload. Profile
overrides in `application-{dev,staging,prod}.yml` set the real
values.

## What PR-10 deliberately does NOT do

- **FastDFS adapter.** The legacy client stays where it is;
  PR-11 brings the read-fallback adapter that wraps it. Until
  then `read-fallback: COS,FASTDFS` silently ignores the
  FASTDFS slot.
- **StorageUrlResolver.** Belongs in PR-11 with the work-read
  controller. Knowing when a URL needs a signed query string
  is tightly coupled to the consumer's auth model.
- **Multi-part upload.** V1.1 §5.2 mentions it for ≥50 MB
  media. Defer until the upload controller actually needs it.
- **Checksum enforcement.** The `checksum_sha256` column is
  added but the upload path doesn't compute it yet. PR-11
  fills it in once the work-upload UseCase is in place.
- **Backfill automation.** The script backfills existing
  `object_key` from `url`. For >1M rows run it via
  `pt-online-schema-change` in chunks.

## Verification

Local compile only — there's no live COS account wired in:

```bash
cd backend-v2
mvn -B -ntp -DskipTests package
[INFO] BUILD SUCCESS
```

The actual upload / read / delete paths only get exercised when
an operator-supplied dev credential lands in
`application-dev.yml` and a Spring Boot startup gets through
the `${...:?}` env check.

## Operator runbook (for the column add)

```bash
# 1. Confirm current row count so you know what backfill is in flight.
mysql ... -e "SELECT COUNT(*) FROM t_work_media;"

# 2. Apply the ADD COLUMN. INSTANT = no rewrite, no lock.
mysql ... < tools/ddl/V4__work_media_storage_columns.sql

# 3. Apply the UPDATE. Off-peak window.
mysql ... < tools/ddl/V4__work_media_storage_columns.sql   # script contains both

# 4. Spot-check.
mysql ... -e "
  SELECT storage_provider, COUNT(*) cnt
    FROM t_work_media
   GROUP BY storage_provider;
"
```

If the result shows rows where `storage_provider = 'FASTDFS'`
but `object_key IS NULL`, the URL pattern didn't match. Most
likely cause: a row whose `url` is empty or null. Decide
case-by-case.

## References

- V1.0 §2.3.5 (文件 URL 与 FastDFS 强耦合)
- V1.1 §5 (文件存储双实现与作品模块重构)
- Upgrade plan: PR-10 (this), PR-11 (FastDFS adapter + UrlResolver + work upload)