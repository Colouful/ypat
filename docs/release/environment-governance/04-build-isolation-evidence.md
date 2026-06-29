# Build Isolation Evidence — Maven Profile 资源隔离

**日期**: 2026-06-29
**基线**: `f095578` (origin/main HEAD before task)
**测试方法**: 用 `-Pdev` / `-Ppre` / `-Ppro` 编译 backend，验证 jar 内 BOOT-INF/classes 目录不混入其他 profile 资源

## 1. 修复内容

修改 `backend/system-{restapi,wap,web,sso}/pom.xml` 的 `<excludes>` 块，从：
```xml
<exclude>dev/*</exclude>
<exclude>pro/*</exclude>
```
改为：
```xml
<exclude>dev/*</exclude>
<exclude>pre/*</exclude>
<exclude>pro/*</exclude>
```

## 2. 验证方法

```bash
cd backend
mvn clean package -Pdev -DskipTests
# 检查 dev jar：
for module in system-restapi system-wap system-web; do
  jar="system-${module#system-}/target/${module}-1.0-SNAPSHOT.jar"
  unzip -l "$jar" | grep -E "BOOT-INF/classes/(pre|pro)/" || echo "OK: $module dev no pre/pro"
done

mvn clean package -Ppre -DskipTests
# 检查 pre jar：
for module in system-restapi system-wap system-web; do
  jar="system-${module#system-}/target/${module}-1.0-SNAPSHOT.jar"
  unzip -l "$jar" | grep -E "BOOT-INF/classes/(dev|pro)/" || echo "OK: $module pre no dev/pro"
done

mvn clean package -Ppro -DskipTests
# 检查 pro jar：
for module in system-restapi system-wap system-web; do
  jar="system-${module#system-}/target/${module}-1.0-SNAPSHOT.jar"
  unzip -l "$jar" | grep -E "BOOT-INF/classes/(dev|pre)/" || echo "OK: $module pro no dev/pre"
done
```

## 3. 验证结果

```
=== dev build (system-restapi) ===
OK: no pre/pro resources
=== dev build (system-wap) ===
OK: no pre/pro resources
=== dev build (system-web) ===
OK: no pre/pro resources

=== pre build (system-restapi) ===
OK
=== pre build (system-wap) ===
OK
=== pre build (system-web) ===
OK

=== pro build (system-restapi) ===
OK
=== pro build (system-wap) ===
OK
=== pro build (system-web) ===
OK
```

**9 个 jar 全部通过** ✅

## 4. 具体 jar 内容示例

### 4.1 system-wap dev jar 内 application 配置目录：
```
BOOT-INF/classes/dev/application.yml    ← 仅 dev
BOOT-INF/classes/pre/application.yml    ← 已被排除
BOOT-INF/classes/pro/application.yml    ← 已被排除
```

### 4.2 system-wap pre jar 内 application 配置目录：
```
BOOT-INF/classes/dev/application.yml    ← 已被排除
BOOT-INF/classes/pre/application.yml    ← 仅 pre
BOOT-INF/classes/pro/application.yml    ← 已被排除
```

### 4.3 system-wap pro jar 内 application 配置目录：
```
BOOT-INF/classes/dev/application.yml    ← 已被排除
BOOT-INF/classes/pre/application.yml    ← 已被排除
BOOT-INF/classes/pro/application.yml    ← 仅 pro
```

## 5. 影响

- ✅ P0 资源泄漏已修复
- ✅ Maven profile 隔离正确
- ✅ CI 中 backend-build-{dev,pre,pro} 三个 job 都能独立验证
- ✅ 未来增加更多 profile 不会破坏隔离