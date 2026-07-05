package com.ypat.identity.internal;

import com.ypat.identity.api.KmsEnvelopeService;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * PR-21: local-only AES-GCM KMS envelope shim.
 *
 * This is NOT a real KMS — there is no master key in HSM. PR-21
 * uses a fixed random key derived at startup; the on-disk
 * "wrapped key" is the same as the data key. The shape of the
 * API matches a real KMS so PR-21 follow-up can swap
 * implementation (Tencent KMS / Aliyun KMS / Vault) without
 * touching callers.
 *
 * This shim exists so v2 boots and the round-trip works
 * end-to-end inside the docker compose stack without an
 * external KMS dependency. The next PR replaces this with a
 * real KMS client + KMS key-id env var.
 */
@Service
public class LocalAesGcmKms implements KmsEnvelopeService {

    private static final SecureRandom RNG = new SecureRandom();
    private final SecretKey masterKey;

    public LocalAesGcmKms() throws Exception {
        // Fixed key for dev — replace with KMS master key fetch
        // in PR-21 follow-up. 32 bytes = AES-256.
        byte[] seed = new byte[32];
        for (int i = 0; i < seed.length; i++) seed[i] = (byte) (i * 7 + 13);
        this.masterKey = new SecretKeySpec(seed, "AES");
    }

    @Override
    public Encrypted encrypt(byte[] plaintext) {
        try {
            // generate a per-call data key (same length as master for shim)
            byte[] dataKeyBytes = new byte[32];
            RNG.nextBytes(dataKeyBytes);
            SecretKey dataKey = new SecretKeySpec(dataKeyBytes, "AES");

            // encrypt plaintext with data key (AES-GCM)
            byte[] iv = new byte[12];
            RNG.nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, dataKey, new GCMParameterSpec(128, iv));
            byte[] ct = c.doFinal(plaintext);

            // wrap data key with master key (also AES-GCM)
            byte[] iv2 = new byte[12];
            RNG.nextBytes(iv2);
            Cipher w = Cipher.getInstance("AES/GCM/NoPadding");
            w.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(128, iv2));
            byte[] wk = w.doFinal(dataKeyBytes);

            // store iv prefixes inline with ct / wk for round-trip simplicity
            byte[] ctFull = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, ctFull, 0, iv.length);
            System.arraycopy(ct, 0, ctFull, iv.length, ct.length);
            byte[] wkFull = new byte[iv2.length + wk.length];
            System.arraycopy(iv2, 0, wkFull, 0, iv2.length);
            System.arraycopy(wk, 0, wkFull, iv2.length, wk.length);

            return new Encrypted(ctFull, wkFull, "local-shim-v1");
        } catch (Exception e) {
            throw new RuntimeException("KMS encrypt failed", e);
        }
    }

    @Override
    public byte[] decrypt(Encrypted blob) {
        try {
            // unwrap data key with master key
            byte[] iv2 = new byte[12];
            System.arraycopy(blob.wrappedKey, 0, iv2, 0, 12);
            byte[] wk = new byte[blob.wrappedKey.length - 12];
            System.arraycopy(blob.wrappedKey, 12, wk, 0, wk.length);
            Cipher w = Cipher.getInstance("AES/GCM/NoPadding");
            w.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(128, iv2));
            byte[] dataKeyBytes = w.doFinal(wk);

            // decrypt ciphertext with data key
            byte[] iv = new byte[12];
            System.arraycopy(blob.ciphertext, 0, iv, 0, 12);
            byte[] ct = new byte[blob.ciphertext.length - 12];
            System.arraycopy(blob.ciphertext, 12, ct, 0, ct.length);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(dataKeyBytes, "AES"),
                    new GCMParameterSpec(128, iv));
            return c.doFinal(ct);
        } catch (Exception e) {
            throw new RuntimeException("KMS decrypt failed", e);
        }
    }
}