import { ImageResponse } from "next/og";

export const runtime = "edge";

export async function GET() {
  return new ImageResponse(
    (
      <div
        style={{
          width: "100%",
          height: "100%",
          display: "flex",
          flexDirection: "column",
          justifyContent: "space-between",
          background: "linear-gradient(135deg, #10130f 0%, #202415 45%, #f2b84b 100%)",
          color: "#fff8e6",
          padding: "72px",
          fontFamily: "system-ui"
        }}
      >
        <div style={{ fontSize: 34, letterSpacing: 2 }}>YPAT 爱去拍</div>
        <div style={{ fontSize: 78, fontWeight: 800, lineHeight: 1.04, maxWidth: 880 }}>
          让每一次约拍都有确定感
        </div>
        <div style={{ fontSize: 30, color: "#ffe2a6" }}>
          摄影师、模特、妆造师与用户的约拍服务平台
        </div>
      </div>
    ),
    {
      width: 1200,
      height: 630
    }
  );
}
