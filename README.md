# 设备报修系统（DGH）

面向校园/社区的设备报修系统演示项目，提供用户端报修、维修端接单与管理端调度能力，支持工单全流程可追踪。

## 功能概览

- 用户端：文字 + 图片链接 + 定位报修、工单状态查看、评价反馈
- 维修端：接单处理、状态更新（待处理/处理中/已完成）、打卡签到
- 管理端：工单派发、人员调度、数据统计（维修时效/高频故障）
- 自动派单：通过环境变量配置默认维修员
- 超时提醒：服务端每小时扫描超时未完成工单并输出提醒日志
- 多角色权限：前端提供角色入口展示（普通用户、维修员、管理员）

## 技术选型

- 前端：Vue 3 + TypeScript + Vant + Vite
- 后端：NestJS + TypeORM (SQLite)
- 数据库：默认 SQLite（便于本地运行），可扩展到 MySQL；缓存/消息机制可扩展 Redis

## 本地运行

### 1. 启动后端

```bash
cd backend
npm install
npm run start:dev
```

默认启动在 `http://localhost:3000`，并自动创建 `data.sqlite`。

#### 自动派单（可选）

```bash
AUTO_ASSIGN_NAME=张师傅 AUTO_ASSIGN_CONTACT=13800000000 npm run start:dev
```

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173` 查看移动端页面。

## API 简要说明

- `POST /tickets` 提交报修
- `GET /tickets` 获取工单列表
- `PATCH /tickets/:id/status` 更新状态
- `PATCH /tickets/:id/assign` 管理员派单
- `POST /tickets/:id/checkins` 维修签到
- `POST /tickets/:id/feedback` 评价反馈
- `GET /tickets/overdue/list` 查询超时工单
- `GET /stats` 数据统计

## 目录结构

```
.
├── backend         # NestJS 服务端
├── frontend        # Vue3 移动端
├── docs            # 说明文档
└── README.md
```

## 后续扩展建议

- 引入登录鉴权与权限控制（RBAC）
- 将 SQLite 替换为 MySQL 并接入 Redis 缓存
- 接入消息通知（短信/企业微信）提升提醒触达

