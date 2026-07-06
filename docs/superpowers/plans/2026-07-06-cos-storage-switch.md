# COS Storage Switch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the new frontend/admin upload backend paths with a configurable backend storage abstraction that can use `COS(COS 对象存储)` or `FastDFS(FastDFS 文件存储)` while keeping URL-compatible responses.

**Architecture:** Add a focused `StorageService(存储服务)` in `backend/system-wap(新版移动端和管理后台后端)`, with `FastDfsStorageService(FastDFS 存储实现)` and `CosStorageService(COS 存储实现)` selected by config. Route only the new upload paths through this service, keep local watermarking, add COS env config, and change work media delete to a database soft delete.

**Tech Stack:** Java 8, Spring Boot 1.5.9, Maven, JUnit 4, Tencent Cloud COS Java SDK, Vue 3, UniApp, Vitest, pnpm.

---

## File Structure

- Create `backend/system-wap/src/main/java/com/ypat/storage/StorageBizPath.java`: enum for `admin/`, `ypat/`, `work/`, `avatar/`, `realname/`.
- Create `backend/system-wap/src/main/java/com/ypat/storage/StoredObject.java`: immutable-ish value object returned by uploads.
- Create `backend/system-wap/src/main/java/com/ypat/storage/StorageService.java`: common upload/delete/url contract.
- Create `backend/system-wap/src/main/java/com/ypat/storage/FastDfsStorageService.java`: wraps existing `FastDFSClient(FastDFS 客户端)`.
- Create `backend/system-wap/src/main/java/com/ypat/storage/CosStorageProperties.java`: binds `system.third.storage.cos.*`.
- Create `backend/system-wap/src/main/java/com/ypat/storage/CosObjectKeyFactory.java`: deterministic key generation and URL parsing.
- Create `backend/system-wap/src/main/java/com/ypat/storage/CosStorageService.java`: uploads to COS through Tencent SDK.
- Create `backend/system-wap/src/main/java/com/ypat/storage/StorageConfiguration.java`: selects provider and validates required COS config.
- Modify `backend/system-wap/src/main/java/com/ypat/config/SystemConfig.java`: add `storage_provider` and keep existing config style.
- Modify `backend/system-wap/src/main/resources/conf/sys_conf.properties`: add storage provider and COS environment-variable bindings using `${YPAT_COS_*}`.
- Modify `backend/system-wap/src/main/resources/dev/application.yml`, `pre/application.yml`, `pro/application.yml`: define provider/default env prefix values.
- Modify `backend/.env.development.example`, `backend/.env.staging.example`, `backend/.env.production.example`, `.env.example`: document COS env variables.
- Modify `backend/system-wap/pom.xml`: add `cos_api(COS Java SDK)` dependency.
- Modify `backend/system-wap/src/main/java/com/ypat/controller/AdminUploadController.java`: use `StorageService`.
- Modify `backend/system-wap/src/main/java/com/ypat/service/WorkMediaService.java`: use `StorageService` and soft delete.
- Modify `backend/system-wap/src/main/java/com/ypat/controller/YpatInfoController.java`: route Base64 submit images through `StorageService`.
- Modify `backend/system-wap/src/main/java/com/ypat/controller/OauthController.java`: route realname Base64 images through `StorageService`.
- Modify `backend/system-wap/src/main/java/com/ypat/controller/UserController.java`: route avatar Base64 images through `StorageService`.
- Modify `backend/system-domain/src/main/java/com/ypat/entity/WorkMedia.java`: add `deletedAt`.
- Modify `backend/system-domain/src/main/java/com/ypat/repository/WorkMediaRepository.java`: add non-deleted queries and soft-delete update.
- Modify `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`: filter soft-deleted media during bind/list/detail.
- Create `docs/sql/pending/V_cos_storage_switch_work_media_soft_delete.sql`: schema migration for `deleted_at`.
- Add/update backend JUnit tests under `backend/system-wap/src/test/java/com/ypat/storage`, `backend/system-wap/src/test/java/com/ypat/service`, `backend/system-wap/src/test/java/com/ypat/controller`, and `backend/system-domain/src/test/java/com/ypat/service`.
- Add source tests in `frontend/src/api/__tests__` and `frontend-admin/tests/unit` proving the frontends still call backend APIs and do not expose COS secrets.

---

### Task 0: Create Dedicated Worktree

**Files:**
- No source files changed.

- [ ] **Step 1: Confirm the base branch is clean enough to branch**

Run:

```bash
git status --short --branch
git branch --show-current
```

Expected: current branch is `dev6`; unrelated `.omx/state/session.json` may be modified and must not be staged.

- [ ] **Step 2: Create the implementation worktree from `dev6`**

Run from `/Users/lizhenwei/workspace/vueworkspace/ypat-workspace`:

```bash
git worktree add ../ypat-cos-storage-switch -b codex/cos-storage-switch dev6
cd ../ypat-cos-storage-switch
```

Expected: new worktree exists at `/Users/lizhenwei/workspace/vueworkspace/ypat-cos-storage-switch` on branch `codex/cos-storage-switch`.

- [ ] **Step 3: Verify the design and plan files are present**

Run:

```bash
test -f docs/superpowers/specs/2026-07-06-cos-storage-switch-design.md
test -f docs/superpowers/plans/2026-07-06-cos-storage-switch.md
git status --short --branch
```

Expected: both files exist; branch is `codex/cos-storage-switch`.

---

### Task 1: Add Storage Configuration Tests

**Files:**
- Create: `backend/system-wap/src/test/java/com/ypat/storage/CosStoragePropertiesTest.java`
- Create: `backend/system-wap/src/test/java/com/ypat/storage/CosObjectKeyFactoryTest.java`
- Modify later: `backend/system-wap/src/main/java/com/ypat/storage/CosStorageProperties.java`
- Modify later: `backend/system-wap/src/main/java/com/ypat/storage/CosObjectKeyFactory.java`

- [ ] **Step 1: Write failing tests for COS required config and key generation**

Create `backend/system-wap/src/test/java/com/ypat/storage/CosStoragePropertiesTest.java`:

```java
package com.ypat.storage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CosStoragePropertiesTest {

    @Test
    public void providerDefaultsToFastdfsWhenBlank() {
        CosStorageProperties properties = new CosStorageProperties();
        assertEquals("fastdfs", properties.normalizedProvider());
        assertFalse(properties.isCosEnabled());
    }

    @Test
    public void cosProviderRequiresAllConnectionFields() {
        CosStorageProperties properties = new CosStorageProperties();
        properties.setProvider("cos");
        properties.setSecretId("sid");
        properties.setSecretKey("skey");
        properties.setRegion("ap-guangzhou");
        properties.setBucket("ypat-1250000000");
        properties.setPublicBaseUrl("https://cdn.example.test");
        properties.setEnvPrefix("pro");

        assertTrue(properties.isCosEnabled());
        properties.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void cosProviderFailsWhenSecretKeyMissing() {
        CosStorageProperties properties = new CosStorageProperties();
        properties.setProvider("cos");
        properties.setSecretId("sid");
        properties.setRegion("ap-guangzhou");
        properties.setBucket("ypat-1250000000");
        properties.setPublicBaseUrl("https://cdn.example.test");
        properties.setEnvPrefix("pro");

        properties.validate();
    }
}
```

Create `backend/system-wap/src/test/java/com/ypat/storage/CosObjectKeyFactoryTest.java`:

