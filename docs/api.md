# API 清单 · 后端契约速查

> 状态：M0~M11 mock 已覆盖；正式后端就绪后用 `docs/openapi.yaml` 真实文档覆盖
> 整理日期：2026-05-15

## 0. 通用约定

| 项                | 约定                                                                                         |
| ----------------- | -------------------------------------------------------------------------------------------- |
| 协议              | HTTPS（生产）/ HTTP（联调）                                                                  |
| BaseURL（后台）   | `VITE_API_BASE_URL`，默认 `http://localhost:3000/api`                                        |
| BaseURL（小程序） | `TARO_APP_API_BASE_URL`，默认 `https://api-dev.example.com/api`                              |
| 鉴权              | `Authorization: Bearer <accessToken>`                                                        |
| Token 刷新        | 401 → POST `/auth/refresh` → 重试，并发请求排队                                              |
| 响应包装          | `{ code, message, data, traceId }`，`code === 0` 视为成功                                    |
| 分页              | `{ list, total, page, pageSize }`                                                            |
| 时间字段          | ISO 8601 字符串                                                                              |
| 金额字段          | string（避免 JS 浮点）                                                                       |
| 错误码            | `40xxx` 业务参数错误 / `40100` token 过期 / `40300` 无权限 / `40400` 不存在 / `5xx` 服务异常 |

调用方列：

- 🌐 **admin**：后台 admin-web 用 MSW 拦截
- 📱 **miniapp**：租户小程序用 `utils/mockApi.ts` 拦截
- 🔵 **both**：两端都用同一份后端接口（推荐生产实现）

---

## 1. 鉴权 Auth

### 1.1 后台账号 / SMS 登录（admin-web）

| 方法 | 路径                    | 用途             | 关键参数                       | 调用方 |
| ---- | ----------------------- | ---------------- | ------------------------------ | ------ |
| POST | `/auth/login`           | 账号密码登录     | `{ username, password }`       | 🌐     |
| POST | `/auth/sms/send`        | 发送短信验证码   | `{ phone }`                    | 🌐     |
| POST | `/auth/sms-login`       | 短信验证码登录   | `{ phone, code }`              | 🌐     |
| POST | `/auth/refresh`         | 刷新 accessToken | `{ refreshToken }`             | 🔵     |
| GET  | `/auth/me`              | 获取当前用户     | –                              | 🔵     |
| POST | `/auth/change-password` | 修改密码         | `{ oldPassword, newPassword }` | 🌐     |
| POST | `/auth/logout`          | 退出登录         | –                              | 🌐     |

Mock 账号（admin-web）：`admin` / `finance` / `service` / `worker` / `ops`，密码任意 ≥ 6 位。

### 1.2 小程序微信 / 支付宝登录

| 方法 | 路径               | 用途              | 关键参数                                                          | 调用方 |
| ---- | ------------------ | ----------------- | ----------------------------------------------------------------- | ------ |
| POST | `/auth/wx-login`   | 微信 code → token | `{ code }` → `{ accessToken, refreshToken, user, needBindPhone }` | 📱     |
| POST | `/auth/bind-phone` | 绑定手机号        | `{ code, encryptedData, iv }`                                     | 📱     |

### 1.3 角色矩阵

| 角色 code         | 名称       | 关键权限                                                |
| ----------------- | ---------- | ------------------------------------------------------- |
| `SuperAdmin`      | 超级管理员 | 全部 30 条权限码                                        |
| `PropertyAdmin`   | 物业管理员 | 租户/合同/账单（手单/推送）/工单（指派/关闭）/报表      |
| `Finance`         | 财务员     | 账单（看/收/导）+ 财务（对账/关账/认领）+ 报表          |
| `CustomerService` | 客服员     | `workorder:view` + `complaint:handle`                   |
| `Maintainer`      | 维修工     | 仅 `workorder:view:mine`                                |
| `Operations`      | 运营专员   | `complaint:handle` + `complaint:appeal` + `report:view` |
| `Tenant`          | 租户       | 仅小程序，本人数据                                      |

---

## 2. 租户 Tenant

