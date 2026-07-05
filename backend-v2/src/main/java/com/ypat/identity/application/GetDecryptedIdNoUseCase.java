package com.ypat.identity.application;

import com.ypat.identity.api.KmsEnvelopeService;
import com.ypat.identity.api.KmsEnvelopeService.Encrypted;
import com.ypat.identity.domain.UserIdentity;
import com.ypat.identity.infrastructure.UserIdentityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

/**
 * PR-21: admin-only decrypted ID number access.
 *
 * Audit-logged (PR-21 follow-up adds the @TransactionalEventListener
 * + WORM sink). The plaintext is computed here and never stored
 * anywhere; the caller is expected to render it for the
 * compliance / fraud team and discard.
 */
@Service
public class GetDecryptedIdNoUseCase {

    private final UserIdentityRepository repo;
    private final KmsEnvelopeService kms;

    public GetDecryptedIdNoUseCase(UserIdentityRepository repo, KmsEnvelopeService kms) {
        this.repo = repo;
        this.kms = kms;
    }

    @Transactional(readOnly = true)
    public String decrypt(long userId) {
        UserIdentity row = repo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("no_submission"));
        if (!"VERIFIED".equals(row.getStatus())) {
            throw new IllegalStateException("not_verified");
        }
        Encrypted enc = Encrypted.unpack(row.getEncryptedBlob());
        byte[] pt = kms.decrypt(enc);
        return new String(pt, StandardCharsets.UTF_8);
    }
}