# COS 存储切换设计

日期：2026-07-06

## 背景

当前新版移动端 `frontend(新版移动端前端)` 和新版管理后台 `frontend-admin(新版管理后台)` 的图片与媒体上传仍由后端写入 `FastDFS(FastDFS 文件存储)`。近期分支里多次修复 `FastDFS(FastDFS 文件存储)` 图片预览和上传问题，说明存储实现已经成为稳定性风险。

本设计将新版上传链路迁移为可配置存储：后端统一代理上传，并通过配置开关选择 `COS(腾讯云对象存储)` 或 `FastDFS(FastDFS 文件存储)`。前端不接触云存储密钥，接口继续返回兼容的 `URL(URL 地址)`。

## 范围

本次只覆盖新版链路：

- `backend/system-wap(新版移动端和管理后台后端)`
- `frontend(新版移动端前端)`
- `frontend-admin(新版管理后台)`

不修改：

- `91pai-master(旧版移动端)`
- `backend/system-web(旧后台服务)`

旧版代码只作为业务行为参考。

## 目标

1. 新增统一 `StorageService(存储服务)`，封装上传、公开地址生成、对象标识解析和软删除相关能力。
2. 通过配置开关在 `COS(腾讯云对象存储)` 和 `FastDFS(FastDFS 文件存储)` 间切换。
3. 保持现有新版接口返回结构兼容，继续返回可公开访问的 `URL(URL 地址)`。
4. 覆盖后台通用上传、后台作品水印上传、移动端作品媒体上传、头像、实名认证证件照、新版旧 `Base64(编码图片)` 提交流程。
5. 图片水印继续使用后端本地 `ImageMarkUtil(水印工具)`，处理后再上传。
6. 移动端媒体删除改为软删除，不立即删除数据库记录，也不立即删除云端文件。

## 非目标

1. 不实现前端直传 `COS(腾讯云对象存储)`。
2. 不迁移历史 `FastDFS(FastDFS 文件存储)` 文件到 `COS(腾讯云对象存储)`。
3. 不改旧版移动端和旧后台。
4. 不引入腾讯云 `CI(数据万象)` 水印。
5. 不在本次实现自动物理清理软删除对象。

## 架构

新增 `StorageService(存储服务)` 接口，提供：

- `upload(input, bizPath, originalFilename, contentType)`：上传文件并返回公开 `URL(URL 地址)` 与存储对象标识。
- `deleteByUrl(url)` 或等价方法：仅供未来物理删除使用，本次移动端删除不主动调用。
- `supportsUrl(url)`：判断某个 `URL(URL 地址)` 是否属于当前存储实现。
- `extractObjectKey(url)`：从公开 `URL(URL 地址)` 解析对象路径，用于后续清理或诊断。

实现层：

- `FastDfsStorageService(FastDFS 存储实现)`：包装现有 `FastDFSClient(FastDFS 客户端)`，保留当前行为。
- `CosStorageService(COS 存储实现)`：使用腾讯云 `COS Java SDK(Java 开发包)` 上传到同一个 `bucket(存储桶)`。

业务层只依赖 `StorageService(存储服务)`，不再直接拼接 `systemConfig.getFdfs_path()` 或直接调用 `FastDFSClient(FastDFS 客户端)`。

## COS 对象路径

采用单 `bucket(存储桶)` 加环境前缀。环境前缀与现有 `Spring profile(Spring 环境配置)` 对齐：

- `dev/`
- `pre/`
- `pro/`

对象路径格式：

```text
{envPrefix}/{bizPath}/{yyyy}/{MM}/{dd}/{uuid}.{ext}
```

业务路径：

- `admin/`：后台通用图片，例如横幅、文章图片。
- `ypat/`：约拍作品图，包含水印。
- `work/`：新版作品媒体，图片包含水印，视频不加水印。
- `avatar/`：头像。
- `realname/`：实名认证证件照。

## 配置

配置沿用现有项目风格：`.env.*.example(环境变量示例文件)` 放占位，真实值由部署环境注入；`application.yml(应用配置文件)` 和 `sys_conf.properties(系统配置文件)` 使用 `${YPAT_...}` 读取。

新增配置项：

```text
YPAT_STORAGE_PROVIDER=fastdfs|cos
YPAT_COS_SECRET_ID=
YPAT_COS_SECRET_KEY=
YPAT_COS_REGION=
YPAT_COS_BUCKET=
YPAT_COS_PUBLIC_BASE_URL=
YPAT_COS_ENV_PREFIX=dev|pre|pro
```

默认 `provider(存储提供方)` 为 `fastdfs(FastDFS 文件存储)`，避免未配置 `COS(腾讯云对象存储)` 时影响现有环境。启用 `cos(COS 对象存储)` 时，`SecretId(密钥 ID)`、`SecretKey(密钥 Key)`、`Region(地域)`、`Bucket(存储桶)`、`PublicBaseUrl(公开访问根地址)` 必须完整配置。

真实密钥不提交到 `git(git 版本控制)`，也不暴露给 `frontend(新版移动端前端)` 或 `frontend-admin(新版管理后台)`。

## 数据流

### 后台通用上传

`frontend-admin(新版管理后台)` 调用 `/admin/upload`。后端校验图片后调用 `StorageService(存储服务)` 上传到 `{env}/admin/`，返回：

```json
{ "urls": ["https://example.com/dev/admin/2026/07/06/xxx.jpg"] }
```

