package com.ypat.work.infrastructure;

import com.ypat.work.infrastructure.persistence.StubWorkReadRepository;
import com.ypat.work.infrastructure.persistence.StubWorkReadRepository.WorkRow;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * PR-11: in-memory implementation of {@link StubWorkReadRepository}.
 *
 * Default profile — present everywhere until the JPA-backed
 * implementation lands with PR-15. PR-15 will mark this with
 * {@code @Profile("stub | test")} so the production boot picks
 * the JPA version automatically.
 *
 * Why ship a stub at all:
 *   - Lets the controller compile and run against something
 *     during the cut-over smoke test, before the JPA
 *     implementation is ready.
 *   - Lets the UseCases have unit tests without a Testcontainers
 *     MySQL. {@code StubWorkReadRepository} is the mock target;
 *     the stub provides a real (if empty) implementation when
 *     no mock is wired in.
 *
 * The stub returns empty data; the controllers see that and
 * return 200 with an empty list. That matches the V1.1 §6.1
 * Golden JSON baseline exactly (empty list is a valid response
 * shape).
 */
@Repository
public class InMemoryWorkReadRepository implements StubWorkReadRepository {

    @Override
    public Page list(String city, List<String> tags,
                     Date cursorPublishTime, Long cursorId, int size) {
        return new Page(Collections.emptyList(), null, null);
    }

    @Override
    public Optional<WorkRow> detail(long workId, Long viewerId) {
        return Optional.empty();
    }
}