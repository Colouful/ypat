# Work Use Case Split (PR-13)

**Status**: PR-13 implemented. Eight UseCase interfaces landed
in `backend-v2/work/application/`. Implementations + controller
swap are later PRs (PR-15+).

## Why this matters

V1.0 §2.3.1 calls out that `WorkService` (684 lines,
`backend/system-domain/src/main/java/com/ypat/service/WorkService.java`)
is doing too much. One class:

1. Validates input
2. Persists Work + WorkMedia + WorkTag
3. Maps WorkStatus / AuditReason / read_count / like_count
4. Replies to like / favorite / complain / quick-apply
5. Returns `Map<String, Object>` everywhere (V1.0 §5 calls
   this out specifically: "业务模块返回 `Map<String, Object>`")

All 8 public methods (plus the 4 idempotent ones PR-09 added)
share the same 8 injected repositories. That's the textbook
"big service class" anti-pattern.

## The eight UseCases

| UseCase | Replaces (legacy) | Public methods |
|---|---|---|
| `SubmitWorkUseCase` | `WorkService.submit` | `submit(SubmitCommand) → SubmitResult` |
| `QueryWorkDetailUseCase` | `WorkService.getDetail` | `detail(DetailQuery) → WorkDetail` |
| `QueryWorkListUseCase` | `WorkService.pageList`, `WorkService.myWorks` | `list(ListQuery) → Page` (cursor-based) |
| `LikeWorkUseCase` | `WorkService.likeIdempotent / unlikeIdempotent` | `like`, `unlike` |
| `FavoriteWorkUseCase` | `WorkService.favoriteIdempotent / unfavoriteIdempotent` | `favorite`, `unfavorite` |
| `OfflineWorkUseCase` | `WorkService.offline` | `offline` |
| `ComplainWorkUseCase` | `WorkService.complain` | `complain` |
| `QuickApplyWorkUseCase` | `WorkService.quickApply` | `quickApply(QuickApplyCommand) → QuickApplyResult` |

All eight live in `backend-v2/work/application/`. The input and
output DTOs are inner final classes of each UseCase interface —
no separate `dto/` package needed for v2.

## Type-safety wins

Every UseCase returns a typed result. The legacy `Map<String,
Object>` disappears. Examples:

- `WorkDetail` carries `List<Media>` and `List<String>` for the
  tags — never `Map<String, Object>`.
- `SubmitResult` is `(Long workId, String status)`, not
  `Map<String, Object>` with magic string keys.
- `Page` carries an explicit `(nextCursorPublishTime,
  nextCursorId)` cursor pair — caller code can't accidentally
  drop the tiebreaker.

Cursor pagination replaces OFFSET (V1.0 §2.3.2). The
`(publishTime, id)` cursor matches the covering index from PR-08
(`idx_city_status_pub`).

## Why interfaces, not concrete classes

The interfaces are what controllers depend on. The implementations
live one layer down (`work.application.internal.*` or
`work.infrastructure.*`) and Spring binds them via
`@Service`. That gives us:

- A clear seam for unit tests: a UseCase has one mock target,
  not eight injected repositories.
- A place to hang `@Transactional` boundaries on the concrete
  class without polluting the public interface.
- An easy place to swap implementations later (e.g. a
  read-through cache UseCase that wraps the real one).

## What PR-13 deliberately does NOT do

- **Implement the UseCases.** PR-13 is the contract. PR-15+
  (user migration, work-write migration) is where the concrete
  classes land, with real repositories behind them.
- **Touch the legacy `WorkService`.** It stays where it is until
  the controller cut-over. That cut-over is the moment we delete
  `WorkService` and its 8 (now 12 with idempotent) public methods.
- **Wire controllers.** `system-web` / `system-wap` controllers
  still call `WorkService.submit(...)`, etc. The cut-over is
  when those imports flip to `UseCase` interfaces.
- **Add unit tests.** Mocking 8 repositories per UseCase is
  mechanical; the meaningful tests are the ones that exercise
  the UseCase-implementation against a Testcontainers MySQL.
  Those arrive with the implementation PR.

## Verification

Local compile only — no Spring Boot context yet for the work
package:

```bash
cd backend-v2
mvn -B -ntp -DskipTests package
[INFO] BUILD SUCCESS
```

CI runs the same on JDK 21. Modulith's `verify()` (PR-12)
already accepts the work module because the eight UseCases
respect the `..application..` boundary.

## Migration map

| Phase | PR | What happens to WorkService |
|---|---|---|
| Now | PR-13 (this) | UseCase interfaces exist; controllers still call WorkService |
| Read-only cut | PR-11 | New v2 controllers handle `GET /work/list`, `GET /work/{id}` via QueryWorkListUseCase / QueryWorkDetailUseCase. Legacy WorkService stays for write paths. |
| User migration | PR-15 | User-facing controllers move to v2. Auth/session bridging via LegacyToken. |
| Write migration | PR-15+ | `POST /work/submit` etc. move to v2. SubmitWorkUseCase gets its concrete class. |
| Storage cut | PR-10 follow-up | SubmitWorkUseCase uploads via StorageService from PR-10. |
| Cleanup | PR-13 follow-up | `WorkService` deleted; legacy `WorkSubmitQo` etc. deleted; system-domain shrinks. |

## References

- V1.0 §2.3.1 (业务 Service 过大)
- V1.0 §2.3.2 (N+1 + 全表扫描)
- V1.0 §5 (work/ 包结构)
- V1.1 §3.1 (游标分页)
- PR-08 (覆盖索引)
- PR-09 (幂等 like / favorite)
- PR-10 (Storage abstraction)