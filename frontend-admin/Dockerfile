# syntax=docker/dockerfile:1
# YPAT 后台管理前端 (Vue3 + Vite + TS) 生产镜像
# 多阶段构建：stage 1 编译静态产物，stage 2 使用 nginx 托管

# -----------------------------------------------------------------------------
# 阶段 1：构建
# -----------------------------------------------------------------------------
FROM node:20-alpine AS builder

LABEL maintainer="YPAT DevOps"
LABEL description="YPAT admin dashboard frontend build image"

WORKDIR /app

# 启用 pnpm（与仓库 lockfile 保持一致）
RUN corepack enable && corepack prepare pnpm@9 --activate

# 先复制依赖描述文件，利用 Docker 构建缓存
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

# 再复制源码（此层变化最频繁）
COPY . .

# 默认使用 production 模式构建；本地 Docker 部署可通过 build arg 覆盖为 docker
ARG APP_ENV=production
ENV APP_ENV=${APP_ENV}

# 先类型检查，再按指定模式构建
RUN pnpm run type-check && pnpm exec vite build --mode ${APP_ENV}

# -----------------------------------------------------------------------------
# 阶段 2：运行
# -----------------------------------------------------------------------------
FROM nginx:1.25-alpine AS runtime

LABEL maintainer="YPAT DevOps"
LABEL description="YPAT admin dashboard frontend runtime image"

# 以非 root 用户运行，容器内监听 8080
RUN rm -f /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 8080

# nginx 镜像默认用户为 root，master 进程需要 root 绑定低端口；
# 这里监听 8080，因此可以降级到 nginx 用户运行 worker 进程
# 需要提前给 nginx 用户创建/授权缓存目录，否则 worker 起不来
RUN mkdir -p /var/cache/nginx/client_temp /var/cache/nginx/proxy_temp \
             /var/cache/nginx/fastcgi_temp /var/cache/nginx/uwsgi_temp \
             /var/cache/nginx/scgi_temp \
    && chown -R nginx:nginx /var/cache/nginx /var/run \
    && touch /var/run/nginx.pid && chown nginx:nginx /var/run/nginx.pid
USER nginx

HEALTHCHECK --interval=10s --timeout=5s --start-period=5s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/ >/dev/null 2>&1 || exit 1

CMD ["nginx", "-g", "daemon off;"]
