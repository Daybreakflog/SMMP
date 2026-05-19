# 智能物业管理系统 · 后端开发计划（Spring Boot 版）

> **版本**：v2.0 · 编制日期：2026-05-15
> **基线**：前端开发计划 v1.2 已交付（M0~M11 commit 全绿）
> **配套文档**：[`docs/api.md`](./api.md)（98 个 endpoint）+ [`docs/openapi.yaml`](./openapi.yaml)（机器可读契约）
> **适用角色**：Java 后端工程师 / 后端 Tech Lead
> **备选方案**：[`docs/backend-plan-nestjs.md`](./backend-plan-nestjs.md)（NestJS + TypeScript）
>
> 本计划以"前端已存在 + 接口契约已固化"为基线倒推，节奏与前端 M0~M11 对齐，
> 可全程**前后端并行**开发，前端 mock 切换为真实接口零代码改动。

---

## 目录

- [Part 1 · 后端需求（从前端反推）](#part-1)
- [Part 2 · 技术架构](#part-2)
- [Part 3 · 数据库设计](#part-3)
- [Part 4 · 开发里程碑（B0~B10）](#part-4)
- [Part 5 · 关键模块设计](#part-5)
- [Part 6 · 部署与运维](#part-6)
- [Part 7 · Claude 执行指令模板](#part-7)
- [附录 A · 前后端对接清单](#a)
- [附录 B · pom.xml 完整依赖](#b)

---

<a id="part-1"></a>

# Part 1 · 后端需求（从前端反推）

## 1.1 接口契约（来自 `docs/api.md`）

| 模块            | endpoints | 关键复杂度                                 |
| --------------- | --------- | ------------------------------------------ |
| 鉴权 + 角色权限 | 9         | RBAC + Token 刷新 + 并发请求队列           |
| 租户            | 9         | Excel 导入预览/提交两阶段                  |
| 房源 / 单元     | 4         | 树结构 + 入住/退租事务                     |
| 合同            | 6         | 状态机 + 批量续约 + OSS STS                |
| 账单 + 费用项   | 14        | 4 种计费规则 + 抄表 + 阶梯 + 异步出账      |
| 财务            | 8         | 对账三方比对 + 关账事务 + 智能认领规则引擎 |
| 工单            | 10        | 状态机 + SLA + WebSocket 聊天 + 改派       |
| 投诉 / 申诉     | 8         | 三向闭环 + 月度分析聚合                    |
| 仪表盘 + 报表   | 10        | 实时聚合 + 异步导出任务                    |
| 系统设置        | 14        | 用户 + 角色矩阵 + 操作日志 + 消息模板引擎  |
| 小程序专属      | 4         | 通知中心 + Badge + 公告                    |
| 文件上传        | 2         | OSS STS 签发                               |
| WebSocket       | 1         | 工单聊天 + 心跳                            |
| **合计**        | **~98**   | –                                          |

## 1.2 性能 / 容量要求

| 项             | 目标                                          |
| -------------- | --------------------------------------------- |
| 并发用户       | 后台 200 并发 / 小程序 5000 并发              |
| 接口 P99       | ≤ 300ms（除报表 + 异步任务）                  |
| 报表查询       | 千万级账单 5s 内                              |
| WebSocket 连接 | 单实例 10k，水平扩展                          |
| 出账批处理     | 10w 户 / 小时                                 |
| 数据保留       | 业务热数据 2 年，操作日志 1 年，归档冷存 7 年 |

## 1.3 非功能需求

| 项         | 要求                                                         |
| ---------- | ------------------------------------------------------------ |
| 部署       | Docker + K8s（或 Docker Compose 小规模起步）                 |
| 高可用     | MySQL 主备 + Redis 哨兵 + 应用 ≥ 2 副本                      |
| 数据库备份 | XtraBackup 每日全备 + binlog 持续归档，RPO ≤ 5min            |
| 审计       | 所有写操作记录到 `op_logs`，含 actor / diff / IP / UA        |
| 安全       | HTTPS 全站 / MyBatis 预编译防 SQL 注入 / XSS / CSRF / BCrypt |
| 合规       | 个人信息脱敏导出 / 用户注销 / 等保 2.0                       |
| 监控       | Prometheus + Grafana + Loki + SkyWalking + Sentry            |

---

<a id="part-2"></a>

# Part 2 · 技术架构

## 2.1 技术选型（推荐 · 国内物业行业实践对齐）

| 类别      | 选型                                                         | 理由                                                                    |
| --------- | ------------------------------------------------------------ | ----------------------------------------------------------------------- |
| JDK       | **JDK 21 LTS** (Eclipse Temurin)                             | 虚拟线程 + Pattern Matching，物业场景大量阻塞 IO 收益高                 |
| 框架      | **Spring Boot 3.3.x**                                        | Jakarta EE 9+，与 Spring Cloud 2023.x 兼容                              |
| 语言      | **Java 21** 主力 / **Kotlin 1.9** 可选                       | 团队熟 Java 选 Java；要享受 NPE 检查 + Coroutine 选 Kotlin              |
| 构建      | **Maven 3.9**                                                | 国内物业行业默认，IDE 支持好；Gradle 可选但调优麻烦                     |
| ORM       | **MyBatis-Plus 3.5.x**                                       | 国内物业行业 80%+ 项目用 MP，CRUD 模板代码极少；JPA 备选但 N+1 易踩坑   |
| 数据库    | **MySQL 8.0**                                                | 物业行业绝对主流；PostgreSQL 备选但运维成本略高                         |
| 连接池    | **Druid 1.2.x**                                              | 阿里出品，国内默认，自带 SQL 监控面板                                   |
| 缓存      | **Redis 7** (Lettuce 客户端)                                 | 单点 / 哨兵 / 集群按规模                                                |
| 鉴权      | **Sa-Token 1.39.x**                                          | 国内简化版安全框架，配置量是 Spring Security 1/10；Spring Security 备选 |
| 消息队列  | **RabbitMQ 3.13**                                            | 物业的可靠消息（出账、通知）优先；高吞吐切 RocketMQ 5.x                 |
| 定时任务  | **XXL-Job 2.4.x**                                            | 国内分布式调度事实标准，分片广播跑批                                    |
| WebSocket | **Spring WebSocket + STOMP** + **Spring Session Redis**      | 跨实例共享 session；规模大切 Netty 自实现                               |
| 验证      | **Hibernate Validator 8** (Jakarta Bean Validation 3)        | Spring Boot 自带                                                        |
| OpenAPI   | **springdoc-openapi 2.6** + **Knife4j 4.5**                  | Knife4j 是 springdoc 中文增强版，文档更友好                             |
| 工具库    | **Hutool 5.8** + **MapStruct 1.6** + **Lombok 1.18**         | Hutool 国内事实标准；MapStruct 编译期 DTO 转换零运行时开销              |
| 文件存储  | **阿里云 OSS** (`aliyun-sdk-oss`) / **MinIO** (`minio-java`) | STS 临时凭证签发                                                        |
| 微信支付  | **`wechatpay-java` 0.2.x**（官方 SDK）                       | V3 接口                                                                 |
| 支付宝    | **`alipay-sdk-java`**                                        | –                                                                       |
| 短信      | **`aliyun-java-sdk-dysmsapi`** / 腾讯云                      | 抽象 channel 适配                                                       |
| 邮件      | **Spring Mail** + 企业 SMTP                                  | –                                                                       |
| Excel     | **EasyExcel 3.3.x**（阿里出品）                              | 大数据量流式读写，物业报表导出必备                                      |
| PDF       | **iText 7 / OpenPDF**                                        | 合同盖章模板                                                            |
| 日志      | **Logback** + **Logstash JSON encoder**                      | 进 ELK / Loki                                                           |
| 链路追踪  | **SkyWalking 10.x** Agent                                    | 国内 APM 事实标准，零代码侵入                                           |
| 测试      | **JUnit 5 + Mockito + Spring Boot Test + Testcontainers**    | –                                                                       |
| 部署      | **Docker multi-stage + K8s** / docker-compose                | –                                                                       |
| CI        | **GitHub Actions**                                           | 与前端共用                                                              |

> **为什么不选 Spring Cloud 全家桶？** 物业系统单体足够（200 后台 + 5k 小程序并发），过度微服务化反而拖慢迭代。等业务真到了拆服务的体量再上 Nacos / Sentinel / Seata。
>
> **为什么 MyBatis-Plus 不选 JPA？** 国内物业行业项目交付场景多对接老 DBA，DBA 看 MyBatis 的 SQL 比 JPA 生成的 DSL 顺眼得多；MP 的 LambdaQueryWrapper 也比 Criteria API 易读。

## 2.2 工程目录结构（单模块 Maven，团队大了再拆）

```
property-backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/property/
│   │   │   ├── PropertyApplication.java        # @SpringBootApplication 入口
│   │   │   ├── common/                         # 通用基础
│   │   │   │   ├── annotation/                 # @AuditLog @CurrentUser @DataScope
│   │   │   │   ├── aspect/                     # AuditLogAspect / DataScopeAspect
│   │   │   │   ├── config/                     # Web/Redis/MyBatis/Knife4j/Sa-Token 配置
│   │   │   │   ├── constant/                   # 错误码、枚举字符串
│   │   │   │   ├── dto/                        # ApiEnvelope, PageQuery, PageResult
│   │   │   │   ├── exception/                  # BusinessException + GlobalExceptionHandler
│   │   │   │   ├── interceptor/                # TraceIdInterceptor
│   │   │   │   └── util/                       # OSS / WxPay / SmsClient 等
│   │   │   ├── infra/
│   │   │   │   ├── mybatis/                    # MybatisPlusConfig + 通用填充
│   │   │   │   ├── redis/                      # RedisConfig + RedisUtil
│   │   │   │   ├── mq/                         # RabbitTemplate + 消费者
│   │   │   │   ├── ws/                         # WebSocket 配置（Redis Pub/Sub 跨实例）
│   │   │   │   └── storage/                    # OSS Client
│   │   │   ├── modules/                        # 业务模块（按聚合根）
│   │   │   │   ├── auth/                       # 鉴权
│   │   │   │   │   ├── controller/AuthController.java
│   │   │   │   │   ├── service/AuthService.java
│   │   │   │   │   ├── dto/LoginDTO.java
│   │   │   │   │   └── vo/LoginVO.java
│   │   │   │   ├── tenant/
│   │   │   │   ├── unit/
│   │   │   │   ├── contract/
│   │   │   │   ├── bill/
│   │   │   │   │   ├── controller/
│   │   │   │   │   ├── service/
│   │   │   │   │   ├── engine/                 # 4 种计费策略 strategy 模式
│   │   │   │   │   └── job/MonthlyBillingJob   # XXL-Job 出账任务
│   │   │   │   ├── finance/
│   │   │   │   │   ├── reconcile/
│   │   │   │   │   ├── close/
│   │   │   │   │   └── claim/                  # 智能认领规则引擎
│   │   │   │   ├── workorder/
│   │   │   │   │   ├── controller/
│   │   │   │   │   ├── service/
│   │   │   │   │   ├── statemachine/           # Spring StateMachine
│   │   │   │   │   └── ws/WorkOrderWsHandler   # WebSocket 处理器
│   │   │   │   ├── complaint/
│   │   │   │   ├── dashboard/
│   │   │   │   ├── report/
│   │   │   │   │   ├── service/
│   │   │   │   │   └── job/ReportExportJob     # 异步导出
│   │   │   │   ├── system/                     # 用户 / 角色 / 权限 / 操作日志 / 消息模板
│   │   │   │   ├── notification/               # 站内 + SMS + EMAIL + PUSH 适配
│   │   │   │   ├── upload/                     # OSS STS
│   │   │   │   └── payment/                    # 微信 / 支付宝 SDK 桥
│   │   │   └── entity/                         # MP @TableName 实体，集中存放方便 Mapper 扫描
│   │   └── resources/
│   │       ├── application.yml                 # 主配置
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── mapper/                         # MyBatis XML（复杂 SQL）
│   │       ├── db/migration/                   # Flyway SQL 脚本 V1__init.sql ...
│   │       └── logback-spring.xml
│   └── test/
│       └── java/com/property/...               # JUnit 5 单测 + Testcontainers 集成测试
├── docker/
│   ├── Dockerfile
│   ├── docker-compose.dev.yml                  # mysql + redis + rabbitmq + xxl-job-admin
│   └── docker-compose.prod.yml
├── .github/workflows/
│   ├── ci.yml
│   └── release.yml
└── docs/
    ├── architecture.md
    ├── db-erd.png
    └── runbook.md
```

## 2.3 关键约定（向前端承诺）

参考 `docs/api.md` 第 0 节。

### 统一响应包装 `ApiEnvelope`

```java
@Data
@Builder
public class ApiEnvelope<T> {
    private int code;          // 0 = 成功；4xxxx 业务错误；5xx 服务异常
    private String message;
    private T data;
    private String traceId;    // UUID v4，每请求一个，写日志 + 透传

    public static <T> ApiEnvelope<T> ok(T data) {
        return ApiEnvelope.<T>builder()
            .code(0).message("ok")
            .data(data).traceId(MDC.get("traceId")).build();
    }

    public static <T> ApiEnvelope<T> fail(int code, String message) {
        return ApiEnvelope.<T>builder()
            .code(code).message(message)
            .traceId(MDC.get("traceId")).build();
    }
}
```

实现：`@RestControllerAdvice + ResponseBodyAdvice<Object>` 自动包装；
`@RestControllerAdvice + @ExceptionHandler` 处理 `BusinessException`。

### 分页结构 `PageResult`

```java
@Data
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int pageSize;
}
```

### 金额 / 时间

- 金额：MySQL `DECIMAL(12,2)`，Java `BigDecimal`，序列化用 `@JsonSerialize(using = ToStringSerializer.class)` 强制 string，避免前端 number 精度问题
- 时间：`@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")` ISO 8601；MySQL `datetime(3)` 毫秒精度
- 时区：JVM 启动参数 `-Duser.timezone=Asia/Shanghai`，Connector/J `serverTimezone=Asia/Shanghai`

---

<a id="part-3"></a>

# Part 3 · 数据库设计

## 3.1 ER 概览（核心 28 张表）

```
租户域：     tenants ─┬─ contracts ── units ─┬─ buildings ── projects
                      │                       └─ tenant_units (历史)
                      └─ tenant_attachments

费用域：     fee_items ─┬─ fee_tiers (阶梯)
                        └─ bills ─┬─ bill_items
                                  ├─ bill_payments
                                  └─ bill_logs

财务域：     payment_records ─┬─ claims (认领关系)
                              ├─ claim_rules
                              ├─ reconciliations (日对账)
                              └─ closes (关账)

工单域：     work_orders ─┬─ wo_timelines
                          ├─ wo_messages (聊天)
                          └─ wo_attachments

投诉域：     complaints ─┬─ complaint_timelines
                        └─ appeals

系统域：     users ─┬─ user_roles ── roles ── role_permissions ── permissions
            op_logs（审计）
            message_templates
            notifications

资源域：     uploads (OSS 文件元信息)
            announcements
            export_tasks (异步报表)
```

## 3.2 关键表 DDL（Flyway V1\_\_init.sql 摘录）

```sql
-- 租户
CREATE TABLE tenants (
    id                  VARCHAR(32) NOT NULL,
    type                VARCHAR(16) NOT NULL COMMENT 'PERSONAL | COMPANY',
    name                VARCHAR(64) NOT NULL,
    id_card             VARCHAR(32),
    phone               VARCHAR(16) NOT NULL,
    social_credit_code  VARCHAR(32),
    contact_name        VARCHAR(32),
    contact_phone       VARCHAR(16),
    bank_account        VARCHAR(64),
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    deleted             TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone, deleted),
    UNIQUE KEY uk_id_card (id_card, deleted),
    UNIQUE KEY uk_social_credit (social_credit_code, deleted),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户表';

-- 合同
CREATE TABLE contracts (
    id              VARCHAR(32) NOT NULL,
    no              VARCHAR(32) NOT NULL,
    tenant_id       VARCHAR(32) NOT NULL,
    unit_id         VARCHAR(32) NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT|PENDING|ACTIVE|EXPIRED|TERMINATED',
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    monthly_rent    DECIMAL(12,2) NOT NULL,
    deposit         DECIMAL(12,2),
    pdf_url         VARCHAR(512),
    signed_at       DATETIME(3),
    terminated_at   DATETIME(3),
    deleted         TINYINT(1) NOT NULL DEFAULT 0,
    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_no (no),
    KEY idx_tenant (tenant_id),
    KEY idx_unit (unit_id),
    KEY idx_status_end_date (status, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同表';

-- 账单
CREATE TABLE bills (
    id              VARCHAR(32) NOT NULL,
    no              VARCHAR(32) NOT NULL,
    period          CHAR(7) NOT NULL COMMENT 'YYYY-MM',
    contract_id     VARCHAR(32) NOT NULL,
    tenant_id       VARCHAR(32) NOT NULL,
    unit_id         VARCHAR(32) NOT NULL,
    total_amount    DECIMAL(12,2) NOT NULL,
    paid_amount     DECIMAL(12,2) NOT NULL DEFAULT 0,
    status          VARCHAR(16) NOT NULL DEFAULT 'UNPAID',
    due_date        DATE NOT NULL,
    paid_at         DATETIME(3),
    voided_at       DATETIME(3),
    deleted         TINYINT(1) NOT NULL DEFAULT 0,
    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_no (no),
    KEY idx_status_due (status, due_date),
    KEY idx_tenant_period (tenant_id, period),
    KEY idx_contract (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表'
PARTITION BY RANGE (TO_DAYS(due_date)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    -- ... 后续按月增加
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- 工单
CREATE TABLE work_orders (
    id              VARCHAR(32) NOT NULL,
    no              VARCHAR(32) NOT NULL,
    category        VARCHAR(16) NOT NULL,
    title           VARCHAR(128) NOT NULL,
    description     TEXT,
    status          VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    tenant_id       VARCHAR(32),
    unit_id         VARCHAR(32),
    maintainer_id   VARCHAR(32),
    sla_due_at      DATETIME(3),
    rated_at        DATETIME(3),
    rating          TINYINT,
    rating_text     VARCHAR(255),
    images          JSON,
    deleted         TINYINT(1) NOT NULL DEFAULT 0,
    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_no (no),
    KEY idx_status (status),
    KEY idx_maintainer_status (maintainer_id, status),
    KEY idx_sla (sla_due_at, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- 用户 + 角色 + 权限（RBAC）
CREATE TABLE users (
    id              VARCHAR(32) NOT NULL,
    username        VARCHAR(32) NOT NULL,
    password_hash   VARCHAR(64) NOT NULL COMMENT 'BCrypt',
    name            VARCHAR(32) NOT NULL,
    phone           VARCHAR(16) NOT NULL,
    avatar          VARCHAR(512),
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    project_id      VARCHAR(32),
    last_login_at   DATETIME(3),
    deleted         TINYINT(1) NOT NULL DEFAULT 0,
    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username, deleted),
    UNIQUE KEY uk_phone (phone, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE roles (
    code        VARCHAR(32) NOT NULL,
    name        VARCHAR(32) NOT NULL,
    description VARCHAR(255),
    built_in    TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (code)
) COMMENT='角色表';

CREATE TABLE permissions (
    code  VARCHAR(64) NOT NULL,
    name  VARCHAR(32) NOT NULL,
    `group` VARCHAR(32) NOT NULL,
    PRIMARY KEY (code)
) COMMENT='权限码';

CREATE TABLE role_permissions (
    role_code       VARCHAR(32) NOT NULL,
    permission_code VARCHAR(64) NOT NULL,
    PRIMARY KEY (role_code, permission_code)
);

CREATE TABLE user_roles (
    user_id    VARCHAR(32) NOT NULL,
    role_code  VARCHAR(32) NOT NULL,
    PRIMARY KEY (user_id, role_code)
);

-- 操作日志（按月分区）
CREATE TABLE op_logs (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    actor_id    VARCHAR(32) NOT NULL,
    actor_name  VARCHAR(32) NOT NULL,
    action      VARCHAR(16) NOT NULL,
    target      VARCHAR(255) NOT NULL,
    diff        JSON,
    ip          VARCHAR(45),
    user_agent  VARCHAR(512),
    trace_id    VARCHAR(64),
    at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id, at),
    KEY idx_actor_at (actor_id, at),
    KEY idx_action_at (action, at),
    KEY idx_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志'
PARTITION BY RANGE (TO_DAYS(at)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);
```

完整 DDL 在 B1 阶段产出 `src/main/resources/db/migration/V1__init.sql`。

## 3.3 索引策略

| 查询场景         | 索引                                        | SQL Hint                  |
| ---------------- | ------------------------------------------- | ------------------------- |
| 工单列表按状态   | `(status, created_at desc)`                 | Covering Index 包含常用列 |
| 工单看板按维修工 | `(maintainer_id, status)`                   | –                         |
| 账单催缴         | `(status, due_date)`                        | –                         |
| 租户欠费 TOP     | 物化日表 `t_bills_arrears_daily`            | 每日 01:00 刷新           |
| 操作日志检索     | `(actor_id, at desc)` + `(action, at desc)` | 按 at 分区裁剪            |
| 支付流水查重     | 唯一 `external_id`                          | –                         |

## 3.4 分区 / 归档

- `op_logs`、`wo_messages`、`bill_payments`：按月 RANGE 分区
- 12 个月前的分区每月归档到 OSS（CSV gzip 压缩），原表 `ALTER TABLE ... DROP PARTITION`
- 报表查询走 `t_bills_monthly` 物化日表，XXL-Job 每日 01:00 跑批

## 3.5 数据范围（多项目隔离）

物业行业常见多项目数据隔离需求：

```java
@DataScope(deptColumn = "project_id")  // 自定义注解
@Select("SELECT * FROM bills WHERE 1=1 ${ew.customSqlSegment}")
List<Bill> listMyProjectBills(@Param("ew") QueryWrapper<Bill> wrapper);
```

通过 `@Aspect` 在 SQL 拼接阶段注入 `AND project_id = #{userProjectId}`，对 SuperAdmin 跳过。

---

<a id="part-4"></a>

# Part 4 · 开发里程碑（B0~B10）

> 单人工期估算；与前端 M0~M11 节奏对齐，可前后端并行。

| #       | 里程碑                            | 工期 | 对接前端 | 关键产出                                                  |
| ------- | --------------------------------- | ---- | -------- | --------------------------------------------------------- |
| **B0**  | 工程初始化                        | 1d   | –        | Spring Boot 3 + Maven + Docker compose + CI               |
| **B1**  | 数据库 + 基础设施                 | 3d   | –        | 28 表 Flyway 脚本 + seed + Redis + RabbitMQ + Knife4j     |
| **B2**  | 鉴权 + RBAC + OpenAPI 自动生成    | 3d   | M3       | Sa-Token 登录 + 7 个 endpoint + 自动导出 openapi.yaml     |
| **B3**  | 租户 / 房源 / 合同                | 5d   | M4       | 19 endpoints + EasyExcel 导入 + OSS STS                   |
| **B4**  | 账单 + 财务                       | 6d   | M5       | 4 种计费引擎 + XXL-Job 月度出账 + 对账三方比对 + 关账事务 |
| **B5**  | 工单 + WebSocket + 投诉           | 5d   | M6       | Spring StateMachine + STOMP WebSocket + SLA 定时器        |
| **B6**  | 仪表盘 + 报表 + 异步导出          | 4d   | M7       | 物化日表 + XXL-Job 导出任务 + EasyExcel 流式生成          |
| **B7**  | 小程序专属（微信/支付/上传/通知） | 3d   | M8/M9    | wechatpay-java + jscode2session + OSS STS + 订阅消息      |
| **B8**  | 系统设置（用户/角色/日志/模板）   | 3d   | M11      | 14 endpoints + AuditLogAspect + 模板引擎                  |
| **B9**  | 性能 / 测试 / 部署                | 4d   | M10      | 索引调优 + Testcontainers e2e + Dockerfile + K8s          |
| **B10** | 灰度 / 监控 / 文档                | 2d   | –        | SkyWalking + Prometheus + Grafana + runbook               |

**总计约 39 个工作日**（与前端并行可压缩到 25 天）。

---

<a id="part-5"></a>

# Part 5 · 关键模块设计

## 5.1 鉴权 + RBAC（Sa-Token 实现）

**为什么选 Sa-Token 而非 Spring Security**：

| 维度               | Sa-Token             | Spring Security  |
| ------------------ | -------------------- | ---------------- |
| 配置量             | 30 行 yaml           | 200 行 Config 类 |
| 学习曲线           | 半天                 | 一周             |
| 国内物业项目占有率 | 70%+                 | 25%              |
| OAuth2 / SSO       | 内置 sa-token-oauth2 | 需额外配置       |
| 文档（中文）       | 完善                 | 一般             |

### 登录流程

```
POST /auth/login
  → 校验 username + BCrypt(password)
  → StpUtil.login(userId, new SaLoginConfig().setTimeout(15*60))
  → 生成 access token（15min） + refresh token（30d，存 Redis）
  → 返回 { accessToken, user }

受保护接口：@SaCheckLogin
  → Sa-Token 拦截器自动校验 token

权限：@SaCheckPermission("bill:edit")
  → StpInterface.getPermissionList(loginId) 返回该用户全部权限码

401 → 前端调 /auth/refresh
  → 读取请求头 X-Refresh-Token（或 httpOnly cookie）
  → 校验 Redis 白名单 + 颁发新 access
  → refresh token 每 7 天强制 rotate
```

### 实现示例

```java
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginVO login(@Validated @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }

    @GetMapping("/me")
    @SaCheckLogin
    public UserVO me() {
        return authService.getCurrentUser();
    }

    @PostMapping("/refresh")
    public TokenVO refresh(@RequestHeader("X-Refresh-Token") String refresh) {
        return authService.refresh(refresh);
    }

    @PostMapping("/logout")
    @SaCheckLogin
    public void logout() {
        StpUtil.logout();
    }
}

@Component
public class StpInterfaceImpl implements StpInterface {
    @Resource UserService userService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return userService.findPermissionsByUserId((String) loginId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return userService.findRolesByUserId((String) loginId);
    }
}
```

**密码策略**：BCrypt cost=12；首次登录强制改；登录失败 5 次 → Redis 锁 15min。

## 5.2 RBAC 数据模型

- 6 个内置角色（与前端 `mocks/handlers/m11.ts` 完全一致）：
  `SuperAdmin` / `PropertyAdmin` / `Finance` / `CustomerService` / `Maintainer` / `Operations`
- 30 个权限码按 group 分组：租户/合同/账单/财务/工单/投诉/报表/系统
- `roles.built_in = 1` 不可删；`SuperAdmin` 不可改权限矩阵（业务层拒绝）
- 多项目数据隔离：`@DataScope` 注解 + Aspect 注入 `project_id` 过滤

## 5.3 4 种计费引擎（Strategy 模式）

```java
public interface BillingStrategy {
    BillingResult calculate(BillingContext ctx);
}

@Component("FIXED")
public class FixedStrategy implements BillingStrategy {
    @Override
    public BillingResult calculate(BillingContext ctx) {
        return BillingResult.of(ctx.getFeeItem().getFixedAmount());
    }
}

@Component("BY_AREA")
public class ByAreaStrategy implements BillingStrategy {
    @Override
    public BillingResult calculate(BillingContext ctx) {
        BigDecimal amount = ctx.getUnit().getArea()
            .multiply(ctx.getFeeItem().getUnitPrice())
            .setScale(2, RoundingMode.HALF_UP);
        return BillingResult.of(amount, ctx.getUnit().getArea(), ctx.getFeeItem().getUnitPrice());
    }
}

@Component("BY_METER")
public class ByMeterStrategy implements BillingStrategy {
    @Override
    public BillingResult calculate(BillingContext ctx) {
        BigDecimal qty = ctx.getCurMeter().subtract(ctx.getPrevMeter());
        BigDecimal amount = qty.multiply(ctx.getFeeItem().getUnitPrice())
            .setScale(2, RoundingMode.HALF_UP);
        return BillingResult.of(amount, qty, ctx.getFeeItem().getUnitPrice());
    }
}

@Component("TIERED")
public class TieredStrategy implements BillingStrategy {
    @Override
    public BillingResult calculate(BillingContext ctx) {
        BigDecimal qty = ctx.getCurMeter().subtract(ctx.getPrevMeter());
        BigDecimal total = BigDecimal.ZERO;
        for (FeeTier tier : ctx.getFeeItem().getTiers()) {
            BigDecimal hi = tier.getToQty() == null ? qty : tier.getToQty().min(qty);
            BigDecimal lo = tier.getFromQty();
            if (qty.compareTo(lo) <= 0) break;
            BigDecimal slice = hi.subtract(lo);
            total = total.add(slice.multiply(tier.getPrice()));
        }
        return BillingResult.of(total.setScale(2, RoundingMode.HALF_UP), qty);
    }
}

@Service
@RequiredArgsConstructor
public class BillingEngine {
    private final Map<String, BillingStrategy> strategies;  // Spring 自动注入 String → bean

    public BillingResult calculate(BillingContext ctx) {
        BillingStrategy strategy = strategies.get(ctx.getFeeItem().getCalcType());
        if (strategy == null) throw new BusinessException(40001, "未知计费类型");
        return strategy.calculate(ctx);
    }
}
```

阶梯测试用例（`TieredStrategyTest`）：

```
tiers: [{from:0, to:100, price:5}, {from:100, to:200, price:7}, {from:200, to:null, price:9}]
qty=250 → 100*5 + 100*7 + 50*9 = 500+700+450 = 1650.00 ✓
qty=50  → 50*5 = 250.00
qty=150 → 100*5 + 50*7 = 850.00
```

## 5.4 月度出账批处理（XXL-Job + 分片广播）

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyBillingJob {
    private final ContractService contractService;
    private final BillingEngine billingEngine;
    private final BillService billService;

    /**
     * Cron: 0 0 1 1 * ?  每月 1 号凌晨 1 点
     * 分片广播：8 个执行器，每个跑 1/8 合同
     */
    @XxlJob("monthlyBillingJob")
    public void execute() throws Exception {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        String period = YearMonth.now().toString();  // 2026-05

        // 仅处理 id mod shardTotal == shardIndex 的合同
        List<Contract> contracts = contractService.listActiveByShard(shardIndex, shardTotal);
        log.info("出账 period={} shard={}/{} count={}", period, shardIndex, shardTotal, contracts.size());

        for (Contract contract : contracts) {
            try {
                String idempotentKey = period + ":" + contract.getId();
                if (billService.existsByIdempotent(idempotentKey)) continue;
                Bill bill = billService.generateBill(contract, period);
                XxlJobHelper.log("Bill 生成 contractId={} no={}", contract.getId(), bill.getNo());
            } catch (Exception e) {
                XxlJobHelper.log("[ERROR] 单合同出账失败 contractId={} {}", contract.getId(), e.getMessage());
                // 不中断；继续下一个
            }
        }
        XxlJobHelper.handleSuccess();
    }
}
```

幂等：`bills.idempotent_key` 唯一索引 = `period + ":" + contract_id`。

## 5.5 智能认领规则引擎（Chain of Responsibility）

```java
public interface ClaimRuleHandler {
    int getPriority();
    Optional<Claim> tryClaim(PaymentRecord payment);
}

@Component
public class AmountEqualHandler implements ClaimRuleHandler {
    @Resource BillService billService;

    @Override public int getPriority() { return 100; }

    @Override
    public Optional<Claim> tryClaim(PaymentRecord payment) {
        return billService.findUnpaidByExactAmount(payment.getAmount())
            .map(bill -> Claim.builder()
                .paymentRecordId(payment.getId())
                .billId(bill.getId())
                .amount(payment.getAmount())
                .matchedBy("RULE")
                .ruleCode("AMOUNT_EQUAL")
                .build());
    }
}

@Component
public class NoteContainsUnitHandler implements ClaimRuleHandler {
    @Override public int getPriority() { return 200; }

    @Override
    public Optional<Claim> tryClaim(PaymentRecord payment) {
        if (StringUtils.isBlank(payment.getPayerHint())) return Optional.empty();
        // 提取单元号正则：01-0203
        Matcher m = Pattern.compile("(\\d{2}-\\d{4})").matcher(payment.getPayerHint());
        if (!m.find()) return Optional.empty();
        String unitNo = m.group(1);
        return billService.findEarliestUnpaidByUnit(unitNo)
            .map(bill -> /* 构建 Claim */);
    }
}

@Service
@RequiredArgsConstructor
public class ClaimDispatcher {
    private final List<ClaimRuleHandler> handlers;

    @Transactional
    public Optional<Claim> dispatch(PaymentRecord payment) {
        return handlers.stream()
            .sorted(Comparator.comparingInt(ClaimRuleHandler::getPriority))
            .map(h -> h.tryClaim(payment))
            .filter(Optional::isPresent)
            .findFirst()
            .map(Optional::get);
    }
}
```

## 5.6 对账 + 关账（事务 + 行锁）

```java
@Service
@RequiredArgsConstructor
public class CloseService {
    private final BillMapper billMapper;
    private final CloseRecordMapper closeRecordMapper;

    @Transactional(rollbackFor = Exception.class)
    public void closeMonth(String period, String confirmText, String operatorId) {
        if (!"我已确认".equals(confirmText)) {
            throw new BusinessException(40001, "请输入确认文案");
        }
        // 1. 行锁本期账单
        billMapper.lockByPeriod(period);  // SELECT * ... WHERE period=? FOR UPDATE

        // 2. 计算汇总
        CloseSummary summary = billMapper.aggregateByPeriod(period);

        // 3. 写关账快照
        closeRecordMapper.insert(CloseRecord.builder()
            .period(period)
            .closedBy(operatorId)
            .closedAt(LocalDateTime.now())
            .snapshot(JSON.toJSONString(summary))
            .build());

        // 4. 标记 bills.closed = 1（DDL 中需加 closed 字段）
        billMapper.markClosedByPeriod(period);
    }
}
```

关账后业务层校验：

```java
@Service
public class BillService {
    public Bill updateBill(String billId, UpdateBillDTO dto) {
        Bill bill = getById(billId);
        if (bill.getClosed()) {
            throw new BusinessException(40903, "本期已关账，请走红冲流程");
        }
        // ...
    }
}
```

## 5.7 工单 WebSocket（Spring WebSocket + STOMP + Redis Pub/Sub）

```java
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final SaTokenHandshakeInterceptor saInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/workorder")
            .addInterceptors(saInterceptor)   // 鉴权
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}

@Controller
@RequiredArgsConstructor
public class WorkOrderWsController {
    private final SimpMessagingTemplate broker;
    private final WorkOrderMessageService msgService;

    @MessageMapping("/workorder/{id}/chat")
    public void chat(@DestinationVariable String id,
                     @Payload ChatPayload payload,
                     StompHeaderAccessor accessor) {
        String userId = (String) accessor.getSessionAttributes().get("userId");
        WoMessage msg = msgService.append(id, userId, payload.getType(), payload.getContent());
        // 广播到房间订阅者；跨实例通过 Redis Adapter 自动转发
        broker.convertAndSend("/topic/workorder/" + id, msg);
    }
}
```

跨实例：用 `spring-session-data-redis` + Redis Pub/Sub 让多副本共享订阅关系；规模大切 RabbitMQ STOMP relay 或 ActiveMQ。

心跳：`@Scheduled(fixedRate = 25000)` 服务端主动 ping。

## 5.8 SLA 倒计时 + 自动关闭

```java
@Component
@RequiredArgsConstructor
public class SlaCheckJob {
    private final WorkOrderService woService;
    private final NotificationService notify;

    @XxlJob("slaCheckJob")  // Cron: 0 */5 * * * ?  每 5min
    public void execute() {
        List<WorkOrder> overdue = woService.findOverdueAndNotMarked();
        for (WorkOrder wo : overdue) {
            wo.setOverdue(true);
            woService.updateById(wo);
            notify.send("飞书告警", "工单 " + wo.getNo() + " SLA 超时");
        }
    }
}

@Component
@RequiredArgsConstructor
public class AutoCloseJob {
    private final WorkOrderService woService;

    @XxlJob("autoCloseJob")  // Cron: 0 0 */1 * * ?  每小时
    public void execute() {
        // 状态 DONE 且 ratedAt 为 null 且 doneAt < now - 72h
        List<WorkOrder> toClose = woService.findDoneOver72h();
        toClose.forEach(wo -> woService.autoClose(wo.getId()));
    }
}
```

## 5.9 操作日志（AOP 切面 + 异步 MQ）

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    String target();      // 'user' / 'bill'
    String action();      // 'CREATE' / 'UPDATE' / 'DELETE'
}

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {
    private final RabbitTemplate rabbit;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        // 异步发到 MQ，由 consumer 异步落库
        OpLogEvent event = OpLogEvent.builder()
            .actorId(StpUtil.getLoginIdAsString())
            .action(auditLog.action())
            .target(auditLog.target() + "/" + extractIdFromResult(result))
            .ip(IpUtil.getClientIp())
            .userAgent(WebUtil.getUserAgent())
            .traceId(MDC.get("traceId"))
            .at(LocalDateTime.now())
            .build();
        rabbit.convertAndSend("op-log.exchange", "op-log.write", event);
        return result;
    }
}

// 使用
@PostMapping("/users")
@SaCheckPermission("system:user")
@AuditLog(target = "user", action = "CREATE")
public UserVO create(@Validated @RequestBody CreateUserDTO dto) { /* ... */ }
```

写库走 MQ 异步，不阻塞业务请求。

## 5.10 消息模板引擎

```java
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final MessageTemplateMapper templateMapper;
    private final Map<String, MessageChannel> channels;  // SMS/EMAIL/PUSH/INSTATION

    public void send(String scene, Map<String, String> vars, String recipientId) {
        MessageTemplate tpl = templateMapper.findPublishedByScene(scene);
        if (tpl == null) return;
        String content = renderTemplate(tpl.getContent(), vars);
        MessageChannel channel = channels.get(tpl.getChannel());
        channel.send(recipientId, tpl.getName(), content);
    }

    /** {var} 占位符替换，与前端 utils/extractVars.ts 算法一致 */
    private String renderTemplate(String content, Map<String, String> vars) {
        Matcher m = Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}").matcher(content);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String value = vars.getOrDefault(m.group(1), "");
            m.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}

// 4 个 Channel 实现
@Component("SMS")
class AliyunSmsChannel implements MessageChannel { /* ... */ }

@Component("INSTATION")
class InStationChannel implements MessageChannel {
    @Override
    public void send(String userId, String title, String content) {
        // 1) 写 notifications 表
        // 2) WebSocket 推送到该用户的会话
    }
}
```

业务触发示例：

```java
@EventListener
public void onBillCreated(BillCreatedEvent event) {
    notificationService.send("BILL_CREATED", Map.of(
        "month", event.getBill().getPeriod(),
        "amount", event.getBill().getTotalAmount().toString(),
        "dueDate", event.getBill().getDueDate().toString()
    ), event.getTenant().getPhone());
}
```

---

<a id="part-6"></a>

# Part 6 · 部署与运维

## 6.1 环境矩阵

| 环境         | 域名                    | 数据库          | Redis            | 队列              | 用途       |
| ------------ | ----------------------- | --------------- | ---------------- | ----------------- | ---------- |
| 本地         | http://localhost:8080   | MySQL docker    | Redis docker     | RabbitMQ docker   | 个人开发   |
| 联调 dev     | https://api-dev.xxx.com | RDS shared      | ElastiCache      | RabbitMQ Cluster  | 与前端联调 |
| 预发 staging | https://api-stg.xxx.com | RDS（生产同构） | ElastiCache      | RabbitMQ          | 上线前回归 |
| 生产 prod    | https://api.xxx.com     | RDS 主备        | ElastiCache 集群 | RabbitMQ 镜像队列 | 真实流量   |

## 6.2 Docker 部署

```dockerfile
# Dockerfile（multi-stage）
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
USER app
COPY --from=build /build/target/property-backend-*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75 -Duser.timezone=Asia/Shanghai"
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q -O- http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
```

`docker-compose.dev.yml`：

```yaml
version: '3.9'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: property
    ports: ['3306:3306']
    volumes: [./mysql-data:/var/lib/mysql]
    command: --default-authentication-plugin=mysql_native_password --character-set-server=utf8mb4
  redis:
    image: redis:7-alpine
    ports: ['6379:6379']
  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    ports: ['5672:5672', '15672:15672']
  xxl-job-admin:
    image: xuxueli/xxl-job-admin:2.4.1
    ports: ['9090:8080']
    environment:
      PARAMS: '--spring.datasource.url=jdbc:mysql://mysql:3306/xxl_job?useSSL=false&serverTimezone=Asia/Shanghai'
    depends_on: [mysql]
```

## 6.3 K8s 编排（生产）

```
Deployment: api (replicas: 3, rollingUpdate maxSurge=1)
  - probes: liveness /actuator/health/liveness, readiness /actuator/health/readiness
  - resources: 1 CPU / 2Gi mem (Spring Boot 至少 1Gi 起步)
  - env: ConfigMap (application-prod.yml) + Secret (DB/Redis 密码)
  - JVM 参数：-XX:+UseG1GC -XX:MaxRAMPercentage=75
Service: ClusterIP api-svc → port 8080
Ingress: nginx-ingress + cert-manager（HTTPS）
HPA: CPU > 70% 扩容到 10
PodDisruptionBudget: minAvailable: 2
CronJob: xxl-job-admin 调度（独立 deployment）
```

数据库 / Redis / MQ 不在 K8s，用云托管（阿里云 RDS / ElastiCache / AMQP）。

## 6.4 监控

- **指标**：`spring-boot-starter-actuator` + `micrometer-registry-prometheus`
  - `/actuator/prometheus` 暴露
  - 自定义业务指标：账单数、工单状态分布、未读通知队列
- **日志**：Logback JSON encoder → Loki / Filebeat → ELK
  - traceId 串前后端 + DB 慢日志（Druid）
- **APM**：SkyWalking Agent（`-javaagent:/opt/skywalking/skywalking-agent.jar`）
- **告警**：Alertmanager / 公司监控平台
  - P99 > 500ms 持续 5min
  - 5xx > 1% 持续 2min
  - MQ 堆积 > 1000
- **错误**：Sentry（与前端共用项目，traceId 关联）

## 6.5 数据库备份与恢复

- XtraBackup 每日 02:00 全备 → OSS（保留 30 天）
- MySQL binlog 持续同步到对象存储（RPO ≤ 5min）
- 每月恢复演练，runbook 在 `docs/runbook.md`
- 关键表（bills / payment_records）开启 `binlog_row_image=FULL`

## 6.6 灰度发布

- 镜像 tag：`v0.10.0` / `latest` / `sha-<short>`
- K8s rollingUpdate（`maxSurge=1, maxUnavailable=0`）
- nginx-ingress canary 切 5% 流量到新版本，观察 30min
- 回滚：`kubectl rollout undo deployment api`

---

<a id="part-7"></a>

# Part 7 · Claude 执行指令模板

> 与前端开发计划 Part 4 同风格，每个里程碑 prompt 可直接复制喂 Claude。

## 通用前置（每次都带）

```
你是一名资深 Java 后端工程师，正在用 Spring Boot 3.3 + JDK 21 + MyBatis-Plus +
MySQL 8 + Redis 7 + Sa-Token + RabbitMQ + XXL-Job 开发"智能物业管理系统"后端。

约定：
- Java 21 + Lombok + MapStruct
- 单模块 Maven 工程；包结构 com.property.{common,infra,modules,entity}
- 所有 controller 返回 ApiEnvelope 自动包装（ResponseBodyAdvice 已实现）
- 所有 DTO 用 Jakarta Bean Validation + @Schema 标注（生成 Swagger）
- DAO 用 MyBatis-Plus，复杂 SQL 写 XML mapper
- 实体类下划线表名 → 驼峰字段，金额 BigDecimal Decimal(12,2)，序列化为 string
- 时间 LocalDateTime + ISO 8601；时区 Asia/Shanghai
- Conventional Commits，body 行 ≤ 100 字符
- 每次产出含：(1) 改动文件清单 (2) 完整代码 (3) 运行/验证 (4) self-review

参考前端契约：docs/api.md（98 endpoints）、docs/openapi.yaml
```

### B0 · 工程初始化

```
任务：搭建 Spring Boot 3.3 + JDK 21 后端骨架。

要求：
1. 用 Spring Initializr 生成基础工程
   - Java 21 + Maven + Spring Boot 3.3.x
   - 依赖：Spring Web, Spring Boot DevTools, Spring Boot Actuator, Lombok, Validation
2. 补充依赖（pom.xml 见附录 B）：
   - MyBatis-Plus 3.5.x + Druid 1.2.x
   - MySQL Connector/J 8.4
   - Spring Data Redis + Lettuce
   - Sa-Token 1.39.x + sa-token-redis-jackson
   - Spring AMQP（RabbitMQ）
   - XXL-Job Core 2.4.x
   - Spring WebSocket
   - Knife4j 4.5.x（springdoc-openapi 增强）
   - Hutool 5.8 + MapStruct 1.6 + EasyExcel 3.3
   - Flyway core + mysql
3. application.yml 基础配置（dev/prod 多 profile）
4. 写 PropertyApplication.java 启动类
5. 编写 ApiEnvelope / PageResult / BusinessException / GlobalExceptionHandler
6. 编写 TraceIdInterceptor（MDC 注入 traceId 透传到日志）
7. docker/docker-compose.dev.yml：mysql + redis + rabbitmq + xxl-job-admin
8. .github/workflows/ci.yml：mvn verify + docker build
9. docs/dev.md 启动说明

DoD：
- mvn clean install 通过
- docker compose up -d 起依赖
- mvn spring-boot:run 后 GET /actuator/health 返 UP
- http://localhost:8080/doc.html 看到 Knife4j 文档（先空着也行）
- 每次请求日志带 traceId
```

### B1 · 数据库 + 基础设施

```
任务：建 28 张表 Flyway 脚本 + 实体类 + Mapper + seed + Redis/MQ 集成。

要求：
1. src/main/resources/db/migration/V1__init.sql：
   - 全部 28 张表 DDL（按 Part 3.2，包含分区表）
   - 注释完整（DBA 友好）
2. src/main/resources/db/migration/V2__seed.sql：
   - 6 个内置角色 + 30 个权限码 + RolePermission 关联
   - 5 个内置用户（BCrypt('123456') 已算好）
   - 3 个项目 + 5 个楼栋 + 50 个单元
   - 10 个 mock 租户 + 8 个 ACTIVE 合同
   - 4 个标准 FeeItem（含 TIERED 阶梯）
3. entity/ 下生成 28 个 MP 实体类（用 @TableName + @TableField）
4. mapper/ 下基础 BaseMapper（继承自动生成）
5. MybatisPlusConfig：分页插件 + 乐观锁插件 + 逻辑删除
6. RedisConfig：Lettuce + 自定义 RedisTemplate + Jackson 序列化
7. RabbitMQConfig：声明 exchange/queue/binding
8. application.yml 数据源 + 连接池调优（Druid SQL 监控开启）

DoD：
- mvn flyway:migrate 后 Adminer / Navicat 看到 28 表 + 种子数据
- 单测：PrismaService 等价的 Spring Boot Test 注入 + 简单 CRUD 通过
- Druid 监控页 http://localhost:8080/druid 可访问（仅 dev profile）
```

### B2 · 鉴权 + RBAC + OpenAPI 自动生成

```
任务：实现 9 个鉴权 endpoint + Sa-Token 集成 + Knife4j 自动出 openapi.yaml。

参考 docs/api.md 第 1 节。

要求：
1. modules/auth/：
   - LoginDTO / SmsLoginDTO / RefreshDTO（Bean Validation）
   - AuthService：BCrypt 校验 + StpUtil 登录 + Redis refresh 白名单
   - AuthController：7 个 endpoint
   - StpInterfaceImpl：权限 / 角色查询
2. modules/system/user 占位：UserService.findById + findPermissionsByUserId
3. SaTokenConfig：拦截器、路由排除（/auth/* + /actuator/health + /doc.html）
4. 错误码：40001 / 40100 / 40300 / 40400
5. Knife4j 配置：标题 + 描述 + 全局 token 参数
6. 启动后输出 openapi.yaml 到 ./openapi.yaml（mvn 插件或手动 export）

DoD：
- Postman/Bruno 跑通：login → me → refresh → logout
- admin/123456 登录返回 SuperAdmin + 30 权限
- @SaCheckPermission("bill:edit") 的接口被 finance 访问 → 403
- /doc.html 看到完整接口文档，导出 yaml 与 docs/openapi.yaml 字段一致
- traceId 在 envelope.traceId、日志 MDC、Sentry 三处一致
```

### B3 · 租户 / 房源 / 合同

```
任务：实现 19 个 endpoint（Part 2/3/4）。

参考 docs/api.md 第 2/3/4 节 + 前端 features/{tenant,asset,contract} 的入参出参。

要求：
1. modules/tenant/：9 endpoints
   - EasyExcel 导入两阶段（preview 解析 + 校验 → commit 真正写库）
   - 用 BeanValidator 程序化校验（身份证 18 位、手机 11 位、统一社会信用代码 18 位）
2. modules/unit/：4 endpoints
   - 树查询：递归 SQL CTE WITH RECURSIVE 一次取齐
   - check-out preflight 查关联未付账单（COUNT + SUM）
3. modules/contract/：6 endpoints
   - 状态机校验（手写 if-else 或 Spring StateMachine）
   - 批量续约 @Transactional：循环创建新合同 + 旧标 EXPIRED
   - 附件 OSS STS：aliyun-sdk-oss + sts SDK
4. 通用：PageQueryDTO、@AuditLog 装饰所有写操作

DoD：
- 前端 admin-web M4 三个页面真接口（关 mock）CRUD 全通
- Excel 导入 10 行（含错误行）：5 OK + 5 FAIL 行号准确
- 退租前置：有欠费返回 hasUnpaidBills:true + unpaidAmount
- OSS STS 凭证能上传一张图到 OSS
```

### B4 · 账单 + 财务

```
任务：22 个 endpoint + 4 种计费引擎 + XXL-Job 月度出账 + 对账三方比对 + 关账事务。

参考 docs/api.md 第 5/6 节。

要求：
1. modules/bill/engine/：4 种 strategy（FIXED/BY_AREA/BY_METER/TIERED）
   - TDD 写单测：100% 覆盖 + 阶梯边界用例
2. modules/bill：14 endpoints
   - GET /bills/stats：SQL aggregate 一次到位（不要循环）
   - POST /bills/push：批量发到 RabbitMQ，consumer 调 NotificationService
3. modules/finance：8 endpoints
   - /reconciliation：拉 BillPayment + 微信流水（模拟接口）+ 银行 CSV 三方比对
   - /close 事务：FOR UPDATE 锁本期 + 写 close_records + 标 bills.closed=1
   - 智能认领：ClaimDispatcher（Chain of Responsibility）
4. job/MonthlyBillingJob：XXL-Job 分片广播，cron 0 0 1 1 * ?
5. 幂等：bills.idempotent_key 唯一索引

DoD：
- XXL-Job 控制台手动触发 monthlyBillingJob → 8 张账单生成
- TieredStrategyTest: qty=250 → 1650.00（断言精确）
- 关账后 PATCH bill → 返 40903
- 智能认领：金额匹配 + 备注含单元号 → 自动 claim 写入
```

### B5 · 工单 + WebSocket + 投诉

```
任务：18 个 endpoint + STOMP WebSocket + SLA 定时器 + 投诉三向闭环。

参考 docs/api.md 第 7/8 节。

要求：
1. modules/workorder：10 endpoints
   - 状态机用 Spring StateMachine（PENDING/IN_PROGRESS/DONE/CLOSED/CANCELED/HELD）
   - 改派写 wo_timelines + 触发通知
2. WebSocketConfig + WorkOrderWsController（见 Part 5.7）
3. job/SlaCheckJob: cron 0 */5 * * * ? 扫超时
4. job/AutoCloseJob: cron 0 0 */1 * * ? DONE 72h 自动关闭
5. modules/complaint：8 endpoints
   - 状态机 PENDING → INVESTIGATING → AWAITING_CONFIRM → CLOSED
   - 申诉 APPEALING 主管裁决
6. WebSocket 跨实例：Redis Pub/Sub Adapter

DoD：
- 双开浏览器连同 workOrderId → 互发消息实时互通
- 故意创建 SLA 1 分钟工单 → 5 min 内扫到 + 飞书告警
- 投诉走完全流程 + 申诉裁决
- WebSocket 离开页面 disconnect 干净（无内存泄漏）
```

### B6 · 仪表盘 + 报表 + 异步导出

```
任务：10 个 endpoint + MySQL 物化日表 + EasyExcel 流式导出。

参考 docs/api.md 第 9/10 节。

要求：
1. modules/dashboard：3 endpoints
   - 用 MyBatis 原生 SQL aggregate + window function
   - @Cacheable 缓存 30s（Redis）
2. modules/report：7 endpoints
   - 物化日表：t_bills_monthly + XXL-Job 每日 01:00 INSERT INTO ... SELECT
3. /reports/export-task：
   - POST 推到 RabbitMQ 报表队列 → 返 taskId
   - GET 查 Redis 状态
   - Consumer：EasyExcel ExcelWriter 流式写到 OSS
4. 完成后通过 NotificationService 推 INSTATION + EMAIL

DoD：
- 仪表盘 4 卡 + 4 图响应 < 200ms（含缓存命中）
- 导出 100k 行账单 ≤ 30s 完成，OSS 拿到链接
- 异步导出全流程：提交 → 轮询 → 通知 → 下载
```

### B7 · 小程序专属（微信登录/支付/上传/通知）

```
任务：wx-login / 微信支付 V3 / OSS STS / 订阅消息推送。

要求：
1. modules/auth/wx-login：
   - HTTP 调微信 jscode2session 拿 openId + sessionKey
   - user_wx_bindings 表关联 User
   - 颁发 Sa-Token
2. modules/auth/bind-phone：
   - 解密 wx getPhoneNumber 返回的 encryptedData（用 sessionKey + IV）
3. modules/payment：
   - 微信支付 V3：wechatpay-java SDK
   - JSAPI 下单 + 回调签名校验 + 入 BillPayment
   - 支付宝：alipay-sdk-java
4. modules/upload/sts：aliyun-sdk-core sts SDK 签发临时凭证
5. modules/notification：
   - 订阅消息：模板 ID + 数据填充（subscribeMessage.send API）
   - 落 notifications 表 + WebSocket 推

DoD：
- 真小程序 Taro.login → 后端联调成功
- 微信支付沙箱 1 笔通走 → BillPayment 入库
- OSS STS 凭证小程序能直传图片
- 订阅消息到达手机
```

### B8 · 系统设置（用户/角色/日志/模板）

```
任务：14 个 endpoint + AuditLog AOP + 模板引擎。

参考 docs/api.md 第 11 节。

要求：
1. modules/system/user：5 endpoints
   - reset-pwd 返回 RandomStringUtils 生成的临时密码（明文返回 1 次后不存）
   - toggle 启停
2. modules/system/role：3 endpoints
   - 角色权限 PUT：SuperAdmin 拒；其他可改 RolePermission
3. modules/system/op-log：2 endpoints（已通过 AOP 写入）
4. modules/system/message-template：4 endpoints
   - extractVars 工具 Java 实现（与前端算法一致）
   - 渲染：手写 {var} 替换或用 Freemarker / Velocity
5. AuditLogAspect 走 RabbitMQ → Consumer → op_logs 表

DoD：
- 角色矩阵 PUT 后下次登录权限即刻生效（Sa-Token Redis 刷新）
- 删除被引用的模板返回 40903
- /op-logs 按 actor + action + 时间筛选 P99 < 100ms
- 渲染："您好 {name}" + {name:'张三'} → "您好 张三"
```

### B9 · 性能 / 测试 / 部署

```
任务：索引优化 + Testcontainers e2e + Dockerfile + K8s + 监控集成。

要求：
1. 所有 list endpoint EXPLAIN 检查，补索引
2. Testcontainers：
   - MySQL + Redis + RabbitMQ 容器化测试环境
   - 编写 auth / bill / workorder 三套 e2e
3. Dockerfile multi-stage（见 6.2）
4. K8s manifests + Helm chart
5. Spring Boot Actuator + micrometer-prometheus
6. Logback JSON encoder → stdout（K8s 收 → Loki）
7. SkyWalking Agent 配置

DoD：
- mvn verify 全过（含 Testcontainers）
- JMeter 1000 QPS 压测 P99 < 300ms
- docker build 成功 → docker run + healthcheck 通过
- /actuator/prometheus 暴露指标
```

### B10 · 灰度 / 监控 / 文档

```
任务：上线前最后一公里。

要求：
1. nginx-ingress canary 灰度配置
2. Grafana 仪表板：QPS、P99、5xx 率、JVM、连接池
3. Alertmanager 规则
4. docs/runbook.md：故障预案
5. docs/architecture.md：ER 图 + 模块依赖图
6. CHANGELOG B0~B10

DoD：
- 灰度 5% 流量验证 30min 无 5xx 增长 → 切 100%
- 故障演练 kill pod，可用性 ≥ 99.95%
- 文档完整
```

---

<a id="a"></a>

# 附录 A · 前后端对接清单

## A.1 启动联调前确认

- [ ] `docs/openapi.yaml` 由后端 Knife4j 导出，前端 `pnpm gen:api` 重生
- [ ] CORS 放开前端 dev 域名 `http://localhost:5173`
- [ ] 错误码表完整对齐（见 `docs/api.md`）
- [ ] 时间 ISO 8601 + UTC+8
- [ ] 金额 string
- [ ] 分页 `{ list, total, page, pageSize }`
- [ ] OSS STS 可用
- [ ] 微信支付双端参数（小程序按 `TARO_ENV` 区分）
- [ ] WebSocket 走 query string token（小程序限制）
- [ ] envelope `{ code, message, data, traceId }`
- [ ] traceId 前后端日志可关联

## A.2 后端给前端的对接物

| 物件                               | 用途                | 频率               |
| ---------------------------------- | ------------------- | ------------------ |
| `docs/openapi.yaml`                | orval 生成 hooks    | 每次接口变更       |
| 联调环境 `https://api-dev.xxx.com` | 前端切换 mock=false | 长期稳定           |
| 测试账号                           | mock 5 角色         | 每次重置数据后告知 |
| 错误码表                           | 前端拦截器分支      | 一次性             |
| WebSocket 地址 + STOMP 协议说明    | 工单聊天            | 一次性             |

## A.3 阶段联调点

| 阶段 | 前端       | 后端       | 联调动作              |
| ---- | ---------- | ---------- | --------------------- |
| 1    | M2 done    | B0/B1 done | OpenAPI 文档对齐      |
| 2    | M3 done    | B2 done    | 登录链路（关 mock）   |
| 3    | M4 done    | B3 done    | 租户/房源/合同        |
| 4    | M5 done    | B4 done    | 账单 + 财务           |
| 5    | M6 done    | B5 done    | 工单 + WebSocket      |
| 6    | M7 done    | B6 done    | 报表 + 异步导出       |
| 7    | M8/M9 done | B7 done    | 小程序登录 + 支付     |
| 8    | M11 done   | B8 done    | 系统设置              |
| 9    | M10 + B9   | –          | 性能压测 + E2E 真接口 |

---

<a id="b"></a>

# 附录 B · pom.xml 完整依赖（参考）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
    </parent>
    <groupId>com.property</groupId>
    <artifactId>property-backend</artifactId>
    <version>0.1.0</version>

    <properties>
        <java.version>21</java.version>
        <mybatis-plus.version>3.5.7</mybatis-plus.version>
        <druid.version>1.2.23</druid.version>
        <sa-token.version>1.39.0</sa-token.version>
        <knife4j.version>4.5.0</knife4j.version>
        <hutool.version>5.8.32</hutool.version>
        <easyexcel.version>3.3.4</easyexcel.version>
        <mapstruct.version>1.6.2</mapstruct.version>
        <xxl-job.version>2.4.1</xxl-job.version>
        <wechatpay.version>0.2.13</wechatpay.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-redis</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-amqp</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-websocket</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-actuator</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-aop</artifactId></dependency>

        <!-- 数据库 -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-3-starter</artifactId>
            <version>${druid.version}</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>

        <!-- 鉴权（Sa-Token） -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-spring-boot3-starter</artifactId>
            <version>${sa-token.version}</version>
        </dependency>
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-redis-jackson</artifactId>
            <version>${sa-token.version}</version>
        </dependency>

        <!-- Knife4j（Swagger UI 增强） -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
            <version>${knife4j.version}</version>
        </dependency>

        <!-- 工具 -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>${hutool.version}</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>easyexcel</artifactId>
            <version>${easyexcel.version}</version>
        </dependency>

        <!-- XXL-Job -->
        <dependency>
            <groupId>com.xuxueli</groupId>
            <artifactId>xxl-job-core</artifactId>
            <version>${xxl-job.version}</version>
        </dependency>

        <!-- 阿里云 OSS -->
        <dependency>
            <groupId>com.aliyun.oss</groupId>
            <artifactId>aliyun-sdk-oss</artifactId>
            <version>3.17.4</version>
        </dependency>
        <dependency>
            <groupId>com.aliyun</groupId>
            <artifactId>aliyun-java-sdk-sts</artifactId>
            <version>3.1.2</version>
        </dependency>

        <!-- 微信支付 V3 -->
        <dependency>
            <groupId>com.github.wechatpay-apiv3</groupId>
            <artifactId>wechatpay-java</artifactId>
            <version>${wechatpay.version}</version>
        </dependency>

        <!-- 监控 -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- 日志 JSON -->
        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
            <version>8.0</version>
        </dependency>

        <!-- 测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>mysql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId></exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

**文档结束。** 后续如需新增模块（智能门禁后端 / 能耗采集 / 社区团购），按本计划格式新增 Bxx 里程碑。

> 备选方案：[`docs/backend-plan-nestjs.md`](./backend-plan-nestjs.md)（NestJS + Prisma 全 TS 版本，团队 TS 背景深时选用）