### 后台作品图水印上传

`frontend-admin(新版管理后台)` 调用 `/admin/ypat/upload`。后端先用 `ImageMarkUtil(水印工具)` 生成带水印图片，再上传到 `{env}/ypat/`，返回结构不变。

### 移动端作品媒体上传

`frontend(新版移动端前端)` 图片调用 `/work/upload/image`。后端校验图片、生成水印、上传到 `{env}/work/`，写入 `WorkMedia(作品媒体)` 表，返回现有 `id/url/type/fileSize` 等字段。

`frontend(新版移动端前端)` 视频调用 `/work/upload/video`。后端校验视频后上传到 `{env}/work/`，不加水印，返回结构不变。

### 新版 Base64 提交兼容

`/ypat/submit` 接收 `Base64(编码图片)` 后，解码、加水印、上传到 `{env}/ypat/`，最终保存公开 `URL(URL 地址)`。

`/oauth/add` 接收实名认证 `Base64(编码图片)` 后，解码并上传到 `{env}/realname/`，最终保存公开 `URL(URL 地址)`。

`/user/upd` 接收头像 `Base64(编码图片)` 后，解码并上传到 `{env}/avatar/`；如果传入值已经是 `URL(URL 地址)`，继续兼容保存。

## 软删除

`/work/upload/media` 删除未绑定媒体时改为业务软删除：

- 不物理删除 `t_work_media(作品媒体表)` 记录。
- 不立即删除 `COS(腾讯云对象存储)` 或 `FastDFS(FastDFS 文件存储)` 文件。
- 新增 `deleted_at(删除时间)` 字段表示软删除状态。
- 作品提交绑定媒体时排除 `deleted_at(删除时间)` 不为空的记录。
- 查询作品媒体时排除已软删除记录。

自动物理清理可作为后续任务处理，例如清理软删除超过 30 天且未绑定作品的对象。本次不实现，避免误删和回滚风险。

## 错误处理

1. 上传失败沿用现有业务错误语义，返回“上传失败”类错误。
2. 水印失败保持现有“水印失败”语义。
3. `COS(腾讯云对象存储)` 配置缺失时，在启用 `cos(COS 对象存储)` 的环境中快速失败，避免运行时静默回退。
4. 日志不输出 `SecretId(密钥 ID)`、`SecretKey(密钥 Key)`、签名串或完整敏感请求信息。
5. 日志可记录 `provider(存储提供方)`、`bizPath(业务路径)`、文件类型、文件大小、异常类型，便于定位问题。

## 测试

后端单元测试：

- `StorageService(存储服务)` 路径生成：环境前缀、业务路径、日期、扩展名。
- `COS(腾讯云对象存储)` 公开 `URL(URL 地址)` 拼接和对象 key(对象路径) 提取。
- `FastDFS(FastDFS 文件存储)` URL(URL 地址) 兼容逻辑。
- `provider(存储提供方)` 开关选择逻辑。
- 启用 `cos(COS 对象存储)` 但配置缺失时失败。
- 软删除不调用物理删除，不允许已软删除媒体被绑定。

后端服务和接口测试：

- `/admin/upload`
- `/admin/ypat/upload`
- `/work/upload/image`
- `/work/upload/video`
- `/ypat/submit`
- `/oauth/add`
- `/user/upd`
- `/work/upload/media` 软删除

前端测试：

- `frontend(新版移动端前端)` 和 `frontend-admin(新版管理后台)` 继续调用现有后端接口。
- 前端配置中不出现 `SecretId(密钥 ID)`、`SecretKey(密钥 Key)` 或 COS(COS 对象存储) 直传配置。
- 上传结果仍按 `URL(URL 地址)` 展示和预览。

## 发布与回滚

1. 默认保持 `YPAT_STORAGE_PROVIDER=fastdfs`，上线代码不改变现有行为。
2. 开发环境先切换 `cos(COS 对象存储)` 验证新版全链路。
3. 预发环境验证上传、预览、分享、后台审核和软删除。
4. 生产环境切换前确认 `bucket(存储桶)`、`Region(地域)`、公开域名、路径前缀、权限策略均正确。
5. 如发现问题，将 `YPAT_STORAGE_PROVIDER` 切回 `fastdfs(FastDFS 文件存储)` 即可恢复旧上传路径。已写入 `COS(腾讯云对象存储)` 的新文件仍保留，数据库中保存的公开 `URL(URL 地址)` 可继续访问。

## 验收标准

1. 新版管理后台通用图片上传返回公开 `URL(URL 地址)` 并可预览。
2. 新版管理后台作品图上传后带水印并可预览。
3. 新版移动端作品图片上传后带水印，视频上传成功，提交作品可在详情页展示。
4. 新版移动端 `/ypat/submit` 的 `Base64(编码图片)` 兼容流程保存的是公开 `URL(URL 地址)`。
5. 新版实名认证证件照保存为公开 `URL(URL 地址)`。
6. 新版头像上传保存为公开 `URL(URL 地址)`，旧 URL(URL 地址) 透传兼容。
7. 软删除媒体后，不能继续绑定到作品，列表和详情不展示该媒体。
8. 前端构建产物不包含 COS(COS 对象存储) 密钥。
9. `fastdfs(FastDFS 文件存储)` 配置下现有上传链路仍可用。
10. `cos(COS 对象存储)` 配置下所有新版上传链路可用。
