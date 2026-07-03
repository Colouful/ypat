package com.ypat.work.application;

import java.util.List;

/**
 * PR-13: Work detail query use case.
 *
 * Replaces {@code WorkService.getDetail(WorkDetailQo)}.
 * Returns a flat DTO so the controller doesn't have to know
 * about the Work / WorkMedia / WorkTag entity trio.
 */
public interface QueryWorkDetailUseCase {

    WorkDetail detail(DetailQuery q);

    final class DetailQuery {
        public final Long workId;
        public final Long viewerId;       // nullable: anonymous view

        public DetailQuery(Long workId, Long viewerId) {
            this.workId = workId;
            this.viewerId = viewerId;
        }
    }

    final class WorkDetail {
        public final Long id;
        public final Long userId;
        public final String nickname;
        public final String avatar;
        public final String description;
        public final String status;
        public final java.util.Date publishTime;
        public final String city;
        public final String area;
        public final String shootLocation;
        public final Integer readCount;
        public final Integer likeCount;
        public final Integer favoriteCount;
        public final Boolean isLiked;
        public final Boolean isFavorited;
        public final List<Media> media;
        public final List<String> tags;

        public WorkDetail(Long id, Long userId, String nickname, String avatar,
                          String description, String status, java.util.Date publishTime,
                          String city, String area, String shootLocation,
                          Integer readCount, Integer likeCount, Integer favoriteCount,
                          Boolean isLiked, Boolean isFavorited,
                          List<Media> media, List<String> tags) {
            this.id = id;
            this.userId = userId;
            this.nickname = nickname;
            this.avatar = avatar;
            this.description = description;
            this.status = status;
            this.publishTime = publishTime;
            this.city = city;
            this.area = area;
            this.shootLocation = shootLocation;
            this.readCount = readCount;
            this.likeCount = likeCount;
            this.favoriteCount = favoriteCount;
            this.isLiked = isLiked;
            this.isFavorited = isFavorited;
            this.media = media;
            this.tags = tags;
        }
    }

    final class Media {
        public final String objectKey;
        public final String contentType;
        public final Integer width;
        public final Integer height;
        public final Integer duration;
        public final Boolean isCover;

        public Media(String objectKey, String contentType, Integer width,
                     Integer height, Integer duration, Boolean isCover) {
            this.objectKey = objectKey;
            this.contentType = contentType;
            this.width = width;
            this.height = height;
            this.duration = duration;
            this.isCover = isCover;
        }
    }
}