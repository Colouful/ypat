# YPAT 爱去拍官网

Next.js 官网项目，面向 SEO 和 GEO 优化：

- `app/layout.tsx`: 全站 metadata、Open Graph、Twitter Card 和 robots 策略
- `app/page.tsx`: 可索引首页内容、FAQ 和 JSON-LD 结构化数据
- `app/sitemap.ts`: sitemap.xml
- `app/robots.ts`: robots.txt，包含 AI 搜索爬虫放行规则
- `app/llms.txt/route.ts`: llms.txt，给 AI 搜索和智能体读取的站点摘要
- `app/og/route.tsx`: 动态 OG 图

开发：

```bash
pnpm install
pnpm dev
```