| 方法   | 路径                       | 用途           | 关键参数                                        | 调用方 |
| ------ | -------------------------- | -------------- | ----------------------------------------------- | ------ |
| GET    | `/tenants`                 | 列表           | `?page&pageSize&keyword&type=PERSONAL\|COMPANY` | 🌐     |
| POST   | `/tenants`                 | 新增           | TenantBody（个人/企业切换字段）                 | 🌐     |
| GET    | `/tenants/:id`             | 详情           | –                                               | 🌐     |
| PATCH  | `/tenants/:id`             | 编辑           | Partial<TenantBody>                             | 🌐     |
| DELETE | `/tenants/:id`             | 删除（软删）   | –                                               | 🌐     |
| GET    | `/tenants/:id/contracts`   | 关联合同       | –                                               | 🌐     |
| GET    | `/tenants/:id/work-orders` | 关联工单       | –                                               | 🌐     |
| POST   | `/tenants/import/preview`  | Excel 导入预览 | `{ rows: TenantRow[] }` → 校验结果含成功/失败行 | 🌐     |
| POST   | `/tenants/import/commit`   | 确认导入       | `{ rows: TenantRow[] }`                         | 🌐     |

校验规则：身份证 18 位 + 手机 11 位 + 统一社会信用代码 18 位。

---

## 3. 房源 / 单元 Unit

| 方法 | 路径                             | 用途                | 关键参数                               | 调用方 |
| ---- | -------------------------------- | ------------------- | -------------------------------------- | ------ |
| GET  | `/units`                         | 项目→楼栋→单元 树   | `?projectId`                           | 🌐     |
| POST | `/units/:id/check-in`            | 入住（绑租户+合同） | `{ tenantId, contractId }`             | 🌐     |
| GET  | `/units/:id/check-out/preflight` | 退租前置校验        | – → `{ hasUnpaidBills, unpaidAmount }` | 🌐     |
| POST | `/units/:id/check-out`           | 退租                | –                                      | 🌐     |

---

## 4. 合同 Contract

| 方法  | 路径                         | 用途              | 关键参数                                             | 调用方 |
| ----- | ---------------------------- | ----------------- | ---------------------------------------------------- | ------ |
| GET   | `/contracts`                 | 列表              | `?page&pageSize&status&expiringIn=30`                | 🔵     |
| POST  | `/contracts`                 | 新增              | ContractBody（4 步向导汇总）                         | 🌐     |
| GET   | `/contracts/:id`             | 详情 + 状态时间轴 | –                                                    | 🔵     |
| PATCH | `/contracts/:id`             | 编辑              | Partial<ContractBody>                                | 🌐     |
| POST  | `/contracts/batch-renew`     | 批量续约          | `{ ids[], newEndDate, rentAdjust: { type, value } }` | 🌐     |
| POST  | `/contracts/attachments/sts` | 合同附件 OSS 签名 | – → STS 凭证                                         | 🌐     |

状态机：`DRAFT → PENDING → ACTIVE → EXPIRED / TERMINATED`

---

## 5. 缴费 / 账单 Bill

| 方法   | 路径                 | 用途                                     | 关键参数                                              | 调用方 |
| ------ | -------------------- | ---------------------------------------- | ----------------------------------------------------- | ------ |
| GET    | `/fee-items`         | 费用项列表                               | –                                                     | 🌐     |
| POST   | `/fee-items`         | 新增费用项                               | FeeItemBody（4 种 calcType）                          | 🌐     |
| PATCH  | `/fee-items/:id`     | 编辑                                     | Partial<FeeItemBody>                                  | 🌐     |
| DELETE | `/fee-items/:id`     | 删除                                     | –                                                     | 🌐     |
| GET    | `/bills`             | 账单列表                                 | `?status=UNPAID\|PAID\|OVERDUE\|VOID&period&tenantId` | 🔵     |
| POST   | `/bills`             | 手动补单                                 | BillBody                                              | 🌐     |
| GET    | `/bills/:id`         | 账单详情（小程序简版）                   | –                                                     | 📱     |
| GET    | `/bills/:id/detail`  | 账单详情（后台全量：明细+收款+操作日志） | –                                                     | 🌐     |
| GET    | `/bills/stats`       | 顶部统计卡                               | `?period` → `{ receivable, received, arrears }`       | 🌐     |
| GET    | `/bills/arrears`     | 欠费分析                                 | `?topN`                                               | 🌐     |
| POST   | `/bills/push`        | 一键推送                                 | `{ ids[] }` → 走消息中心                              | 🌐     |
| POST   | `/bills/:id/payment` | 收款登记（线下）                         | `{ method, amount, voucherUrl }`                      | 🌐     |
| POST   | `/bills/:id/void`    | 红冲 / 作废                              | `{ reason }`                                          | 🌐     |
| POST   | `/bills/:id/pay`     | 小程序支付成功回写                       | – → 更新为 PAID                                       | 📱     |
| POST   | `/payment/prepare`   | 支付下单（拿支付参数）                   | `{ billId, amount }` → wx/支付宝参数                  | 📱     |

