import type { Metadata, Viewport } from "next";
import "./globals.css";

const siteUrl = "https://www.ypat.cn";
const siteName = "YPAT 爱去拍";
const description =
  "YPAT 爱去拍是面向摄影师、模特、妆造师与修图师的约拍服务平台，提供创作者认证、灵感约拍、档期撮合、作品展示与安全交易。";

export const metadata: Metadata = {
  metadataBase: new URL(siteUrl),
  applicationName: siteName,
  title: {
    default: "YPAT 爱去拍 | 摄影约拍服务平台",
    template: "%s | YPAT 爱去拍"
  },
  description,
  keywords: [
    "约拍",
    "摄影约拍",
    "摄影师",
    "模特约拍",
    "妆造师",
    "写真拍摄",
    "城市摄影",
    "爱去拍",
    "YPAT"
  ],
  authors: [{ name: "YPAT 爱去拍" }],
  creator: "YPAT 爱去拍",
  publisher: "YPAT 爱去拍",
  category: "photography marketplace",
  alternates: {
    canonical: "/"
  },
  openGraph: {
    type: "website",
    locale: "zh_CN",
    url: "/",
    siteName,
    title: "YPAT 爱去拍 | 让每一次约拍都有确定感",
    description,
    images: [
      {
        url: "/og",
        width: 1200,
        height: 630,
        alt: "YPAT 爱去拍摄影约拍服务平台"
      }
    ]
  },
  twitter: {
    card: "summary_large_image",
    title: "YPAT 爱去拍 | 摄影约拍服务平台",
    description,
    images: ["/og"]
  },
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      "max-image-preview": "large",
      "max-snippet": -1,
      "max-video-preview": -1
    }
  },
  verification: {
    other: {
      "ai-content-declaration": "human-edited, service-information"
    }
  }
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 5,
  themeColor: "#0d0f0c",
  colorScheme: "dark light"
};

export default function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <body>{children}</body>
    </html>
  );
}
