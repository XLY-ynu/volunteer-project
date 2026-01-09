# 志愿者服务活动中心多媒体展示系统 - 环境配置手册

## 一、环境要求

### 必需软件

| 软件 | 版本要求 | 说明 |
|-----|---------|------|
| JDK | 17+ (17/21均可) | 后端运行环境 |
| Node.js | 18+ | 前端运行环境 |
| MySQL | 8.0+ | 数据库 |
| Maven | 3.8+ | 后端构建工具 |

### 开发工具（任选其一）

- IntelliJ IDEA (推荐)
- VS Code
- Eclipse

---

## 二、环境安装

### 2.1 JDK 安装

**Windows:**
1. 下载 JDK 17: https://adoptium.net/
2. 安装后配置环境变量:
   - `JAVA_HOME` = JDK安装路径
   - `Path` 添加 `%JAVA_HOME%\bin`
3. 验证: `java -version`

### 2.2 Node.js 安装

1. 下载: https://nodejs.org/ (LTS版本)
2. 安装后验证: `node -v` 和 `npm -v`

### 2.3 MySQL 安装

1. 下载: https://dev.mysql.com/downloads/mysql/
2. 安装时设置 root 密码
3. 验证: `mysql -u root -p`

---

## 三、数据库配置

### 3.1 创建数据库

打开 MySQL 命令行或 Navicat/MySQL Workbench，执行：

```sql
CREATE DATABASE IF NOT EXISTS volunteer 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 3.2 初始化表结构和数据

执行项目中的 SQL 文件（包含表结构和初始数据）：

```bash
# 方式1: 命令行
mysql -u root -p volunteer < backend/src/main/resources/volunteer_complete.sql

# 方式2: 在 MySQL Workbench 或 Navicat 中打开 volunteer_complete.sql 执行
```

### 3.3 修改数据库连接配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/volunteer?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
    username: root
    password: 你的MySQL密码  # 修改为你的密码
```

---

## 四、后端启动

### 4.1 使用 IDEA

1. 用 IDEA 打开 `backend` 文件夹
2. 等待 Maven 自动下载依赖
3. 找到 `VolunteerApplication.java`
4. 右键 → Run

### 4.2 使用 VS Code

1. 安装 Java 扩展包
2. 打开 `backend` 文件夹
3. 按 F5 运行

### 4.3 使用命令行

```bash
cd backend
mvn clean spring-boot:run
```

**启动成功标志:** 控制台显示 `Started VolunteerApplication in x.xxx seconds`

**后端地址:** http://localhost:8080

---

## 五、前端启动

### 5.1 安装依赖

```bash
cd frontend
npm install
```

如果下载慢，可以使用淘宝镜像：
```bash
npm config set registry https://registry.npmmirror.com
npm install
```

### 5.2 启动开发服务器

```bash
npm run dev
```

**前端地址:** http://localhost:5173

---

## 六、访问系统

### 四端架构说明

本系统采用四端架构设计：

| 端 | 说明 | 主要功能 |
|---|------|---------|
| 管理员端 | 系统管理 | 用户管理、权限管理、操作日志 |
| 志愿者组织端 | 组织管理 | 内容管理、媒体管理、活动管理、志愿者审核、求助处理 |
| 志愿者端 | 志愿者服务 | 内容浏览、媒体观看、活动参与、加入组织 |
| 普通用户端 | 公众服务 | 内容浏览、媒体观看、发布求助 |

### 访问地址

| 端 | 地址 | 默认账号 |
|---|------|---------|
| 管理员后台 | http://localhost:5173/login | admin / admin123 |
| 组织管理端 | http://localhost:5173/org/login | org1 / admin123 |
| 志愿者门户 | http://localhost:5173/portal | 手机号注册后登录 |
| 普通用户门户 | http://localhost:5173/user-portal | 注册后登录 |

### 角色说明

| 角色代码 | 角色名称 | 权限范围 |
|---------|---------|---------|
| ADMIN | 管理员 | 系统管理（用户、权限、日志） |
| ORG | 志愿者组织 | 内容、媒体、活动、志愿者、求助管理 |
| VOLUNTEER | 志愿者 | 浏览内容、参与活动、加入组织 |
| USER | 普通用户 | 浏览内容、发布求助 |

---

## 七、常见问题

### Q1: Maven 下载依赖很慢

配置阿里云镜像，编辑 `~/.m2/settings.xml`：

```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/central</url>
  </mirror>
</mirrors>
```

### Q2: 端口被占用

- 后端 8080 端口: 修改 `application.yml` 中的 `server.port`
- 前端 5173 端口: 修改 `vite.config.ts` 中的 `server.port`

### Q3: 数据库连接失败

1. 确认 MySQL 服务已启动
2. 确认用户名密码正确
3. 确认数据库 `volunteer` 已创建

### Q4: IDEA 提示 JDK 版本不对

1. File → Project Structure → Project SDK → 选择 JDK 17+
2. File → Settings → Build → Compiler → Java Compiler → Target bytecode version → 17

### Q5: 前端编译报错

```bash
# 删除 node_modules 重新安装
rm -rf node_modules
rm package-lock.json
npm install
```

---

## 八、项目结构

```text
volunteer-project/
├── backend/                          # 后端 SpringBoot 项目
│   ├── src/main/java/               # Java 源码
│   │   └── com/example/volunteer/
│   │       ├── controller/          # 控制器
│   │       ├── service/             # 服务层
│   │       ├── mapper/              # 数据访问层
│   │       ├── entity/              # 实体类
│   │       ├── dto/                 # 数据传输对象
│   │       ├── config/              # 配置类
│   │       └── security/            # 安全相关
│   ├── src/main/resources/          # 配置文件
│   │   ├── application.yml          # 主配置文件
│   │   └── volunteer_complete.sql   # 数据库完整脚本
│   └── pom.xml                      # Maven 配置
├── frontend/                         # 前端 Vue3 项目
│   ├── src/
│   │   ├── views/                   # 页面组件
│   │   ├── components/              # 公共组件
│   │   ├── api/                     # API 接口
│   │   ├── router/                  # 路由配置
│   │   └── stores/                  # 状态管理
│   ├── package.json                 # npm 配置
│   └── vite.config.ts               # Vite 配置
├── README.md                         # 项目说明
└── SETUP_GUIDE.md                    # 本手册
```

---

## 九、技术栈

| 层级 | 技术 |
|-----|------|
| 后端框架 | SpringBoot 3.2 |
| ORM | MyBatis-Plus 3.5 |
| 安全 | Spring Security + JWT |
| 数据库 | MySQL 8.0 |
| 前端框架 | Vue 3 + TypeScript |
| UI组件 | Element Plus |
| 构建工具 | Vite |

---

## 十、联系方式

如有问题，请联系项目负责人。
