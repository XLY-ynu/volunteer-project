# 志愿者多媒体平台 - 团队代码分发

## ✅ 每个成员的代码现在可以独立运行！

每个成员文件夹中都包含一个完整的 `frontend/` 项目，可以独立运行。

## 运行方式

```bash
# 进入成员的 frontend 目录
cd 团队分发/1-陈力宏-系统管理/frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 前提条件

- 后端服务需要在 http://localhost:8080 运行
- 数据库需要正常连接

## 成员项目端口分配

| 序号 | 成员 | 模块 | 端口 | 测试账号 |
|------|------|------|------|----------|
| 1 | 陈力宏 | 系统管理 | 5173 | admin / admin123 |
| 2 | 张昊然 | 内容浏览 | 5179 | testuser / admin123 |
| 3 | 贺嘉伟 | 活动参与 | 5178 | 18890470633 / xly666 |
| 4 | 谢龙洋 | 多媒体发布 | 5174 | org1 / admin123 |
| 5 | 孔令超 | 视频展示管理 | 5175 | org1 / admin123 |
| 6 | 曹宇涵 | 活动管理 | 5176 | org1 / admin123 |
| 7 | 梁玉杰 | 内容展示管理 | 5177 | org1 / admin123 |

## 目录结构

```
团队分发/
├── 1-陈力宏-系统管理/
│   ├── frontend/          # 可独立运行的前端项目
│   │   ├── package.json
│   │   ├── vite.config.ts
│   │   ├── src/
│   │   │   ├── views/     # 负责的Vue组件
│   │   │   ├── router/    # 路由配置
│   │   │   ├── api/       # API接口
│   │   │   └── stores/    # 状态管理
│   │   └── README.md      # 运行说明
│   ├── 前端代码/          # 原始代码文件（参考用）
│   ├── 后端代码/          # 后端控制器（参考用）
│   └── README.md
│
├── 2-张昊然-内容浏览/
│   └── frontend/          # 可独立运行
│
├── 3-贺嘉伟-活动参与/
│   └── frontend/          # 可独立运行
│
├── 4-谢龙洋-多媒体发布/
│   └── frontend/          # 可独立运行
│
├── 5-孔令超-视频展示管理/
│   └── frontend/          # 可独立运行
│
├── 6-曹宇涵-活动管理/
│   └── frontend/          # 可独立运行
│
└── 7-梁玉杰-内容展示管理/
    └── frontend/          # 可独立运行
```

## 代码作者标记

完整项目（backend + frontend）中的代码文件已添加作者注释标记：
- 前端Vue文件：`<!-- @Author: xxx -->`
- 后端Java文件：`/** @Author: xxx */`

## 技术栈

- 前端：Vue 3 + TypeScript + Element Plus + Vite
- 后端：Spring Boot 3 + MyBatis Plus + MySQL
- 数据库：MySQL 8.0

## 后端启动

所有成员的前端项目都依赖同一个后端服务：

```bash
cd backend
mvn spring-boot:run
# 后端运行在 http://localhost:8080
```

## 数据库配置

### 快速初始化（新成员必看）

1. 进入 `团队分发/database/` 目录
2. 执行 `volunteer_complete.sql` 脚本

```bash
# 命令行方式
mysql -u root -p < 团队分发/database/volunteer_complete.sql
```

或者使用 MySQL Workbench / Navicat 打开并执行该文件。

### 修改后端配置

编辑 `backend/src/main/resources/application.yml`，修改数据库密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/volunteer?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 你的MySQL密码    # ← 修改为你的密码
```

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 组织端 | org1 | admin123 |

详细说明请查看：`团队分发/database/数据库配置说明.md`
