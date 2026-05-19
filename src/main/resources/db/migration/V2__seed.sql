-- ============================================================
-- V2__seed.sql  开发种子数据
-- BCrypt('123456', rounds=10) = $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- ============================================================

SET NAMES utf8mb4;
SET time_zone = '+08:00';

-- ============================================================
-- 权限码（30 个，按 group 分组）
-- ============================================================
INSERT INTO permissions (code, name, `group`) VALUES
-- 租户
('tenant:view',    '查看租户',     'tenant'),
('tenant:create',  '新增租户',     'tenant'),
('tenant:edit',    '编辑租户',     'tenant'),
('tenant:delete',  '删除租户',     'tenant'),
('tenant:import',  '导入租户',     'tenant'),
-- 合同
('contract:view',      '查看合同',   'contract'),
('contract:create',    '新增合同',   'contract'),
('contract:edit',      '编辑合同',   'contract'),
('contract:terminate', '终止合同',   'contract'),
('contract:renew',     '续签合同',   'contract'),
-- 账单
('bill:view',   '查看账单',   'bill'),
('bill:create', '生成账单',   'bill'),
('bill:edit',   '编辑账单',   'bill'),
('bill:void',   '作废账单',   'bill'),
('bill:export', '导出账单',   'bill'),
-- 财务
('finance:view',       '查看财务',   'finance'),
('finance:reconcile',  '发起对账',   'finance'),
('finance:close',      '执行关账',   'finance'),
('finance:claim',      '认领流水',   'finance'),
-- 工单
('workorder:view',     '查看工单',   'workorder'),
('workorder:create',   '新建工单',   'workorder'),
('workorder:assign',   '分配工单',   'workorder'),
('workorder:complete', '完成工单',   'workorder'),
-- 投诉
('complaint:view',   '查看投诉',   'complaint'),
('complaint:handle', '处理投诉',   'complaint'),
('complaint:appeal', '处理申诉',   'complaint'),
-- 报表
('report:view',   '查看报表',   'report'),
('report:export', '导出报表',   'report'),
-- 系统
('system:user', '用户管理',   'system'),
('system:role', '角色管理',   'system');

-- ============================================================
-- 内置角色（6 个）
-- ============================================================
INSERT INTO roles (code, name, description, built_in) VALUES
('SuperAdmin',       '超级管理员', '拥有全部权限，不可删除',           1),
('PropertyAdmin',    '物业管理员', '日常物业管理，不含系统用户角色设置', 1),
('Finance',          '财务专员',   '账单、财务、报表权限',               1),
('CustomerService',  '客服专员',   '租户、合同、工单权限',               1),
('Maintainer',       '维修人员',   '查看并完成工单',                     1),
('Operations',       '运营专员',   '报表查看和基础数据查看',             1);

-- ============================================================
-- 角色权限关联
-- ============================================================

-- SuperAdmin：全部 30 个权限
INSERT INTO role_permissions (role_code, permission_code)
SELECT 'SuperAdmin', code FROM permissions;

-- PropertyAdmin：除 system:user / system:role
INSERT INTO role_permissions (role_code, permission_code)
SELECT 'PropertyAdmin', code FROM permissions
WHERE code NOT IN ('system:user', 'system:role');

-- Finance：bill + finance + report
INSERT INTO role_permissions (role_code, permission_code)
SELECT 'Finance', code FROM permissions
WHERE `group` IN ('bill', 'finance', 'report');

-- CustomerService：tenant + contract + workorder:view/create
INSERT INTO role_permissions (role_code, permission_code)
SELECT 'CustomerService', code FROM permissions
WHERE `group` IN ('tenant', 'contract')
   OR code IN ('workorder:view', 'workorder:create');

-- Maintainer：workorder:view/complete + tenant:view
INSERT INTO role_permissions (role_code, permission_code)
VALUES
('Maintainer', 'workorder:view'),
('Maintainer', 'workorder:complete'),
('Maintainer', 'tenant:view');

