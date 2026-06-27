# 页面：首页 / 发现 / 搜索 (Module B)

## 1. 身份
| 新版 | 旧版 | 映射 |
|---|---|---|
| pages/home/index | pages/home/home/index (+ homeList 组件) | MAPPED |
| pages/discover/index | (无) **新增** | NEW |
| pages-sub/ypat/search | (无,旧无搜索页) **新增** | NEW |

## 2. 关键事实(纠正基线假设)
- **旧首页无 GPS 定位、无搜索**。旧版筛选靠右侧抽屉(职业/性别/实名/保证金)+ tab(currentTab type)+ homeList 组件分页。
- 因此新版的"同城/定位""搜索""发现页""风格 chips"均为**新增功能,非 1:1 迁移项**。
- 旧首页 onShow 调 `user_get → getNextUrl(res,'home')`,即资料不完整在首页也会被引导(新版仅登录时引导,见差异)。
- 图片: 旧 homeItem `:src="item2"`(pics 原值,无前缀)→ 后端 pics 为绝对地址;新版 `item.pics[0]` 原值一致 ✅(无需 imageBaseUrl 前缀)。

## 3. 列表/分页/状态(核心浏览流)
| 维度 | 旧 | 新 | 结论 |
|---|---|---|---|
| 列表接口 | tc/zx/tj_list(GET) | getLatestList/getRecommendList(GET ✅) | 一致 |
| 状态过滤 | shtg(服务端) | 传 status:'shtg'(服务端亦强制) | 一致 |
| 下拉刷新 | onPullDownRefresh→downRefresh | onPullDownRefresh→loadList(true)+stopPullDownRefresh ✅ | 一致 |
| 上拉分页 | onReachBottom→reachBottom | onReachBottom→loadList() ✅ | 一致 |
| Loading | tui-skeleton | home-skeleton 骨架 ✅ | 一致 |
| Empty | — | KeepState empty ✅ | 新版更完善 |
| 详情跳转 | navigateTo desc?id | navigateTo detail?id ✅ | 一致 |

## 4. 差异与处理
| 编号 | 级别 | 旧 | 新 | 处理 |
|---|---|---|---|---|
| GAP-B-01 | P2 | — | 搜索"热门风格"标签把风格当 city 传 → 永远搜不到 | **已修复**: search 命中已知 PHOTO_STYLE → patstyle,否则 city |
| GAP-B-02 | P3 | 首页 onShow getNextUrl 引导完善资料 | 仅登录时引导 | 可接受: 登录门禁覆盖主路径;发布另有前置兜底;避免每次进首页强跳的激进 UX。记录 |
| GAP-STATE-01 | P3(降级) | 旧无 GPS | 新"同城"tab getLocation 拿到坐标后仍写死"全国"(无逆地理编码) | 新功能非parity;当前优雅降级为全国(=不按城市过滤)。逆地理编码需地图SDK,留作增强 |
| GAP-B-03 | P3 | 筛选: 职业/性别/实名/保证金 | 筛选: target/合作方式/风格 | 功能集差异,均可用;非阻塞,记录 |
| GAP-B-04 | P3 | — | 搜索无历史记录 | 新功能无parity基线;留作增强(localStorage 最近搜索) |

## 5. 修改
- frontend/src/pages-sub/ypat/search.vue: 关键词命中 PHOTO_STYLE → patstyle 过滤(修复首页/发现风格标签搜索)
- 未改视觉

## 6. 验证
- type-check ✅ / test 42/42 ✅
- 结论: 核心浏览流(列表/刷新/分页/详情)与旧版一致;新增功能可用;无 P0/P1/P2 遗留(GAP-B-01 已修复)
