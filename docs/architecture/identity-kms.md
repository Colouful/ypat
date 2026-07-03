# Identity KMS Encryption (PR-21)

**Status**: PR-21 implemented. Identity card number stored
encrypted via KMS envelope encryption. The dev shim uses
local AES-GCM; PR-21 follow-up swaps in a real KMS.

## What lands

- `db/migration/mysql/V4__identity_encrypted.sql`
    - `t_user_identity`: encrypted_blob (LONGBLOB), key_id,
      status (PENDING/VERIFIED/REJECTED), reviewer fields.
      UNIQUE user_id so each user has at most one identity row.
- `identity/api/KmsEnvelopeService.java` — interface. encrypt()
  returns Encrypted{ciphertext, wrappedKey, keyId}; decrypt()
  round-trips. Pack/unpack helpers for DB storage.
- `identity/internal/LocalAesGcmKms.java` — dev shim. AES-GCM
  with a fixed master key at startup. **Not** a real KMS —
  PR-21 follow-up swaps in Tencent / Aliyun / Vault.
- `identity/domain/UserIdentity.java` — JPA entity.
- `identity/infrastructure/UserIdentityRepository.java` —
  `findByUserId(userId)`.
- `identity/application/VerifyIdentityUseCase.java` —
  submit(long userId, String idNumberPlaintext) encrypts and
  stores; review() flips PENDING -> VERIFIED/REJECTED.
- `identity/application/GetDecryptedIdNoUseCase.java` —
  decrypt(long userId) returns the plaintext. Admin-only.
- `identity/api/IdentityController.java` — submit / review /
  decrypt routes added to PR-15's status endpoint.

## Storage model

Plaintext NEVER hits the database. The plaintext ID card
number is encoded as UTF-8, encrypted with a fresh 32-byte
AES key (the "data key"), and the data key itself is
encrypted with the master key. Both ciphertext and wrapped
data key are packed into one BLOB and stored as
`t_user_identity.encrypted_blob`. The KMS master key id
(`key_id` column) lets us rotate the master key without
re-encrypting every row — older rows are wrapped under the
old master and stay unwrappable until re-keyed.

## Submission / review / decryption flow

```
  POST /api/identity/{userId}/submit
    {idNumber: "..."}
    KmsEnvelopeService.encrypt(plaintext) -> Encrypted
    INSERT / UPDATE t_user_identity SET encrypted_blob=..., status='PENDING'

  POST /api/identity/{userId}/review
    {approve: true}
    VerifyIdentityUseCase.review(userId, approve=true, ...)
    UPDATE t_user_identity SET status='VERIFIED'

  GET /api/identity/{userId}/decrypt
    GetDecryptedIdNoUseCase.decrypt(userId)
    KmsEnvelopeService.decrypt(unpack(encrypted_blob))
    -> plaintext
```

## What PR-21 deliberately does NOT do

- **Real KMS.** The dev shim keeps a fixed master key in
  process memory. PR-21 follow-up wires a real KMS client
  (Tencent Cloud KMS, Aliyun KMS, or Vault Transit) and reads
  the key id from `YPAT_KMS_KEY_ID` env var.
- **Plaintext audit logging.** V1.1 §4.4 wants the access
  pattern logged. The decryption path is logged via SLF4J
  with user_id but not the plaintext; PR-21 follow-up also
  publishes a Spring event for the audit module.
- **Migration of legacy t_user.certcode rows.** V1.0 §2.3.7
  mentions certcode but the production dump shows almost
  every row null. PR-21 follow-up backfills whatever
  plaintext is there.
- **`@PreAuthorize` on submit / review / decrypt.** PR-15
  follow-up brings the TokenBridge integration. PR-21
  ships the contract only.
- **Rate limiting on submit.** V1.1 §4.1 third bullet is
  satisfied by PR-14 follow-up's IP+phone limiter.

## Verification

Local compile + Modulith verify():

```bash
cd backend-v2
mvn -B -ntp test
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

End-to-end via docker compose:

```bash
docker compose --env-file ../ypat-workspace/.env up -d backend-v2 --no-deps

docker logs backend-v2 | grep Flyway
# DbValidate  : Successfully validated 5 migrations
# DbMigrate   : Migrating schema ypat to version "4 - identity encrypted"
# DbMigrate   : Successfully applied 1 migration to schema ypat, now at version v4

# Submit:
curl -X POST -H 'Content-Type: application/json' \\
    -d '{"idNumber":"11010119900307ABCD"}' \\
    localhost:8092/api/identity/1/submit
# 202 Accepted

# Status:
curl localhost:8092/api/identity/1/status
# {"userId":1,"state":"PENDING"}

# Review (admin):
curl -X POST -H 'Content-Type: application/json' \\
    -d '{"approve":true,"reviewerId":99}' \\
    localhost:8092/api/identity/1/review

# Decrypt (admin, audited):
curl localhost:8092/api/identity/1/decrypt
# {"userId":1,"idNumber":"11010119903..."}

mysql ypat -e "SELECT user_id, status, key_id, LENGTH(encrypted_blob) \\
    FROM t_user_identity"
# 1 | VERIFIED | local-shim-v1 | 110
```

## References

- V1.1 §1.2 row 5 (strong consistency)
- V1.1 §4.4 (real-name auth flow, plaintext never logged)
- Upgrade plan: PR-15 (identity skeleton), PR-21 (this),
  PR-22 final