# Favorite / Like Idempotency (PR-09)

**Status**: PR-09 implemented. Java idempotent methods landed;
SQL staged in `tools/ddl/` until PR-07b flips Flyway on.

## Why this matters

V1.0 §2.3.3 and V1.1 §3.2 both flag the same concurrency hole
in `WorkService.like / unlike / favorite / unfavorite`:

```
1. existsByWorkIdAndUserId(workId, userId)  -- SELECT
2. if (exists) throw "already liked"        -- branch
3. save(like)                               -- INSERT
4. workRepository.incrLikeCount(workId)     -- UPDATE counter
```

Steps 1 and 3 are not atomic. Two concurrent "first like"
requests both see step 1 return false, both INSERT; the loser
hits `DataIntegrityViolation` and the user gets a 500. Worse,
in some interleavings both succeed at step 1 (no row yet) and
both pass step 2, but only the first INSERT survives the unique
key — step 4 then runs for both, double-incrementing the
counter.

## What PR-09 does

PR-09 lands the safe path; it does not yet switch controllers
over to it.

### 1. SQL — `tools/ddl/V3__favorite_like_unique_keys.sql`

```sql
ALTER TABLE t_work_like
    ADD UNIQUE INDEX uk_work_like_user_work (work_id, user_id),
    ALGORITHM = INPLACE, LOCK = NONE;

ALTER TABLE t_work_favorite
    ADD UNIQUE INDEX uk_work_favorite_user_work (work_id, user_id),
    ALGORITHM = INPLACE, LOCK = NONE;
```

> **Pre-flight:** the ALTER will fail with `Duplicate entry` if
> existing rows already contain duplicates. The dedup script
> lives at `tools/ddl/rollback/V3__favorite_like_unique_keys.sql`
> as a comment block — read it before applying.

> **Schema reality vs. V1.1 §3.2:** V1.1 §3.2 imagined a single
> `t_favorite` table with a `biz_type` column to distinguish
> like vs favorite. The actual schema is two separate tables
> (`t_work_like` + `t_work_favorite`), each shaped
> `(id, work_id, user_id, created_at)`. The keys here match
> that layout.

### 2. Java — `WorkService.likeIdempotent / unlikeIdempotent / favoriteIdempotent / unfavoriteIdempotent`

These four methods live at the end of `WorkService.java`. They
do **not** pre-check existence:

```java
public void likeIdempotent(Long workId, Long userId) {
    WorkLike like = new WorkLike();
    like.setWorkId(workId);
    like.setUserId(userId);
    like.setCreatedAt(new Date());
    try {
        workLikeRepository.save(like);
    } catch (DataIntegrityViolationException dup) {
        return;                     // already liked — idempotent OK
    }
    workRepository.incrLikeCount(workId);
}
```

The unique key from step 1 is the database-side guard. Java
catches the violation and treats it as success. Cancel
(unlike / unfavorite) uses `deleteByWorkIdAndUserId` which
returns an affected row count; zero means "wasn't there" and
we return without decrementing the counter.

## Why the legacy methods stay

The existing `like()` / `favorite()` throw `SysException`
("already liked") on the duplicate case. Several controllers
and external scripts depend on that exact error contract. PR-09
keeps them frozen and adds new methods alongside. Switching
controllers over is a separate, larger PR:

- **PR-13** (WorkService split) tears `WorkService` apart into
  the per-use-case service classes. The idempotent methods
  move with their owner (likeIdempotent → `LikeWorkUseCase`,
  etc.) and the legacy `like()` is deleted at the same time.
- **PR-15** (user migration) is when the controllers get
  rewritten on top of the new service classes.

Until then, the old methods keep working as they always have,
PR-09 just removes the "race condition under load" trapdoor.

## What PR-09 deliberately does NOT do

- **Switch controllers over.** That requires the WorkService
  split (PR-13) so the new methods land in their right home.
- **Add unit tests for the new methods.** PR-13 is where the
  WorkService test surface gets redone; writing throwaway
  tests now is wasted work.
- **Replace `incrLikeCount` / `decrLikeCount` with
  `GREATEST(count ± 1, 0)`.** The current counters can go
  negative if the cancel path runs without a prior like. The
  fix is mechanical (one line in `WorkRepository`) but
  belongs in PR-13 alongside the rest of the WorkService
  rewrite.
- **Migration to Flyway.** SQL lives in `tools/ddl/` for now
  because Flyway is still `enabled: false` (PR-07a). After
  PR-07b lands, this V3 file moves into
  `backend-v2/db/migration/mysql/` in a follow-up PR.

## Operator runbook

```bash
# 1. Pre-flight dedup (read tools/ddl/rollback/V3*.sql).

mysql -h "$YPAT_MYSQL_HOST" -u root -p ypat <<'SQL'
SELECT work_id, user_id, COUNT(*) cnt
  FROM t_work_like
 GROUP BY work_id, user_id
HAVING cnt > 1;

SELECT work_id, user_id, COUNT(*) cnt
  FROM t_work_favorite
 GROUP BY work_id, user_id
HAVING cnt > 1;
SQL

# If anything returned cnt > 1, run the dedup DELETE first
# (also in the rollback file as a commented SQL block).

# 2. Apply the keys.
mysql -h "$YPAT_MYSQL_HOST" -u root -p ypat \
  < tools/ddl/V3__favorite_like_unique_keys.sql

# 3. Confirm.
mysql -h "$YPAT_MYSQL_HOST" -u root -p ypat -e "
  SHOW INDEX FROM t_work_like    WHERE Key_name='uk_work_like_user_work';
  SHOW INDEX FROM t_work_favorite WHERE Key_name='uk_work_favorite_user_work';
"
```

Application rollout happens in PR-13, not here.

## Risk if applied without PR-13

Until the controllers switch, the old `like()` / `favorite()`
methods still do their pre-check. With the unique key in
place:

- Concurrent first-like: loser hits `DataIntegrityViolation`
  inside `save()`, not caught by the old code path → 500 to
  the user.
- Cancel-of-never-liked: returns "未点赞" SysException. The
  counter never goes negative (no UPDATE happens).

So the SQL change alone **introduces** a small regression in
the concurrent-first-like path. That's why PR-09 keeps the new
methods dormant until PR-13 wires them up. If you want the
keys in production early, ship them the same day as the
controller switch — don't ship them alone.

## References

- V1.0 §2.3.3 (点赞和收藏计数存在并发一致性问题)
- V1.1 §3.2 (点赞/收藏幂等与计数)
- PR plan: PR-09 (this), PR-13 (WorkService split, controller swap)