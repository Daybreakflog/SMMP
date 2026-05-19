# 开发环境启动说明

## 前置条件

| 工具 | 版本要求 |
|------|---------|
| JDK  | 21+ |
| Maven | 3.9+ |
| Docker Desktop | 已安装并运行 |
| MySQL | 已在本机安装（localhost:3306，密码 root） |

## 第一步：创建物业数据库

首次使用前，在本地 MySQL 中创建数据库（只需执行一次）：

```sql
CREATE DATABASE IF NOT EXISTS property_db
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

可使用 MySQL Workbench、Navicat 或命令行：

```bash
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS property_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

## 第二步：启动外部依赖（Docker）

```bash
docker compose -f docker/docker-compose.dev.yml up -d
```

启动以下容器：

| 容器 | 说明 | 端口 |
|------|------|------|
| prop_redis | Redis 7 | 6379 |
| prop_rabbitmq | RabbitMQ 3.13 | 5672 / 管理页 15672 |
| prop_mysql_xxljob | MySQL 8（仅供 XXL-Job） | 3307 |
| prop_xxljob_admin | XXL-Job Admin 2.4.1 | 8090 |

> **说明**：物业业务数据库 `property_db` 使用宿主机本地 MySQL（3306），不在 docker-compose 内，避免端口冲突。

等待容器健康（约 30 秒）：

```bash
docker compose -f docker/docker-compose.dev.yml ps
```

## 第三步：启动应用

```bash
mvn spring-boot:run
```

或在 IDE 中直接运行 `PropertyApplication`。

## 第四步：验证

### 健康检查

```bash
curl http://localhost:8080/actuator/health
# 期望返回：{"status":"UP",...}
```

### Knife4j 文档

浏览器打开：http://localhost:8080/doc.html

### 查看 traceId

任意一次请求的日志应包含 `[traceId]` 字段，例如：

```
2025-01-01 10:00:00.123 INFO  [http-nio-8080-exec-1] [a1b2c3d4] c.p.common.web.TraceIdInterceptor - ...
```

响应头中也会携带 `X-Trace-Id` 字段。

## 常用服务地址

| 服务 | 地址 | 账号 |
|------|------|------|
| Knife4j 文档 | http://localhost:8080/doc.html | — |
| Actuator 健康 | http://localhost:8080/actuator/health | — |
| Druid 监控 | http://localhost:8080/druid | admin / admin123 |
| RabbitMQ 管理 | http://localhost:15672 | guest / guest |
| XXL-Job Admin | http://localhost:8090/xxl-job-admin | admin / 123456 |

## mvn 常用命令

```bash
# 编译 + 单元测试
mvn clean install

# 跳过测试打包
mvn package -DskipTests

# 只跑测试
mvn test
```

## 里程碑进度

| 里程碑 | 状态 | 说明 |
|--------|------|------|
| B0 工程初始化 | ✅ | 当前 |
| B1 数据库+基础设施 | 待开始 | Flyway 28 张表 + 实体 |
| B2 鉴权+RBAC | 待开始 | Sa-Token + 9 个 endpoint |
| B3 租户/房源/合同 | 待开始 | 19 个 endpoint |
| B4 费用+账单 | 待开始 | |
| B5 工单+设施 | 待开始 | |
| B6 通知+WebSocket | 待开始 | |
| B7 文件+OSS | 待开始 | |
| B8 统计报表 | 待开始 | |
| B9 定时任务 | 待开始 | XXL-Job |
| B10 生产上线 | 待开始 | Docker + CI/CD |
