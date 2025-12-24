# 志愿者服务活动中心多媒体展示系统（骨架）

技术栈：Spring Boot 3 + MyBatis Plus + Redis + MySQL；前端 Vue3 + Element Plus + Vite。

## 目录
- `backend/` 后端骨架
- `frontend/` 前端骨架

## 快速开始
### 后端
```bash
cd backend
mvn spring-boot:run
```
运行前请在 `src/main/resources/application.yml` 配置数据库/Redis，并建库 `volunteer`（可先执行 `schema.sql` 初始化表）。

健康检查：`GET http://localhost:8080/api/ping` → `pong`。
默认账号：`admin/admin123`（启动时自动初始化，生产请修改）。

### 前端
```bash
cd frontend
npm install
npm run dev
```
默认代理 `/api` 到本地后端。

## 后续待办
- 后端：补充建表 SQL/Flyway，登录/JWT 鉴权，资源上传/内容/播放列表/终端等接口与 mapper/service。
- 前端：资源上传/列表、播放列表构建、终端管理、日志与系统管理页面。