```java
package com.ypat.storage;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CosObjectKeyFactoryTest {

    @Test
    public void createsEnvironmentAndBusinessScopedObjectKey() {
        CosObjectKeyFactory factory = new CosObjectKeyFactory("pro");
        String key = factory.createKey(StorageBizPath.WORK, "photo.jpeg", new Date(1783296000000L), "fixeduuid");

        assertEquals("pro/work/2026/07/06/fixeduuid.jpeg", key);
    }

    @Test
    public void fallsBackToJpgWhenOriginalFileHasNoExtension() {
        CosObjectKeyFactory factory = new CosObjectKeyFactory("dev");
        String key = factory.createKey(StorageBizPath.AVATAR, "avatar", new Date(1783296000000L), "abc");

        assertEquals("dev/avatar/2026/07/06/abc.jpg", key);
    }

    @Test
    public void stripsPublicBaseUrlToObjectKey() {
        CosObjectKeyFactory factory = new CosObjectKeyFactory("pre");

        assertEquals(
                "pre/work/2026/07/06/a.jpg",
                factory.extractObjectKey("https://cdn.example.test/files/", "https://cdn.example.test/files/pre/work/2026/07/06/a.jpg")
        );
        assertNull(factory.extractObjectKey("https://cdn.example.test/files", "https://other.example.test/pre/work/a.jpg"));
        assertTrue(factory.supportsUrl("https://cdn.example.test/files/", "https://cdn.example.test/files/pre/work/a.jpg"));
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=CosStoragePropertiesTest,CosObjectKeyFactoryTest test
```

Expected: compilation fails because `CosStorageProperties`, `CosObjectKeyFactory`, and `StorageBizPath` do not exist.

- [ ] **Step 3: Commit failing tests**

```bash
git add backend/system-wap/src/test/java/com/ypat/storage/CosStoragePropertiesTest.java backend/system-wap/src/test/java/com/ypat/storage/CosObjectKeyFactoryTest.java
git commit -m "test: define COS storage configuration behavior" -m "Capture the storage provider defaults, required COS credentials, and deterministic object key layout before adding the implementation." -m "Tested: mvn -pl system-wap -Dtest=CosStoragePropertiesTest,CosObjectKeyFactoryTest test (expected compile failure before implementation)" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 2: Implement Storage Configuration and Key Helpers

**Files:**
- Create: `backend/system-wap/src/main/java/com/ypat/storage/StorageBizPath.java`
- Create: `backend/system-wap/src/main/java/com/ypat/storage/CosStorageProperties.java`
- Create: `backend/system-wap/src/main/java/com/ypat/storage/CosObjectKeyFactory.java`

- [ ] **Step 1: Implement `StorageBizPath`**

Create `backend/system-wap/src/main/java/com/ypat/storage/StorageBizPath.java`:

```java
package com.ypat.storage;

public enum StorageBizPath {
    ADMIN("admin"),
    YPAT("ypat"),
    WORK("work"),
    AVATAR("avatar"),
    REALNAME("realname");

    private final String path;

    StorageBizPath(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
```

- [ ] **Step 2: Implement `CosStorageProperties`**

Create `backend/system-wap/src/main/java/com/ypat/storage/CosStorageProperties.java`:

```java
package com.ypat.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "system.third.storage")
public class CosStorageProperties {
    private String provider = "fastdfs";
    private String secretId;
    private String secretKey;
    private String region;
    private String bucket;
    private String publicBaseUrl;
    private String envPrefix = "dev";

    public String normalizedProvider() {
        return isBlank(provider) ? "fastdfs" : provider.trim().toLowerCase();
    }

    public boolean isCosEnabled() {
        return "cos".equals(normalizedProvider());
    }

    public void validate() {
        if (!isCosEnabled()) return;
        require("YPAT_COS_SECRET_ID", secretId);
        require("YPAT_COS_SECRET_KEY", secretKey);
        require("YPAT_COS_REGION", region);
        require("YPAT_COS_BUCKET", bucket);
        require("YPAT_COS_PUBLIC_BASE_URL", publicBaseUrl);
        require("YPAT_COS_ENV_PREFIX", envPrefix);
    }

    private void require(String name, String value) {
        if (isBlank(value)) {
            throw new IllegalStateException("COS storage requires " + name);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getSecretId() { return secretId; }
    public void setSecretId(String secretId) { this.secretId = secretId; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }
    public String getPublicBaseUrl() { return publicBaseUrl; }
    public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl; }
    public String getEnvPrefix() { return envPrefix; }
    public void setEnvPrefix(String envPrefix) { this.envPrefix = envPrefix; }
}
```

- [ ] **Step 3: Implement `CosObjectKeyFactory`**

Create `backend/system-wap/src/main/java/com/ypat/storage/CosObjectKeyFactory.java`:

```java
package com.ypat.storage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CosObjectKeyFactory {
    private final String envPrefix;

    public CosObjectKeyFactory(String envPrefix) {
        this.envPrefix = trimSlash(envPrefix);
    }

    public String createKey(StorageBizPath bizPath, String originalFilename, Date now, String uuid) {
        String datePath = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(now);
        return envPrefix + "/" + bizPath.path() + "/" + datePath + "/" + uuid + "." + extension(originalFilename);
    }

    public boolean supportsUrl(String publicBaseUrl, String url) {
        return extractObjectKey(publicBaseUrl, url) != null;
    }

    public String extractObjectKey(String publicBaseUrl, String url) {
        String base = trimTrailingSlash(publicBaseUrl);
        if (base == null || url == null) return null;
        String text = url.trim();
        if (!text.startsWith(base + "/")) return null;
        String key = text.substring(base.length() + 1);
        return key.startsWith(envPrefix + "/") ? key : null;
    }

    private String extension(String originalFilename) {
        if (originalFilename == null) return "jpg";
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) return "jpg";
        String ext = originalFilename.substring(dot + 1).toLowerCase(Locale.ENGLISH);
        if ("jpeg".equals(ext) || "jpg".equals(ext) || "png".equals(ext) || "webp".equals(ext) || "gif".equals(ext) || "mp4".equals(ext) || "mov".equals(ext)) {
            return ext;
        }
        return "jpg";
    }

