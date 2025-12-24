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
文件上传：默认存储到后端工作目录下 `uploads/`（可配置 `app.storage.root`），访问路径 `/uploads/**`。

### 前端
```bash
cd frontend
npm install
npm run dev
```
默认代理 `/api` 到本地后端。
页面：登录、仪表盘、资源管理（上传/删除/列表）、分类管理、内容管理、播放列表（创建/编辑/删除）、终端管理（心跳查看、列表分发）。

## 后续待办
- 后端：补充 Flyway/Liquibase，完善权限、播放计划/终端心跳日志、资源删除物理文件等。
- 前端：播放列表构建、终端管理、日志与系统管理页面，美化布局与表单校验。
