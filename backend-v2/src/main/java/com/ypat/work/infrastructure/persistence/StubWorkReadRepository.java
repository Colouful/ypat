package com.ypat.work.infrastructure.persistence;

import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PR-11: minimal persistence contract for the work read path.
 *
 * PR-11 ships the interface only; the concrete JPA / native-query
 * implementation lands with PR-15 (user migration) when the User
 * entity is available to v2.
 *
 * Why an interface instead of a concrete class:
 *   - Lets {@link com.ypat.work.application.QueryWorkListUseCase}
 *     and {@link com.ypat.work.application.QueryWorkDetailUseCase}
 *     have one mock target each in unit tests.
 *   - Lets us swap implementations later (read-through cache,
 *     search-engine fallback) without touching the UseCases.
 *   - Modulith's verify() (PR-12) sees {@code
 *     infrastructure.persistence} as an internal package, so the
 *     UseCases in {@code application} cannot depend on the
 *     concrete class. They depend on this interface, which sits
 *     one layer above internal but below application — that's
 *     the Modulith-friendly place for it.
 */
@Repository
public interface StubWorkReadRepository {

    /** Cursor-paged list. Empty page when the cursor is exhausted. */
    Page list(String city,
              java.util.List<String> tags,
              java.util.Date cursorPublishTime,
              Long cursorId,
              int size);

    /** Single work by id, with media + tags pre-joined. */
    Optional<WorkRow> detail(long workId, Long viewerId);

    /** Page result. */
    final class Page {
        public final java.util.List<WorkRow> items;
        public final java.util.Date nextCursorPublishTime;
        public final Long nextCursorId;

        public Page(java.util.List<WorkRow> items,
                    java.util.Date nextCursorPublishTime,
                    Long nextCursorId) {
            this.items = items;
            this.nextCursorPublishTime = nextCursorPublishTime;
            this.nextCursorId = nextCursorId;
        }
    }

    /** Flat row representation — independent of any JPA entity. */
    final class WorkRow {
        public final long id;
        public final long userId;
        public final String nickname;
        public final String avatar;
        public final String description;
        public final String status;
        public final java.util.Date publishTime;
        public final String city;
        public final String area;
        public final Integer likeCount;
        public final Integer favoriteCount;
        public final Boolean isLikedByViewer;
        public final Boolean isFavoritedByViewer;
        public final java.util.List<MediaRow> media;
        public final java.util.List<String> tags;

        public WorkRow(long id, long userId, String nickname, String avatar,
                       String description, String status, java.util.Date publishTime,
                       String city, String area, Integer likeCount,
                       Integer favoriteCount, Boolean isLikedByViewer,
                       Boolean isFavoritedByViewer,
                       java.util.List<MediaRow> media, java.util.List<String> tags) {
            this.id = id;
            this.userId = userId;
            this.nickname = nickname;
            this.avatar = avatar;
            this.description = description;
            this.status = status;
            this.publishTime = publishTime;
            this.city = city;
            this.area = area;
            this.likeCount = likeCount;
            this.favoriteCount = favoriteCount;
            this.isLikedByViewer = isLikedByViewer;
            this.isFavoritedByViewer = isFavoritedByViewer;
            this.media = media;
            this.tags = tags;
        }
    }

    final class MediaRow {
        public final String objectKey;
        public final String contentType;
        public final Integer width;
        public final Integer height;
        public final Integer duration;
        public final boolean isCover;

        public MediaRow(String objectKey, String contentType, Integer width,
                        Integer height, Integer duration, boolean isCover) {
            this.objectKey = objectKey;
            this.contentType = contentType;
            this.width = width;
            this.height = height;
            this.duration = duration;
            this.isCover = isCover;
        }
    }
}