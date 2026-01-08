# Git 团队协作指南

## 一、项目负责人操作（初始化）

### 1. 初始化Git仓库并推送到远程
```bash
# 在项目根目录执行
cd volunteer-platform

# 初始化Git仓库（如果还没有）
git init

# 添加所有文件
git add .

# 首次提交
git commit -m "初始化项目：志愿者多媒体平台"

# 添加远程仓库（替换为你的仓库地址）
git remote add origin https://github.com/你的用户名/volunteer-platform.git

# 推送到远程
git push -u origin main
```

### 2. 为每个成员创建开发分支
```bash
# 创建7个成员的开发分支
git branch dev-陈力宏
git branch dev-张昊然
git branch dev-贺嘉伟
git branch dev-谢龙洋
git branch dev-孔令超
git branch dev-曹宇涵
git branch dev-梁玉杰

# 推送所有分支到远程
git push origin dev-陈力宏
git push origin dev-张昊然
git push origin dev-贺嘉伟
git push origin dev-谢龙洋
git push origin dev-孔令超
git push origin dev-曹宇涵
git push origin dev-梁玉杰
```

---

## 二、团队成员操作

### 每个成员克隆项目并切换到自己的分支

#### 陈力宏
```bash
git clone https://github.com/你的用户名/volunteer-platform.git
cd volunteer-platform
git checkout dev-陈力宏
```

#### 张昊然
```bash
git clone https://github.com/你的用户名/volunteer-platform.git
cd volunteer-platform
git checkout dev-张昊然
```

#### 贺嘉伟
```bash
git clone https://github.com/你的用户名/volunteer-platform.git
cd volunteer-platform
git checkout dev-贺嘉伟
```

#### 谢龙洋
```bash
git clone https://github.com/你的用户名/volunteer-platform.git
cd volunteer-platform
git checkout dev-谢龙洋
```

#### 孔令超
```bash
git clone https://github.com/你的用户名/volunteer-platform.git
cd volunteer-platform
git checkout dev-孔令超
```

#### 曹宇涵
```bash
git clone https://github.com/你的用户名/volunteer-platform.git
cd volunteer-platform
git checkout dev-曹宇涵
```

#### 梁玉杰
```bash
git clone https://github.com/你的用户名/volunteer-platform.git
cd volunteer-platform
git checkout dev-梁玉杰
```

---

## 三、日常开发流程

### 1. 开发前先同步最新代码
```bash
# 切换到自己的分支
git checkout dev-你的名字

# 拉取最新代码
git pull origin dev-你的名字

# 同步主分支的更新（如果需要）
git merge origin/main
```

### 2. 修改代码后提交
```bash
# 查看修改的文件
git status

# 添加修改的文件
git add .

# 提交（写清楚做了什么）
git commit -m "完成用户管理模块的增删改查功能"

# 推送到远程
git push origin dev-你的名字
```

### 3. 合并到主分支（由负责人操作）
```bash
# 切换到主分支
git checkout main

# 合并成员的分支
git merge dev-陈力宏

# 解决冲突（如果有）后推送
git push origin main
```

---

## 四、各成员负责的文件（只修改这些文件）

### 陈力宏 - 系统管理
```
frontend/src/views/Users.vue
frontend/src/views/OperationLogs.vue
frontend/src/views/Login.vue
frontend/src/views/Layout.vue
frontend/src/views/Dashboard.vue
frontend/src/views/System.vue
backend/.../controller/UserController.java
backend/.../controller/OperationLogController.java
backend/.../controller/AuthController.java
backend/.../controller/SystemController.java
```

### 张昊然 - 内容浏览
```
frontend/src/views/VolunteerPortal.vue (内容浏览部分)
frontend/src/views/UserPublicPortal.vue (内容浏览部分)
frontend/src/views/PublicContent.vue
frontend/src/views/PublicActivities.vue
backend/.../controller/PublicController.java
```

### 贺嘉伟 - 活动参与
```
frontend/src/views/VolunteerPortal.vue (活动参与部分)
frontend/src/views/Checkin.vue
frontend/src/views/VolunteerPublic.vue
backend/.../controller/PortalController.java
backend/.../controller/ActivityCheckinController.java
```

### 谢龙洋 - 多媒体发布+加入组织+成为志愿者
```
frontend/src/views/Layouts.vue
frontend/src/views/Broadcasts.vue
frontend/src/views/Terminals.vue
frontend/src/views/TerminalPreview.vue
frontend/src/views/ScreenPlayer.vue
frontend/src/views/VolunteerPortal.vue (加入组织部分)
frontend/src/views/UserPublicPortal.vue (成为志愿者部分)
backend/.../controller/LayoutTemplateController.java
backend/.../controller/BroadcastController.java
backend/.../controller/TerminalController.java
backend/.../controller/UserPortalController.java
```

### 孔令超 - 视频展示管理
```
frontend/src/views/Media.vue
frontend/src/views/Playlists.vue
backend/.../controller/MediaAssetController.java
backend/.../controller/PlaylistController.java
```

### 曹宇涵 - 活动管理+求助处理
```
frontend/src/views/Activities.vue
frontend/src/views/OrgActivities.vue
frontend/src/views/OrgHelpRequests.vue
backend/.../controller/ActivityController.java
backend/.../controller/OrgController.java (求助处理部分)
```

### 梁玉杰 - 内容展示管理+志愿者管理
```
frontend/src/views/Categories.vue
frontend/src/views/Content.vue
frontend/src/views/OrgVolunteers.vue
frontend/src/views/OrgDashboard.vue
frontend/src/views/OrgLayout.vue
frontend/src/views/OrgLogin.vue
backend/.../controller/MenuCategoryController.java
backend/.../controller/ContentController.java
backend/.../controller/OrgController.java (志愿者审核部分)
```

---

## 五、环境配置（每个成员都需要）

### 1. 安装环境
- JDK 17+
- Node.js 18+
- MySQL 8.0
- Maven 3.8+

### 2. 数据库配置
```sql
-- 创建数据库
CREATE DATABASE volunteer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入数据（在backend目录下）
mysql -u root -p volunteer < src/main/resources/schema.sql
mysql -u root -p volunteer < src/main/resources/init-data.sql
```

### 3. 修改数据库连接（如果密码不同）
编辑 `backend/src/main/resources/application.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/volunteer
    username: root
    password: 你的密码
```

### 4. 启动项目
```bash
# 启动后端
cd backend
mvn spring-boot:run

# 启动前端（新开终端）
cd frontend
npm install
npm run dev
```

### 5. 测试账号
| 端 | 路由 | 账号 | 密码 |
|---|------|------|------|
| 管理员端 | /login | admin | admin123 |
| 志愿者组织端 | /org/login | org1 | admin123 |
| 志愿者端 | /portal | 18890470633 | xly666 |
| 普通用户端 | /user-portal | testuser | admin123 |

---

## 六、注意事项

1. **只修改自己负责的文件**，避免冲突
2. **修改公共文件前先沟通**（如 router/index.ts, App.vue）
3. **每天开始工作前先 git pull**
4. **提交前先测试功能是否正常**
5. **提交信息要写清楚做了什么**