`calcType` 四种：`FIXED / BY_AREA / BY_METER / TIERED`（阶梯计费）。

状态机：`待生成 → 待支付 → 已支付 / 欠费中 / 已作废`

---

## 6. 财务 Finance

| 方法 | 路径                             | 用途                 | 关键参数                                           | 调用方 |
| ---- | -------------------------------- | -------------------- | -------------------------------------------------- | ------ |
| GET  | `/finance/reconciliation`        | 对账中心三方流水     | `?date` → `{ system, gateway, bank }`              | 🌐     |
| POST | `/finance/reconciliation/adjust` | 差异调账             | `{ recordId, reason, amount }`                     | 🌐     |
| GET  | `/finance/close-status`          | 当前关账状态         | `?period` → `{ closed, closedAt, by }`             | 🌐     |
| POST | `/finance/close`                 | 月度关账             | `{ period, confirm: '我已确认' }`                  | 🌐     |
| GET  | `/finance/claim/pool`            | 待认领支付流水池     | –                                                  | 🌐     |
| POST | `/finance/claim/manual`          | 手动指派             | `{ paymentId, allocations: [{ billId, amount }] }` | 🌐     |
| GET  | `/finance/claim/rules`           | 认领规则列表         | –                                                  | 🌐     |
| PUT  | `/finance/claim/rules`           | 规则保存（含优先级） | `{ rules[] }`                                      | 🌐     |

---

## 7. 工单 WorkOrder

| 方法  | 路径                        | 用途                               | 关键参数                                     | 调用方 |
| ----- | --------------------------- | ---------------------------------- | -------------------------------------------- | ------ |
| GET   | `/work-orders`              | 列表 / 看板                        | `?status&category&maintainerId&mine`         | 🔵     |
| POST  | `/work-orders`              | 新建报修                           | `{ category, title, description, images[] }` | 🔵     |
| GET   | `/work-orders/:id`          | 工单简略详情（小程序）             | –                                            | 📱     |
| GET   | `/work-orders/:id/detail`   | 工单完整详情（含 timeline / chat） | –                                            | 🌐     |
| PATCH | `/work-orders/:id/status`   | 状态流转                           | `{ action: ACCEPT\|COMPLETE\|HOLD\|CANCEL }` | 🌐     |
| PATCH | `/work-orders/:id/assign`   | 指派 / 改派维修工                  | `{ maintainerId }`                           | 🌐     |
| POST  | `/work-orders/:id/rate`     | 5 星评价                           | `{ rating: 1-5, ratingText? }`               | 🔵     |
| GET   | `/work-orders/:id/messages` | 聊天历史                           | –                                            | 📱     |
| POST  | `/work-orders/:id/messages` | 发送消息（mock 用 REST 模拟 ws）   | `{ type: text\|image, content }`             | 📱     |
| GET   | `/maintainers`              | 维修工列表（改派下拉）             | –                                            | 🌐     |

WebSocket：`wss://api.../ws/workorder/:id?token=...` · 协议 `{type:'chat', data:{content,msgType}}` · 心跳 30s

状态机：`PENDING → IN_PROGRESS → DONE → CLOSED` · `CANCELED` · `HELD`

类别：`WATER / ELEC / HVAC / DOOR / CLEAN / OTHER`

---

## 8. 投诉 / 申诉 Complaint