-- Operations：report + tenant:view + contract:view + bill:view
INSERT INTO role_permissions (role_code, permission_code)
SELECT 'Operations', code FROM permissions
WHERE `group` = 'report'
   OR code IN ('tenant:view', 'contract:view', 'bill:view');

-- ============================================================
-- 内置用户（5 个，密码均为 123456）
-- ============================================================
-- 如哈希不匹配可用 Spring BCryptPasswordEncoder.encode("123456") 重新生成
INSERT INTO users (id, username, password_hash, name, phone, status, project_id, deleted, created_at, updated_at) VALUES
('USR001', 'admin',            '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '超级管理员', '13800000001', 'ACTIVE', NULL,     0, NOW(), NOW()),
('USR002', 'propertyAdmin',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '物业管理员', '13800000002', 'ACTIVE', 'PRJ001', 0, NOW(), NOW()),
('USR003', 'finance01',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '财务小王',   '13800000003', 'ACTIVE', 'PRJ001', 0, NOW(), NOW()),
('USR004', 'customerService01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '客服小李',   '13800000004', 'ACTIVE', 'PRJ001', 0, NOW(), NOW()),
('USR005', 'maintainer01',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '维修老张',   '13800000005', 'ACTIVE', 'PRJ001', 0, NOW(), NOW());

-- 用户角色关联
INSERT INTO user_roles (user_id, role_code) VALUES
('USR001', 'SuperAdmin'),
('USR002', 'PropertyAdmin'),
('USR003', 'Finance'),
('USR004', 'CustomerService'),
('USR005', 'Maintainer');

-- ============================================================
-- 项目（3 个）
-- ============================================================
INSERT INTO projects (id, name, address, manager_id, status, deleted, created_at, updated_at) VALUES
('PRJ001', '阳光国际小区', '北京市朝阳区阳光路 1 号',     'USR002', 'ACTIVE', 0, NOW(), NOW()),
('PRJ002', '绿城花园',     '上海市浦东新区绿城路 88 号',   'USR002', 'ACTIVE', 0, NOW(), NOW()),
('PRJ003', '和谐家园',     '广州市天河区和谐大道 666 号',  'USR002', 'ACTIVE', 0, NOW(), NOW());

-- ============================================================
-- 楼栋（5 个）
-- ============================================================
INSERT INTO buildings (id, name, floor_count, project_id, status, deleted, created_at, updated_at) VALUES
('BLD001', 'A 栋', 10, 'PRJ001', 'ACTIVE', 0, NOW(), NOW()),
('BLD002', 'B 栋', 12, 'PRJ001', 'ACTIVE', 0, NOW(), NOW()),
('BLD003', 'A 栋', 15, 'PRJ002', 'ACTIVE', 0, NOW(), NOW()),
('BLD004', '1 号楼', 8, 'PRJ003', 'ACTIVE', 0, NOW(), NOW()),
('BLD005', '2 号楼', 10,'PRJ003', 'ACTIVE', 0, NOW(), NOW());

-- ============================================================
-- 单元（50 个，每栋 10 个）
-- ============================================================
INSERT INTO units (id, no, floor, area, type, room_count, status, building_id, project_id, deleted, created_at, updated_at) VALUES
-- BLD001 A栋 (PRJ001)
('UNIT001','A-101',1, 75.50,'APARTMENT',2,'VACANT',  'BLD001','PRJ001',0,NOW(),NOW()),
('UNIT002','A-102',1, 82.30,'APARTMENT',2,'OCCUPIED', 'BLD001','PRJ001',0,NOW(),NOW()),
('UNIT003','A-201',2, 75.50,'APARTMENT',2,'OCCUPIED', 'BLD001','PRJ001',0,NOW(),NOW()),
('UNIT004','A-202',2, 90.00,'APARTMENT',3,'VACANT',   'BLD001','PRJ001',0,NOW(),NOW()),
('UNIT005','A-301',3,100.00,'APARTMENT',3,'OCCUPIED', 'BLD001','PRJ001',0,NOW(),NOW()),
('UNIT006','A-302',3, 88.50,'APARTMENT',2,'OCCUPIED', 'BLD001','PRJ001',0,NOW(),NOW()),
('UNIT007','A-401',4, 75.50,'APARTMENT',2,'VACANT',   'BLD001','PRJ001',0,NOW(),NOW()),
('UNIT008','A-402',4,120.00,'APARTMENT',4,'OCCUPIED', 'BLD001','PRJ001',0,NOW(),NOW()),
('UNIT009','A-501',5, 95.00,'APARTMENT',3,'OCCUPIED', 'BLD001','PRJ001',0,NOW(),NOW()),
('UNIT010','A-502',5, 85.00,'APARTMENT',2,'VACANT',   'BLD001','PRJ001',0,NOW(),NOW()),
-- BLD002 B栋 (PRJ001)
('UNIT011','B-101',1, 68.00,'APARTMENT',2,'OCCUPIED', 'BLD002','PRJ001',0,NOW(),NOW()),
('UNIT012','B-102',1, 72.50,'APARTMENT',2,'VACANT',   'BLD002','PRJ001',0,NOW(),NOW()),
('UNIT013','B-201',2, 68.00,'APARTMENT',2,'OCCUPIED', 'BLD002','PRJ001',0,NOW(),NOW()),
('UNIT014','B-202',2, 80.00,'APARTMENT',3,'OCCUPIED', 'BLD002','PRJ001',0,NOW(),NOW()),
('UNIT015','B-301',3, 68.00,'APARTMENT',2,'VACANT',   'BLD002','PRJ001',0,NOW(),NOW()),
('UNIT016','B-302',3, 95.00,'APARTMENT',3,'OCCUPIED', 'BLD002','PRJ001',0,NOW(),NOW()),
('UNIT017','B-401',4,110.00,'APARTMENT',4,'OCCUPIED', 'BLD002','PRJ001',0,NOW(),NOW()),
('UNIT018','B-402',4, 68.00,'APARTMENT',2,'VACANT',   'BLD002','PRJ001',0,NOW(),NOW()),
('UNIT019','B-501',5, 72.50,'APARTMENT',2,'OCCUPIED', 'BLD002','PRJ001',0,NOW(),NOW()),
('UNIT020','B-502',5, 88.00,'APARTMENT',2,'OCCUPIED', 'BLD002','PRJ001',0,NOW(),NOW()),
-- BLD003 A栋 (PRJ002)
('UNIT021','A-101',1, 65.00,'APARTMENT',2,'OCCUPIED', 'BLD003','PRJ002',0,NOW(),NOW()),
('UNIT022','A-102',1, 78.00,'APARTMENT',2,'VACANT',   'BLD003','PRJ002',0,NOW(),NOW()),
('UNIT023','A-201',2, 65.00,'APARTMENT',2,'OCCUPIED', 'BLD003','PRJ002',0,NOW(),NOW()),
('UNIT024','A-202',2, 92.00,'APARTMENT',3,'OCCUPIED', 'BLD003','PRJ002',0,NOW(),NOW()),
('UNIT025','A-301',3, 65.00,'APARTMENT',2,'VACANT',   'BLD003','PRJ002',0,NOW(),NOW()),
('UNIT026','A-302',3, 85.00,'APARTMENT',2,'OCCUPIED', 'BLD003','PRJ002',0,NOW(),NOW()),
('UNIT027','A-401',4,105.00,'APARTMENT',3,'OCCUPIED', 'BLD003','PRJ002',0,NOW(),NOW()),
('UNIT028','A-402',4, 65.00,'APARTMENT',2,'VACANT',   'BLD003','PRJ002',0,NOW(),NOW()),
('UNIT029','A-501',5, 78.00,'APARTMENT',2,'OCCUPIED', 'BLD003','PRJ002',0,NOW(),NOW()),
('UNIT030','A-502',5, 96.00,'APARTMENT',3,'OCCUPIED', 'BLD003','PRJ002',0,NOW(),NOW()),
-- BLD004 1号楼 (PRJ003)
('UNIT031','1-101',1, 55.00,'APARTMENT',1,'OCCUPIED', 'BLD004','PRJ003',0,NOW(),NOW()),
('UNIT032','1-102',1, 62.00,'APARTMENT',2,'VACANT',   'BLD004','PRJ003',0,NOW(),NOW()),
('UNIT033','1-201',2, 55.00,'APARTMENT',1,'OCCUPIED', 'BLD004','PRJ003',0,NOW(),NOW()),
('UNIT034','1-202',2, 78.00,'APARTMENT',2,'OCCUPIED', 'BLD004','PRJ003',0,NOW(),NOW()),
('UNIT035','1-301',3, 55.00,'APARTMENT',1,'VACANT',   'BLD004','PRJ003',0,NOW(),NOW()),
('UNIT036','1-302',3, 90.00,'APARTMENT',3,'OCCUPIED', 'BLD004','PRJ003',0,NOW(),NOW()),
('UNIT037','1-401',4, 62.00,'APARTMENT',2,'OCCUPIED', 'BLD004','PRJ003',0,NOW(),NOW()),
('UNIT038','1-402',4, 55.00,'APARTMENT',1,'VACANT',   'BLD004','PRJ003',0,NOW(),NOW()),
('UNIT039','1-501',5, 78.00,'APARTMENT',2,'OCCUPIED', 'BLD004','PRJ003',0,NOW(),NOW()),
('UNIT040','1-502',5, 62.00,'APARTMENT',2,'OCCUPIED', 'BLD004','PRJ003',0,NOW(),NOW()),
-- BLD005 2号楼 (PRJ003)
('UNIT041','2-101',1, 72.00,'APARTMENT',2,'OCCUPIED', 'BLD005','PRJ003',0,NOW(),NOW()),
('UNIT042','2-102',1, 80.00,'APARTMENT',2,'VACANT',   'BLD005','PRJ003',0,NOW(),NOW()),
('UNIT043','2-201',2, 72.00,'APARTMENT',2,'OCCUPIED', 'BLD005','PRJ003',0,NOW(),NOW()),
('UNIT044','2-202',2, 95.00,'APARTMENT',3,'OCCUPIED', 'BLD005','PRJ003',0,NOW(),NOW()),
('UNIT045','2-301',3, 72.00,'APARTMENT',2,'VACANT',   'BLD005','PRJ003',0,NOW(),NOW()),
('UNIT046','2-302',3,108.00,'APARTMENT',3,'OCCUPIED', 'BLD005','PRJ003',0,NOW(),NOW()),
('UNIT047','2-401',4, 80.00,'APARTMENT',2,'OCCUPIED', 'BLD005','PRJ003',0,NOW(),NOW()),
('UNIT048','2-402',4, 72.00,'APARTMENT',2,'VACANT',   'BLD005','PRJ003',0,NOW(),NOW()),
('UNIT049','2-501',5, 95.00,'APARTMENT',3,'OCCUPIED', 'BLD005','PRJ003',0,NOW(),NOW()),
('UNIT050','2-502',5, 80.00,'APARTMENT',2,'OCCUPIED', 'BLD005','PRJ003',0,NOW(),NOW());

-- ============================================================
-- 租户（10 个 mock）
-- ============================================================
INSERT INTO tenants (id, type, name, id_card, phone, status, deleted, created_at, updated_at) VALUES
('TNT001','PERSONAL','张伟',  '110101199001011234','13901000001','ACTIVE',0,NOW(),NOW()),
('TNT002','PERSONAL','李娜',  '110101199205152345','13901000002','ACTIVE',0,NOW(),NOW()),
('TNT003','PERSONAL','王磊',  '110101198811223456','13901000003','ACTIVE',0,NOW(),NOW()),
('TNT004','PERSONAL','刘洋',  '110101199307074567','13901000004','ACTIVE',0,NOW(),NOW()),
('TNT005','PERSONAL','陈静',  '110101199412185678','13901000005','ACTIVE',0,NOW(),NOW()),
('TNT006','PERSONAL','杨帆',  '110101198903226789','13901000006','ACTIVE',0,NOW(),NOW()),
('TNT007','PERSONAL','黄丽',  '110101199108307890','13901000007','ACTIVE',0,NOW(),NOW()),
('TNT008','COMPANY', '博远科技有限公司',NULL,'13901000008','ACTIVE',0,NOW(),NOW()),
('TNT009','COMPANY', '鸿达贸易股份公司',NULL,'13901000009','ACTIVE',0,NOW(),NOW()),
('TNT010','PERSONAL','周鑫',  '110101199602018901','13901000010','ACTIVE',0,NOW(),NOW());

-- 公司补全联系人
UPDATE tenants SET social_credit_code='91110105MA0123456X', contact_name='李总', contact_phone='13901000088' WHERE id='TNT008';
UPDATE tenants SET social_credit_code='91110105MA0654321Y', contact_name='王总', contact_phone='13901000099' WHERE id='TNT009';

-- ============================================================
-- 合同（8 个 ACTIVE）
-- ============================================================
INSERT INTO contracts (id, no, tenant_id, unit_id, status, start_date, end_date, monthly_rent, deposit, deleted, created_at, updated_at) VALUES
('CTR001','C-2025-001','TNT001','UNIT002','ACTIVE','2025-01-01','2026-12-31', 3500.00, 7000.00, 0,NOW(),NOW()),
('CTR002','C-2025-002','TNT002','UNIT003','ACTIVE','2025-03-01','2027-02-28', 4000.00, 8000.00, 0,NOW(),NOW()),
('CTR003','C-2025-003','TNT003','UNIT005','ACTIVE','2025-06-01','2026-05-31', 5200.00,10400.00, 0,NOW(),NOW()),
('CTR004','C-2025-004','TNT004','UNIT006','ACTIVE','2025-02-15','2026-02-14', 4500.00, 9000.00, 0,NOW(),NOW()),
('CTR005','C-2025-005','TNT005','UNIT008','ACTIVE','2025-04-01','2027-03-31', 6500.00,13000.00, 0,NOW(),NOW()),
('CTR006','C-2025-006','TNT006','UNIT009','ACTIVE','2025-07-01','2026-06-30', 5000.00,10000.00, 0,NOW(),NOW()),
('CTR007','C-2025-007','TNT007','UNIT011','ACTIVE','2025-01-15','2026-01-14', 3200.00, 6400.00, 0,NOW(),NOW()),
('CTR008','C-2025-008','TNT010','UNIT013','ACTIVE','2025-08-01','2027-07-31', 3000.00, 6000.00, 0,NOW(),NOW());

-- 同步单元状态为 OCCUPIED（已在 V2 前设置，此处幂等更新）
UPDATE units SET status='OCCUPIED' WHERE id IN ('UNIT002','UNIT003','UNIT005','UNIT006','UNIT008','UNIT009','UNIT011','UNIT013');

-- ============================================================
-- 费用项（4 个标准费用，含 TIERED 阶梯电费）
-- ============================================================
INSERT INTO fee_items (id, name, type, fixed_amount, unit_price, project_id, status, deleted, created_at, updated_at) VALUES
('FEE001','物业管理费','FIXED',   500.00,  NULL, NULL,    'ACTIVE',0,NOW(),NOW()),
('FEE002','停车费',    'BY_AREA',  NULL,   20.00, NULL,   'ACTIVE',0,NOW(),NOW()),
('FEE003','水费',      'BY_METER', NULL,    5.00, NULL,   'ACTIVE',0,NOW(),NOW()),
('FEE004','电费',      'TIERED',   NULL,    NULL, NULL,   'ACTIVE',0,NOW(),NOW());

-- 电费阶梯（0~100度: 0.60元/度，100~300度: 0.80元/度，300度以上: 1.20元/度）
INSERT INTO fee_tiers (id, fee_item_id, min_qty, max_qty, unit_price, created_at) VALUES
('FTR001','FEE004',  0.00, 100.00, 0.60, NOW()),
('FTR002','FEE004',100.00, 300.00, 0.80, NOW()),
('FTR003','FEE004',300.00,  NULL,  1.20, NOW());
