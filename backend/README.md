# Volunteer Media Platform Backend

Spring Boot 3 + MyBatis Plus + Redis + MySQL skeleton.

## 运行
1. 修改 `src/main/resources/application.yml` 中的数据库和 Redis 配置。
2. 创建数据库 `volunteer`（或调整连接字符串）。
3. 启动：
```bash
mvn spring-boot:run
```
4. 初始化数据：启动时自动创建默认账号 `admin/admin123`（请尽快修改）。
5. 健康检查：`GET http://localhost:8080/api/ping` → `pong`。
6. 文件上传：默认保存到 `uploads` 目录（可通过 `app.storage.root` 配置），静态访问 `/uploads/**`。

## 结构
- `common/ApiResponse` 统一响应
- `config/` 安全配置、MyBatis Plus 分页、数据初始化
- `controller/` 健康检查、认证、分类、内容（关键词/详情）、媒体（上传/下载/删除）、布局、播放列表（条目查询/更新/删除）、终端（绑定播放列表、心跳记录查询/播放拉取）、插播广播、操作日志、公共展示接口、公用系统/备份接口、志愿者、活动
- `entity/` 用户/角色/菜单/内容/媒体资产/播放列表/终端/布局/插播/心跳/操作日志等实体
- `mapper/` MyBatis Plus mapper 接口
- `dto/` 登录、资源、内容、播放列表、终端、插播等请求 DTO
- `security/` JWT 工具与过滤器
- `schema.sql` 数据库建表脚本

## 说明
- 操作日志：拦截 `/api/**`（除 ping/auth），写入 `operation_log`，`/api/ops/logs` 为 admin 权限查看。
- 安全：`/api/ops/**` 需 ADMIN 权限，其余接口需认证。JWT 在登录返回，角色存储于 token。
- 插播：`/api/broadcasts` 创建/查询，`/api/broadcasts/active` 按终端查当前有效插播。