| 方法 | 路径                     | 用途                        | 关键参数                                                    | 调用方 |
| ---- | ------------------------ | --------------------------- | ----------------------------------------------------------- | ------ |
| GET  | `/complaints`            | 列表                        | `?status&severity&from&to`                                  | 🔵     |
| POST | `/complaints`            | 创建                        | `{ title, target, severity, description, attachments[] }`   | 🔵     |
| GET  | `/complaints/:id`        | 详情（小程序简版）          | –                                                           | 📱     |
| GET  | `/complaints/:id/detail` | 详情（后台全量含 timeline） | –                                                           | 🌐     |
| POST | `/complaints/:id/handle` | 处理动作                    | `{ action: ACCEPT\|INVESTIGATE\|RESOLVE\|CLOSE, opinion? }` | 🌐     |
| GET  | `/complaints/analysis`   | 月度分析数据                | `?month` → `{ bySeverity, byEmployee, trend }`              | 🌐     |
| GET  | `/appeals`               | 申诉列表                    | `?status`                                                   | 🌐     |
| POST | `/appeals/:id/decide`    | 主管裁决                    | `{ action: UPHOLD\|REVOKE\|ADJUST, newSeverity? }`          | 🌐     |

状态机：`PENDING → INVESTIGATING → AWAITING_CONFIRM → CLOSED` 或 `APPEALING → 裁决`

严重度：`LIGHT / MEDIUM / HEAVY`

---

## 9. 仪表盘 Dashboard

| 方法 | 路径                  | 用途                                          | 关键参数 | 调用方 |
| ---- | --------------------- | --------------------------------------------- | -------- | ------ |
| GET  | `/dashboard/overview` | 顶部 4 卡（入住率/应收/实收/待办工单）        | –        | 🌐     |
| GET  | `/dashboard/stats`    | 4 张图表数据（收入/收缴率/工单分类/投诉趋势） | –        | 🌐     |
| GET  | `/dashboard/todos`    | 右侧待办（合同到期/欠费 TOP10/未处理工单）    | –        | 🌐     |

---

## 10. 报表 Reports

| 方法 | 路径                            | 用途                  | 关键参数                                | 调用方 |
| ---- | ------------------------------- | --------------------- | --------------------------------------- | ------ |
| GET  | `/reports/rent-income`          | 租金收入              | `?from&to&projectId`                    | 🌐     |
| GET  | `/reports/collection-rate`      | 收缴率                | `?from&to&projectId`                    | 🌐     |
| GET  | `/reports/arrears`              | 欠费明细              | `?asOf&topN`                            | 🌐     |
| GET  | `/reports/workorder-efficiency` | 工单时效              | `?from&to&maintainerId`                 | 🌐     |
| GET  | `/reports/satisfaction`         | 满意度                | `?from&to`                              | 🌐     |
| POST | `/reports/export-task`          | 异步导出（> 5000 行） | `{ reportType, params }` → `{ taskId }` | 🌐     |
| GET  | `/reports/export-task/:id`      | 轮询任务状态          | – → `{ status, downloadUrl? }`          | 🌐     |

任务状态：`PENDING → RUNNING → DONE / FAILED`

---

## 11. 系统设置 System（M11，仅 SuperAdmin）

### 11.1 用户管理

| 方法  | 路径                          | 用途                    | 关键参数                                     | 调用方 |
| ----- | ----------------------------- | ----------------------- | -------------------------------------------- | ------ |
| GET   | `/system/users`               | 用户列表                | `?page&pageSize&keyword&role&status`         | 🌐     |
| POST  | `/system/users`               | 新增                    | `{ username, name, phone, role, projectId }` | 🌐     |
| PATCH | `/system/users/:id`           | 编辑（不能改 username） | Partial<UserBody>                            | 🌐     |
| POST  | `/system/users/:id/reset-pwd` | 重置密码                | – → `{ tempPassword }`                       | 🌐     |
| POST  | `/system/users/:id/toggle`    | 启用 / 停用             | `{ status: ACTIVE\|DISABLED }`               | 🌐     |

### 11.2 角色 / 权限

