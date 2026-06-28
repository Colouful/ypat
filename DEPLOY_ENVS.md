# 部署注意事项

## 环境配置

- `.env.staging` → 预发环境（panghu.work，本次部署）
- `.env.production` → 生产环境（www.panghu.work，TODO）
- `.env.development` → 本地开发环境

## 本地开发预发

```bash
cd frontend
# 编译预发版
npm run build:h5 -- --mode staging
# 查看结果
open dist/build/h5/index.html
```

## 部署预发

```bash
cd frontend
npm run build:h5 -- --mode staging
rsync -avz --delete dist/build/h5/ root@82.156.14.216:/var/www/panghu.work/
```

## 部署生产

```bash
cd frontend
npm run build:h5 -- --mode production
# TODO: rsync 到生产服务器
```

## 后端 Profile

- `dev` → 本地开发
- `pre` → 预发环境（本次部署）
- `pro` → 生产环境（TODO）