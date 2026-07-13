package com.ypat.work.application;

import java.util.Date;
import java.util.List;

/**
 * PR-13: Work list query use case.
 *
 * Replaces {@code WorkService.pageList(WorkListQo)} and
 * {@code WorkService.myWorks(...)}.
 *
 * Cursor-based pagination (V1.0 §2.3.2, V1.1 §3.1). The legacy
 * OFFSET-based pagination is gone; v2 always uses a tuple
 * cursor (publishTime, id) which scales to large tables.
 */
public interface QueryWorkListUseCase {

    /**
     * @param q list query. cursor is null for the first page.
     * @return page result + nextCursor for the caller to pass
     *         back as the next page's cursor.
     */
    Page list(ListQuery q);

    final class ListQuery {
        public final String city;        // nullable for "nationwide"
        public final List<String> tags;  // nullable for no tag filter
        public final Long cursorPublishTime; // nullable for first page
        public final Long cursorId;          // nullable for first page
        public final int size;          // <= 50

        public ListQuery(String city, List<String> tags,
                         Long cursorPublishTime, Long cursorId, int size) {
            this.city = city;
            this.tags = tags;
            this.cursorPublishTime = cursorPublishTime;
            this.cursorId = cursorId;
            this.size = size;
        }
    }

    final class Page {
        public final List<Item> items;
        public final Date nextCursorPublishTime;  // null when no more
        public final Long nextCursorId;          // null when no more

        public Page(List<Item> items, Date nextCursorPublishTime, Long nextCursorId) {
            this.items = items;
            this.nextCursorPublishTime = nextCursorPublishTime;
            this.nextCursorId = nextCursorId;
        }
    }

    final class Item {
        public final Long id;
        public final Long userId;
        public final String nickname;
        public final String coverObjectKey;
        public final String city;
        public final Date publishTime;
        public final Integer likeCount;
        public final Integer favoriteCount;

        public Item(Long id, Long userId, String nickname, String coverObjectKey,
                    String city, Date publishTime, Integer likeCount,
                    Integer favoriteCount) {
            this.id = id;
            this.userId = userId;
            this.nickname = nickname;
            this.coverObjectKey = coverObjectKey;
            this.city = city;
            this.publishTime = publishTime;
            this.likeCount = likeCount;
            this.favoriteCount = favoriteCount;
        }
    }
}