package com.ypat.identity.application;

import com.ypat.identity.api.KmsEnvelopeService;
import com.ypat.identity.domain.UserIdentity;
import com.ypat.identity.infrastructure.UserIdentityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * PR-21: identity verification.
 *
 * The plaintext ID card number is encrypted via KmsEnvelopeService
 * and stored as a byte[] in t_user_identity.encrypted_blob. The
 * master key never leaves the KMS (or, in the dev shim, never
 * leaves process memory). The reviewer/admin path goes through
 * GetDecryptedIdNoUseCase, which calls KmsEnvelopeService.decrypt.
 *
 * On submit we do NOT log the plaintext id number, do NOT
 * include it in any error response, and do NOT echo it back to
 * the caller. V1.1 §4.4 makes all of these explicit.
 */
@Service
public class VerifyIdentityUseCase {

    private final UserIdentityRepository repo;
    private final KmsEnvelopeService kms;

    public VerifyIdentityUseCase(UserIdentityRepository repo, KmsEnvelopeService kms) {
        this.repo = repo;
        this.kms = kms;
    }

    @Transactional
    public void submit(long userId, String idNumberPlaintext) {
        if (idNumberPlaintext == null || idNumberPlaintext.isBlank()) {
            throw new IllegalArgumentException("id_number_blank");
        }
        byte[] pt = idNumberPlaintext.getBytes(StandardCharsets.UTF_8);
        KmsEnvelopeService.Encrypted enc = kms.encrypt(pt);

        UserIdentity row = repo.findByUserId(userId).orElseGet(() -> {
            UserIdentity u = new UserIdentity();
            u.setUserId(userId);
            return u;
        });
        row.setEncryptedBlob(enc.pack());
        row.setKeyId(enc.keyId);
        row.setStatus("PENDING");
        Date now = new Date();
        row.setSubmittedAt(now);
        row.setCreatedAt(row.getCreatedAt() == null ? now : row.getCreatedAt());
        row.setUpdatedAt(now);
        repo.save(row);
    }

    /**
     * Review path. The reviewer sees a status enum, never the
     * plaintext. The decrypted form is exposed only via
     * {@link GetDecryptedIdNoUseCase} which is admin-only.
     */
    @Transactional
    public void review(long userId, boolean approve, String rejectReason, long reviewerId) {
        UserIdentity row = repo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("no_submission"));
        row.setStatus(approve ? "VERIFIED" : "REJECTED");
        row.setRejectReason(approve ? null : rejectReason);
        row.setReviewerId(reviewerId);
        row.setReviewedAt(new Date());
        row.setUpdatedAt(new Date());
        repo.save(row);
    }
}