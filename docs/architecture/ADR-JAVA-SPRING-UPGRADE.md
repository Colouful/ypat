# ADR-001: Java 8 → Java 17 LTS 与 Spring Boot 1.5.x → 3.x 升级路径

**状态**：PROPOSED（提案中，本轮不执行）
**日期**：2026-06-29
**决策者**：后端架构组
**影响范围**：所有 backend 模块、CI、部署脚本、文档

## 1. 背景与动机

YPAT 后端当前使用：
- Java 8 (Zulu 8 / Temurin 8)
- Spring Boot 1.5.9.RELEASE（2017 年发布，已停止 OSS 支持）
- Spring Cloud Edgware（2018 年）
- JJWT 0.x（已知 CVE）

这导致：

1. **安全风险**：Spring Boot 1.5.x 不再接收安全补丁；JJWT 0.x 有 CVE。
2. **Java 8 EOL**：Zulu 8 免费支持已于 2022 年结束；Temurin 8 在 2026 年起需要商业支持。
3. **依赖版本陈旧**：Spring Boot 1.5 不支持 Jakarta EE（仍 javax.*），迁移 Spring Boot 2/3 时一次性改名。
4. **运维风险**：CI 上 Java 8 镜像已不在 Ubuntu LTS 默认仓库，需要专门拉取。
5. **新特性无法使用**：Java 17 的 records / sealed classes / pattern matching 不能用。

## 2. 决策

**未来 6 个月内分两阶段升级**：

### 阶段一（Q3 2026）：Java 17 + Spring Boot 2.7.x（保守升级）

- 编译目标 Java 17，运行 Java 17
- Spring Boot 1.5.9 → 2.7.18（Spring Boot 2.x 末班车，仍支持 Java 8）
- Spring Cloud Edgware → 2021.0.x
- javax.* 保持（Spring Boot 2.7 不强制 jakarta）
- JJWT 0.x → 0.11.x
- 移除 commons-io / commons-fileupload 旧版本

**理由**：保留 javax 命名空间，避免一次性大爆炸；保留现有 MyBatis / Druid / FastDFS 集成。

### 阶段二（Q1 2027）：Spring Boot 3.2.x + jakarta + Java 17 LTS

- Spring Boot 2.7 → 3.2.x
- javax.* → jakarta.* 一次性改名（Spring Boot 3 强制）
- 最小 Java 17（Spring Boot 3 不支持 Java 8）
- Spring Cloud 2023.0.x
- 移除 Shiro（如已迁移到 Spring Security 6）

## 3. 当前轮（本 ADR 不执行的内容）

**本轮治理任务不升级框架**，仅做以下：

- 记录技术债（本文档）
- 在 `pom.xml` 注释中标注未来升级路径
- CI 增加 Java 17 编译测试（不部署，只验证）
- 锁定所有依赖版本到 Java 8 兼容的最后一个版本

## 4. 拒绝的方案

### 4.1 一次性升级到 Spring Boot 3.x

**拒绝理由**：风险太高。Spring Boot 1.5 → 3.x 跳两个大版本，javax → jakarta + Java 17 强制 + Spring Security 5 → 6 同时发生，难以独立回滚。

### 4.2 升级到 Spring Boot 2.0 / 2.3 / 2.5 等中间版本

**拒绝理由**：这些中间版本已 EOL，没有意义。直接到 2.7.18 LTS。

### 4.3 保留 Java 8 + 升级 Spring Boot 到 2.7

**部分采用**：阶段一可保留 Java 8 编译兼容性（Spring Boot 2.7 同时支持 Java 8 和 17），但生产环境强制 Java 17 运行。

## 5. 影响与回滚

- 阶段一升级后，回滚需保留 Java 8 编译开关
- 阶段二升级后，无法直接回滚（API breaking change），必须保留旧版本分支至少 6 个月

## 6. 升级触发条件（推迟触发）

不要因为以下原因启动升级：
- "新功能需要" → 在 1.5 上通常有 workaround
- "某个依赖升级" → 锁定版本号

启动条件：
- Spring Boot 1.5 安全 CVE 公开披露
- JJWT 0.x 关键 CVE
- 团队超过 50% 新成员不会 Java 8
- 业务要求 Java 17（如某合规要求）

## 7. 参考

- Spring Boot 官方支持矩阵：https://spring.io/projects/spring-boot#support
- Java 8 EOL 公告：https://www.oracle.com/java/technologies/java-se-support-roadmap.html
- JJWT CVE 历史：https://github.com/jwtk/jjwt/security/advisories
- 本 ADR 关联任务：未创建（待评估）

## 8. 决策日志

| 日期 | 决策 | 决策者 |
|------|------|--------|
| 2026-06-29 | 提议分两阶段升级 | 后端架构组 |
| — | 待 Q3 2026 评审 | — |