| 方法 | 路径                  | 用途                                | 关键参数                    | 调用方 |
| ---- | --------------------- | ----------------------------------- | --------------------------- | ------ |
| GET  | `/system/permissions` | 全量权限码（按 group）              | – → 30 条权限               | 🌐     |
| GET  | `/system/roles`       | 角色列表（含 permissions[]）        | – → 6 个内置角色            | 🌐     |
| PUT  | `/system/roles/:code` | 覆盖某角色权限矩阵（SuperAdmin 拒） | `{ permissions: string[] }` | 🌐     |

权限组：租户 / 合同 / 账单 / 财务 / 工单 / 投诉 / 报表 / 系统

### 11.3 操作日志

| 方法 | 路径                  | 用途                     | 关键参数                                  | 调用方 |
| ---- | --------------------- | ------------------------ | ----------------------------------------- | ------ |
| GET  | `/system/op-logs`     | 列表                     | `?actorName&action&from&to&page&pageSize` | 🌐     |
| GET  | `/system/op-logs/:id` | 详情（含 diff 字段变更） | –                                         | 🌐     |

action 类型：`CREATE / UPDATE / DELETE / LOGIN / EXPORT / OTHER`

### 11.4 消息模板

| 方法   | 路径                            | 用途 | 关键参数                                            | 调用方 |
| ------ | ------------------------------- | ---- | --------------------------------------------------- | ------ |
| GET    | `/system/message-templates`     | 列表 | `?channel&keyword`                                  | 🌐     |
| POST   | `/system/message-templates`     | 新增 | `{ channel, scene, name, content, vars[], status }` | 🌐     |
| PATCH  | `/system/message-templates/:id` | 编辑 | Partial                                             | 🌐     |
| DELETE | `/system/message-templates/:id` | 删除 | –                                                   | 🌐     |

通道：`SMS / EMAIL / PUSH / INSTATION`
状态：`DRAFT / PUBLISHED`
变量：用 `{var}` 占位，保存时自动从 content 解析回写 vars

---

## 12. 小程序专属

### 12.1 通知 Notification（小程序消息中心）

| 方法 | 路径                      | 用途         | 关键参数                                             | 调用方 |
| ---- | ------------------------- | ------------ | ---------------------------------------------------- | ------ |
| GET  | `/notifications`          | 列表         | `?category=PAYMENT\|WORKORDER\|ANNOUNCEMENT\|SYSTEM` | 📱     |
| POST | `/notifications/:id/read` | 标记单条已读 | –                                                    | 📱     |
| POST | `/notifications/read-all` | 全部已读     | –                                                    | 📱     |

### 12.2 公告 Announcement

| 方法 | 路径             | 用途         | 关键参数 | 调用方 |
| ---- | ---------------- | ------------ | -------- | ------ |
| GET  | `/announcements` | 首页轮播公告 | –        | 📱     |

### 12.3 Badge 计数

| 方法 | 路径         | 用途                             | 关键参数                              | 调用方 |
| ---- | ------------ | -------------------------------- | ------------------------------------- | ------ |
| GET  | `/me/badges` | tabBar 红点（未付账单/未读消息） | – → `{ unpaidBills, unreadMessages }` | 📱     |

---

## 13. 文件上传 OSS

| 方法 | 路径                         | 用途                   | 关键参数                 | 调用方 |
| ---- | ---------------------------- | ---------------------- | ------------------------ | ------ |
| POST | `/upload/sts`                | 通用 OSS STS（小程序） | – → STS 凭证 + uploadUrl | 📱     |
| POST | `/contracts/attachments/sts` | 合同附件 STS（后台）   | – → STS 凭证             | 🌐     |

STS 字段：`{ accessKeyId, accessKeySecret, securityToken, expiration, bucket, region, host, dir, uploadUrl }`

---

## 14. 实时通信 WebSocket

| 通道     | URL                                                        | 用途                    |
| -------- | ---------------------------------------------------------- | ----------------------- |
| 工单聊天 | `wss://<api>/ws/workorder/:id?token=<accessToken>`         | 工单详情租户↔维修工聊天 |
| 投诉通知 | `wss://<api>/ws/complaint/:id?token=<accessToken>`（规划） | 投诉状态变更推送        |

消息协议（JSON）：

