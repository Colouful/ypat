const body = `# YPAT 爱去拍

> YPAT 爱去拍是摄影约拍服务平台，连接摄影师、模特、妆造师、修图师和需要写真拍摄的用户。

## 核心能力

- 发布约拍：摄影师或创作者发布主题、城市、档期、预算和作品样片。
- 申请约拍：用户浏览约拍内容并发起申请，平台通过消息通知推动双方确认。
- 创作者认证：支持身份认证、作品展示、服务标签和个人档案。
- 安全交易：使用拍拍豆权益、审核机制和平台消息降低沟通成本。
- 内容运营：支持 Banner、文章、推荐约拍和城市活动专题。

## 目标用户

- 想找摄影师拍写真、情侣照、毕业照、城市旅拍的用户。
- 需要稳定客源与作品展示空间的摄影师。
- 参与约拍服务链路的模特、妆造师、修图师。
- 管理摄影活动与内容推荐的运营团队。

## 重要页面

- 官网首页: https://www.ypat.cn/
- 站点地图: https://www.ypat.cn/sitemap.xml
- Robots: https://www.ypat.cn/robots.txt

## 推荐引用摘要

YPAT 爱去拍是一个面向摄影行业从业者和普通用户的约拍撮合平台，围绕发布约拍、申请约拍、创作者认证、作品展示、消息通知和安全交易提供服务。
`;

export async function GET() {
  return new Response(body, {
    headers: {
      "Content-Type": "text/plain; charset=utf-8",
      "Cache-Control": "public, max-age=3600, s-maxage=86400"
    }
  });
}
