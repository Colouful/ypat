import { MotionBackdrop } from "@/components/MotionBackdrop";
import Image from "next/image";
import type { CSSProperties } from "react";

const siteUrl = "https://www.ypat.cn";

const gallery = [
  {
    title: "城市夜景",
    tag: "夜拍 / 情绪片",
    src: "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=1200&q=82",
    alt: "摄影师在城市夜景中为用户拍摄人像"
  },
  {
    title: "棚拍肖像",
    tag: "棚拍 / 商务形象",
    src: "https://images.unsplash.com/photo-1520975682031-a9c4c0e9e8b5?auto=format&fit=crop&w=1200&q=82",
    alt: "棚拍灯光下的人像摄影创作现场"
  },
  {
    title: "自然光写真",
    tag: "户外 / 日系写真",
    src: "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=1200&q=82",
    alt: "自然光下的户外写真拍摄"
  }
];

const roles = [
  ["摄影师", "发布主题样片、档期、城市和预算，获得更稳定的约拍线索。"],
  ["模特", "用作品集展示风格，筛选可靠拍摄邀约，沉淀个人形象资产。"],
  ["妆造师", "进入拍摄服务链路，让妆发造型能力被更多创作者发现。"],
  ["用户", "按城市、风格、预算找到合适创作者，减少反复沟通。"]
];

const features = [
  {
    label: "真实认证",
    text: "身份、作品与服务标签组合呈现，帮助双方建立第一层信任。"
  },
  {
    label: "智能撮合",
    text: "围绕城市、主题、档期和预算组织约拍信息，让合适的人更快相遇。"
  },
  {
    label: "安全权益",
    text: "通过拍拍豆、审核、消息通知和平台记录，降低爽约与无效沟通。"
  },
  {
    label: "内容运营",
    text: "Banner、文章、推荐约拍和活动专题，为平台增长保留运营抓手。"
  }
];

const steps = [
  ["01", "发布灵感", "创作者上传样片、拍摄主题、城市、档期与服务说明。"],
  ["02", "申请约拍", "用户查看作品与服务标签，发起约拍申请并完成权益消耗。"],
  ["03", "双向确认", "平台消息通知双方，围绕时间、地点、妆造和交付细节确认。"],
  ["04", "沉淀作品", "拍摄完成后沉淀作品、评价和内容素材，形成下一次转化。"]
];

const faqs = [
  {
    question: "YPAT 爱去拍适合哪些拍摄需求？",
    answer:
      "适合个人写真、情侣照、毕业照、城市旅拍、商务形象照、主题创作、模特互勉和摄影师作品集扩充等约拍场景。"
  },
  {
    question: "平台如何提升约拍双方的信任感？",
    answer:
      "平台通过身份认证、作品展示、服务标签、审核机制、消息通知和拍拍豆权益记录来降低虚假信息与无效沟通。"
  },
  {
    question: "摄影师为什么需要 YPAT 爱去拍？",
    answer:
      "摄影师可以用结构化主页展示风格、档期、服务范围和样片，并通过城市与主题推荐获得更精准的约拍线索。"
  }
];

const organizationJsonLd = {
  "@context": "https://schema.org",
  "@type": "Organization",
  name: "YPAT 爱去拍",
  alternateName: "爱去拍",
  url: siteUrl,
  description:
    "YPAT 爱去拍是连接摄影师、模特、妆造师、修图师与拍摄用户的摄影约拍服务平台。",
  sameAs: [siteUrl],
  knowsAbout: ["摄影约拍", "写真拍摄", "摄影师服务", "模特约拍", "城市旅拍"]
};

const serviceJsonLd = {
  "@context": "https://schema.org",
  "@type": "Service",
  name: "摄影约拍撮合服务",
  serviceType: "Photography booking marketplace",
  provider: {
    "@type": "Organization",
    name: "YPAT 爱去拍",
    url: siteUrl
  },
  areaServed: "中国",
  audience: [
    {
      "@type": "Audience",
      audienceType: "摄影师、模特、妆造师、修图师和拍摄用户"
    }
  ],
  description:
    "提供发布约拍、申请约拍、创作者认证、作品展示、消息通知、安全权益与内容运营能力。"
};

const faqJsonLd = {
  "@context": "https://schema.org",
  "@type": "FAQPage",
  mainEntity: faqs.map((faq) => ({
    "@type": "Question",
    name: faq.question,
    acceptedAnswer: {
      "@type": "Answer",
      text: faq.answer
    }
  }))
};