    private String trimSlash(String value) {
        if (value == null || value.trim().isEmpty()) return "dev";
        String text = value.trim();
        while (text.startsWith("/")) text = text.substring(1);
        while (text.endsWith("/")) text = text.substring(0, text.length() - 1);
        return text.isEmpty() ? "dev" : text;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String text = value.trim();
        while (text.endsWith("/")) text = text.substring(0, text.length() - 1);
        return text.isEmpty() ? null : text;
    }
}
```

- [ ] **Step 4: Run storage helper tests**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=CosStoragePropertiesTest,CosObjectKeyFactoryTest test
```

Expected: PASS.

- [ ] **Step 5: Commit implementation**

```bash
git add backend/system-wap/src/main/java/com/ypat/storage/StorageBizPath.java backend/system-wap/src/main/java/com/ypat/storage/CosStorageProperties.java backend/system-wap/src/main/java/com/ypat/storage/CosObjectKeyFactory.java
git commit -m "feat: add storage configuration helpers" -m "Add provider configuration and deterministic COS object key generation for the new backend-proxied storage flow." -m "Tested: mvn -pl system-wap -Dtest=CosStoragePropertiesTest,CosObjectKeyFactoryTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 3: Add Storage Service Contract and FastDFS Adapter

**Files:**
- Create: `backend/system-wap/src/main/java/com/ypat/storage/StoredObject.java`
- Create: `backend/system-wap/src/main/java/com/ypat/storage/StorageService.java`
- Create: `backend/system-wap/src/main/java/com/ypat/storage/FastDfsStorageService.java`
- Test: `backend/system-wap/src/test/java/com/ypat/storage/FastDfsStorageServiceSourceTest.java`

- [ ] **Step 1: Write source test for FastDFS adapter behavior**

Create `backend/system-wap/src/test/java/com/ypat/storage/FastDfsStorageServiceSourceTest.java`:

```java
package com.ypat.storage;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FastDfsStorageServiceSourceTest {

    @Test
    public void fastDfsAdapterWrapsClientAndReturnsStoredObject() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/storage/FastDfsStorageService.java");

        assertTrue(source.contains("implements StorageService"));
        assertTrue(source.contains("fastDFSClient.uploanFile1"));
        assertTrue(source.contains("new StoredObject(fileId, url)"));
        assertTrue(source.contains("Storage upload failed"));
        assertFalse(source.contains("SecretKey"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-wap/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=FastDfsStorageServiceSourceTest test
```

Expected: FAIL because `FastDfsStorageService.java` does not exist.

- [ ] **Step 3: Implement value object and interface**

Create `backend/system-wap/src/main/java/com/ypat/storage/StoredObject.java`:

```java
package com.ypat.storage;

public class StoredObject {
    private final String objectKey;
    private final String url;

    public StoredObject(String objectKey, String url) {
        this.objectKey = objectKey;
        this.url = url;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getUrl() {
        return url;
    }
}
```

Create `backend/system-wap/src/main/java/com/ypat/storage/StorageService.java`:

```java
package com.ypat.storage;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {
    StoredObject upload(InputStream inputStream, String originalFilename, String contentType, StorageBizPath bizPath) throws IOException;

    boolean supportsUrl(String url);

    String extractObjectKey(String url);

    void deleteByUrl(String url);
}
```

- [ ] **Step 4: Implement FastDFS adapter**

Create `backend/system-wap/src/main/java/com/ypat/storage/FastDfsStorageService.java`:

```java
package com.ypat.storage;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.config.SystemConfig;
import com.ypat.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;

public class FastDfsStorageService implements StorageService {

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private SystemConfig systemConfig;

    @Override
    public StoredObject upload(InputStream inputStream, String originalFilename, String contentType, StorageBizPath bizPath) throws IOException {
        String fileId = fastDFSClient.uploanFile1(inputStream, originalFilename);
        if (fileId == null) {
            throw new SysException(ResponseCode.FAIL_UPLOAD, "Storage upload failed");
        }
        String url = joinPublicFileUrl(systemConfig.getFdfs_path(), fileId);
        return new StoredObject(fileId, url);
    }

    @Override
    public boolean supportsUrl(String url) {
        return extractObjectKey(url) != null;
    }

    @Override
    public String extractObjectKey(String url) {
        return extractFastDfsFileId(systemConfig.getFdfs_path(), url);
    }

    @Override
    public void deleteByUrl(String url) {
        String fileId = extractObjectKey(url);
        if (fileId == null) return;
        int slash = fileId.indexOf('/');
        if (slash <= 0) return;
        fastDFSClient.deleteFile(fileId.substring(0, slash), fileId.substring(slash + 1));
    }

    public static String joinPublicFileUrl(String publicBaseUrl, String fileId) {
        String base = trimSlashes(publicBaseUrl, false);
        String path = trimSlashes(fileId, true);
        if (base == null || path == null) {
            throw new SysException(ResponseCode.FAIL_UPLOAD, "文件访问地址未配置");
        }
        return base + "/" + path;
    }

    public static String extractFastDfsFileId(String publicBaseUrl, String url) {
        String base = trimSlashes(publicBaseUrl, false);
        if (base == null || url == null || url.trim().isEmpty()) return null;
        String text = url.trim();
        if (!text.startsWith(base + "/")) return null;
        String fileId = trimSlashes(text.substring(base.length()), true);
        return fileId != null && fileId.startsWith("group") && fileId.indexOf('/') > 0 ? fileId : null;
    }

    private static String trimSlashes(String value, boolean leading) {
        if (value == null) return null;
        String text = value.trim();
        if (text.isEmpty()) return null;
        if (leading) {
            while (text.startsWith("/")) text = text.substring(1);
        } else {
            while (text.endsWith("/")) text = text.substring(0, text.length() - 1);
        }
        return text.isEmpty() ? null : text;
    }
}
```

- [ ] **Step 5: Update old URL tests to target adapter helpers**

Modify `backend/system-wap/src/test/java/com/ypat/service/WorkMediaServiceUrlTest.java` by replacing `WorkMediaService.joinPublicFileUrl` with `FastDfsStorageService.joinPublicFileUrl`, and `WorkMediaService.extractFastDfsFileId` with `FastDfsStorageService.extractFastDfsFileId`. Add this import:

```java
import com.ypat.storage.FastDfsStorageService;
```

- [ ] **Step 6: Run tests**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=FastDfsStorageServiceSourceTest,WorkMediaServiceUrlTest test
```

Expected: PASS.

- [ ] **Step 7: Commit adapter**

```bash
git add backend/system-wap/src/main/java/com/ypat/storage/StoredObject.java backend/system-wap/src/main/java/com/ypat/storage/StorageService.java backend/system-wap/src/main/java/com/ypat/storage/FastDfsStorageService.java backend/system-wap/src/test/java/com/ypat/storage/FastDfsStorageServiceSourceTest.java backend/system-wap/src/test/java/com/ypat/service/WorkMediaServiceUrlTest.java
git commit -m "feat: add FastDFS storage adapter" -m "Introduce the shared storage contract and wrap the existing FastDFS client without changing upload behavior." -m "Tested: mvn -pl system-wap -Dtest=FastDfsStorageServiceSourceTest,WorkMediaServiceUrlTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 4: Add COS SDK and COS Storage Service

**Files:**
- Modify: `backend/system-wap/pom.xml`
- Create: `backend/system-wap/src/main/java/com/ypat/storage/CosStorageService.java`
- Create: `backend/system-wap/src/test/java/com/ypat/storage/CosStorageServiceSourceTest.java`

- [ ] **Step 1: Write source test for COS implementation safety**

Create `backend/system-wap/src/test/java/com/ypat/storage/CosStorageServiceSourceTest.java`:

```java
package com.ypat.storage;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CosStorageServiceSourceTest {

    @Test
    public void cosStorageUsesSdkWithoutLoggingSecrets() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/storage/CosStorageService.java");

        assertTrue(source.contains("new COSClient"));
        assertTrue(source.contains("new BasicCOSCredentials"));
        assertTrue(source.contains("PutObjectRequest"));
        assertTrue(source.contains("ObjectMetadata"));
        assertTrue(source.contains("properties.getBucket()"));
        assertTrue(source.contains("new StoredObject(key, publicUrl(key))"));
        assertFalse(source.contains("logger.info(properties.getSecretKey()"));
        assertFalse(source.contains("logger.error(properties.getSecretKey()"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-wap/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 2: Add COS dependency**

Modify `backend/system-wap/pom.xml` inside `<dependencies>`:

```xml
        <dependency>
            <groupId>com.qcloud</groupId>
            <artifactId>cos_api</artifactId>
            <version>5.6.227</version>
        </dependency>
```

- [ ] **Step 3: Implement COS storage service**

Create `backend/system-wap/src/main/java/com/ypat/storage/CosStorageService.java`:

```java
package com.ypat.storage;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

public class CosStorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(CosStorageService.class);

    private final CosStorageProperties properties;
    private final CosObjectKeyFactory keyFactory;

    public CosStorageService(CosStorageProperties properties) {
        this.properties = properties;
        this.properties.validate();
        this.keyFactory = new CosObjectKeyFactory(properties.getEnvPrefix());
    }

    @Override
    public StoredObject upload(InputStream inputStream, String originalFilename, String contentType, StorageBizPath bizPath) throws IOException {
        String key = keyFactory.createKey(bizPath, originalFilename, new Date(), UUID.randomUUID().toString().replace("-", ""));
        ObjectMetadata metadata = new ObjectMetadata();
        if (contentType != null && !contentType.trim().isEmpty()) {
            metadata.setContentType(contentType);
        }
        COSClient client = createClient();
        try {
            PutObjectRequest request = new PutObjectRequest(properties.getBucket(), key, inputStream, metadata);
            client.putObject(request);
            return new StoredObject(key, publicUrl(key));
        } catch (RuntimeException e) {
            logger.error("COS upload failed provider=cos bizPath={} type={} error={}", bizPath.path(), contentType, e.getClass().getSimpleName());
            throw new SysException(ResponseCode.FAIL_UPLOAD, "Storage upload failed");
        } finally {
            client.shutdown();
        }
    }

    @Override
    public boolean supportsUrl(String url) {
        return keyFactory.supportsUrl(properties.getPublicBaseUrl(), url);
    }

    @Override
    public String extractObjectKey(String url) {
        return keyFactory.extractObjectKey(properties.getPublicBaseUrl(), url);
    }

    @Override
    public void deleteByUrl(String url) {
        String key = extractObjectKey(url);
        if (key == null) return;
        COSClient client = createClient();
        try {
            client.deleteObject(properties.getBucket(), key);
        } finally {
            client.shutdown();
        }
    }

    private COSClient createClient() {
        COSCredentials credentials = new BasicCOSCredentials(properties.getSecretId(), properties.getSecretKey());
        ClientConfig config = new ClientConfig(new Region(properties.getRegion()));
        return new COSClient(credentials, config);
    }

    private String publicUrl(String key) {
        String base = properties.getPublicBaseUrl().trim();
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/" + key;
    }
}
```

- [ ] **Step 4: Run COS source and helper tests**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=CosStorageServiceSourceTest,CosStoragePropertiesTest,CosObjectKeyFactoryTest test
```

Expected: PASS.

- [ ] **Step 5: Commit COS implementation**

```bash
git add backend/system-wap/pom.xml backend/system-wap/src/main/java/com/ypat/storage/CosStorageService.java backend/system-wap/src/test/java/com/ypat/storage/CosStorageServiceSourceTest.java
git commit -m "feat: add COS storage implementation" -m "Add the Tencent COS SDK-backed storage service with safe logging and URL-compatible public object responses." -m "Tested: mvn -pl system-wap -Dtest=CosStorageServiceSourceTest,CosStoragePropertiesTest,CosObjectKeyFactoryTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 5: Wire Provider Selection and Environment Config

**Files:**
- Create: `backend/system-wap/src/main/java/com/ypat/storage/StorageConfiguration.java`
- Modify: `backend/system-wap/src/main/resources/conf/sys_conf.properties`
- Modify: `backend/system-wap/src/main/resources/dev/application.yml`
- Modify: `backend/system-wap/src/main/resources/pre/application.yml`
- Modify: `backend/system-wap/src/main/resources/pro/application.yml`
- Modify: `backend/.env.development.example`
- Modify: `backend/.env.staging.example`
- Modify: `backend/.env.production.example`
- Modify: `.env.example`
- Test: `backend/system-wap/src/test/java/com/ypat/storage/StorageConfigurationSourceTest.java`

- [ ] **Step 1: Write source test for provider selection**

Create `backend/system-wap/src/test/java/com/ypat/storage/StorageConfigurationSourceTest.java`:

```java
package com.ypat.storage;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class StorageConfigurationSourceTest {

    @Test
    public void storageConfigurationSelectsFastdfsOrCosFromProvider() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/storage/StorageConfiguration.java");

        assertTrue(source.contains("@Configuration"));
        assertTrue(source.contains("@Bean"));
        assertTrue(source.contains("StorageService storageService"));
        assertTrue(source.contains("properties.isCosEnabled()"));
        assertTrue(source.contains("return new CosStorageService(properties)"));
        assertTrue(source.contains("return new FastDfsStorageService()"));
    }

    @Test
    public void sysConfExposesCosEnvironmentVariables() throws Exception {
        String source = read("backend/system-wap/src/main/resources/conf/sys_conf.properties");

        assertTrue(source.contains("system.third.storage.provider = ${YPAT_STORAGE_PROVIDER:fastdfs}"));
        assertTrue(source.contains("system.third.storage.secret_id = ${YPAT_COS_SECRET_ID:}"));
        assertTrue(source.contains("system.third.storage.secret_key = ${YPAT_COS_SECRET_KEY:}"));
        assertTrue(source.contains("system.third.storage.region = ${YPAT_COS_REGION:}"));
        assertTrue(source.contains("system.third.storage.bucket = ${YPAT_COS_BUCKET:}"));
        assertTrue(source.contains("system.third.storage.public_base_url = ${YPAT_COS_PUBLIC_BASE_URL:}"));
        assertTrue(source.contains("system.third.storage.env_prefix = ${YPAT_COS_ENV_PREFIX:dev}"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-wap/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 2: Implement `StorageConfiguration`**

Create `backend/system-wap/src/main/java/com/ypat/storage/StorageConfiguration.java`:

```java
package com.ypat.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfiguration {

    @Bean
    public StorageService storageService(CosStorageProperties properties) {
        if (properties.isCosEnabled()) {
            return new CosStorageService(properties);
        }
        return new FastDfsStorageService();
    }
}
```

- [ ] **Step 3: Add config keys to `sys_conf.properties`**

Append to `backend/system-wap/src/main/resources/conf/sys_conf.properties` after file storage:

```properties
# 统一存储开关：fastdfs 或 cos
system.third.storage.provider = ${YPAT_STORAGE_PROVIDER:fastdfs}
system.third.storage.secret_id = ${YPAT_COS_SECRET_ID:}
system.third.storage.secret_key = ${YPAT_COS_SECRET_KEY:}
system.third.storage.region = ${YPAT_COS_REGION:}
system.third.storage.bucket = ${YPAT_COS_BUCKET:}
system.third.storage.public_base_url = ${YPAT_COS_PUBLIC_BASE_URL:}
system.third.storage.env_prefix = ${YPAT_COS_ENV_PREFIX:dev}
```

- [ ] **Step 4: Set env-specific prefixes in application configs**

In `backend/system-wap/src/main/resources/dev/application.yml`, under `system.third`, add:

```yaml
    storage:
      provider: ${YPAT_STORAGE_PROVIDER:fastdfs}
      env_prefix: ${YPAT_COS_ENV_PREFIX:dev}
```

In `backend/system-wap/src/main/resources/pre/application.yml`, under `system.third`, add:

```yaml
    storage:
      provider: ${YPAT_STORAGE_PROVIDER:fastdfs}
      env_prefix: ${YPAT_COS_ENV_PREFIX:pre}
```

In `backend/system-wap/src/main/resources/pro/application.yml`, under `system.third`, add:

```yaml
    storage:
      provider: ${YPAT_STORAGE_PROVIDER:fastdfs}
      env_prefix: ${YPAT_COS_ENV_PREFIX:pro}
```

- [ ] **Step 5: Add env template values**

Add this block after the existing FastDFS block in `backend/.env.development.example`:

```dotenv
# === Storage Provider / Tencent COS ===
YPAT_STORAGE_PROVIDER=fastdfs
YPAT_COS_SECRET_ID=
YPAT_COS_SECRET_KEY=
YPAT_COS_REGION=ap-guangzhou
YPAT_COS_BUCKET=
YPAT_COS_PUBLIC_BASE_URL=
YPAT_COS_ENV_PREFIX=dev
```

Add this block after the existing FastDFS block in `backend/.env.staging.example`:

```dotenv
# === Storage Provider / Tencent COS ===
YPAT_STORAGE_PROVIDER=fastdfs
YPAT_COS_SECRET_ID=
YPAT_COS_SECRET_KEY=
YPAT_COS_REGION=
YPAT_COS_BUCKET=
YPAT_COS_PUBLIC_BASE_URL=
YPAT_COS_ENV_PREFIX=pre
```

Add this block after the existing FastDFS block in `backend/.env.production.example`:

```dotenv
# === Storage Provider / Tencent COS ===
YPAT_STORAGE_PROVIDER=fastdfs
YPAT_COS_SECRET_ID=
YPAT_COS_SECRET_KEY=
YPAT_COS_REGION=
YPAT_COS_BUCKET=
YPAT_COS_PUBLIC_BASE_URL=
YPAT_COS_ENV_PREFIX=pro
```

Add this block after the FastDFS public URL block in root `.env.example`:

```dotenv
# === Storage Provider / Tencent COS ===
YPAT_STORAGE_PROVIDER=fastdfs
YPAT_COS_SECRET_ID=
YPAT_COS_SECRET_KEY=
YPAT_COS_REGION=ap-guangzhou
YPAT_COS_BUCKET=
YPAT_COS_PUBLIC_BASE_URL=https://cdn.example.invalid
YPAT_COS_ENV_PREFIX=pre
```

- [ ] **Step 6: Run config tests**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=StorageConfigurationSourceTest,CosStoragePropertiesTest test
```

Expected: PASS.

- [ ] **Step 7: Commit provider wiring**

```bash
git add backend/system-wap/src/main/java/com/ypat/storage/StorageConfiguration.java backend/system-wap/src/main/resources/conf/sys_conf.properties backend/system-wap/src/main/resources/dev/application.yml backend/system-wap/src/main/resources/pre/application.yml backend/system-wap/src/main/resources/pro/application.yml backend/.env.development.example backend/.env.staging.example backend/.env.production.example .env.example backend/system-wap/src/test/java/com/ypat/storage/StorageConfigurationSourceTest.java
git commit -m "feat: wire configurable storage provider" -m "Expose COS and FastDFS provider selection through the existing environment-backed configuration style." -m "Tested: mvn -pl system-wap -Dtest=StorageConfigurationSourceTest,CosStoragePropertiesTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 6: Route Admin Uploads Through StorageService

**Files:**
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/AdminUploadController.java`
- Test: `backend/system-wap/src/test/java/com/ypat/controller/AdminUploadControllerSourceTest.java`

- [ ] **Step 1: Write source test for admin upload routing**

Create `backend/system-wap/src/test/java/com/ypat/controller/AdminUploadControllerSourceTest.java`:

```java
package com.ypat.controller;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminUploadControllerSourceTest {

    @Test
    public void adminUploadUsesStorageServiceAndBusinessPaths() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/controller/AdminUploadController.java");

        assertTrue(source.contains("private StorageService storageService"));
        assertTrue(source.contains("StorageBizPath.ADMIN"));
        assertTrue(source.contains("StorageBizPath.YPAT"));
        assertTrue(source.contains("stored.getUrl()"));
        assertFalse(source.contains("fastDFSClient.uploanFile1"));
        assertFalse(source.contains("systemConfig.getFdfs_path() + fileId"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-wap/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=AdminUploadControllerSourceTest test
```

Expected: FAIL while controller still references `FastDFSClient(FastDFS 客户端)`.

- [ ] **Step 3: Modify controller imports and fields**

In `backend/system-wap/src/main/java/com/ypat/controller/AdminUploadController.java`, replace `FastDFSClient` and `SystemConfig` imports/fields with:

```java
import com.ypat.storage.StorageBizPath;
import com.ypat.storage.StorageService;
import com.ypat.storage.StoredObject;
```

Use this field:

```java
    @Autowired
    private StorageService storageService;
```

- [ ] **Step 4: Replace normal upload call**

Inside `/admin/upload`, replace the file upload and URL append block with:

```java
            StoredObject stored = storageService.upload(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    StorageBizPath.ADMIN);
            urls.add(stored.getUrl());
```

- [ ] **Step 5: Replace watermarked upload call**

Inside `/admin/ypat/upload`, replace the file upload and URL append block with:

```java
            StoredObject stored = storageService.upload(
                    imageMarkUtil.waterMake(file.getInputStream()),
                    ImageConst.IMAGE_TYPE,
                    "image/jpeg",
                    StorageBizPath.YPAT);
            urls.add(stored.getUrl());
```

- [ ] **Step 6: Run admin upload test**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=AdminUploadControllerSourceTest test
```

Expected: PASS.

- [ ] **Step 7: Commit admin upload routing**

```bash
git add backend/system-wap/src/main/java/com/ypat/controller/AdminUploadController.java backend/system-wap/src/test/java/com/ypat/controller/AdminUploadControllerSourceTest.java
git commit -m "feat: route admin uploads through storage service" -m "Move new admin general and watermarked uploads behind the configurable storage abstraction." -m "Tested: mvn -pl system-wap -Dtest=AdminUploadControllerSourceTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 7: Route Work Media Uploads Through StorageService

**Files:**
- Modify: `backend/system-wap/src/main/java/com/ypat/service/WorkMediaService.java`
- Test: `backend/system-wap/src/test/java/com/ypat/service/WorkMediaServiceSourceTest.java`

- [ ] **Step 1: Write source test for work media storage routing**

Create `backend/system-wap/src/test/java/com/ypat/service/WorkMediaServiceSourceTest.java`:

```java
package com.ypat.service;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkMediaServiceSourceTest {

    @Test
    public void workMediaUploadsUseStorageServiceAndWorkPath() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/service/WorkMediaService.java");

        assertTrue(source.contains("private StorageService storageService"));
        assertTrue(source.contains("StorageBizPath.WORK"));
        assertTrue(source.contains("stored.getUrl()"));
        assertFalse(source.contains("fastDFSClient.uploanFile1"));
        assertFalse(source.contains("joinPublicFileUrl(systemConfig.getFdfs_path(), fileId)"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-wap/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=WorkMediaServiceSourceTest test
```

Expected: FAIL while service still calls `FastDFSClient(FastDFS 客户端)`.

- [ ] **Step 3: Replace fields and imports**

In `backend/system-wap/src/main/java/com/ypat/service/WorkMediaService.java`, remove `FastDFSClient` and `SystemConfig` fields. Add imports:

```java
import com.ypat.storage.StorageBizPath;
import com.ypat.storage.StorageService;
import com.ypat.storage.StoredObject;
```

Add field:

```java
    @Autowired private StorageService storageService;
```

- [ ] **Step 4: Replace image upload block**

In `uploadImage`, replace the `fileId`/`url` block with:

```java
            StoredObject stored = storageService.upload(
                    imageMarkUtil.waterMake(new ByteArrayInputStream(bytes)),
                    "work.jpg",
                    "image/jpeg",
                    StorageBizPath.WORK);
            String url = stored.getUrl();
```

- [ ] **Step 5: Replace video upload block**

In `uploadVideo`, replace the `fileId`/`url` block with:

```java
            StoredObject stored = storageService.upload(
                    new ByteArrayInputStream(bytes),
                    file.getOriginalFilename(),
                    mime,
                    StorageBizPath.WORK);
            String url = stored.getUrl();
```

- [ ] **Step 6: Remove old URL helper methods**

Delete `joinPublicFileUrl`, `extractFastDfsFileId`, and `trimSlashes` from `WorkMediaService`. Those helpers now live in `FastDfsStorageService(FastDFS 存储实现)`.

- [ ] **Step 7: Run service test**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=WorkMediaServiceSourceTest,WorkMediaServiceUrlTest test
```

Expected: PASS.

- [ ] **Step 8: Commit work upload routing**

```bash
git add backend/system-wap/src/main/java/com/ypat/service/WorkMediaService.java backend/system-wap/src/test/java/com/ypat/service/WorkMediaServiceSourceTest.java
git commit -m "feat: route work media uploads through storage service" -m "Use the configurable storage abstraction for new work image and video uploads while preserving response shape." -m "Tested: mvn -pl system-wap -Dtest=WorkMediaServiceSourceTest,WorkMediaServiceUrlTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 8: Route Base64 Ypat, Realname, and Avatar Flows

**Files:**
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/YpatInfoController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/OauthController.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/controller/UserController.java`
- Test: `backend/system-wap/src/test/java/com/ypat/controller/Base64StorageSourceTest.java`

- [ ] **Step 1: Write source test for Base64 flow routing**

Create `backend/system-wap/src/test/java/com/ypat/controller/Base64StorageSourceTest.java`:

```java
package com.ypat.controller;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Base64StorageSourceTest {

    @Test
    public void ypatSubmitUploadsBase64ToYpatPath() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/controller/YpatInfoController.java");

        assertTrue(source.contains("private StorageService storageService"));
        assertTrue(source.contains("StorageBizPath.YPAT"));
        assertTrue(source.contains("stored.getUrl()"));
        assertFalse(source.contains("fastDFSClient.uploanFile1(waterStream"));
    }

    @Test
    public void oauthAddUploadsBase64ToRealnamePath() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/controller/OauthController.java");

        assertTrue(source.contains("private StorageService storageService"));
        assertTrue(source.contains("StorageBizPath.REALNAME"));
        assertTrue(source.contains("stored.getUrl()"));
        assertFalse(source.contains("fastDFSClient.uploanFile1(new ByteArrayInputStream(bytes), ImageConst.IMAGE_TYPE)"));
    }

    @Test
    public void userAvatarBase64UploadsToAvatarPathAndUrlPassthroughRemains() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/controller/UserController.java");

        assertTrue(source.contains("private StorageService storageService"));
        assertTrue(source.contains("StorageBizPath.AVATAR"));
        assertTrue(source.contains("stored.getUrl()"));
        assertTrue(source.contains("userQo.setImgpath(pics.trim())"));
        assertFalse(source.contains("systemConfig.getFdfs_path()+fileId"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-wap/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=Base64StorageSourceTest test
```

Expected: FAIL while controllers still call `FastDFSClient(FastDFS 客户端)`.

- [ ] **Step 3: Update `YpatInfoController`**

Add imports:

```java
import com.ypat.storage.StorageBizPath;
import com.ypat.storage.StorageService;
import com.ypat.storage.StoredObject;
```

Add field:

```java
    @Autowired
    private StorageService storageService;
```

Replace the watermarked Base64 upload block in `/ypat/submit` with:

```java
                    InputStream inputStream = new ByteArrayInputStream(bytes);
                    InputStream waterStream = imageMarkUtil.waterMake(inputStream);
                    StoredObject stored = storageService.upload(waterStream, ImageConst.IMAGE_TYPE, "image/jpeg", StorageBizPath.YPAT);
                    picsList.add(stored.getUrl());
```

- [ ] **Step 4: Update `OauthController`**

Add imports:

```java
import com.ypat.storage.StorageBizPath;
import com.ypat.storage.StorageService;
import com.ypat.storage.StoredObject;
```

Add field:

```java
    @Autowired
    private StorageService storageService;
```

Replace the realname upload block in `/oauth/add` with:

```java
                StoredObject stored = storageService.upload(
                        new ByteArrayInputStream(bytes),
                        ImageConst.IMAGE_TYPE,
                        "image/jpeg",
                        StorageBizPath.REALNAME);
                picsList.add(stored.getUrl());
```

- [ ] **Step 5: Update `UserController`**

Add imports:

```java
import com.ypat.storage.StorageBizPath;
import com.ypat.storage.StorageService;
import com.ypat.storage.StoredObject;
```

Add field:

```java
    @Autowired
    private StorageService storageService;
```

Replace the avatar Base64 upload block in `/user/upd` with:

```java
                StoredObject stored = storageService.upload(
                        new ByteArrayInputStream(bytes),
                        ImageConst.IMAGE_TYPE,
                        "image/jpeg",
                        StorageBizPath.AVATAR);
                logger.info("avatar uploaded via storage provider");
                userQo.setImgpath(stored.getUrl());
```

Keep this existing URL passthrough branch unchanged:

```java
                userQo.setImgpath(pics.trim());
```

- [ ] **Step 6: Run Base64 source tests**

Run:

```bash
cd backend
mvn -pl system-wap -Dtest=Base64StorageSourceTest,UserControllerAvatarUpdateTest test
```

Expected: PASS.

- [ ] **Step 7: Commit Base64 flow routing**

```bash
git add backend/system-wap/src/main/java/com/ypat/controller/YpatInfoController.java backend/system-wap/src/main/java/com/ypat/controller/OauthController.java backend/system-wap/src/main/java/com/ypat/controller/UserController.java backend/system-wap/src/test/java/com/ypat/controller/Base64StorageSourceTest.java
git commit -m "feat: route Base64 image flows through storage service" -m "Store new ypat, realname, and avatar Base64 image submissions through the configurable backend storage abstraction while preserving URL passthrough." -m "Tested: mvn -pl system-wap -Dtest=Base64StorageSourceTest,UserControllerAvatarUpdateTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 9: Add Work Media Soft Delete

**Files:**
- Modify: `backend/system-domain/src/main/java/com/ypat/entity/WorkMedia.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/repository/WorkMediaRepository.java`
- Modify: `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`
- Modify: `backend/system-wap/src/main/java/com/ypat/service/WorkMediaService.java`
- Create: `docs/sql/pending/V_cos_storage_switch_work_media_soft_delete.sql`
- Test: `backend/system-wap/src/test/java/com/ypat/service/WorkMediaSoftDeleteSourceTest.java`
- Test: `backend/system-domain/src/test/java/com/ypat/service/WorkServiceMediaSoftDeleteSourceTest.java`

- [ ] **Step 1: Write source tests for soft delete behavior**

Create `backend/system-wap/src/test/java/com/ypat/service/WorkMediaSoftDeleteSourceTest.java`:

```java
package com.ypat.service;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkMediaSoftDeleteSourceTest {

    @Test
    public void deleteMediaMarksDeletedAtAndDoesNotDeleteStorageObject() throws Exception {
        String source = read("backend/system-wap/src/main/java/com/ypat/service/WorkMediaService.java");

        assertTrue(source.contains("media.setDeletedAt(new java.util.Date())"));
        assertTrue(source.contains("workMediaRepository.save(media)"));
        assertFalse(source.contains("storageService.deleteByUrl"));
        assertFalse(source.contains("fastDFSClient.deleteFile"));
        assertFalse(source.contains("workMediaRepository.delete(media)"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-wap/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
```

Create `backend/system-domain/src/test/java/com/ypat/service/WorkServiceMediaSoftDeleteSourceTest.java`:

```java
package com.ypat.service;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class WorkServiceMediaSoftDeleteSourceTest {

    @Test
    public void workServiceUsesOnlyNonDeletedMediaQueries() throws Exception {
        String source = read("backend/system-domain/src/main/java/com/ypat/service/WorkService.java");

        assertTrue(source.contains("findByIdInAndUserIdAndDeletedAtIsNull"));
        assertTrue(source.contains("findByWorkIdAndDeletedAtIsNullOrderBySortNoAsc"));
    }

    @Test
    public void repositoryContainsSoftDeleteQueries() throws Exception {
        String source = read("backend/system-domain/src/main/java/com/ypat/repository/WorkMediaRepository.java");

        assertTrue(source.contains("findByWorkIdAndDeletedAtIsNullOrderBySortNoAsc"));
        assertTrue(source.contains("findByIdInAndUserIdAndDeletedAtIsNull"));
        assertTrue(source.contains("where m.workId is null and m.deletedAt is null"));
    }

    private String read(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) path = Paths.get(file.replace("backend/system-domain/", ""));
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run:

```bash
cd backend
mvn -pl system-wap,system-domain -Dtest=WorkMediaSoftDeleteSourceTest,WorkServiceMediaSoftDeleteSourceTest test
```

Expected: FAIL while code still hard-deletes and queries all media.

- [ ] **Step 3: Add `deletedAt` field**

In `backend/system-domain/src/main/java/com/ypat/entity/WorkMedia.java`, add field near `createdAt`:

```java
    @Column(name = "deleted_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedAt;
```

Add accessors:

```java
    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }
```

- [ ] **Step 4: Add repository methods**

Modify `backend/system-domain/src/main/java/com/ypat/repository/WorkMediaRepository.java`:

```java
    /** 按 workId 升序查未删除媒体 */
    List<WorkMedia> findByWorkIdAndDeletedAtIsNullOrderBySortNoAsc(Long workId);

    /** 按 ID 列表查未删除媒体（校验归属） */
    List<WorkMedia> findByIdInAndUserIdAndDeletedAtIsNull(List<Long> ids, Long userId);

    /** 孤儿媒体清理候选（24h 前且未软删） */
    @Query("select m from WorkMedia m where m.workId is null and m.deletedAt is null and m.createdAt < :threshold")
    List<WorkMedia> findActiveOrphansOlderThan(@Param("threshold") java.util.Date threshold);
```

Keep old methods temporarily if other code still compiles against them.

- [ ] **Step 5: Update `WorkService` to use non-deleted queries**

In `WorkService.submit`, replace:

```java
List<WorkMedia> medias = workMediaRepository.findByIdInAndUserId(mediaIds, userId);
```

with:

```java
List<WorkMedia> medias = workMediaRepository.findByIdInAndUserIdAndDeletedAtIsNull(mediaIds, userId);
```

Replace these `WorkService` media lookup calls:

```java
workMediaRepository.findByWorkIdOrderBySortNoAsc(workId)
workMediaRepository.findByWorkIdOrderBySortNoAsc(w.getId())
workMediaRepository.findByWorkIdOrderBySortNoAsc(work.getId())
```

with:

```java
workMediaRepository.findByWorkIdAndDeletedAtIsNullOrderBySortNoAsc(workId)
workMediaRepository.findByWorkIdAndDeletedAtIsNullOrderBySortNoAsc(w.getId())
workMediaRepository.findByWorkIdAndDeletedAtIsNullOrderBySortNoAsc(work.getId())
```

- [ ] **Step 6: Update delete behavior in `WorkMediaService`**

In `deleteMedia`, replace the hard delete and storage delete block with:

```java
        media.setDeletedAt(new java.util.Date());
        workMediaRepository.save(media);
        logger.info("作品媒体已软删除 mediaId={} userId={}", mediaId, userId);
```

Keep the existing ownership and `workId != null` checks.

- [ ] **Step 7: Add SQL migration**

Create `docs/sql/pending/V_cos_storage_switch_work_media_soft_delete.sql`:

```sql
ALTER TABLE t_work_media
    ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间';

CREATE INDEX idx_work_media_work_deleted_sort
    ON t_work_media (work_id, deleted_at, sort_no);
```

- [ ] **Step 8: Run soft delete tests**

Run:

```bash
cd backend
mvn -pl system-wap,system-domain -Dtest=WorkMediaSoftDeleteSourceTest,WorkServiceMediaSoftDeleteSourceTest test
```

Expected: PASS.

- [ ] **Step 9: Commit soft delete**

```bash
git add backend/system-domain/src/main/java/com/ypat/entity/WorkMedia.java backend/system-domain/src/main/java/com/ypat/repository/WorkMediaRepository.java backend/system-domain/src/main/java/com/ypat/service/WorkService.java backend/system-wap/src/main/java/com/ypat/service/WorkMediaService.java docs/sql/pending/V_cos_storage_switch_work_media_soft_delete.sql backend/system-wap/src/test/java/com/ypat/service/WorkMediaSoftDeleteSourceTest.java backend/system-domain/src/test/java/com/ypat/service/WorkServiceMediaSoftDeleteSourceTest.java
git commit -m "feat: soft delete work media" -m "Preserve uploaded storage objects by marking deleted media with deleted_at and filtering them from bind and read paths." -m "Tested: mvn -pl system-wap,system-domain -Dtest=WorkMediaSoftDeleteSourceTest,WorkServiceMediaSoftDeleteSourceTest test" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 10: Add Frontend No-COS-Secret Regression Tests

**Files:**
- Modify: `frontend/src/api/__tests__/media.test.ts`
- Create: `frontend/src/config/__tests__/cos-secrets.test.ts`
- Modify: `frontend-admin/tests/unit/api.test.ts`
- Create: `frontend-admin/tests/unit/cos-secrets.test.ts`

- [ ] **Step 1: Add frontend mobile secret scan test**

Create `frontend/src/config/__tests__/cos-secrets.test.ts`:

```ts
import { describe, expect, it } from 'vitest'
import { readFileSync, readdirSync, statSync } from 'node:fs'
import { join } from 'node:path'

function collectFiles(dir: string, out: string[] = []): string[] {
  for (const name of readdirSync(dir)) {
    const full = join(dir, name)
    if (statSync(full).isDirectory()) collectFiles(full, out)
    else if (/\.(ts|vue|js|json|env|example)$/.test(name)) out.push(full)
  }
  return out
}

describe('COS secrets are backend-only', () => {
  it('does not expose Tencent COS credential names in the mobile frontend source', () => {
    const root = join(process.cwd(), 'src')
    const text = collectFiles(root).map((file) => readFileSync(file, 'utf8')).join('\n')

    expect(text).not.toContain('YPAT_COS_SECRET_ID')
    expect(text).not.toContain('YPAT_COS_SECRET_KEY')
    expect(text).not.toContain('TENCENT_COS_SECRET')
  })
})
```

- [ ] **Step 2: Add admin secret scan test**

Create `frontend-admin/tests/unit/cos-secrets.test.ts`:

```ts
import { describe, expect, it } from 'vitest'
import { readFileSync, readdirSync, statSync } from 'node:fs'
import { join } from 'node:path'

function collectFiles(dir: string, out: string[] = []): string[] {
  for (const name of readdirSync(dir)) {
    const full = join(dir, name)
    if (statSync(full).isDirectory()) collectFiles(full, out)
    else if (/\.(ts|vue|js|json|env|example)$/.test(name)) out.push(full)
  }
  return out
}

describe('COS secrets are backend-only', () => {
  it('does not expose Tencent COS credential names in admin source', () => {
    const srcText = collectFiles(join(process.cwd(), 'src')).map((file) => readFileSync(file, 'utf8')).join('\n')
    const envText = collectFiles(process.cwd()).filter((file) => file.includes('.env')).map((file) => readFileSync(file, 'utf8')).join('\n')
    const text = srcText + '\n' + envText

    expect(text).not.toContain('YPAT_COS_SECRET_ID')
    expect(text).not.toContain('YPAT_COS_SECRET_KEY')
    expect(text).not.toContain('TENCENT_COS_SECRET')
  })
})
```

- [ ] **Step 3: Strengthen existing media API test wording if needed**

In `frontend/src/api/__tests__/media.test.ts`, keep the expected upload URLs as backend endpoints:

```ts
expect(uploadMock).toHaveBeenCalledWith({
  url: 'https://panghu.work/api/work/upload/image',
  filePath: '/tmp/a.jpg',
  name: 'file',
  showLoading: false,
})
```

and:

```ts
expect(uploadMock).toHaveBeenCalledWith({
  url: '/work/upload/video',
  filePath: '/tmp/a.mp4',
  name: 'file',
  showLoading: false,
})
```

- [ ] **Step 4: Run frontend tests**

Run:

```bash
cd frontend
pnpm test -- media cos-secrets
cd ../frontend-admin
pnpm test -- cos-secrets api
```

Expected: PASS.

- [ ] **Step 5: Commit frontend regression tests**

```bash
git add frontend/src/config/__tests__/cos-secrets.test.ts frontend/src/api/__tests__/media.test.ts frontend-admin/tests/unit/cos-secrets.test.ts frontend-admin/tests/unit/api.test.ts
git commit -m "test: keep COS credentials out of frontends" -m "Add regression coverage that the mobile frontend and admin app continue to use backend upload APIs and do not expose COS credential variables." -m "Tested: pnpm test -- media cos-secrets in frontend; pnpm test -- cos-secrets api in frontend-admin" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 11: Full Verification

**Files:**
- No new source files required.

- [ ] **Step 1: Run backend focused tests**

Run:

```bash
cd backend
mvn -pl system-wap,system-domain -Dtest=CosStoragePropertiesTest,CosObjectKeyFactoryTest,CosStorageServiceSourceTest,StorageConfigurationSourceTest,FastDfsStorageServiceSourceTest,WorkMediaServiceUrlTest,AdminUploadControllerSourceTest,WorkMediaServiceSourceTest,Base64StorageSourceTest,WorkMediaSoftDeleteSourceTest,WorkServiceMediaSoftDeleteSourceTest,UserControllerAvatarUpdateTest test
```

Expected: PASS.

- [ ] **Step 2: Run backend module tests**

Run:

```bash
cd backend
mvn -pl system-wap,system-domain test
```

Expected: PASS.

- [ ] **Step 3: Run frontend tests**

Run:

```bash
cd frontend
pnpm test
pnpm type-check
cd ../frontend-admin
pnpm test
pnpm type-check
```

Expected: PASS.

- [ ] **Step 4: Run secret-oriented scan**

Run:

```bash
rg -n "SecretKey|SecretId|YPAT_COS_SECRET|TENCENT_COS_SECRET" frontend frontend-admin backend/system-wap/src/main/java backend/system-wap/src/main/resources backend/.env*.example .env.example
```

Expected: matches only backend configuration classes, backend resource config, backend env example files, and tests that assert secrets are absent from frontend sources.

- [ ] **Step 5: Inspect git diff**

Run:

```bash
git status --short
git diff --stat
git diff -- backend/system-web 91pai-master
```

Expected: no changes in `backend/system-web(旧后台服务)` or `91pai-master(旧版移动端)`.

- [ ] **Step 6: Commit final verification note if generated files changed**

If verification commands changed no files, do not commit. If generated lockfiles or test snapshots changed, inspect them and commit only intentional changes:

```bash
git add <intentional-files>
git commit -m "chore: update verification artifacts" -m "Record intentional generated artifacts from final COS storage switch verification." -m "Tested: backend module tests, frontend tests, frontend-admin tests" -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

---

### Task 12: Merge Back to dev6 and Push

**Files:**
- Git operations only.

- [ ] **Step 1: Confirm branch status**

Run from `/Users/lizhenwei/workspace/vueworkspace/ypat-cos-storage-switch`:

```bash
git status --short --branch
```

Expected: clean branch `codex/cos-storage-switch`.

- [ ] **Step 2: Merge into dev6**

Run:

```bash
git checkout dev6
git merge --no-ff codex/cos-storage-switch
```

Expected: merge succeeds without conflicts.

- [ ] **Step 3: Push dev6 to origin/main as requested**

Run:

```bash
git push origin dev6:main
```

Expected: remote `origin/main` receives the merged `dev6` state. If protected branch rules reject this push, stop and report the rejection message.

- [ ] **Step 4: Return to original worktree and verify**

Run:

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
git status --short --branch
```

Expected: original worktree still has only pre-existing unrelated changes such as `.omx/state/session.json`.

---

## Self-Review

Spec coverage:

- New-only scope: Task 0 and Task 11 explicitly guard against changes to `91pai-master(旧版移动端)` and `backend/system-web(旧后台服务)`.
- Backend proxy and provider switch: Tasks 1-5.
- COS single bucket with `dev/pre/pro` prefixes and business paths: Tasks 1-5.
- Public URL compatibility: Tasks 3-8.
- Local watermarking: Tasks 6-8 preserve `ImageMarkUtil(水印工具)`.
- Full image flows: Tasks 6-8 cover admin upload, ypat upload, work media, Base64 submit, realname, avatar.
- Soft delete: Task 9.
- Frontend no direct COS credentials: Task 10.
- Verification and merge/push: Tasks 11-12.

Plan completeness scan:

- The only example values are explicit env-template values for documentation, not implementation gaps.
- Every code-changing step includes exact files, code snippets, commands, and expected results.

Type consistency:

- `StorageBizPath`, `StoredObject`, `StorageService`, `CosStorageProperties`, `CosObjectKeyFactory`, `FastDfsStorageService`, and `CosStorageService` names are used consistently across tasks.
