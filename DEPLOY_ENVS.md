# 部署注意事项

## 环境配置

- `.env.staging` → 预发环境（panghu.work，本次部署）
- `.env.production` → 生产环境（www.panghu.work，TODO）
- `.env.development` → 本地开发环境

## 本地开发预发

### H5

```bash
cd frontend
# 预发环境开发
npm run dev:h5:staging
# 生产环境开发
npm run dev:h5:production
```

### 微信小程序

```bash
cd frontend
# 预发环境开发
npm run dev:mp-weixin:staging
# 生产环境开发
npm run dev:mp-weixin:production
```

## 部署预发

### H5

```bash
cd frontend
npm run build:h5:staging
rsync -avz --delete dist/build/h5/ root@82.156.14.216:/var/www/panghu.work/
```

### 微信小程序

```bash
cd frontend
npm run build:mp-weixin:staging
# 用微信开发者工具打开 dist/build/mp-weixin
```

## 部署生产

### H5

```bash
cd frontend
npm run build:h5:production
# TODO: rsync 到生产服务器
```

### 微信小程序

```bash
cd frontend
npm run build:mp-weixin:production
# 用微信开发者工具打开 dist/build/mp-weixin，提交审核
```

## 后端 Profile

- `dev` → 本地开发
- `pre` → 预发环境（本次部署）
- `pro` → 生产环境（TODO）