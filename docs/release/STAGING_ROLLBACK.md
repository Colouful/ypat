# STAGING 回滚指南

## 回滚场景

- 新部署导致服务异常
- 数据库迁移失败
- 前端部署错误
- 配置文件错误

## 回滚流程

### 1. 前端回滚

```bash
# 查看可用备份
ls -la /opt/ypat-data/backups/ | grep frontend

# 回滚到指定版本
BACKUP_DIR=/opt/ypat-data/backups/frontend_20250629_120000
rm -rf /var/www/panghu.work
mkdir -p /var/www/panghu.work
cp -r ${BACKUP_DIR}/* /var/www/panghu.work/
```

### 2. 后端镜像回滚

```bash
# 查看可用镜像版本
docker images | grep ypat

# 停止当前服务
docker compose -f docker-compose.staging.yml down

# 修改 docker-compose.staging.yml 指定旧版本镜像
# 然后启动
docker compose -f docker-compose.staging.yml up -d
```

### 3. 数据库回滚

```bash
# 查看备份
ls -la /opt/ypat-data/backups/mysql/

# 恢复备份
BACKUP_FILE=/opt/ypat-data/backups/mysql/ypat_20250629_120000.sql.gz
gunzip -c ${BACKUP_FILE} | docker exec -i ypat-mysql mysql -u root -p'$MYSQL_PASSWORD' ypat
```

### 4. Nginx 配置回滚

```bash
# 查看备份
ls -la /etc/nginx/conf.d/panghu.work.conf.bak.*

# 恢复备份
cp /etc/nginx/conf.d/panghu.work.conf.bak.20250629_120000 /etc/nginx/conf.d/panghu.work.conf
nginx -t
systemctl reload nginx
```

### 5. 使用回滚脚本

```bash
./scripts/deploy/rollback-staging.sh 20250629_120000
```

## 回滚检查清单

- [ ] 前端版本已回滚
- [ ] 后端服务已重启
- [ ] 数据库已恢复
- [ ] Nginx 配置已恢复
- [ ] 端到端验证通过
- [ ] 监控指标正常

## 数据库回滚注意事项

⚠️ **警告**: 数据库回滚可能导致数据丢失

- 回滚前必须备份当前数据库
- 确认回滚时间点和影响范围
- 通知相关人员
- 回滚后执行数据验证

## 联系信息

| 角色 | 联系人 | 联系方式 |
| --- | --- | --- |
| 运维负责人 | TODO | TODO |
| 开发负责人 | TODO | TODO |
| 数据库管理员 | TODO | TODO |