```ts
// 客户端 → 服务端
{ type: 'chat', data: { content: string, msgType: 'text' | 'image' } }
// 服务端 → 客户端
{ type: 'chat', data: ChatMessage }
{ type: 'status', data: { workOrderId, status } }
```

心跳：30s 一次 `{ type: 'ping' }` → 服务端回 `{ type: 'pong' }`。dev 环境小程序用 1.2s REST 轮询模拟。

---

## 15. 接口汇总统计

| 模块        | endpoints                                    | 平台                |
| ----------- | -------------------------------------------- | ------------------- |
| 鉴权        | 9                                            | admin + miniapp     |
| 租户        | 9                                            | admin               |
| 房源        | 4                                            | admin               |
| 合同        | 6                                            | admin + miniapp     |
| 账单 / 费用 | 14                                           | admin + miniapp     |
| 财务        | 8                                            | admin               |
| 工单        | 10                                           | admin + miniapp     |
| 投诉 / 申诉 | 8                                            | admin + miniapp     |
| 仪表盘      | 3                                            | admin               |
| 报表        | 7                                            | admin               |
| 系统设置    | 14                                           | admin（SuperAdmin） |
| 小程序专属  | 4                                            | miniapp             |
| 上传        | 2                                            | both                |
| **总计**    | **约 98 个 REST 端点** + 1 个 WebSocket 通道 |

---

## 16. 后端实装顺序建议

按 ROI 优先级（高 → 低）：

1. **鉴权** + 用户管理（M3 + M11.1）— 所有页面依赖
2. **租户 / 合同 / 房源** — 业务主线
3. **账单 / 费用项 / 支付** — 现金流核心
4. **工单 + WebSocket** — 客户体验关键
5. **投诉 / 申诉** — 合规
6. **财务对账 / 关账 / 认领** — 财务月结
7. **仪表盘 / 报表** — 决策支持
8. **角色权限 / 操作日志 / 消息模板** — 运营治理

---

## 17. 联调 Checklist

- [ ] `docs/openapi.yaml` 由后端导出，前端 `pnpm gen:api` 重生 hooks
- [ ] CORS 放开前端 dev 域名（`http://localhost:5173`）
- [ ] 错误码表对齐：`40001` 必填 / `40100` token 过期 / `40300` 无权限 / `40400` 不存在 / `40900` 业务冲突 / `5xx` 服务异常
- [ ] OSS STS 实际可拿到临时凭证
- [ ] 支付下单返回 wx/支付宝双端参数（小程序按 `TARO_ENV` 选）
- [ ] WebSocket 鉴权方式：query string `?token=` vs header（小程序 `Taro.connectSocket` 不支持自定义 header，建议 query）
- [ ] 时间字段统一 ISO 8601 + UTC+8
- [ ] 金额字段全部 string，避免 number
- [ ] 分页结构 `{ list, total, page, pageSize }` 不要改

---

附：所有 mock 端点的源代码位置

| 模块                                                   | 后台 MSW handlers                                        | 小程序 mock                         |
| ------------------------------------------------------ | -------------------------------------------------------- | ----------------------------------- |
| Auth                                                   | `apps/admin-web/src/mocks/handlers/auth.ts`              | `apps/miniapp/src/utils/mockApi.ts` |
| Tenant                                                 | `apps/admin-web/src/mocks/handlers/tenants.ts` + `m4.ts` | –                                   |
| Contract / Unit                                        | `apps/admin-web/src/mocks/handlers/m4.ts` + `others.ts`  | `apps/miniapp/src/utils/mockApi.ts` |
| Bill / Fee / Finance                                   | `apps/admin-web/src/mocks/handlers/m5.ts` + `others.ts`  | `apps/miniapp/src/utils/mockApi.ts` |
| WorkOrder / Complaint / Appeal                         | `apps/admin-web/src/mocks/handlers/m6.ts` + `others.ts`  | `apps/miniapp/src/utils/mockApi.ts` |
| Dashboard / Reports                                    | `apps/admin-web/src/mocks/handlers/m7.ts` + `others.ts`  | –                                   |
| System                                                 | `apps/admin-web/src/mocks/handlers/m11.ts`               | –                                   |
| Notification / Announcement / Badge / Upload / Payment | –                                                        | `apps/miniapp/src/utils/mockApi.ts` |
