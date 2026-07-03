package com.ypat.work.application;

/**
 * PR-13: Work submission use case.
 *
 * Replaces {@code WorkService.submit(WorkSubmitQo)} from
 * system-domain. PR-15+ implements this against the v2 entity set.
 *
 * Sibling method on WorkService (legacy, do not call from v2):
 *   - submit(WorkSubmitQo qo)
 */
public interface SubmitWorkUseCase {

    SubmitResult submit(SubmitCommand cmd);

    /** Input DTO. Fields map to the legacy WorkSubmitQo. */
    final class SubmitCommand {
        public final Long userId;
        public final String description;
        public final String device;
        public final String shootLocation;
        public final String mediaType;       // IMAGE / VIDEO
        public final java.util.List<MediaRef> media;
        public final java.util.List<String> tags;
        public final String city;
        public final String area;

        public SubmitCommand(Long userId,
                             String description,
                             String device,
                             String shootLocation,
                             String mediaType,
                             java.util.List<MediaRef> media,
                             java.util.List<String> tags,
                             String city,
                             String area) {
            this.userId = userId;
            this.description = description;
            this.device = device;
            this.shootLocation = shootLocation;
            this.mediaType = mediaType;
            this.media = media;
            this.tags = tags;
            this.city = city;
            this.area = area;
        }
    }

    /** Media uploaded but not yet persisted to a row. */
    final class MediaRef {
        public final String objectKey;
        public final String contentType;
        public final long sizeBytes;
        public final Integer width;
        public final Integer height;
        public final Integer duration;

        public MediaRef(String objectKey, String contentType, long sizeBytes,
                        Integer width, Integer height, Integer duration) {
            this.objectKey = objectKey;
            this.contentType = contentType;
            this.sizeBytes = sizeBytes;
            this.width = width;
            this.height = height;
            this.duration = duration;
        }
    }

    /** Output of a successful submit. */
    final class SubmitResult {
        public final Long workId;
        public final String status;

        public SubmitResult(Long workId, String status) {
            this.workId = workId;
            this.status = status;
        }
    }
}