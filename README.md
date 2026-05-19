# 智能物业管理系统 — 后端

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 21 (Microsoft OpenJDK) |
| Spring Boot | 3.3.4 |
| MyBatis-Plus | 3.5.7 |
| Sa-Token | 1.39 |
| MySQL | 8.0 |
| Redis | 7 |
| RabbitMQ | 3.x |
| XXL-Job | 2.4.1 |
| Knife4j | 4.5.0 |
| Flyway | Spring Boot 内置 |

## 项目结构

```
com.property/
├── controller/          28 个 REST 控制器（187 个接口）
├── service/             28 个 Service 接口
│   ├── impl/            29 个 Service 实现
│   └── strategy/        4 个计费策略（Strategy 模式）
├── mapper/              51 个 MyBatis-Plus Mapper
├── entity/              51 个数据库实体
├── dto/
│   ├── request/         70 个请求 DTO
│   └── response/        51 个响应 VO
├── config/              9 个配置类
├── exception/           异常体系（ErrorCode + BusinessException + GlobalHandler）
├── common/api/          统一响应包装（ApiEnvelope + PageResult）
├── mq/                  RabbitMQ 配置与消费者
├── job/                 定时任务（月度出账等）
├── aop/                 AOP 切面（操作日志）
├── annotation/          自定义注解
├── satoken/             Sa-Token 权限提供者
├── ws/                  WebSocket 端点
├── constant/            常量
├── enums/               枚举（待充实）
└── util/                工具类（待充实）
```

## 本地启动

### 前置依赖

- JDK 21
- Maven 3.9+
- MySQL 8.0（localhost:3306，database: property_db）
- Redis（localhost:6379）
- RabbitMQ（localhost:5672）

### 启动步骤

```bash
# 1. 编译
mvn compile -q

# 2. 运行
mvn spring-boot:run

# 3. 访问
# API 文档：http://localhost:8080/doc.html
# Swagger：http://localhost:8080/swagger-ui.html
```

### Docker 一键启动（开发环境）

```bash
cd docker
docker-compose -f docker-compose.dev.yml up -d
```

## 数据库

- 36 张表，Flyway 自动迁移（src/main/resources/db/migration/V1~V11）
- 启动时自动执行，无需手动建表

## API 概览

| 域 | 模块 | 接口数 |
|---|---|---|
| 鉴权与系统 | auth, config, dict, template, oplog | 31 |
| 租户/合同 | tenant, contract | 19 |
| 费用/财务 | bill, feeItem, finance | 22 |
| 服务工单 | workorder, complaint, repair | 32 |
| 资源运营 | parking, facility, inspection, visitor | 38 |
| 社区互动 | announcement, poll, activity | 27 |
| 基础能力 | notification, upload, export, report, statistics | 18 |
| **合计** | — | **187** |
