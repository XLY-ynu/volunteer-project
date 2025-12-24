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
- `controller/` 健康检查、认证、分类、内容、媒体、播放列表、终端
- `entity/` 用户/角色/菜单/内容/媒体资产/播放列表/终端等实体
- `mapper/` MyBatis Plus mapper 接口
- `dto/` 登录、资源、内容、播放列表、终端等请求 DTO
- `security/` JWT 工具与过滤器
- `schema.sql` 数据库建表脚本

后续需要添加：
- Flyway/Liquibase 管理 schema
- 资源文件上传存储（目前仅保存 URL）
- 更多校验与审计日志
