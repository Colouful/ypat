package com.ypat.work.api;

import com.ypat.work.application.QueryWorkDetailUseCase;
import com.ypat.work.application.QueryWorkListUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PR-11: read-only Work controller, v2's first HTTP-facing code.
 *
 * Routes:
 *   GET /api/work/list?city=...&size=...&cursor=...
 *   GET /api/work/{id}
 *
 * Both routes are reachable via Nginx prefix /api/work/* once the
 * cut-over (5% -> 25% -> 50% -> 100%) starts. The legacy
 * system-wap / system-restapi stay on the same prefix until the
 * last percentage step.
 *
 * This controller never reads from the legacy WorkService directly;
 * the request only ever touches v2 code. If v2 returns 5xx, the
 * Nginx upstream health check flips traffic back to legacy. That's
 * the rollback story.
 *
 * The stub implementations behind the UseCases (PR-11) return
 * empty data. PR-15 swaps the stub for the real JPA-backed
 * implementation; the controller does not change.
 */
@RestController
@RequestMapping("/api/work")
public class WorkController {

    private final QueryWorkListUseCase list;
    private final QueryWorkDetailUseCase detail;

    public WorkController(QueryWorkListUseCase list, QueryWorkDetailUseCase detail) {
        this.list = list;
        this.detail = detail;
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listWorks(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Long cursorPublishTime,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false, defaultValue = "20") int size) {

        QueryWorkListUseCase.Page page = list.list(
                new QueryWorkListUseCase.ListQuery(city, tags, cursorPublishTime, cursorId, size));

        Map<String, Object> body = new HashMap<>();
        body.put("items", page.items);
        body.put("nextCursorPublishTime", page.nextCursorPublishTime);
        body.put("nextCursorId", page.nextCursorId);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWork(@PathVariable long id) {
        QueryWorkDetailUseCase.WorkDetail d = detail.detail(
                new QueryWorkDetailUseCase.DetailQuery(id, null));
        if (d == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(d);
    }
}