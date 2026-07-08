# 项目修改边界

## 默认目标端

- 小程序、移动端、用户侧前端需求，默认只修改 `frontend` 新版小程序。
- 管理后台、运营后台、审核治理需求，默认只修改 `frontend-admin` 新版管理后台。
- 后端接口需求按实际接口归属修改后端模块，但前端对接仍以 `frontend` 和 `frontend-admin` 为准。

## 禁止误改

- 禁止把新版需求实现到 `91pai-master`。
- `91pai-master` 只在用户明确写出“旧小程序”或明确点名 `91pai-master` 时维护。
- 不要修改 `.omx/state/session.json`。
- 已存在的用户未提交改动必须保留，不得回退、覆盖或顺手清理。

## 执行要求

- 修改前先确认目标目录，涉及小程序时优先检查 `frontend/src/pages.json`。
- 涉及后台时优先检查 `frontend-admin/src/constants/menu.ts` 和权限路由映射。
- 完成后至少运行与本次修改相关的结构验证、类型检查或单元测试。
