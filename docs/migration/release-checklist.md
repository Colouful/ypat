# YPAT 发布检查清单

## 微信小程序后台配置

### 服务器域名配置
- [ ] request 合法域名: `https://www.91qupaier.com`
- [ ] uploadFile 合法域名: `https://www.91qupaier.com`
- [ ] downloadFile 合法域名: `https://www.91qupaier.com`
- [ ] socket 合法域名: (暂不需要)

### 业务域名
- [ ] `https://www.91qupaier.com` (用于 web-view)

### 隐私协议
- [ ] 配置用户隐私保护指引
- [ ] 声明收集的用户信息: 位置信息、相册、相机
- [ ] 配置隐私协议接口调用说明

### 用户信息授权
- [ ] 配置 getUserProfile 使用说明
- [ ] 配置获取手机号使用说明 (如需要)
- [ ] 配置位置信息使用说明

### 订阅消息模板
- [ ] 拍摄通知模板 (TEMP_0)
- [ ] 实名认证审核模板 (TEMP_1)
- [ ] 发布信息审核模板 (TEMP_2)
- [ ] 新订单通知模板 (TEMP_3)

### 微信支付
- [ ] 确认商户号 (1561830771) 状态正常
- [ ] 确认支付权限已开通
- [ ] 配置支付回调 URL
- [ ] 测试环境支付联调

### 服务类目
- [ ] 选择合适的小程序服务类目
- [ ] 准备对应资质文件

### 小程序备案
- [ ] ICP 备案信息
- [ ] 网站域名备案号
- [ ] 主体信息确认

## 测试账号准备
- [ ] 准备微信小程序审核测试账号
- [ ] 账号中有足够拍拍豆余额
- [ ] 账号已完善资料
- [ ] 准备审核演示路径说明

## 审核路径
```
首页 → 浏览约拍列表 → 查看约拍详情 → (登录)
→ 发布约拍 → 提交 → 查看我的发布
→ 我的 → 实名认证 → 充值
→ 消息 → 查看消息详情
```

## 审核演示说明
- 功能入口: 首页 Tab 进入
- 核心流程: 浏览约拍 → 申请约拍 → 发布约拍
- 登录方式: 微信授权一键登录
- 注意事项: 测试环境数据，非真实交易

## H5 部署

### Nginx 配置
```nginx
server {
    listen 443 ssl;
    server_name www.91qupaier.com;
    
    ssl_certificate /etc/nginx/cert/fullchain.pem;
    ssl_certificate_key /etc/nginx/cert/privkey.pem;
    
    root /var/www/ypat-h5/;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://127.0.0.1:8081/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 7d;
        add_header Cache-Control "public, immutable";
    }
    
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
}
```

### HTTPS
- [ ] SSL 证书有效
- [ ] HTTP 自动跳转 HTTPS
- [ ] 证书覆盖所有子域名

### 跨域
- [ ] 后端 CORS 配置允许 H5 域名
- [ ] 或通过 Nginx 反向代理解决

### 缓存策略
- HTML: no-cache (每次请求最新)
- JS/CSS: 7天强缓存 (文件名含 hash)
- 图片: 7天强缓存
- API: 不缓存

## 回滚方式

### 微信小程序
1. 登录微信小程序后台
2. 版本管理 → 选择上一个稳定版本
3. 点击"回退"
4. 确认回退

### H5
1. 保留上一版本构建产物
2. Nginx 指向上一版本目录
3. 执行 `nginx -s reload`

## 版本发布步骤

### 微信小程序发布
1. 本地运行 `npm run build:mp-weixin`
2. 微信开发者工具打开 `dist/build/mp-weixin`
3. 检查包体积 (主包 < 2MB, 总包 < 20MB)
4. 预览测试核心流程
5. 上传代码
6. 登录小程序后台提交审核
7. 填写版本说明
8. 等待审核通过
9. 灰度发布 (10% → 50% → 100%)

### H5 发布
1. 本地运行 `npm run build:h5`
2. 检查 dist/build/h5 产物
3. 上传到服务器
4. 验证页面正常访问
5. 验证接口正常调用
6. 清除 CDN 缓存 (如有)

## 上线前必须确认

### 配置类
- [ ] 微信 AppID 已填入 manifest.json
- [ ] 生产环境 API 地址正确
- [ ] 图片域名配置正确
- [ ] 去除所有 console.log (生产)
- [ ] 关闭 devtools

### 安全类
- [ ] 无硬编码密钥
- [ ] 无测试 Token
- [ ] 身份证信息脱敏
- [ ] 手机号脱敏
- [ ] Source Map 不发布到生产

### 功能类
- [ ] 登录流程正常
- [ ] 约拍浏览正常
- [ ] 发布约拍正常
- [ ] 支付流程正常 (需真实环境)
- [ ] 消息通知正常
- [ ] 退出登录正常