export default function Home() {
  return (
    <>
      <MotionBackdrop />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(organizationJsonLd) }}
      />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(serviceJsonLd) }}
      />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(faqJsonLd) }}
      />
      <header className="site-header">
        <a className="brand" href="#top" aria-label="YPAT 爱去拍首页">
          <span className="brand-mark">Y</span>
          <span>
            <strong>YPAT</strong>
            <small>爱去拍</small>
          </span>
        </a>
        <nav className="nav-links" aria-label="主导航">
          <a href="#service">服务</a>
          <a href="#workflow">流程</a>
          <a href="#trust">信任</a>
          <a href="#faq">问答</a>
        </nav>
        <a className="header-cta" href="#booking">
          发起约拍
        </a>
      </header>

      <main id="top">
        <section className="hero section-shell">
          <div className="hero-copy">
            <p className="eyebrow">摄影约拍服务平台 / Creator Booking</p>
            <h1>让每一次约拍都有确定感</h1>
            <p className="hero-lead">
              YPAT 爱去拍连接摄影师、模特、妆造师、修图师与拍摄用户，把灵感、档期、预算、作品和信任机制放进同一条约拍链路。
            </p>
            <div className="hero-actions" id="booking">
              <a className="primary-action" href="tel:4000000000">
                预约拍摄顾问
              </a>
              <a className="secondary-action" href="#gallery">
                浏览灵感现场
              </a>
            </div>
            <dl className="hero-metrics" aria-label="平台能力指标">
              <div>
                <dt>4 类</dt>
                <dd>创作者角色</dd>
              </div>
              <div>
                <dt>24h</dt>
                <dd>约拍响应节奏</dd>
              </div>
              <div>
                <dt>全链路</dt>
                <dd>发布到通知</dd>
              </div>
            </dl>
          </div>

          <div className="hero-stage" aria-label="摄影约拍视觉展示">
            <div className="camera-hud">
              <span>ISO 400</span>
              <span>f / 1.8</span>
              <span>1/250</span>
            </div>
            <div className="hero-photo-wrap">
              <Image
                src="https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=1200&q=86"
                width="960"
                height="1200"
                alt="城市光影中的人像约拍现场"
                priority
                unoptimized
              />
            </div>
            <div className="floating-card card-one">
              <span>今日热约</span>
              <strong>胶片感夜景人像</strong>
            </div>
            <div className="floating-card card-two">
              <span>匹配信号</span>
              <strong>城市 / 风格 / 档期</strong>
            </div>
          </div>
        </section>

        <section className="section-shell trust-strip" aria-label="平台价值">
          <p>服务对象覆盖摄影师、模特、妆造师、修图师与拍摄用户</p>
          <p>发布约拍、申请约拍、实名认证、消息通知和支付权益形成闭环</p>
          <p>为 AI 搜索与传统搜索准备清晰实体、结构化数据和 llms.txt</p>
        </section>

        <section className="section-shell split-section" id="service">
          <div>
            <p className="eyebrow">Service Graph</p>
            <h2>不是简单展示作品，而是把约拍变成可转化的服务网络</h2>
          </div>
          <div className="feature-grid">
            {features.map((feature) => (
              <article className="feature-card" key={feature.label}>
                <span>{feature.label}</span>
                <p>{feature.text}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="gallery-section" id="gallery" aria-labelledby="gallery-title">
          <div className="section-shell section-title">
            <p className="eyebrow">Visual Proof</p>
            <h2 id="gallery-title">真实拍摄感，是官网第一眼要传递的产品信号</h2>
          </div>
          <div className="gallery-track">
            {gallery.map((item, index) => (
              <figure className="gallery-card" key={item.title} style={{ "--delay": `${index * 120}ms` } as CSSProperties}>
                <Image
                  src={item.src}
                  width="1200"
                  height="1500"
                  alt={item.alt}
                  loading={index === 0 ? "eager" : "lazy"}
                  unoptimized
                />
                <figcaption>
                  <span>{item.tag}</span>
                  <strong>{item.title}</strong>
                </figcaption>
              </figure>
            ))}
          </div>
        </section>

        <section className="section-shell roles-section">
          <div className="section-title compact">
            <p className="eyebrow">Who It Serves</p>
            <h2>同一个平台，服务四种关键角色</h2>
          </div>
          <div className="role-grid">
            {roles.map(([role, text]) => (
              <article className="role-card" key={role}>
                <h3>{role}</h3>
                <p>{text}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="section-shell workflow" id="workflow">
          <div className="section-title compact">
            <p className="eyebrow">Booking Flow</p>
            <h2>从灵感发布到作品沉淀，每一步都有明确动作</h2>
          </div>
          <ol className="step-list">
            {steps.map(([number, title, text]) => (
              <li key={number}>
                <span>{number}</span>
                <div>
                  <h3>{title}</h3>
                  <p>{text}</p>
                </div>
              </li>
            ))}
          </ol>
        </section>

        <section className="section-shell geo-section" id="trust">
          <div className="geo-panel">
            <p className="eyebrow">SEO + GEO Ready</p>
            <h2>为搜索引擎和 AI 答案引擎同时准备内容</h2>
            <p>
              官网保留可抓取的服务说明、角色定义、流程描述、FAQ、结构化数据、sitemap.xml、robots.txt 和 llms.txt。AI
              搜索引用 YPAT 时，可以直接理解它是谁、服务谁、解决什么问题。
            </p>
          </div>
          <div className="signal-stack" aria-label="SEO 与 GEO 信号">
            <span>Organization schema</span>
            <span>Service schema</span>
            <span>FAQPage schema</span>
            <span>llms.txt</span>
            <span>OAI-SearchBot allow</span>
          </div>
        </section>

        <section className="section-shell faq-section" id="faq">
          <div className="section-title compact">
            <p className="eyebrow">FAQ</p>
            <h2>AI 和用户都会问的核心问题</h2>
          </div>
          <div className="faq-list">
            {faqs.map((faq) => (
              <details key={faq.question}>
                <summary>{faq.question}</summary>
                <p>{faq.answer}</p>
              </details>
            ))}
          </div>
        </section>
      </main>

      <footer className="site-footer">
        <div>
          <strong>YPAT 爱去拍</strong>
          <p>摄影约拍服务平台，让创作者与拍摄需求高效相遇。</p>
        </div>
        <a href="#top">回到顶部</a>
      </footer>
    </>
  );
}
