# 智能物业管理系统 · 后端开发计划

> **版本**：v1.0 · 编制日期：2026-05-15
> **基线**：前端开发计划 v1.2 已交付（M0~M11 commit 全绿）
> **配套文档**：[`docs/api.md`](./api.md)（98 个 endpoint）+ [`docs/openapi.yaml`](./openapi.yaml)（机器可读契约）
> **适用角色**：后端工程师 / 后端 Tech Lead
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
- [附录 B · 选型备选方案（Spring Boot / Go）](#b)

---

<a id="part-1"></a>

# Part 1 · 后端需求（从前端反推）

## 1.1 接口契约（来自 `docs/api.md`）

| 模块            | endpoints | 关键复杂度                                 |
| --------------- | --------- | ------------------------------------------ |
| 鉴权 + 角色权限 | 9         | RBAC + token 队列 + RefreshToken httpOnly  |
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
| WebSocket 连接 | 单实例 5k，水平扩展                           |
| 出账批处理     | 10w 户 / 小时                                 |
| 数据保留       | 业务热数据 2 年，操作日志 1 年，归档冷存 7 年 |

## 1.3 非功能需求

| 项         | 要求                                                      |
| ---------- | --------------------------------------------------------- |
| 部署       | Docker + K8s（或 Docker Compose 小规模起步）              |
| 高可用     | 数据库主备 + Redis 哨兵 + 应用 ≥ 2 副本                   |
| 数据库备份 | 每日全备 + WAL 持续归档，RPO ≤ 5min                       |
| 审计       | 所有写操作记录到 `op_logs`，含 actor / diff / IP / UA     |
| 安全       | HTTPS 全站 / SQL 注入 ORM 防护 / XSS / CSRF / 密码 bcrypt |
| 合规       | 个人信息脱敏导出 / 用户注销 / 日志可追溯                  |
| 监控       | Prometheus + Grafana + Loki + Sentry（与前端共用）        |

---

<a id="part-2"></a>

# Part 2 · 技术架构

## 2.1 技术选型（推荐）

| 类别        | 选型                                                   | 理由                                            |
| ----------- | ------------------------------------------------------ | ----------------------------------------------- |
| 运行时      | **Node.js 20 LTS**                                     | 与前端同语言生态，TS 类型可共享                 |
| 框架        | **NestJS 10**                                          | 装饰器 + DI + 模块化 + OpenAPI 一等支持         |
| 语言        | **TypeScript 5（strict）**                             | 全栈类型一致                                    |
| ORM         | **Prisma 5**                                           | DSL 干净、Migration 工具链完整、TS 类型自动生成 |
| 数据库      | **PostgreSQL 16**                                      | JSON + 全文检索 + 时序分区，物业财务必需 ACID   |
| 缓存 / 队列 | **Redis 7**（队列 + 缓存 + token 黑名单 + ws pub/sub） | 中小规模够用；规模大切 RabbitMQ                 |
| 队列消费    | **BullMQ**                                             | 与 Redis 配套，可视化管理面板                   |
| WebSocket   | **Socket.IO 4**（NestJS Gateway 封装）                 | 房间 + 自动重连 + 跨实例 Redis adapter          |
| 鉴权        | **JWT (access + refresh) + Passport**                  | refreshToken 走 httpOnly cookie 推荐            |
| 验证        | **class-validator + class-transformer**（NestJS 内置） | DTO 自校验                                      |
| OpenAPI     | **`@nestjs/swagger`**                                  | 装饰器自动生成 `openapi.yaml` 喂前端 orval      |
| 文件存储    | **阿里云 OSS / MinIO（自建）**                         | STS 临时凭证签发                                |
| 微信支付    | **`wxpay-v3`** + 自有签名服务                          | V3 接口                                         |
| 支付宝      | **`alipay-sdk`**                                       | –                                               |
| 短信        | **阿里云 / 腾讯云 SMS**                                | 抽象 channel 适配                               |
| 邮件        | **Nodemailer + 企业 SMTP**                             | –                                               |
| 推送        | **微信小程序订阅消息 / 个推**                          | –                                               |
| 日志        | **Pino**（NestJS logger 适配）                         | JSON 结构化，进 Loki                            |
| 测试        | **Jest + Supertest**（NestJS 默认）                    | –                                               |
| 部署        | **Docker multi-stage + K8s / docker-compose**          | –                                               |
| CI          | **GitHub Actions**                                     | 与前端共用                                      |

> **为什么不选 Spring Boot？** 没问题，可选——但你的前端是 TS+orval，TS 端到端语言一致带来的收益高于 Java 生态成熟。备选见附录 B。

## 2.2 工程目录结构

```
property-backend/
├── apps/
│   └── api/                       # 主 API 服务
│       ├── src/
│       │   ├── main.ts            # bootstrap + swagger 注册
│       │   ├── app.module.ts
│       │   ├── modules/           # 业务模块（DDD 风格按聚合根划分）
│       │   │   ├── auth/
│       │   │   ├── tenant/
│       │   │   ├── unit/
│       │   │   ├── contract/
│       │   │   ├── bill/
│       │   │   │   ├── fee-item/
│       │   │   │   └── billing-job/   # 月度出账 batch
│       │   │   ├── finance/
│       │   │   │   ├── reconcile/
│       │   │   │   ├── close/
│       │   │   │   └── claim/
│       │   │   ├── workorder/
│       │   │   ├── complaint/
│       │   │   ├── dashboard/
│       │   │   ├── report/
│       │   │   ├── system/        # 用户 / 角色 / 日志 / 模板
│       │   │   ├── notification/  # 站内 + 三方推送适配
│       │   │   ├── upload/        # OSS STS
│       │   │   └── payment/       # 微信 / 支付宝 SDK 桥
│       │   ├── common/
│       │   │   ├── decorators/    # @CurrentUser @Roles @AuditLog
│       │   │   ├── filters/       # GlobalExceptionFilter (envelope)
│       │   │   ├── guards/        # JwtAuthGuard / RolesGuard / PermissionsGuard
│       │   │   ├── interceptors/  # EnvelopeInterceptor / LoggingInterceptor
│       │   │   ├── pipes/         # ZodValidationPipe / PaginationPipe
│       │   │   ├── dto/           # PageQueryDto / EnvelopeDto
│       │   │   └── utils/         # bcrypt / dayjs / oss / wx-pay 等
│       │   ├── infra/
│       │   │   ├── prisma/        # PrismaService + module
│       │   │   ├── redis/
│       │   │   ├── queue/         # BullMQ producers
│       │   │   ├── socket/        # Socket.IO Gateway 基类 + Redis adapter
│       │   │   └── storage/       # OSS client
│       │   ├── jobs/              # BullMQ 消费者（出账 / 报表 / 推送）
│       │   ├── ws/                # WebSocket Gateway（工单聊天）
│       │   └── config/            # env schema + 配置加载
│       ├── prisma/
│       │   ├── schema.prisma      # 所有表结构
│       │   ├── migrations/        # 自动生成的 SQL migration
│       │   └── seed.ts            # 种子数据
│       ├── test/                  # e2e 测试
│       └── package.json
├── libs/                          # （可选）抽取共享代码
│   ├── shared-types/              # 与前端 packages/shared-types 同源
│   ├── rule-engine/               # 智能认领规则引擎
│   └── billing-engine/            # 4 种计费规则
├── docker/
│   ├── Dockerfile
│   ├── docker-compose.dev.yml     # 一键起 postgres + redis + adminer
│   └── docker-compose.prod.yml
├── .github/workflows/
│   ├── ci.yml                     # lint + typecheck + test + build
│   └── release.yml                # tag → docker push GHCR
├── docs/
│   ├── architecture.md
│   ├── db-erd.md
│   └── runbook.md
└── package.json
```

## 2.3 关键约定（向前端承诺）

参考 `docs/api.md` 第 0 节：

```ts
// 统一响应包装
interface ApiEnvelope<T> {
  code: number; // 0 = 成功；4xxxx 业务错误；5xx 服务异常
  message: string;
  data: T | null;
  traceId: string; // UUID v4，每请求一个，写日志 + 透传
}

// 分页结构
interface Page<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}
```

实现：`EnvelopeInterceptor` 自动包装 controller 返回值；`GlobalExceptionFilter` 把抛出的 `BusinessException` / `NotFoundException` 等转成 envelope。

时间字段：Prisma DateTime → JSON.stringify 自动 ISO 8601。
金额字段：Prisma `Decimal(12,2)` → DTO 转 string，禁止落 number。

---

<a id="part-3"></a>

# Part 3 · 数据库设计

## 3.1 ER 概览（核心 28 张表）

```
租户域：           tenants ─┬─ contracts ── units ─┬─ buildings ── projects
                            │                       └─ tenant_units (历史)
                            └─ tenant_attachments

费用域：           fee_items ─┬─ fee_tiers (阶梯)
                              └─ bills ─┬─ bill_items
                                         ├─ bill_payments
                                         └─ bill_logs

财务域：           payment_records ─┬─ claims (认领关系)
                                    ├─ claim_rules
                                    ├─ reconciliations (日对账)
                                    └─ closes (关账)

工单域：           work_orders ─┬─ wo_timelines
                                ├─ wo_messages (聊天)
                                └─ wo_attachments

投诉域：           complaints ─┬─ complaint_timelines
                              └─ appeals

系统域：           users ─┬─ roles ── role_permissions ── permissions
                          └─ user_roles
                  op_logs（审计）
                  message_templates
                  notifications

资源域：           uploads (OSS 文件元信息)
                  announcements
                  export_tasks (异步报表)
```

## 3.2 关键表 DDL 草稿（Prisma 风格）

```prisma
// schema.prisma 摘录

model Tenant {
  id              String   @id @default(cuid())
  type            TenantType  // PERSONAL | COMPANY
  name            String
  idCard          String?  @unique  // 个人
  phone           String   @unique
  socialCreditCode String? @unique  // 企业
  contactName     String?
  contactPhone    String?
  bankAccount     String?
  status          String   @default("ACTIVE")
  createdAt       DateTime @default(now())
  updatedAt       DateTime @updatedAt
  contracts       Contract[]
  workOrders      WorkOrder[]
  @@index([phone])
}

model Project {
  id        String   @id @default(cuid())
  name      String
  address   String?
  buildings Building[]
}

model Building {
  id        String   @id @default(cuid())
  projectId String
  name      String   // "1 号楼"
  floors    Int
  project   Project  @relation(fields: [projectId], references: [id])
  units     Unit[]
}

model Unit {
  id           String   @id @default(cuid())
  buildingId   String
  floor        Int
  number       String   // "0203"
  area         Decimal  @db.Decimal(8, 2)
  status       String   @default("VACANT") // VACANT | RENTED | RESERVED
  building     Building @relation(fields: [buildingId], references: [id])
  contracts    Contract[]
  @@unique([buildingId, floor, number])
}

model Contract {
  id            String   @id @default(cuid())
  no            String   @unique
  tenantId      String
  unitId        String
  status        ContractStatus  @default(DRAFT)
  startDate     DateTime
  endDate       DateTime
  monthlyRent   Decimal  @db.Decimal(12, 2)
  deposit       Decimal? @db.Decimal(12, 2)
  pdfUrl        String?
  signedAt      DateTime?
  terminatedAt  DateTime?
  tenant        Tenant   @relation(fields: [tenantId], references: [id])
  unit          Unit     @relation(fields: [unitId], references: [id])
  @@index([status, endDate])
}

enum ContractStatus { DRAFT PENDING ACTIVE EXPIRED TERMINATED }

model FeeItem {
  id          String   @id @default(cuid())
  name        String
  calcType    String   // FIXED | BY_AREA | BY_METER | TIERED
  unitPrice   Decimal? @db.Decimal(12, 4)
  fixedAmount Decimal? @db.Decimal(12, 2)
  meterType   String?  // WATER | ELEC | GAS
  tiers       FeeTier[]
  enabled     Boolean  @default(true)
}

model FeeTier {
  id        String  @id @default(cuid())
  feeItemId String
  fromQty   Decimal @db.Decimal(12, 2)
  toQty     Decimal? @db.Decimal(12, 2)  // null = 无上限
  price     Decimal @db.Decimal(12, 4)
  feeItem   FeeItem @relation(fields: [feeItemId], references: [id])
}

model Bill {
  id          String   @id @default(cuid())
  no          String   @unique
  period      String   // YYYY-MM
  contractId  String
  tenantId    String
  unitId      String
  totalAmount Decimal  @db.Decimal(12, 2)
  paidAmount  Decimal  @default(0) @db.Decimal(12, 2)
  status      BillStatus  @default(UNPAID)
  dueDate     DateTime
  paidAt      DateTime?
  voidedAt    DateTime?
  items       BillItem[]
  payments    BillPayment[]
  createdAt   DateTime @default(now())
  @@index([status, dueDate])
  @@index([tenantId, period])
}

enum BillStatus { UNPAID PAID OVERDUE VOID }

model BillItem {
  id        String   @id @default(cuid())
  billId    String
  feeItemId String
  name      String
  qty       Decimal? @db.Decimal(12, 2)
  unitPrice Decimal? @db.Decimal(12, 4)
  amount    Decimal  @db.Decimal(12, 2)
  bill      Bill     @relation(fields: [billId], references: [id])
}

model BillPayment {
  id         String   @id @default(cuid())
  billId     String
  method     String   // WX | ALIPAY | OFFLINE_TRANSFER | CASH
  amount     Decimal  @db.Decimal(12, 2)
  voucherUrl String?
  externalId String?  // 微信/支付宝交易号
  paidAt     DateTime @default(now())
  bill       Bill     @relation(fields: [billId], references: [id])
}

model PaymentRecord {
  id            String  @id @default(cuid())
  externalId    String  @unique
  channel       String  // WX | ALIPAY | BANK_INFLOW
  amount        Decimal @db.Decimal(12, 2)
  paidAt        DateTime
  payerHint     String? // 备注、付款人姓氏等
  claimed       Boolean @default(false)
  claim         Claim?
}

model Claim {
  id              String   @id @default(cuid())
  paymentRecordId String   @unique
  billId          String
  amount          Decimal  @db.Decimal(12, 2)
  matchedBy       String   // RULE | MANUAL
  ruleId          String?
  claimedAt       DateTime @default(now())
  paymentRecord   PaymentRecord @relation(fields: [paymentRecordId], references: [id])
}

model WorkOrder {
  id           String   @id @default(cuid())
  no           String   @unique
  category     String
  title        String
  description  String
  status       WorkOrderStatus  @default(PENDING)
  tenantId     String?
  unitId       String?
  maintainerId String?
  slaDueAt     DateTime?
  ratedAt      DateTime?
  rating       Int?
  ratingText   String?
  images       Json?    // string[]
  createdAt    DateTime @default(now())
  timelines    WoTimeline[]
  messages     WoMessage[]
  @@index([status])
  @@index([maintainerId, status])
}

enum WorkOrderStatus { PENDING IN_PROGRESS DONE CLOSED CANCELED HELD }

model WoMessage {
  id          String   @id @default(cuid())
  workOrderId String
  from        String   // tenant | maintainer | system
  type        String   // text | image
  content     String
  at          DateTime @default(now())
  workOrder   WorkOrder @relation(fields: [workOrderId], references: [id])
  @@index([workOrderId, at])
}

model User {
  id            String   @id @default(cuid())
  username      String   @unique
  passwordHash  String
  name          String
  phone         String   @unique
  avatar        String?
  status        UserStatus @default(ACTIVE)
  projectId     String?
  lastLoginAt   DateTime?
  createdAt     DateTime @default(now())
  roles         UserRole[]
}

enum UserStatus { ACTIVE DISABLED }

model Role {
  code         String   @id   // SuperAdmin | PropertyAdmin | ...
  name         String
  description  String?
  builtIn      Boolean  @default(false)
  permissions  RolePermission[]
  users        UserRole[]
}

model Permission {
  code  String @id   // bill:edit
  name  String
  group String      // 账单
  roles RolePermission[]
}

model RolePermission {
  roleCode       String
  permissionCode String
  role           Role       @relation(fields: [roleCode], references: [code])
  permission     Permission @relation(fields: [permissionCode], references: [code])
  @@id([roleCode, permissionCode])
}

model UserRole {
  userId   String
  roleCode String
  user     User @relation(fields: [userId], references: [id])
  role     Role @relation(fields: [roleCode], references: [code])
  @@id([userId, roleCode])
}

model OpLog {
  id         String   @id @default(cuid())
  actorId    String
  actorName  String
  action     String   // CREATE | UPDATE | DELETE | LOGIN | EXPORT
  target     String   // 'bill/BILL_xxx'
  diff       Json?    // { field: { from, to } }
  ip         String?
  userAgent  String?
  traceId    String
  at         DateTime @default(now())
  @@index([actorId, at])
  @@index([action, at])
}
```

完整 schema 参考 `prisma/schema.prisma`（B1 阶段产出）。

## 3.3 索引策略

| 查询场景         | 索引                                        |
| ---------------- | ------------------------------------------- |
| 工单列表按状态   | `(status, created_at desc)`                 |
| 工单看板按维修工 | `(maintainer_id, status)`                   |
| 账单催缴         | `(status, due_date)`                        |
| 租户欠费 TOP     | `(tenant_id, period)` + 物化视图            |
| 操作日志检索     | `(actor_id, at desc)` + `(action, at desc)` |
| 支付流水查重     | `external_id` 唯一                          |

## 3.4 分区 / 归档

- `op_logs`、`wo_messages`、`bill_payments` 按月 RANGE 分区
- 12 个月前的分区每月归档到冷存（OSS Parquet）
- 报表查询走 `bills_monthly_view`（物化视图，每日 01:00 刷新）

---

<a id="part-4"></a>

# Part 4 · 开发里程碑（B0~B10）

> 单人工期估算；与前端 M0~M11 节奏对齐，可前后端并行。

| #       | 里程碑                            | 工期 | 对接前端 | 关键产出                                        |
| ------- | --------------------------------- | ---- | -------- | ----------------------------------------------- |
| **B0**  | 工程初始化                        | 1d   | –        | NestJS + Prisma + Docker + CI                   |
| **B1**  | 数据库 + 基础设施                 | 3d   | –        | 28 表 schema + migration + seed + Redis + 队列  |
| **B2**  | 鉴权 + RBAC + OpenAPI 自动生成    | 3d   | M3       | Login + JWT refresh + Guards + Swagger          |
| **B3**  | 租户 / 房源 / 合同                | 5d   | M4       | 9+4+6 endpoints + Excel 解析 + OSS STS          |
| **B4**  | 账单 + 财务                       | 6d   | M5       | 计费引擎 + 出账 batch + 对账三方比对 + 关账事务 |
| **B5**  | 工单 + WebSocket + 投诉           | 5d   | M6       | Socket.IO + 状态机 + SLA 定时器 + 三向闭环      |
| **B6**  | 仪表盘 + 报表 + 异步导出          | 4d   | M7       | 物化视图 + BullMQ 导出任务 + Excel 流式生成     |
| **B7**  | 小程序专属（微信/支付/上传/通知） | 3d   | M8/M9    | wx-login + payment-v3 + OSS STS + 模板消息      |
| **B8**  | 系统设置（用户/角色/日志/模板）   | 3d   | M11      | 14 endpoints + 操作日志拦截器 + 模板引擎        |
| **B9**  | 性能 / 测试 / 部署                | 4d   | M10      | 索引调优 + Jest e2e + Dockerfile + K8s 编排     |
| **B10** | 灰度 / 监控 / 文档                | 2d   | –        | Prometheus + Grafana + runbook                  |

**总计约 39 个工作日**（与前端并行可压缩到 25 天，因为 B0/B1 是前置依赖）。

详细 prompt 见 [Part 7](#part-7)。

---

<a id="part-5"></a>

# Part 5 · 关键模块设计

## 5.1 鉴权 + RBAC

```
flowchart:
  Login(POST /auth/login)
    → 校验 username + bcrypt(password)
    → 生成 accessToken (15min, JWT 携带 userId + roles)
    → 生成 refreshToken (30d, 写 httpOnly cookie + Redis 白名单)
    → 返回 { accessToken, user }

  受保护接口
    → JwtAuthGuard 验证 access
    → RolesGuard / PermissionsGuard 检查角色/权限码
    → @CurrentUser() 注入 controller 参数

  401 → 前端调 /auth/refresh
    → 读 cookie 中 refresh → Redis 校验白名单
    → 颁发新 access（refresh 不变）
    → 旋转策略：refresh 每 7 天强制换发

  Logout
    → Redis 拉黑 refresh（DEL）
    → 清 cookie
```

**密码策略**：bcrypt cost=12；首次登录强制改；连续 5 次失败锁 15min（Redis 计数）。

**权限实现**：

```ts
@Get('/bills')
@Permissions('bill:view')
@Roles('SuperAdmin', 'PropertyAdmin', 'Finance')
async list() {}
```

## 5.2 RBAC 数据模型

- 6 个内置角色（与前端 `mocks/handlers/m11.ts` PRESET 一致）：SuperAdmin / PropertyAdmin / Finance / CustomerService / Maintainer / Operations
- 30+ 权限码按组织分（租户/合同/账单/财务/工单/投诉/报表/系统）
- `roles.builtIn = true` 的内置角色权限可调，但不可删；SuperAdmin 全权且禁改

## 5.3 4 种计费引擎

```ts
// libs/billing-engine
export interface BillingContext {
  unit: Unit;
  feeItem: FeeItem;
  period: string; // 2026-05
  prevMeter?: number;
  curMeter?: number;
}

export interface BillingResult {
  amount: Decimal;
  qty?: number;
  unitPrice?: Decimal;
}

const strategies: Record<CalcType, BillingStrategy> = {
  FIXED: new FixedStrategy(), // amount = fixedAmount
  BY_AREA: new ByAreaStrategy(), // amount = area * unitPrice
  BY_METER: new ByMeterStrategy(), // amount = (curMeter - prevMeter) * unitPrice
  TIERED: new TieredStrategy(), // 按 tiers 分段累计
};
```

阶梯 `TIERED` 测试用例：

```
tiers: [{from:0, to:100, price:5}, {from:100, to:200, price:7}, {from:200, to:null, price:9}]
qty=250 → 100*5 + 100*7 + 50*9 = 500+700+450 = 1650
```

## 5.4 月度出账批处理

```
触发：cron `0 1 1 * *`（每月 1 号 01:00）
流程：
  1. 查所有 ACTIVE 合同
  2. for each → 算 N 个 FeeItem → 生成 Bill + BillItem
  3. 写入：Promise.all 并发 50，超大分批 chunk
  4. 推送通知（站内 + 短信）
  5. 异常隔离：单合同失败不影响整体，写 OpLog
```

实现：BullMQ Queue `billing` + Worker，幂等 key = `period + contractId`。

## 5.5 智能认领规则引擎

```
PaymentRecord 入库后：
  1. 顺序匹配 ClaimRule（按 priority 升序）
  2. 每条 rule = matchBy + matchValue + targetBillSelector
     matchBy: AMOUNT_EQUAL | NOTE_CONTAINS | PAYER_NAME | TIME_RANGE
     selector: 最早未付 | 指定 feeItem | 指定 contract
  3. 命中即创建 Claim + 标 PaymentRecord.claimed = true
  4. 全部未命中 → 进入待认领池等手动指派
```

DSL 示例：

```json
{
  "priority": 1,
  "matchBy": "NOTE_CONTAINS",
  "matchValue": "01-0203",
  "selector": { "type": "EARLIEST_UNPAID_BY_UNIT", "value": "0203" }
}
```

## 5.6 对账 + 关账

**对账中心 `GET /finance/reconciliation?date=`**：

```
系统流水（BillPayment）⊕ 微信流水（拉 wx api）⊕ 银行流水（CSV 导入）
→ 按金额 + 时间 ± 5min 匹配
→ 输出三列对比，标差异行
```

**关账 `POST /finance/close`**：

```sql
BEGIN;
  -- 锁本期所有相关表
  SELECT * FROM bills WHERE period = '2026-05' FOR UPDATE;
  -- 写关账记录
  INSERT INTO closes (period, closed_by, snapshot_data) VALUES ...;
  -- 触发归档：物化结算金额到 close_snapshots
COMMIT;
```

关账后：账单 PATCH 接口返 `40903 期已关闭`；前端提示红冲流程。

## 5.7 工单 WebSocket（Socket.IO Gateway）

```ts
@WebSocketGateway({ namespace: '/workorder', cors: true })
export class WorkOrderGateway {
  @WebSocketServer() server: Server;

  // 连接鉴权
  async handleConnection(client: Socket) {
    const token = client.handshake.query.token;
    const user = await this.auth.verifyAccessToken(token);
    if (!user) return client.disconnect();
    client.data.userId = user.id;
  }

  @SubscribeMessage('join')
  joinRoom(@MessageBody() workOrderId: string, @ConnectedSocket() client: Socket) {
    client.join(`workorder:${workOrderId}`);
  }

  @SubscribeMessage('chat')
  async chat(
    @MessageBody() body: { workOrderId: string; content: string; msgType: 'text' | 'image' },
    @ConnectedSocket() client: Socket,
  ) {
    const msg = await this.workOrder.appendMessage({
      workOrderId: body.workOrderId,
      from: client.data.userId,
      type: body.msgType,
      content: body.content,
    });
    this.server.to(`workorder:${body.workOrderId}`).emit('chat', msg);
  }
}
```

跨实例：Redis adapter (`@socket.io/redis-adapter`) 让消息在 2+ 副本间广播。

心跳：默认 25s pingTimeout + 60s pingInterval。

## 5.8 SLA 倒计时 + 自动关闭

- 工单创建时算 `slaDueAt = createdAt + categoryConfig.slaHours * 1h`
- 定时 job 每 5min 扫一次：超时未接单 → 飞书/钉钉告警 + 标 `OVERDUE` 字段
- 状态 `DONE` 持续 72h 无评价 → 自动 `CLOSED`（BullMQ delayed job）

## 5.9 操作日志（审计）

实现：`@AuditLog('user', 'UPDATE')` 装饰器 + 拦截器：

```ts
// 拦截器在 controller 返回后异步写 op_logs
{
  actorId, actorName,
  action: 'UPDATE',
  target: 'user/U_123',
  diff: deepDiff(before, after),
  ip, userAgent, traceId,
  at: now()
}
```

写入走 BullMQ 异步，避免阻塞业务请求。

## 5.10 消息模板引擎

```
触发：billing job 完成 → emit('bill.created', {billId, tenantPhone, ...})
监听：NotificationService
  1. 找 scene=BILL_CREATED 的 PUBLISHED 模板
  2. 渲染 {var} → 实际值（Mustache 轻量替换）
  3. 按 channel 路由：
     - SMS → 阿里云短信
     - INSTATION → 写 notifications 表 + ws 推
     - PUSH → 微信订阅消息 / 个推
     - EMAIL → Nodemailer
  4. 落 notification_logs 记录发送状态
```

---

<a id="part-6"></a>

# Part 6 · 部署与运维

## 6.1 环境矩阵

| 环境         | 域名                    | 数据库          | Redis            | 用途       |
| ------------ | ----------------------- | --------------- | ---------------- | ---------- |
| 本地         | http://localhost:3000   | postgres docker | redis docker     | 个人开发   |
| 联调 dev     | https://api-dev.xxx.com | RDS shared      | ElastiCache      | 与前端联调 |
| 预发 staging | https://api-stg.xxx.com | RDS（生产同构） | ElastiCache      | 上线前回归 |
| 生产 prod    | https://api.xxx.com     | RDS 主备        | ElastiCache 集群 | 真实流量   |

## 6.2 Docker 部署

```dockerfile
# Dockerfile（multi-stage）
FROM node:20-alpine AS deps
WORKDIR /app
RUN corepack enable && corepack prepare pnpm@8.15.4 --activate
COPY pnpm-lock.yaml package.json ./
RUN pnpm install --frozen-lockfile

FROM node:20-alpine AS build
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN pnpm prisma generate && pnpm build

FROM node:20-alpine AS runtime
WORKDIR /app
ENV NODE_ENV=production
COPY --from=build /app/node_modules ./node_modules
COPY --from=build /app/dist ./dist
COPY --from=build /app/prisma ./prisma
EXPOSE 3000
HEALTHCHECK CMD wget -q -O- http://127.0.0.1:3000/healthz || exit 1
CMD ["node", "dist/apps/api/main.js"]
```

## 6.3 K8s 编排（生产）

```
Deployment: api (replicas: 3, rollingUpdate)
  - probes: liveness /healthz, readiness /readyz
  - resources: 500m CPU / 1Gi mem
  - env: 通过 ConfigMap + Secret 注入
Service: ClusterIP api-svc → port 3000
Ingress: nginx-ingress → tls + cert-manager
CronJob: billing-job @01:00 / 1, sla-check */5 * * * *
StatefulSet: 不在 K8s 部署 postgres（用云 RDS）
```

## 6.4 监控

- **指标**：`prom-client` 暴露 `/metrics` → Prometheus scrape
  - http 请求数 / 延迟（按 path 分桶）
  - DB 连接池使用率
  - BullMQ 队列堆积深度
  - WebSocket 连接数
- **日志**：Pino JSON → Loki，按 `traceId` 关联前后端
- **告警**：Alertmanager → 飞书/钉钉
  - P99 > 500ms 5min
  - 5xx > 1% 2min
  - 队列堆积 > 1000
- **链路追踪**：OpenTelemetry → Jaeger（按需）
- **错误监控**：Sentry（与前端共用项目，traceId 关联）

## 6.5 数据库备份与恢复

- pg_basebackup 每日全备 → OSS
- WAL 持续归档（archive_command），RPO ≤ 5min
- 每月做一次恢复演练，runbook 在 `docs/runbook.md`

## 6.6 灰度发布

- 镜像 tag 语义：`v0.10.0` / `latest` / `sha-<short>`
- K8s rollingUpdate `maxSurge=1, maxUnavailable=0`
- nginx-ingress `nginx.ingress.kubernetes.io/canary` 切 5% 流量到新版本，观察 30min 后切 100%
- 回滚：`kubectl rollout undo deployment api`

---

<a id="part-7"></a>

# Part 7 · Claude 执行指令模板

> 与前端开发计划 Part 4 一致风格，每个里程碑 prompt 可直接复制喂 Claude。

## 通用前置（每次都带）

```
你是一名资深 Node.js 后端工程师，正在用 NestJS 10 + Prisma 5 + PostgreSQL 16
开发"智能物业管理系统"后端。

约定：
- TypeScript 严格模式，no any
- 所有 controller 返回值由 EnvelopeInterceptor 自动包装为 { code, message, data, traceId }
- 所有 DTO 用 class-validator + @ApiProperty（生成 swagger）
- ORM 用 Prisma，禁用原生 SQL 除非性能场景
- 时间字段 ISO 8601；金额 Decimal(12,2)，DTO 转 string
- Conventional Commits，body 行 ≤ 100 字符
- 每次产出包含：(1) 改动文件清单 (2) 完整代码 (3) 运行/验证 (4) self-review checklist

参考前端契约：docs/api.md（98 endpoints）、docs/openapi.yaml
```

### B0 · 工程初始化

```
任务：搭建 NestJS 后端工程骨架。

要求：
1. nest new property-backend --strict --skip-git，安装 pnpm
2. 装包：
   - 框架：@nestjs/swagger @nestjs/config @nestjs/jwt @nestjs/passport passport passport-jwt
   - 数据：prisma @prisma/client
   - 缓存/队列：ioredis @nestjs/bullmq bullmq
   - 校验：class-validator class-transformer
   - WS：@nestjs/websockets @nestjs/platform-socket.io @socket.io/redis-adapter
   - 工具：bcrypt argon2 dayjs uuid pino nestjs-pino
3. 配 ESLint + Prettier + Husky + Commitlint（与前端一致）
4. 写 src/main.ts：
   - app.useGlobalPipes(new ValidationPipe({ transform: true, whitelist: true }))
   - app.useGlobalInterceptors(new EnvelopeInterceptor())
   - app.useGlobalFilters(new GlobalExceptionFilter())
   - swagger 注册到 /docs，输出 openapi.yaml 到 ./openapi.yaml
5. docker/docker-compose.dev.yml：起 postgres:16 + redis:7 + adminer
6. .env.example：DB_URL / REDIS_URL / JWT_SECRET 等
7. .github/workflows/ci.yml：lint + typecheck + test + build

DoD：
- pnpm install 通过
- docker compose -f docker-compose.dev.yml up -d 起 db
- pnpm start:dev → http://localhost:3000/healthz 返回 ok
- http://localhost:3000/docs 看到 swagger
- pnpm lint / typecheck / build 全绿
```

### B1 · 数据库 + 基础设施

```
任务：建 28 张表 schema + Prisma migration + seed + Redis 集成。

要求：
1. prisma/schema.prisma：按 Part 3.2 写完整 schema（28 表）
2. pnpm prisma migrate dev --name init → 生成 SQL migration
3. prisma/seed.ts：
   - 6 个内置角色 + 30 个权限码 + 全部 RolePermission 关联
   - 5 个内置用户（admin/finance/service/worker/ops），密码 bcrypt('123456')
   - 3 个项目 + 5 个楼栋 + 50 个单元
   - 10 个 mock 租户 + 8 个 ACTIVE 合同
   - 4 个标准 FeeItem（含 TIERED 阶梯）
4. infra/prisma/prisma.service.ts + module（onModuleInit 调 $connect）
5. infra/redis/redis.module.ts（用 ioredis）
6. infra/queue/queue.module.ts（BullMQ + Redis）
7. 全局 PrismaModule + RedisModule 注册到 app.module

DoD：
- pnpm prisma db push + seed 后用 adminer 看到所有表 + 种子数据
- 跑 e2e：connect prisma + redis 成功
- 启动日志显示 prisma connected
```

### B2 · 鉴权 + RBAC + Swagger

```
任务：实现 9 个鉴权 endpoint + Guards + Swagger 自动生成。

参考 docs/api.md 第 1 节。

要求：
1. modules/auth/：
   - LoginDto / SmsLoginDto / RefreshDto（class-validator）
   - AuthService：bcrypt 校验 + JWT 签发 + Redis 白名单
   - AuthController：7 个 endpoint
   - JwtStrategy + JwtAuthGuard
   - RolesGuard + PermissionsGuard（读 user.roles + roles.permissions）
   - @CurrentUser() decorator + @Roles() + @Permissions() decorators
2. modules/system/user 模块占位（只暴露 /auth/me 用 UserService.findById）
3. 错误码：40001 必填 / 40100 token 过期 / 40300 无权限
4. swagger：所有 DTO + 响应类型用 @ApiProperty + @ApiResponse 标注

DoD：
- 用 Postman/Bruno 跑通：login → me → refresh → logout
- mock 用户 admin/123456 登录成功，me 返回 6 个角色 + permissions
- 装饰 @Roles('Finance') 的接口被 admin 之外角色访问 → 403
- /docs 看到完整接口文档
- 导出的 openapi.yaml 与 docs/openapi.yaml 字段对齐
```

### B3 · 租户 / 房源 / 合同

```
任务：实现 19 个 endpoint（Part 2/3/4）。

参考 docs/api.md 第 2/3/4 节 + 现有前端 features/tenant、asset、contract 的入参出参形状。

要求：
1. modules/tenant/：9 endpoints，包含：
   - Excel 导入两阶段（preview 用 xlsx 解析 + Zod 校验，commit 后真正写库）
   - 关联查询 /tenants/:id/contracts、/work-orders
2. modules/unit/：4 endpoints，树结构查询用 GROUP BY 聚合
   - check-out preflight 查关联未付账单
3. modules/contract/：6 endpoints，状态机校验
   - 批量续约：循环创建新合同 + 标记旧合同 EXPIRED + 单事务
   - 附件 OSS STS：调用 STS SDK 签发临时凭证
4. 公共：PageQueryDto、@AuditLog 装饰所有写操作

DoD：
- 跑前端 admin-web M4 三个页面真接口（关 mock），CRUD 全通
- Excel 导入 10 行（含错误行）正确返回 5 OK + 5 FAIL
- 退租前置检查：有欠费时返回 hasUnpaidBills:true
- OSS STS 拿到能上传的临时 token
```

### B4 · 账单 + 财务

```
任务：实现 22 个 endpoint + 月度出账批处理 + 对账三方比对 + 关账事务。

参考 docs/api.md 第 5/6 节。

要求：
1. libs/billing-engine：4 种策略（FIXED/BY_AREA/BY_METER/TIERED），TDD 写
2. modules/bill：14 endpoints
   - GET /bills/stats 用 SQL 聚合（不要 ORM 循环）
   - POST /bills/push 批量推消息（队列异步）
3. modules/finance：8 endpoints
   - /reconciliation：拉 BillPayment + mock 微信/银行流水 三表比对
   - /close：FOR UPDATE 锁本期账单 + 事务关账 + 写 close_snapshots
   - 智能认领：libs/rule-engine + ClaimService
4. jobs/billing.processor.ts：cron `0 1 1 * *` + 幂等
5. 单测：billing-engine 100% 覆盖（4 种 calcType × 边界值）

DoD：
- 手动触发 billing job → 8 个合同生成 8 张 5 月账单
- 阶梯计费 (0-100:5, 100-200:7, 200+:9) qty=250 → 1650.00
- 关账后再 PATCH bill → 返回 40903
- 智能认领规则引擎：金额完全匹配 + 备注含单元号 → 自动 claim
```

### B5 · 工单 + WebSocket + 投诉

```
任务：实现 18 个 endpoint + Socket.IO Gateway + SLA 定时器 + 投诉三向闭环。

参考 docs/api.md 第 7/8 节。

要求：
1. modules/workorder：10 endpoints
   - 状态机用 xstate 或手写 transitions map
   - 改派写 timeline + 通知双方
2. ws/workorder.gateway.ts：参考 Part 5.7
3. jobs/sla.processor.ts：cron `*/5 * * * *` 扫超时工单
4. jobs/auto-close.processor.ts：DONE 状态 72h 后延迟任务
5. modules/complaint：8 endpoints
   - 状态机 PENDING → INVESTIGATING → AWAITING_CONFIRM → CLOSED
   - 申诉 APPEALING 走另一支
6. WebSocket 跨实例：@socket.io/redis-adapter

DoD：
- 双开两个浏览器连同一 workOrderId → 互发消息瞬间互通
- 故意创建 SLA 1 分钟工单 → 5 min 后扫到并打 OVERDUE
- 投诉走完全流程并能申诉 → 裁决 → 闭环
```

### B6 · 仪表盘 + 报表 + 异步导出

```
任务：实现 10 个 endpoint + 物化视图 + Excel 流式导出。

参考 docs/api.md 第 9/10 节。

要求：
1. modules/dashboard：3 endpoints
   - 用 SQL aggregate + window function 不要 ORM
   - 缓存 staleTime=30s（@CacheTTL）
2. modules/report：7 endpoints
   - 物化视图：CREATE MATERIALIZED VIEW bills_monthly_view
   - 每日 01:00 cron REFRESH MATERIALIZED VIEW
3. /reports/export-task + /export-task/:id：
   - POST → BullMQ 推任务 → 返 taskId
   - GET → 查 Redis 中任务状态
   - 大数据用 exceljs streaming writer 到 OSS
4. 通知任务完成：调消息模板 INSTATION + EMAIL

DoD：
- 仪表盘 4 卡片 + 4 图响应 < 200ms
- 导出 100k 行 Excel ≤ 30s 完成，OSS 拿到下载链接
- 报表筛选条件按 URL 同步前端
```

### B7 · 小程序专属（微信登录/支付/上传/通知）

```
任务：实现 wx-login / payment-v3 / OSS STS / 模板消息推送。

要求：
1. modules/auth/wx-login：
   - 调微信 jscode2session 拿 openId + sessionKey
   - 写库 user_wx_bindings
   - 颁发 JWT
2. modules/auth/bind-phone：
   - 解密 wx getPhoneNumber 返回的 encryptedData
3. modules/payment：
   - 微信支付 V3：unifiedOrder + 回调签名校验
   - 支付宝：trade.create + 回调
   - 回调入 BillPayment + 触发 claim
4. modules/upload/sts：阿里云 STS SDK
5. modules/notification：
   - 微信订阅消息：模板 ID + 数据填充
   - 落 notifications 表 + Socket.IO 站内推送

DoD：
- 真小程序 Taro.login + 后端联调成功
- 微信支付沙箱跑通 1 笔
- OSS STS 拿到能上传的凭证
- 订阅消息到达手机
```

### B8 · 系统设置（用户/角色/日志/模板）

```
任务：实现 14 个 endpoint + 操作日志拦截器 + 模板引擎。

参考 docs/api.md 第 11 节。

要求：
1. modules/system/user：5 endpoints
   - reset-pwd 返回临时密码（不存原文，仅短期 Redis 存哈希）
   - toggle 启停
2. modules/system/role：3 endpoints
   - PUT 角色权限：内置 SuperAdmin 拒；其他可改 RolePermission
3. modules/system/op-log：2 endpoints
   - 已通过装饰器拦截写入
   - 列表查询 ES / PG 全文检索
4. modules/system/message-template：4 endpoints
   - extractVars 工具（与前端 utils/extractVars 同算法）
   - 模板渲染：Mustache 或自写 {var} 替换

DoD：
- 角色矩阵 PUT 后立即反映到下次登录用户的权限
- 删除 PUBLISHED 模板时，先校验场景是否被引用
- /op-logs 能按 actor + action + 时间筛选
- 模板渲染："您好 {name}" + {name:'张三'} → "您好 张三"
```

### B9 · 性能 / 测试 / 部署

```
任务：索引优化 + Jest e2e + Dockerfile + K8s + 监控集成。

要求：
1. 给所有 list endpoint 加 EXPLAIN ANALYZE，补索引
2. Jest e2e（test/）：
   - auth e2e（login → refresh → logout）
   - bill e2e（创建合同 → 出账 → 收款 → 状态）
   - workorder e2e（创建 → 接单 → WS 通信 → 完成 → 评价）
3. Dockerfile multi-stage（见 6.2）
4. K8s manifests（见 6.3）+ Helm chart
5. prom-client 暴露 /metrics
6. Pino logger → stdout（K8s 收 logs 进 Loki）
7. Sentry 集成

DoD：
- e2e 全过
- 1000 QPS 压测 P99 < 300ms
- docker build 成功，docker run 起 + 健康检查通过
- /metrics 有 http_request_duration 等指标
```

### B10 · 灰度 / 监控 / 文档

```
任务：上线前最后一公里。

要求：
1. nginx-ingress canary 灰度配置
2. Grafana 仪表板：QPS、P99、5xx 率、队列堆积、DB 连接池
3. Alertmanager 告警规则
4. docs/runbook.md：常见故障预案
5. docs/architecture.md：ER 图 + 模块依赖图
6. CHANGELOG B0~B10

DoD：
- 灰度 5% 流量验证 30min 无 5xx 增长 → 切 100%
- 故障演练：kill 一个 pod，可用性 ≥ 99.95%
- 文档完整
```

---

<a id="a"></a>

# 附录 A · 前后端对接清单

## A.1 启动联调前需要确认

- [ ] `docs/openapi.yaml` 由后端导出，前端 `pnpm gen:api` 重生
- [ ] CORS 放开前端 dev 域名 `http://localhost:5173`
- [ ] 错误码表完整对齐（见 `docs/api.md`）
- [ ] 时间字段 ISO 8601 + UTC+8
- [ ] 金额字段 string
- [ ] 分页结构 `{ list, total, page, pageSize }`
- [ ] OSS STS 实际可用
- [ ] 支付下单返回 wx/支付宝双端参数
- [ ] WebSocket 走 query string token（小程序限制）
- [ ] envelope `{ code, message, data, traceId }`
- [ ] traceId 透传到日志便于前后端关联

## A.2 后端给前端的对接物

| 物件                               | 用途                             | 频率               |
| ---------------------------------- | -------------------------------- | ------------------ |
| `docs/openapi.yaml`                | orval 生成 hooks                 | 每次接口变更       |
| 联调环境 `https://api-dev.xxx.com` | 前端切换 mock=false              | 长期稳定           |
| 测试账号                           | mock 5 角色（admin/finance/...） | 每次重置数据后告知 |
| 错误码表                           | 前端拦截器分支                   | 一次性             |
| WebSocket 地址                     | 工单聊天                         | 一次性             |

## A.3 阶段联调点

| 阶段 | 前端       | 后端       | 联调动作                |
| ---- | ---------- | ---------- | ----------------------- |
| 1    | M2 done    | B0/B1 done | 接口文档对齐            |
| 2    | M3 done    | B2 done    | 登录链路联调（关 mock） |
| 3    | M4 done    | B3 done    | 租户/房源/合同 联调     |
| 4    | M5 done    | B4 done    | 账单 + 财务 联调        |
| 5    | M6 done    | B5 done    | 工单 + WebSocket 联调   |
| 6    | M7 done    | B6 done    | 报表 + 异步导出 联调    |
| 7    | M8/M9 done | B7 done    | 小程序登录 + 支付       |
| 8    | M11 done   | B8 done    | 系统设置联调            |
| 9    | M10 + B9   | –          | 性能压测 + E2E 真接口   |

---

<a id="b"></a>

# 附录 B · 选型备选方案

## B.1 Spring Boot 3 + Kotlin

| 类别      | NestJS 选型      | Spring Boot 等价                           |
| --------- | ---------------- | ------------------------------------------ |
| 框架      | NestJS           | Spring Boot 3.2+                           |
| 语言      | TS               | Kotlin（首选）/ Java 21                    |
| ORM       | Prisma           | Spring Data JPA + Hibernate / MyBatis-Plus |
| 校验      | class-validator  | Bean Validation (Hibernate Validator)      |
| 鉴权      | Passport + JWT   | Spring Security + JJWT                     |
| 队列      | BullMQ           | Spring Batch / Quartz + RabbitMQ           |
| WebSocket | Socket.IO        | Spring WebSocket / STOMP                   |
| OpenAPI   | @nestjs/swagger  | springdoc-openapi                          |
| 测试      | Jest + Supertest | JUnit 5 + RestAssured                      |
| 缓存      | ioredis          | Spring Cache + Lettuce                     |

**何时选 Spring Boot**：团队 Java 背景深 / 已有 Java 微服务集群 / 需要 Spring 生态（XXL-Job, Sentinel, Nacos 等）。

## B.2 Go 1.22 + Gin/Echo

| 类别      | Go 选型                              |
| --------- | ------------------------------------ |
| 框架      | Gin / Echo / Fiber                   |
| ORM       | Ent / GORM / SQLBoiler               |
| 校验      | go-playground/validator              |
| 鉴权      | golang-jwt + custom middleware       |
| 队列      | asynq（Redis-based）/ go-rabbitmq    |
| WebSocket | gorilla/websocket / nhooyr/websocket |
| OpenAPI   | swaggo                               |
| 测试      | testify + httptest                   |

**何时选 Go**：极致性能 / 低内存占用 / 微服务化 / 团队 Go 经验丰富。物业项目 1 万 QPS 内 Go 优势不明显，但运维成本低。

## B.3 决策矩阵

| 维度                 | NestJS     | Spring Boot | Go         |
| -------------------- | ---------- | ----------- | ---------- |
| 开发效率             | ⭐⭐⭐⭐⭐ | ⭐⭐⭐      | ⭐⭐⭐     |
| 团队招聘（一线城市） | ⭐⭐⭐     | ⭐⭐⭐⭐⭐  | ⭐⭐⭐     |
| 团队招聘（二三线）   | ⭐⭐       | ⭐⭐⭐⭐⭐  | ⭐⭐       |
| 前后端类型一致       | ⭐⭐⭐⭐⭐ | ⭐⭐        | ⭐⭐       |
| 性能                 | ⭐⭐⭐     | ⭐⭐⭐⭐    | ⭐⭐⭐⭐⭐ |
| 内存占用             | ⭐⭐⭐     | ⭐⭐        | ⭐⭐⭐⭐⭐ |
| 生态成熟度           | ⭐⭐⭐⭐   | ⭐⭐⭐⭐⭐  | ⭐⭐⭐⭐   |
| 文档质量             | ⭐⭐⭐⭐   | ⭐⭐⭐⭐⭐  | ⭐⭐⭐⭐   |
| 物业行业实践案例     | ⭐⭐       | ⭐⭐⭐⭐⭐  | ⭐⭐       |

> 综合推荐：**NestJS**（前后端 TS 共享类型 + 开发效率高 + 适合中型团队）；
> 如果团队 Java 背景深或客户/SI 偏好 Java 生态，选 **Spring Boot 3 + Kotlin**。

---

**文档结束。** 后续如需新增模块（智能门禁后端 / 能耗采集 / 社区团购），按本计划格式新增 Bxx 里程碑。
