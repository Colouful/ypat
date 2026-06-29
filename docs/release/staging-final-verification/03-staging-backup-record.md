# Staging 备份记录 — 旧 /opt/ypat 取证

## 备份目录

```
/opt/ypat-data/backups/repository-state/20260629_180034/
```

权限 `700`,总大小 12 MB。

## 文件清单

| 文件 | 用途 | 大小 |
| --- | --- | --- |
| `repository-info.txt` | hostname / path / HEAD / branch / origin / 最近 20 提交 | 213 B |
| `git-status.txt` | `git status --short` 全文 | 1.1 KB |
| `git-status.z` | NUL 分隔的 porcelain v1 输出 | 1.1 KB |
| `tracked-working-tree.patch` | `git diff --binary` 全文(working tree vs index) | 17 KB |
| `tracked-index.patch` | `git diff --cached --binary`(空) | 0 B |
| `tracked-diff-stat.txt` | 12 个 tracked 修改的统计 | 846 B |
| `tracked-name-status.txt` | name-status 列表 | 577 B |
| `untracked-files.z` | NUL 分隔的未跟踪文件路径列表 | 1.1 KB |
| `untracked-files.tar.gz` | 全部 untracked 文件归档(含 21 个文件) | 6.6 KB |
| `repository.bundle` | `git bundle --all` (完整 ref + object) | 5.3 MB |
| `repository-files.tar.gz` | 整目录快照(排除 `.git/objects` / `node_modules` / `dist` / `*/target`) | 5.9 MB |
| `SHA256SUMS` | 上述全部文件的 SHA256 | 950 B |

## 校验

| 校验 | 结果 |
| --- | --- |
| TAR (repository-files.tar.gz) | OK |
| TAR (untracked-files.tar.gz) | OK |
| Bundle | OK (4 refs, complete history, sha1) |
| Patch reverse-apply check | FAIL (已应用到 worktree,无法重复应用 — 这是预期) |

Bundle 内 ref:
```
13fb74754129ce72fbcfa5e4acd7ca08318cc2fb refs/heads/main
13fb74754129ce72fbcfa5e4acd7ca08318cc2fb refs/remotes/origin/HEAD
13fb74754129ce72fbcfa5e4acd7ca08318cc2fb refs/remotes/origin/main
13fb74754129ce72fbcfa5e4acd7ca08318cc2fb HEAD
```

## 旧工作区基础信息

```
hostname = VM-0-5-opencloudos
path = /opt/ypat
HEAD = 13fb74754129ce72fbcfa5e4acd7ca08318cc2fb
branch = main (shallow / single-commit)
origin = https://github.com/Colouful/ypat.git
modified (tracked) = 12 个
untracked entries = 12 (展开 21 个文件)
```

## 旧目录处理

`/opt/ypat` **保留不变**。

未执行:`git reset --hard` / `git clean` / `git stash` / `git pull`。

后续可在审批后归档为 `/opt/ypat-legacy-<timestamp>`,但本轮不做。
