package com.ypat.identity.api;

import java.nio.ByteBuffer;

/**
 * PR-21: KMS envelope encryption contract.
 *
 * Identity card numbers (and any other PII we don't want
 * sitting in plaintext at rest) are encrypted with a per-row
 * data key. The data key itself is encrypted by a KMS master
 * key. The plaintext data key only ever lives in memory during
 * the encrypt / decrypt call.
 *
 * The interface is intentionally minimal so the real KMS
 * (Tencent Cloud KMS / Aliyun KMS / AWS KMS / Vault Transit)
 * can drop in without touching the callers.
 */
public interface KmsEnvelopeService {

    /** Encrypt plaintext under a fresh data key. Returns the
     *  ciphertext blob and the wrapped (encrypted) data key
     *  in a single buffer for storage. */
    Encrypted encrypt(byte[] plaintext);

    /** Decrypt a previously-encrypted blob. Throws if the
     *  master key has rotated or the blob is tampered. */
    byte[] decrypt(Encrypted blob);

    /** Result of {@link #encrypt}. Two fields, both base64-safe. */
    final class Encrypted {
        public final byte[] ciphertext;     // AES-GCM of the plaintext
        public final byte[] wrappedKey;     // data key, encrypted by master key
        public final String keyId;          // KMS master key id

        public Encrypted(byte[] ciphertext, byte[] wrappedKey, String keyId) {
            this.ciphertext = ciphertext;
            this.wrappedKey = wrappedKey;
            this.keyId = keyId;
        }

        /** Pack for DB storage. Format is documented; PR-21 follow-up
         *  swaps to a real KMS payload format (e.g. KMS Envelope
         *  Encryption RFC). */
        public byte[] pack() {
            return ByteBuffer.allocate(4 + ciphertext.length + 4 + wrappedKey.length + 4 + keyId.length())
                    .putInt(ciphertext.length).put(ciphertext)
                    .putInt(wrappedKey.length).put(wrappedKey)
                    .putInt(keyId.length()).put(keyId.getBytes())
                    .array();
        }

        public static Encrypted unpack(byte[] packed) {
            ByteBuffer bb = ByteBuffer.wrap(packed);
            int cl = bb.getInt(); byte[] ct = new byte[cl]; bb.get(ct);
            int kl = bb.getInt(); byte[] wk = new byte[kl]; bb.get(wk);
            int idl = bb.getInt(); byte[] id = new byte[idl]; bb.get(id);
            return new Encrypted(ct, wk, new String(id));
        }
    }
}