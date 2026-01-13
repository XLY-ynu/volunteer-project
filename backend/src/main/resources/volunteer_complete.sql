-- ============================================
-- 志愿者服务活动中心多媒体展示系统
-- 完整数据库初始化脚本（一键执行版）
-- 更新日期：2026-01-13
-- ============================================
-- 使用方法:
--   方式1: mysql -u root -p < volunteer_complete.sql
--   方式2: 在MySQL Workbench中打开并执行此文件
-- ============================================
-- 注意: 此脚本会删除并重建 volunteer 数据库
-- ============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================
-- 第一部分：创建数据库
-- ============================================

-- 删除已存在的数据库（如果需要保留数据请注释此行）
DROP DATABASE IF EXISTS volunteer;

-- 创建数据库
CREATE DATABASE volunteer 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换到 volunteer 数据库
USE volunteer;

-- ============================================
-- 第二部分：创建表结构
-- ============================================

-- 用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(64),
    role_code VARCHAR(32) NOT NULL,
    enabled TINYINT(1) DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME
);

-- 角色表
CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    description VARCHAR(255)
);

-- 用户角色关联表
CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 菜单分类表
CREATE TABLE menu_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    parent_id BIGINT,
    sort_order INT DEFAULT 0
);

-- 内容项表
CREATE TABLE content_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    summary VARCHAR(255),
    body TEXT,
    cover_url VARCHAR(255),
    published TINYINT(1) DEFAULT 0,
    publish_time DATETIME,
    created_at DATETIME,
    updated_at DATETIME,
    headline TINYINT(1) DEFAULT 0,
    recommended TINYINT(1) DEFAULT 0,
    recommend_weight INT DEFAULT 0,
    sort_order INT DEFAULT 0
);

-- 内容配置表
CREATE TABLE content_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recommend_interval_sec INT DEFAULT 6,
    preview_interval_sec INT DEFAULT 10,
    recommend_count INT DEFAULT 6,
    recommend_strategy VARCHAR(16) DEFAULT 'prefer',
    updated_at DATETIME
);

-- 媒体资源表
CREATE TABLE media_asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    url VARCHAR(255) NOT NULL,
    thumb_url VARCHAR(255),
    size_bytes BIGINT,
    duration_seconds INT,
    width INT,
    height INT,
    checksum VARCHAR(128),
    created_at DATETIME,
    bitrate_kbps INT,
    frame_rate DECIMAL(6,2)
);

-- 播放列表表
CREATE TABLE playlist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    cover_url VARCHAR(255),
    layout_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME
);

-- 播放列表项表
CREATE TABLE playlist_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    playlist_id BIGINT NOT NULL,
    media_id BIGINT,
    content_id BIGINT,
    display_duration INT DEFAULT 10,
    sort_order INT DEFAULT 0,
    area_index INT DEFAULT 0
);

-- 布局表
CREATE TABLE layout (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    layout_json TEXT NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
);

-- 终端表
CREATE TABLE terminal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    group_name VARCHAR(64),
    status VARCHAR(32) DEFAULT 'offline',
    last_heartbeat DATETIME,
    attributes JSON,
    created_at DATETIME,
    updated_at DATETIME
);

-- 终端播放列表关联表
CREATE TABLE terminal_playlist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    terminal_id BIGINT NOT NULL,
    playlist_id BIGINT NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    active TINYINT(1) DEFAULT 1
);

-- 广播任务表
CREATE TABLE broadcast_job (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    media_id BIGINT,
    content_id BIGINT,
    target_group VARCHAR(64),
    target_terminal_code VARCHAR(64),
    start_time DATETIME,
    end_time DATETIME,
    status VARCHAR(32) DEFAULT 'scheduled',
    priority INT DEFAULT 0,
    queue_mode VARCHAR(32) DEFAULT 'queue'
);

-- 终端心跳表
CREATE TABLE terminal_heartbeat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    terminal_id BIGINT NOT NULL,
    status VARCHAR(32),
    created_at DATETIME
);

-- 操作日志表
CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64),
    method VARCHAR(16),
    path VARCHAR(255),
    status INT,
    created_at DATETIME
);

-- 志愿者表
CREATE TABLE volunteer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    name VARCHAR(64) NOT NULL,
    phone VARCHAR(32),
    email VARCHAR(128),
    organization VARCHAR(128),
    id_card VARCHAR(32),
    status VARCHAR(32) DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 活动表（新增 org_id 和 members_only 字段）
CREATE TABLE activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    start_time DATETIME,
    end_time DATETIME,
    capacity INT,
    checkin_code VARCHAR(32),
    org_id BIGINT,
    members_only TINYINT(1) DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_org_id (org_id)
);

-- 活动报名表
CREATE TABLE activity_signup (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    status VARCHAR(32) DEFAULT 'applied',
    checkin_time DATETIME,
    checked_in TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 志愿者站内消息表
CREATE TABLE volunteer_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    volunteer_id BIGINT NOT NULL,
    activity_id BIGINT,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    type VARCHAR(32) DEFAULT 'reminder',
    is_read TINYINT(1) DEFAULT 0,
    created_at DATETIME,
    INDEX idx_volunteer_id (volunteer_id),
    INDEX idx_activity_id (activity_id)
);

-- 门户消息已读记录表
CREATE TABLE portal_message_read (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    volunteer_id BIGINT NOT NULL,
    message_key VARCHAR(128) NOT NULL,
    read_at DATETIME,
    INDEX idx_volunteer_id (volunteer_id),
    UNIQUE KEY uk_volunteer_message (volunteer_id, message_key)
);


-- ============================================
-- 扩展功能表
-- ============================================

-- 活动签到日志表
CREATE TABLE activity_checkin_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 活动提醒日志表
CREATE TABLE activity_reminder_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    volunteer_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    reminder_type VARCHAR(32),
    channel VARCHAR(32),
    status VARCHAR(32),
    message VARCHAR(255),
    created_at DATETIME
);

-- 告警静默配置表
CREATE TABLE alert_silence (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(64),
    channel VARCHAR(32),
    start_time DATETIME,
    end_time DATETIME,
    enabled TINYINT(1) DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME
);

-- 告警订阅表
CREATE TABLE alert_subscription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(64),
    channel VARCHAR(32),
    target VARCHAR(128),
    enabled TINYINT(1) DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME
);

-- 布局区域内容池表
CREATE TABLE layout_area_pool (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    layout_id BIGINT NOT NULL,
    area_index INT NOT NULL,
    media_id BIGINT,
    content_id BIGINT,
    display_duration INT DEFAULT 10,
    sort_order INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME
);

-- 布局模板表
CREATE TABLE layout_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    layout_json TEXT,
    tags VARCHAR(255),
    cover_url VARCHAR(255),
    builtin TINYINT(1) DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME
);

-- 布局模板历史表
CREATE TABLE layout_template_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    layout_json LONGTEXT,
    tags VARCHAR(255),
    cover_url VARCHAR(255),
    version INT DEFAULT 1,
    created_at DATETIME
);

-- 通知渠道配置表
CREATE TABLE notification_channel_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel VARCHAR(32) NOT NULL,
    config_json TEXT,
    enabled TINYINT(1) DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME
);

-- 通知日志表
CREATE TABLE notification_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel VARCHAR(32),
    target VARCHAR(128),
    title VARCHAR(128),
    content TEXT,
    status VARCHAR(32),
    created_at DATETIME,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    next_retry_at DATETIME,
    error_message VARCHAR(255),
    provider_message_id VARCHAR(128),
    updated_at DATETIME
);

-- 终端告警历史表
CREATE TABLE terminal_alert_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(64),
    total INT DEFAULT 0,
    offline INT DEFAULT 0,
    rule_threshold INT DEFAULT 0,
    channel VARCHAR(32),
    target VARCHAR(128),
    silenced TINYINT(1) DEFAULT 0,
    created_at DATETIME
);

-- 终端分组规则表
CREATE TABLE terminal_group_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(64) NOT NULL,
    offline_threshold INT DEFAULT 1,
    enabled TINYINT(1) DEFAULT 1,
    notify_channel VARCHAR(32),
    notify_target VARCHAR(128),
    created_at DATETIME,
    updated_at DATETIME
);

-- 志愿者提醒设置表
CREATE TABLE volunteer_reminder_setting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    volunteer_id BIGINT NOT NULL,
    signup_reminder TINYINT(1) DEFAULT 1,
    checkin_reminder TINYINT(1) DEFAULT 1,
    channel VARCHAR(32) DEFAULT 'sms',
    reminder_minutes INT DEFAULT 30,
    created_at DATETIME,
    updated_at DATETIME
);

-- 志愿者状态日志表
CREATE TABLE volunteer_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    volunteer_id BIGINT NOT NULL,
    status VARCHAR(32),
    remark VARCHAR(255),
    created_at DATETIME
);

-- ============================================
-- 四端架构扩展表
-- ============================================

-- 志愿者组织表
CREATE TABLE volunteer_org (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    description TEXT,
    logo_url VARCHAR(255),
    contact_name VARCHAR(64),
    contact_phone VARCHAR(32),
    contact_email VARCHAR(128),
    address VARCHAR(255),
    status VARCHAR(32) DEFAULT 'active',
    user_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);

-- 志愿者-组织关联表
CREATE TABLE volunteer_org_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    volunteer_id BIGINT NOT NULL,
    org_id BIGINT NOT NULL,
    status VARCHAR(32) DEFAULT 'pending',
    joined_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_volunteer_org (volunteer_id, org_id),
    INDEX idx_org_id (org_id)
);

-- 求助信息表
CREATE TABLE help_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    org_id BIGINT,
    title VARCHAR(128) NOT NULL,
    content TEXT,
    contact_name VARCHAR(64),
    contact_phone VARCHAR(32),
    address VARCHAR(255),
    status VARCHAR(32) DEFAULT 'pending',
    reply TEXT,
    replied_at DATETIME,
    replied_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_org_id (org_id),
    INDEX idx_status (status)
);

-- 志愿服务记录表（统计时长）
CREATE TABLE volunteer_service_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    volunteer_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    org_id BIGINT,
    service_hours DECIMAL(5,2) DEFAULT 0,
    service_date DATE,
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_volunteer_id (volunteer_id),
    INDEX idx_activity_id (activity_id)
);


-- ============================================
-- 第三部分：初始化数据
-- ============================================

-- 1. 基础角色数据（四种角色）
INSERT INTO role (code, name, description) VALUES
('ADMIN', '管理员', '系统管理员'),
('ORG', '志愿者组织', '志愿者组织管理员'),
('VOLUNTEER', '志愿者', '注册志愿者'),
('USER', '普通用户', '普通用户');

-- 2. 管理员账号 (密码: admin123)
INSERT INTO user (username, password, nickname, role_code, enabled, created_at, updated_at) VALUES
('admin', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '系统管理员', 'ADMIN', 1, NOW(), NOW());

-- 3. 示例志愿者组织账号 (密码: admin123) - 3个组织
INSERT INTO user (username, password, nickname, role_code, enabled, created_at, updated_at) VALUES
('org1', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '阳光志愿服务队', 'ORG', 1, NOW(), NOW()),
('org2', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '爱心公益协会', 'ORG', 1, NOW(), NOW()),
('org3', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '青年志愿者联盟', 'ORG', 1, NOW(), NOW());

-- 创建对应的组织信息
INSERT INTO volunteer_org (id, name, code, description, contact_name, contact_phone, contact_email, address, user_id, created_at) VALUES
(1, '阳光志愿服务队', 'sunshine', '致力于社区服务和公益活动的志愿者组织，成立于2015年，现有注册志愿者500余人', '张队长', '13800138001', 'sunshine@volunteer.org', '长沙市岳麓区麓山南路123号', 2, NOW()),
(2, '爱心公益协会', 'loveheart', '专注于扶贫助困、关爱弱势群体的公益组织，累计帮助困难家庭超过1000户', '李会长', '13800138002', 'loveheart@volunteer.org', '长沙市天心区芙蓉路456号', 3, NOW()),
(3, '青年志愿者联盟', 'youth', '由高校青年学生组成的志愿服务组织，主要开展支教、环保等公益活动', '王主席', '13800138003', 'youth@volunteer.org', '长沙市开福区中山路789号', 4, NOW());

-- 4. 六大主菜单分类
INSERT INTO menu_category (id, name, code, parent_id, sort_order) VALUES
(1, '文明XX', 'wenming', NULL, 1),
(2, 'XX志愿者APP', 'app', NULL, 2),
(3, 'XX志愿者网', 'web', NULL, 3),
(4, '雷锋热线', 'leifeng', NULL, 4),
(5, '公益活动', 'gongyi', NULL, 5),
(6, '公益广告', 'ad', NULL, 6);

-- 5. 布局模板 (6个基础布局)
INSERT INTO layout (id, name, layout_json, created_at, updated_at) VALUES
(1, '单屏全屏', '{"areas":[{"x":0,"y":0,"w":100,"h":100}]}', NOW(), NOW()),
(2, '左右分屏', '{"areas":[{"x":0,"y":0,"w":50,"h":100},{"x":50,"y":0,"w":50,"h":100}]}', NOW(), NOW()),
(3, '上下分屏', '{"areas":[{"x":0,"y":0,"w":100,"h":50},{"x":0,"y":50,"w":100,"h":50}]}', NOW(), NOW()),
(4, '三分屏', '{"areas":[{"x":0,"y":0,"w":50,"h":100},{"x":50,"y":0,"w":50,"h":50},{"x":50,"y":50,"w":50,"h":50}]}', NOW(), NOW()),
(5, '四宫格', '{"areas":[{"x":0,"y":0,"w":50,"h":50},{"x":50,"y":0,"w":50,"h":50},{"x":0,"y":50,"w":50,"h":50},{"x":50,"y":50,"w":50,"h":50}]}', NOW(), NOW()),
(6, '左一右二', '{"areas":[{"x":0,"y":0,"w":60,"h":100},{"x":60,"y":0,"w":40,"h":50},{"x":60,"y":50,"w":40,"h":50}]}', NOW(), NOW());

-- 6. 内容配置
INSERT INTO content_config (id, recommend_interval_sec, preview_interval_sec, recommend_count, recommend_strategy, updated_at) VALUES
(1, 6, 10, 10, 'weight_only', NOW());

-- 6.5 示例媒体资源（使用实际存在的文件）
-- 注意：这些文件路径对应 backend/uploads 目录下的实际文件
INSERT INTO media_asset (id, name, type, url, thumb_url, size_bytes, duration_seconds, width, height, created_at) VALUES
(1, '学雷锋活动视频.mp4', 'video', '/uploads/3708d9cd-81bf-4fcb-b8d0-6bc70592fe83.mp4', '/uploads/thumbs/thumb-fcbf83c3-5bc8-4afa-af57-8c8f5b6c64eb.jpg', 8605362, NULL, NULL, NULL, NOW()),
(2, '志愿者宣传片.mp4', 'video', '/uploads/da463aa3-9eac-486e-9329-7e879b556fac.mp4', '/uploads/thumbs/thumb-2dd9767f-6b0f-4910-8075-da4851a6f062.png', 8385489, NULL, NULL, NULL, NOW()),
(3, '图书馆志愿活动介绍视频来咯！来一起做志愿活动吧！.mp4', 'video', '/uploads/842089b6-a70d-41d3-a743-e61f8107bdde.mp4', '/uploads/thumbs/thumb-9d62b1c6-1723-46ef-bad0-e68cedf379e3.png', 8401576, NULL, NULL, NULL, NOW()),
(4, '环保公益宣传.png', 'image', '/uploads/f94e1c3b-9da0-4f42-88e3-a8177c6dee99.png', '/uploads/f94e1c3b-9da0-4f42-88e3-a8177c6dee99.png', 255730, NULL, 700, 476, NOW()),
(5, '公益广告—诚信.jpg', 'image', '/uploads/c7679cd2-0714-4c8b-bdc5-46211e8e7ca0.jpg', '/uploads/c7679cd2-0714-4c8b-bdc5-46211e8e7ca0.jpg', 122915, NULL, 1000, 707, NOW()),
(6, '志愿者活动合照.png', 'image', '/uploads/f385677c-21b5-4b6b-9f14-2da8fa16e6da.png', '/uploads/f385677c-21b5-4b6b-9f14-2da8fa16e6da.png', 641033, NULL, 800, 403, NOW()),
(7, '志愿服务中心外景.jpg', 'image', '/uploads/8673a4ea-0efd-4e68-a2c1-d1733cf8030d.jpg', '/uploads/8673a4ea-0efd-4e68-a2c1-d1733cf8030d.jpg', 143310, NULL, 1080, 856, NOW());

-- 7. 示例内容数据 (每个分类6条，共36条)

-- 分类1: 文明XX（6条）
INSERT INTO content_item (id, category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at, headline, recommended, recommend_weight) VALUES
(1, 1, '我市荣获全国文明城市称号', '经过全市人民共同努力，我市成功创建全国文明城市', 
'近日，中央文明办公布了新一届全国文明城市名单，我市凭借优异的创建成绩，成功入选全国文明城市。这是全市人民共同努力的结果，也是城市文明程度的重要体现。',
'https://picsum.photos/seed/city1/400/300', 1, NOW(), NOW(), NOW(), 1, 1, 100),
(2, 1, '文明交通志愿服务在行动', '千名志愿者走上街头，倡导文明出行', 
'为进一步提升市民文明交通意识，我市组织开展文明交通志愿服务月活动。千名志愿者走上街头，在主要路口开展文明劝导。',
'https://picsum.photos/seed/traffic1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 80),
(3, 1, '社区文明实践站揭牌成立', '打通服务群众最后一公里', 
'今日，我市首批10个新时代文明实践站正式揭牌成立，将为社区居民提供更加便捷的志愿服务。',
'https://picsum.photos/seed/community1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0),
(19, 1, '文明城市创建工作推进会召开', '部署下一阶段文明创建重点工作', 
'市委召开文明城市创建工作推进会，总结前期工作成效，部署下一阶段重点任务，确保创建工作取得实效。',
'https://picsum.photos/seed/meeting1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0),
(20, 1, '文明家庭评选活动启动', '寻找身边的文明家庭典范', 
'为弘扬家庭美德，传承良好家风，我市启动年度文明家庭评选活动，欢迎广大市民积极推荐。',
'https://picsum.photos/seed/family1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 60),
(21, 1, '文明餐桌行动深入推进', '光盘行动成为新风尚', 
'我市持续推进文明餐桌行动，倡导节约粮食、杜绝浪费，光盘行动已成为市民新风尚。',
'https://picsum.photos/seed/table1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0);

-- 分类2: XX志愿者APP（6条）
INSERT INTO content_item (id, category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at, headline, recommended, recommend_weight) VALUES
(4, 2, '志愿者APP全新上线', '一键报名，轻松参与志愿服务', 
'志愿者APP是我市官方志愿服务移动平台，集活动报名、签到打卡、时长统计于一体，让志愿服务更加便捷。',
'https://picsum.photos/seed/app1/400/300', 1, NOW(), NOW(), NOW(), 1, 1, 90),
(5, 2, 'APP新功能：活动地图上线', '附近志愿活动一目了然', 
'志愿者APP新增活动地图功能，可以查看附近正在进行的志愿活动，方便志愿者就近参与。',
'https://picsum.photos/seed/map1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 70),
(6, 2, '本周热门志愿招募', '多个志愿岗位等你来', 
'本周新增志愿岗位：社区助老服务、图书馆导读、环保宣传等，欢迎志愿者踊跃报名。',
'https://picsum.photos/seed/recruit1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0),
(22, 2, 'APP使用教程：如何报名活动', '手把手教你使用志愿者APP', 
'本教程详细介绍如何使用志愿者APP报名志愿活动，包括注册、登录、浏览活动、报名签到等步骤。',
'https://picsum.photos/seed/tutorial1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 65),
(23, 2, 'APP版本更新公告', '新版本带来更多实用功能', 
'志愿者APP发布2.0版本，新增消息推送、活动评价、证书下载等功能，欢迎更新体验。',
'https://picsum.photos/seed/update1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0),
(24, 2, '志愿者积分商城上线', '用积分兑换精美礼品', 
'志愿者APP积分商城正式上线，志愿者可以用服务积分兑换各类精美礼品，感谢您的付出。',
'https://picsum.photos/seed/mall1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 75);

-- 分类3: XX志愿者网（6条）
INSERT INTO content_item (id, category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at, headline, recommended, recommend_weight) VALUES
(7, 3, '志愿者网平台介绍', '官方志愿服务信息发布平台', 
'志愿者网是我市志愿服务工作的官方门户网站，提供志愿者注册、活动发布、时长查询等功能。',
'https://picsum.photos/seed/web1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0),
(8, 3, '2025年度优秀志愿者表彰', '百名优秀志愿者受表彰', 
'在2025年度志愿服务总结大会上，100名优秀志愿者受到表彰，他们用实际行动诠释了志愿精神。',
'https://picsum.photos/seed/award1/400/300', 1, NOW(), NOW(), NOW(), 1, 1, 85),
(9, 3, '志愿服务专题：关爱空巢老人', '用爱心温暖每一位老人', 
'关爱空巢老人专题活动持续开展中，志愿者们定期走访社区空巢老人，送去温暖和关爱。',
'https://picsum.photos/seed/elderly1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 70),
(25, 3, '志愿者注册指南', '三步完成志愿者注册', 
'本指南详细介绍如何在志愿者网完成注册，成为一名光荣的志愿者，开启您的志愿服务之旅。',
'https://picsum.photos/seed/guide1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0),
(26, 3, '志愿服务时长证明开具说明', '如何获取志愿服务证明', 
'志愿者可通过志愿者网在线申请志愿服务时长证明，用于升学、就业等场景。',
'https://picsum.photos/seed/cert1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 60),
(27, 3, '志愿者培训课程上线', '提升志愿服务专业技能', 
'志愿者网推出系列在线培训课程，涵盖急救知识、心理辅导、沟通技巧等内容，欢迎学习。',
'https://picsum.photos/seed/course1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0);

-- 分类4: 雷锋热线（6条）
INSERT INTO content_item (id, category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at, headline, recommended, recommend_weight) VALUES
(10, 4, '雷锋热线服务指南', '24小时志愿服务热线', 
'雷锋热线是我市志愿服务求助与帮扶的桥梁，拨打热线即可获得志愿服务帮助，全天候为您服务。',
'https://picsum.photos/seed/hotline1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 80),
(11, 4, '热线帮扶案例：为独居老人送温暖', '志愿者上门帮助独居老人', 
'张奶奶今年82岁，独居在家。通过雷锋热线求助后，志愿者定期上门帮助，让老人感受到社会的温暖。',
'https://picsum.photos/seed/help1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 75),
(12, 4, '品牌活动：学雷锋志愿服务月', '传承雷锋精神，践行志愿服务', 
'每年3月是学雷锋志愿服务月，全市开展形式多样的志愿服务活动，传承和弘扬雷锋精神。',
'https://picsum.photos/seed/leifeng1/400/300', 1, NOW(), NOW(), NOW(), 1, 1, 90),
(28, 4, '热线帮扶案例：助力残疾人就业', '志愿者帮助残疾人找到工作', 
'小李因意外导致腿部残疾，通过雷锋热线求助后，志愿者帮助他联系企业，成功找到适合的工作岗位。',
'https://picsum.photos/seed/job1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0),
(29, 4, '雷锋热线年度工作报告', '全年接听求助电话超万次', 
'雷锋热线2025年度工作报告显示，全年共接听求助电话12580次，成功帮扶案例8960件。',
'https://picsum.photos/seed/report1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0),
(30, 4, '热线志愿者招募', '加入雷锋热线志愿者团队', 
'雷锋热线现招募热线接听志愿者，要求普通话标准、有爱心耐心，欢迎有志之士加入。',
'https://picsum.photos/seed/recruit2/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 65);

-- 分类5: 公益活动（6条）
INSERT INTO content_item (id, category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at, headline, recommended, recommend_weight) VALUES
(13, 5, '让轮椅飞：无障碍出行公益行动', '帮助残障人士实现出行梦想', 
'让轮椅飞是我市品牌公益项目，旨在帮助残障人士无障碍出行，已累计服务残障人士超过5000人次。',
'https://picsum.photos/seed/wheelchair1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 85),
(14, 5, '名师一堂课：公益教育进社区', '优质教育资源惠及更多家庭', 
'名师一堂课邀请优秀教师走进社区，为孩子们提供免费辅导，让优质教育资源惠及更多家庭。',
'https://picsum.photos/seed/teacher1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 80),
(15, 5, '睡前讲故事：陪伴留守儿童成长', '用故事温暖童年', 
'睡前讲故事项目通过视频连线，为留守儿童讲述睡前故事，用爱心陪伴孩子们健康成长。',
'https://picsum.photos/seed/story1/400/300', 1, NOW(), NOW(), NOW(), 1, 1, 95),
(31, 5, '爱心义卖：为山区孩子筹集学费', '一份爱心，一份希望', 
'爱心义卖活动成功举办，共筹集善款15万元，将用于资助山区贫困学生完成学业。',
'https://picsum.photos/seed/sale1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0),
(32, 5, '绿色环保：植树造林公益行动', '种下一棵树，收获一片绿', 
'植树造林公益行动在郊区成功举办，500名志愿者共种植树苗3000余株，为城市增添绿色。',
'https://picsum.photos/seed/tree1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 70),
(33, 5, '暖冬行动：为环卫工人送温暖', '致敬城市美容师', 
'暖冬行动为全市环卫工人送去保暖物资和热饮，感谢他们为城市清洁付出的辛勤劳动。',
'https://picsum.photos/seed/warm1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0);

-- 分类6: 公益广告（6条）
INSERT INTO content_item (id, category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at, headline, recommended, recommend_weight) VALUES
(16, 6, '图说我们的价值观：诚信篇', '诚信是金，立身之本', 
'诚信是中华民族的传统美德，是社会主义核心价值观的重要内容。让我们共同践行诚信，建设诚信社会。',
'https://picsum.photos/seed/honest1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 75),
(17, 6, '讲文明树新风：文明餐桌', '光盘行动，从我做起', 
'珍惜粮食，杜绝浪费。让我们一起践行光盘行动，做文明餐桌的践行者。',
'https://picsum.photos/seed/food1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0),
(18, 6, '公益广告：关爱未成年人', '呵护成长，守护未来', 
'未成年人是祖国的花朵，让我们共同关爱未成年人健康成长，为他们创造良好的成长环境。',
'https://picsum.photos/seed/children1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 80),
(34, 6, '图说我们的价值观：友善篇', '与人为善，和谐相处', 
'友善是社会主义核心价值观的重要内容，让我们以友善之心对待他人，共建和谐社会。',
'https://picsum.photos/seed/kind1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0),
(35, 6, '讲文明树新风：文明旅游', '文明出行，从我做起', 
'文明旅游，人人有责。让我们做文明游客，爱护环境，尊重当地风俗，展现良好形象。',
'https://picsum.photos/seed/travel1/400/300', 1, NOW(), NOW(), NOW(), 0, 1, 65),
(36, 6, '公益广告：保护环境', '绿水青山就是金山银山', 
'保护环境，人人有责。让我们从身边小事做起，节约资源，减少污染，共建美丽家园。',
'https://picsum.photos/seed/env1/400/300', 1, NOW(), NOW(), NOW(), 0, 0, 0);

-- 8. 示例志愿活动 (12个示例活动，org1有6个活动)
INSERT INTO activity (id, title, description, location, start_time, end_time, capacity, checkin_code, org_id, members_only, created_at, updated_at) VALUES
(1, '情暖三湘·志愿同行', '聚焦老年人尤其是空巢、独居老人的急难愁盼问题，开展慰问陪护、清洁卫生等志愿服务。', 
'敬老院、养老院等', '2026-01-15 09:00:00', '2026-01-15 17:00:00', 50, '382388', 1, 0, NOW(), NOW()),
(2, '环保志愿行动', '组织志愿者参与城市环境清洁、垃圾分类宣传等活动，提升市民环保意识。', 
'市民广场', '2026-02-01 08:30:00', '2026-02-01 12:00:00', 30, '123456', 1, 0, NOW(), NOW()),
(3, '组织内部培训', '仅限阳光志愿服务队成员参与的内部培训活动，学习志愿服务技能。', 
'组织会议室', '2026-02-15 14:00:00', '2026-02-15 17:00:00', 20, '888888', 1, 1, NOW(), NOW()),
(4, '爱心助学行动', '为山区贫困学生捐赠学习用品，开展一对一帮扶活动。', 
'希望小学', '2026-02-20 09:00:00', '2026-02-20 16:00:00', 40, '456789', 2, 0, NOW(), NOW()),
(5, '社区义诊服务', '联合医院专家为社区居民提供免费健康检查和医疗咨询服务。', 
'幸福社区服务中心', '2026-03-01 08:00:00', '2026-03-01 12:00:00', 25, '567890', 2, 0, NOW(), NOW()),
(6, '青年支教活动', '组织大学生志愿者前往乡村学校开展为期一周的支教活动。', 
'湘西土家族苗族自治州', '2026-03-10 08:00:00', '2026-03-16 18:00:00', 15, '678901', 3, 0, NOW(), NOW()),
(7, '文明交通劝导', '在主要交通路口开展文明交通劝导，引导市民遵守交通规则。', 
'五一广场周边路口', '2026-03-20 07:30:00', '2026-03-20 11:30:00', 60, '789012', 1, 0, NOW(), NOW()),
(8, '关爱留守儿童', '走进农村，为留守儿童送去温暖和关爱，开展心理辅导和课业辅导。', 
'宁乡市花明楼镇', '2026-04-05 09:00:00', '2026-04-05 17:00:00', 35, '890123', 2, 0, NOW(), NOW()),
-- 新增org1的活动（活动9,10为org1）
(9, '社区便民服务日', '为社区居民提供免费理发、家电维修、法律咨询等便民服务。', 
'阳光社区服务中心', '2026-02-25 09:00:00', '2026-02-25 16:00:00', 40, '901234', 1, 0, NOW(), NOW()),
(10, '春节慰问孤寡老人', '春节期间走访慰问社区孤寡老人，送去节日祝福和慰问品。', 
'各社区孤寡老人家中', '2026-01-28 09:00:00', '2026-01-28 17:00:00', 30, '012345', 1, 0, NOW(), NOW()),
-- 新增其他组织的活动
(11, '图书馆志愿服务', '协助图书馆进行图书整理、读者引导等服务工作。', 
'市图书馆', '2026-03-15 09:00:00', '2026-03-15 17:00:00', 20, '111222', 2, 0, NOW(), NOW()),
(12, '大学生志愿者培训', '针对新加入的大学生志愿者开展志愿服务理念和技能培训。', 
'青年志愿者联盟培训中心', '2026-03-25 14:00:00', '2026-03-25 17:00:00', 50, '333444', 3, 1, NOW(), NOW());

-- 9. 示例志愿者账号和数据 (密码: admin123)
-- 志愿者用户账号（16个志愿者）
INSERT INTO user (id, username, password, nickname, role_code, enabled, created_at, updated_at) VALUES
(5, '13800001111', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '张三', 'VOLUNTEER', 1, NOW(), NOW()),
(6, '13800002222', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '李四', 'VOLUNTEER', 1, NOW(), NOW()),
(7, '13800003333', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '王五', 'VOLUNTEER', 1, NOW(), NOW()),
(8, '13800004444', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '赵六', 'VOLUNTEER', 1, NOW(), NOW()),
(9, '13800005555', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '孙七', 'VOLUNTEER', 1, NOW(), NOW()),
(10, '13800006666', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '周八', 'VOLUNTEER', 1, NOW(), NOW()),
(11, '13800007777', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '吴九', 'VOLUNTEER', 1, NOW(), NOW()),
(12, '13800008888', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '郑十', 'VOLUNTEER', 1, NOW(), NOW()),
-- 新增8个志愿者用户
(19, '13800009999', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '陈一', 'VOLUNTEER', 1, NOW(), NOW()),
(20, '13800010000', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '林二', 'VOLUNTEER', 1, NOW(), NOW()),
(21, '13800011111', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '黄三', 'VOLUNTEER', 1, NOW(), NOW()),
(22, '13800012222', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '刘四', 'VOLUNTEER', 1, NOW(), NOW()),
(23, '13800013333', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '杨五', 'VOLUNTEER', 1, NOW(), NOW()),
(24, '13800014444', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '何六', 'VOLUNTEER', 1, NOW(), NOW()),
(25, '13800015555', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '罗七', 'VOLUNTEER', 1, NOW(), NOW()),
(26, '13800016666', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '高八', 'VOLUNTEER', 1, NOW(), NOW());

-- 志愿者详细信息（16个志愿者）
INSERT INTO volunteer (id, user_id, name, phone, email, organization, id_card, status, created_at) VALUES
(1, 5, '张三', '13800001111', 'zhangsan@example.com', '阳光志愿服务队', '430102199001011234', 'approved', NOW()),
(2, 6, '李四', '13800002222', 'lisi@example.com', '阳光志愿服务队', '430102199002022345', 'approved', NOW()),
(3, 7, '王五', '13800003333', 'wangwu@example.com', NULL, '430102199003033456', 'pending', NOW()),
(4, 8, '赵六', '13800004444', 'zhaoliu@example.com', '阳光志愿服务队', '430102199504044567', 'approved', NOW()),
(5, 9, '孙七', '13800005555', 'sunqi@example.com', '爱心公益协会', '430102199605055678', 'approved', NOW()),
(6, 10, '周八', '13800006666', 'zhouba@example.com', '爱心公益协会', '430102199706066789', 'approved', NOW()),
(7, 11, '吴九', '13800007777', 'wujiu@example.com', NULL, '430102199807077890', 'rejected', NOW()),
(8, 12, '郑十', '13800008888', 'zhengshi@example.com', '青年志愿者联盟', '430102199908088901', 'approved', NOW()),
-- 新增8个志愿者信息
(9, 19, '陈一', '13800009999', 'chenyi@example.com', '阳光志愿服务队', '430102200001019012', 'approved', NOW()),
(10, 20, '林二', '13800010000', 'liner@example.com', '阳光志愿服务队', '430102200002020123', 'approved', NOW()),
(11, 21, '黄三', '13800011111', 'huangsan@example.com', '阳光志愿服务队', '430102200003031234', 'approved', NOW()),
(12, 22, '刘四', '13800012222', 'liusi@example.com', NULL, '430102200004042345', 'pending', NOW()),
(13, 23, '杨五', '13800013333', 'yangwu@example.com', NULL, '430102200005053456', 'pending', NOW()),
(14, 24, '何六', '13800014444', 'heliu@example.com', NULL, '430102200006064567', 'pending', NOW()),
(15, 25, '罗七', '13800015555', 'luoqi@example.com', NULL, '430102200007075678', 'pending', NOW()),
(16, 26, '高八', '13800016666', 'gaoba@example.com', NULL, '430102200008086789', 'pending', NOW());

-- 志愿者加入组织（多个组织成员关系）
-- org1(阳光志愿服务队): 已通过6人(1,2,4,9,10,11), 待审核6人(3,12,13,14,15,16)
INSERT INTO volunteer_org_member (volunteer_id, org_id, status, joined_at, created_at) VALUES
(1, 1, 'approved', NOW(), NOW()),
(2, 1, 'approved', NOW(), NOW()),
(4, 1, 'approved', NOW(), NOW()),
(9, 1, 'approved', NOW(), NOW()),
(10, 1, 'approved', NOW(), NOW()),
(11, 1, 'approved', NOW(), NOW()),
(5, 2, 'approved', NOW(), NOW()),
(6, 2, 'approved', NOW(), NOW()),
(8, 3, 'approved', NOW(), NOW()),
(3, 1, 'pending', NULL, NOW()),
(12, 1, 'pending', NULL, NOW()),
(13, 1, 'pending', NULL, NOW()),
(14, 1, 'pending', NULL, NOW()),
(15, 1, 'pending', NULL, NOW()),
(16, 1, 'pending', NULL, NOW()),
(7, 2, 'rejected', NULL, NOW());

-- 活动报名记录（更多报名）
INSERT INTO activity_signup (activity_id, volunteer_id, status, created_at) VALUES
(1, 1, 'applied', NOW()),
(1, 2, 'applied', NOW()),
(1, 4, 'applied', NOW()),
(1, 5, 'applied', NOW()),
(2, 1, 'applied', NOW()),
(2, 2, 'applied', NOW()),
(2, 4, 'applied', NOW()),
(2, 6, 'applied', NOW()),
(3, 1, 'applied', NOW()),
(3, 2, 'applied', NOW()),
(4, 1, 'applied', NOW()),
(4, 5, 'applied', NOW()),
(4, 6, 'applied', NOW()),
(5, 2, 'applied', NOW()),
(5, 4, 'applied', NOW()),
(5, 8, 'applied', NOW()),
(6, 1, 'applied', NOW()),
(6, 2, 'applied', NOW()),
(6, 5, 'applied', NOW()),
(7, 4, 'applied', NOW()),
(7, 6, 'applied', NOW()),
(8, 1, 'applied', NOW()),
(8, 8, 'applied', NOW());

-- 10. 示例普通用户账号 (密码: admin123) - 6个普通用户
INSERT INTO user (id, username, password, nickname, role_code, enabled, created_at, updated_at) VALUES
(13, 'user1', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '普通用户小明', 'USER', 1, NOW(), NOW()),
(14, 'user2', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '普通用户小红', 'USER', 1, NOW(), NOW()),
(15, 'user3', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '普通用户小刚', 'USER', 1, NOW(), NOW()),
(16, 'user4', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '普通用户小丽', 'USER', 1, NOW(), NOW()),
(17, 'user5', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '普通用户小华', 'USER', 1, NOW(), NOW()),
(18, 'user6', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '普通用户小强', 'USER', 1, NOW(), NOW());

-- 示例求助信息（8条求助）
INSERT INTO help_request (user_id, org_id, title, content, contact_name, contact_phone, address, status, reply, replied_at, created_at) VALUES
(13, 1, '需要帮助搬运物资', '社区有一批捐赠物资需要搬运到仓库，希望能有志愿者帮忙。物资约有50箱，需要5-6名志愿者。', '小明', '13900001111', '幸福社区3栋1单元', 'pending', NULL, NULL, NOW()),
(13, 1, '独居老人需要陪伴', '我家邻居是一位独居老人，今年85岁，希望能有志愿者定期上门陪伴聊天。', '小明', '13900001111', '幸福社区5栋2单元', 'replied', '已安排志愿者张三每周三、周六上门陪伴，请保持联系。', NOW(), NOW()),
(14, 2, '残疾人出行帮助', '我父亲腿脚不便，需要定期去医院复查，希望能有志愿者协助出行。', '小红', '13900002222', '阳光小区8栋3单元', 'pending', NULL, NULL, NOW()),
(15, 1, '课后辅导需求', '我家孩子上小学三年级，数学成绩不太好，希望能有志愿者帮忙辅导功课。', '小刚', '13900003333', '和平路社区12栋', 'replied', '已联系青年志愿者联盟的大学生志愿者，每周末可以上门辅导。', NOW(), NOW()),
(16, 2, '社区环境整治', '我们小区绿化带杂草丛生，希望能组织志愿者帮忙清理整治。', '小丽', '13900004444', '翠苑小区', 'pending', NULL, NULL, NOW()),
(17, 3, '图书捐赠收集', '我们学校想为山区孩子捐赠图书，需要志愿者帮忙收集和整理。', '小华', '13900005555', '长沙市第一中学', 'replied', '已安排本周六在学校门口设立捐赠点，届时会有志愿者协助。', NOW(), NOW()),
(18, 1, '老人手机使用指导', '我奶奶想学习使用智能手机，希望能有耐心的志愿者教她。', '小强', '13900006666', '银杏社区2栋5单元', 'pending', NULL, NULL, NOW()),
(13, 2, '社区义卖活动支持', '我们社区计划举办义卖活动，需要志愿者帮忙布置场地和维持秩序。', '小明', '13900001111', '幸福社区活动中心', 'replied', '活动定于下周六举行，已安排8名志愿者参与支持。', NOW(), NOW());

-- 11. 示例终端数据（6个终端）
INSERT INTO terminal (id, code, name, group_name, status, last_heartbeat, created_at, updated_at) VALUES
(1, 'T001', '一楼大厅展示屏', '大厅组', 'online', NOW(), NOW(), NOW()),
(2, 'T002', '二楼走廊展示屏', '走廊组', 'online', NOW(), NOW(), NOW()),
(3, 'T003', '会议室展示屏', '会议室组', 'offline', DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), NOW()),
(4, 'T004', '志愿者服务中心大屏', '服务中心组', 'online', NOW(), NOW(), NOW()),
(5, 'T005', '社区活动室展示屏', '社区组', 'online', NOW(), NOW(), NOW()),
(6, 'T006', '图书馆入口展示屏', '图书馆组', 'offline', DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW(), NOW());

-- 12. 示例志愿服务记录（时长统计）- 更多记录
INSERT INTO volunteer_service_record (volunteer_id, activity_id, org_id, service_hours, service_date, remark, created_at) VALUES
(1, 1, 1, 4.5, '2026-01-10', '参与敬老院慰问活动，陪伴老人聊天', NOW()),
(1, 2, 1, 3.0, '2026-01-08', '参与环保宣传活动，发放宣传资料', NOW()),
(2, 1, 1, 4.5, '2026-01-10', '参与敬老院慰问活动，帮助打扫卫生', NOW()),
(1, 4, 2, 6.0, '2026-01-05', '参与爱心助学行动，捐赠学习用品', NOW()),
(2, 5, 2, 4.0, '2026-01-03', '参与社区义诊服务，协助医生登记', NOW()),
(4, 1, 1, 4.5, '2026-01-10', '参与敬老院慰问活动', NOW()),
(4, 2, 1, 3.0, '2026-01-08', '参与环保宣传活动', NOW()),
(5, 4, 2, 6.0, '2026-01-05', '参与爱心助学行动', NOW()),
(5, 5, 2, 4.0, '2026-01-03', '参与社区义诊服务', NOW()),
(6, 5, 2, 4.0, '2026-01-03', '参与社区义诊服务，维持现场秩序', NOW()),
(8, 6, 3, 40.0, '2026-01-01', '参与青年支教活动，教授语文课程', NOW()),
(1, 7, 1, 4.0, '2025-12-20', '参与文明交通劝导活动', NOW()),
(2, 7, 1, 4.0, '2025-12-20', '参与文明交通劝导活动', NOW()),
(4, 7, 1, 4.0, '2025-12-20', '参与文明交通劝导活动', NOW());

-- 13. 示例播放列表（6个播放列表）
INSERT INTO playlist (id, name, description, cover_url, layout_id, created_at, updated_at) VALUES
(1, '大厅宣传轮播', '一楼大厅展示屏播放列表，展示文明城市创建成果', 'https://picsum.photos/seed/playlist1/400/300', 1, NOW(), NOW()),
(2, '志愿活动展示', '志愿活动相关内容轮播，展示各类公益活动', 'https://picsum.photos/seed/playlist2/400/300', 2, NOW(), NOW()),
(3, '公益广告轮播', '公益广告内容轮播，传播正能量', 'https://picsum.photos/seed/playlist3/400/300', 1, NOW(), NOW()),
(4, '雷锋精神专题', '学雷锋志愿服务专题内容', 'https://picsum.photos/seed/playlist4/400/300', 3, NOW(), NOW()),
(5, '志愿者风采', '优秀志愿者事迹展示', 'https://picsum.photos/seed/playlist5/400/300', 2, NOW(), NOW()),
(6, '社区服务宣传', '社区志愿服务宣传内容', 'https://picsum.photos/seed/playlist6/400/300', 1, NOW(), NOW());

-- 14. 播放列表项（关联内容）- 更多项目
INSERT INTO playlist_item (playlist_id, media_id, content_id, display_duration, sort_order, area_index) VALUES
(1, NULL, 1, 10, 1, 0),
(1, NULL, 2, 10, 2, 0),
(1, NULL, 3, 10, 3, 0),
(1, NULL, 19, 10, 4, 0),
(1, NULL, 20, 10, 5, 0),
(1, NULL, 21, 10, 6, 0),
(2, NULL, 13, 15, 1, 0),
(2, NULL, 14, 15, 2, 0),
(2, NULL, 15, 15, 3, 1),
(2, NULL, 31, 15, 4, 0),
(2, NULL, 32, 15, 5, 1),
(2, NULL, 33, 15, 6, 0),
(3, NULL, 16, 12, 1, 0),
(3, NULL, 17, 12, 2, 0),
(3, NULL, 18, 12, 3, 0),
(3, NULL, 34, 12, 4, 0),
(3, NULL, 35, 12, 5, 0),
(3, NULL, 36, 12, 6, 0),
(4, NULL, 10, 15, 1, 0),
(4, NULL, 11, 15, 2, 0),
(4, NULL, 12, 15, 3, 0),
(4, NULL, 28, 15, 4, 0),
(4, NULL, 29, 15, 5, 0),
(4, NULL, 30, 15, 6, 0),
(5, NULL, 8, 12, 1, 0),
(5, NULL, 9, 12, 2, 0),
(5, NULL, 25, 12, 3, 0),
(5, NULL, 26, 12, 4, 0),
(6, NULL, 4, 10, 1, 0),
(6, NULL, 5, 10, 2, 0),
(6, NULL, 6, 10, 3, 0),
(6, NULL, 22, 10, 4, 0),
(6, NULL, 23, 10, 5, 0),
(6, NULL, 24, 10, 6, 0);

-- 15. 终端播放列表关联（更多关联）
INSERT INTO terminal_playlist (terminal_id, playlist_id, start_time, end_time, active) VALUES
(1, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(2, 2, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(3, 3, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(4, 4, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(5, 5, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(6, 6, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1);

-- 16. 示例广播任务（6个任务）
INSERT INTO broadcast_job (id, title, media_id, content_id, target_group, target_terminal_code, start_time, end_time, status, priority, queue_mode) VALUES
(1, '紧急通知：志愿者大会', NULL, 1, '大厅组', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 2 HOUR), 'scheduled', 10, 'interrupt'),
(2, '公益广告轮播', NULL, 16, NULL, 'T001', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 'scheduled', 5, 'queue'),
(3, '学雷锋活动宣传', NULL, 12, '服务中心组', NULL, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 'scheduled', 7, 'queue'),
(4, '环保公益宣传', NULL, 32, NULL, 'T002', DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 14 DAY), 'scheduled', 6, 'queue'),
(5, '志愿者招募通知', NULL, 30, '社区组', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), 'running', 8, 'queue'),
(6, '文明城市创建宣传', NULL, 1, '图书馆组', NULL, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 'scheduled', 4, 'queue');

-- 17. 示例布局模板（8个模板）
INSERT INTO layout_template (id, name, description, layout_json, tags, cover_url, builtin, created_at, updated_at) VALUES
(1, '经典单屏', '适用于单一内容全屏展示', '{"areas":[{"x":0,"y":0,"w":100,"h":100}]}', '单屏,全屏,简洁', 'https://picsum.photos/seed/tpl1/400/300', 1, NOW(), NOW()),
(2, '左右双栏', '左侧主内容，右侧辅助信息', '{"areas":[{"x":0,"y":0,"w":70,"h":100},{"x":70,"y":0,"w":30,"h":100}]}', '双栏,左右,信息展示', 'https://picsum.photos/seed/tpl2/400/300', 1, NOW(), NOW()),
(3, '上下分屏', '上方标题区，下方内容区', '{"areas":[{"x":0,"y":0,"w":100,"h":30},{"x":0,"y":30,"w":100,"h":70}]}', '上下,标题,内容', 'https://picsum.photos/seed/tpl3/400/300', 1, NOW(), NOW()),
(4, '三分屏', '适用于多内容同时展示', '{"areas":[{"x":0,"y":0,"w":50,"h":100},{"x":50,"y":0,"w":50,"h":50},{"x":50,"y":50,"w":50,"h":50}]}', '三分,多内容,复杂', 'https://picsum.photos/seed/tpl4/400/300', 1, NOW(), NOW()),
(5, '四宫格', '四个等分区域，适合多内容展示', '{"areas":[{"x":0,"y":0,"w":50,"h":50},{"x":50,"y":0,"w":50,"h":50},{"x":0,"y":50,"w":50,"h":50},{"x":50,"y":50,"w":50,"h":50}]}', '四宫格,等分,多媒体', 'https://picsum.photos/seed/tpl5/400/300', 1, NOW(), NOW()),
(6, '画中画', '主画面配小窗口，适合视频+信息展示', '{"areas":[{"x":0,"y":0,"w":100,"h":100},{"x":70,"y":70,"w":28,"h":28}]}', '画中画,视频,叠加', 'https://picsum.photos/seed/tpl6/400/300', 1, NOW(), NOW()),
(7, '顶部横幅', '顶部通知栏+主内容区', '{"areas":[{"x":0,"y":0,"w":100,"h":15},{"x":0,"y":15,"w":100,"h":85}]}', '横幅,通知,公告', 'https://picsum.photos/seed/tpl7/400/300', 0, NOW(), NOW()),
(8, '侧边导航', '左侧导航栏+右侧主内容', '{"areas":[{"x":0,"y":0,"w":20,"h":100},{"x":20,"y":0,"w":80,"h":100}]}', '导航,侧边栏,菜单', 'https://picsum.photos/seed/tpl8/400/300', 0, NOW(), NOW());

-- 18. 示例志愿者站内消息（每个志愿者多条消息）
INSERT INTO volunteer_message (volunteer_id, activity_id, title, content, type, is_read, created_at) VALUES
-- 志愿者1（张三）的消息
(1, 1, '活动报名成功', '您已成功报名"情暖三湘·志愿同行"活动，请准时参加。活动地点：敬老院，时间：2026年1月15日9:00。', 'signup', 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 1, '活动即将开始', '"情暖三湘·志愿同行"活动将于明天上午9点开始，请做好准备，记得携带志愿者证。', 'reminder', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 2, '活动报名成功', '您已成功报名"环保志愿行动"活动，活动地点：市民广场。', 'signup', 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1, NULL, '服务时长更新', '您本月累计服务时长已达15.5小时，感谢您的付出！', 'system', 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, NULL, '志愿者等级提升', '恭喜您！您的志愿者等级已提升为"银牌志愿者"，继续加油！', 'system', 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 7, '活动报名成功', '您已成功报名"文明交通劝导"活动。', 'signup', 1, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(1, NULL, '组织公告', '阳光志愿服务队将于本周六举行年度总结大会，请各位志愿者准时参加。', 'announcement', 0, NOW()),
-- 志愿者2（李四）的消息
(2, 1, '活动报名成功', '您已成功报名"情暖三湘·志愿同行"活动，请准时参加。', 'signup', 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 5, '活动报名成功', '您已成功报名"社区义诊服务"活动。', 'signup', 1, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(2, NULL, '服务时长更新', '您本月累计服务时长已达12.5小时。', 'system', 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, NULL, '活动取消通知', '原定于下周的"图书整理"活动因故取消，给您带来不便敬请谅解。', 'system', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 7, '活动报名成功', '您已成功报名"文明交通劝导"活动。', 'signup', 1, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(2, NULL, '组织公告', '阳光志愿服务队将于本周六举行年度总结大会，请各位志愿者准时参加。', 'announcement', 0, NOW()),
-- 志愿者4（赵六）的消息
(4, 1, '活动报名成功', '您已成功报名"情暖三湘·志愿同行"活动。', 'signup', 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 2, '活动报名成功', '您已成功报名"环保志愿行动"活动。', 'signup', 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(4, NULL, '服务时长更新', '您本月累计服务时长已达11.5小时。', 'system', 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 7, '活动即将开始', '"文明交通劝导"活动将于明天早上7:30开始，请准时到达五一广场集合点。', 'reminder', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, NULL, '组织公告', '阳光志愿服务队将于本周六举行年度总结大会，请各位志愿者准时参加。', 'announcement', 0, NOW()),
-- 志愿者5（孙七）的消息
(5, 4, '活动报名成功', '您已成功报名"爱心助学行动"活动。', 'signup', 1, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(5, 5, '活动报名成功', '您已成功报名"社区义诊服务"活动。', 'signup', 1, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(5, NULL, '服务时长更新', '您本月累计服务时长已达10小时，感谢您的付出！', 'system', 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, NULL, '欢迎加入', '欢迎加入爱心公益协会！我们期待与您一起传递爱心。', 'system', 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(5, 6, '活动报名成功', '您已成功报名"青年支教活动"活动。', 'signup', 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, NULL, '组织公告', '爱心公益协会本月将开展多项公益活动，欢迎积极参与。', 'announcement', 0, NOW()),
-- 志愿者6（周八）的消息
(6, 5, '活动报名成功', '您已成功报名"社区义诊服务"活动。', 'signup', 1, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(6, NULL, '服务时长更新', '您本月累计服务时长已达4小时。', 'system', 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(6, 7, '活动报名成功', '您已成功报名"文明交通劝导"活动。', 'signup', 0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(6, NULL, '组织公告', '爱心公益协会本月将开展多项公益活动，欢迎积极参与。', 'announcement', 0, NOW()),
-- 志愿者8（郑十）的消息
(8, 6, '活动报名成功', '您已成功报名"青年支教活动"活动，请做好为期一周的支教准备。', 'signup', 1, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(8, 6, '支教活动提醒', '支教活动将于下周一开始，请提前准备好教学资料和生活用品。', 'reminder', 0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(8, NULL, '服务时长更新', '您本月累计服务时长已达40小时，您是本月服务之星！', 'system', 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(8, NULL, '志愿者等级提升', '恭喜您！您的志愿者等级已提升为"金牌志愿者"！', 'system', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8, 8, '活动报名成功', '您已成功报名"关爱留守儿童"活动。', 'signup', 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(8, NULL, '组织公告', '青年志愿者联盟招募新一批支教志愿者，有意者请报名。', 'announcement', 0, NOW());

-- 19. 示例活动签到记录（更多签到）
INSERT INTO activity_checkin_log (activity_id, volunteer_id, created_at) VALUES
(1, 1, '2026-01-15 08:55:00'),
(1, 2, '2026-01-15 08:58:00'),
(1, 4, '2026-01-15 09:02:00'),
(1, 5, '2026-01-15 09:05:00'),
(2, 1, '2026-02-01 08:25:00'),
(2, 2, '2026-02-01 08:28:00'),
(2, 4, '2026-02-01 08:30:00'),
(2, 6, '2026-02-01 08:35:00');

-- 更新活动报名表的签到状态
UPDATE activity_signup SET checked_in = 1, checkin_time = '2026-01-15 08:55:00' WHERE activity_id = 1 AND volunteer_id = 1;
UPDATE activity_signup SET checked_in = 1, checkin_time = '2026-01-15 08:58:00' WHERE activity_id = 1 AND volunteer_id = 2;
UPDATE activity_signup SET checked_in = 1, checkin_time = '2026-01-15 09:02:00' WHERE activity_id = 1 AND volunteer_id = 4;
UPDATE activity_signup SET checked_in = 1, checkin_time = '2026-01-15 09:05:00' WHERE activity_id = 1 AND volunteer_id = 5;
UPDATE activity_signup SET checked_in = 1, checkin_time = '2026-02-01 08:25:00' WHERE activity_id = 2 AND volunteer_id = 1;
UPDATE activity_signup SET checked_in = 1, checkin_time = '2026-02-01 08:28:00' WHERE activity_id = 2 AND volunteer_id = 2;
UPDATE activity_signup SET checked_in = 1, checkin_time = '2026-02-01 08:30:00' WHERE activity_id = 2 AND volunteer_id = 4;
UPDATE activity_signup SET checked_in = 1, checkin_time = '2026-02-01 08:35:00' WHERE activity_id = 2 AND volunteer_id = 6;

-- 20. 示例志愿者提醒设置（更多志愿者）
INSERT INTO volunteer_reminder_setting (volunteer_id, signup_reminder, checkin_reminder, channel, reminder_minutes, created_at, updated_at) VALUES
(1, 1, 1, 'sms', 30, NOW(), NOW()),
(2, 1, 1, 'sms', 60, NOW(), NOW()),
(4, 1, 1, 'sms', 30, NOW(), NOW()),
(5, 1, 0, 'email', 60, NOW(), NOW()),
(6, 1, 1, 'sms', 45, NOW(), NOW()),
(8, 1, 1, 'email', 30, NOW(), NOW());

-- 21. 示例操作日志（最近操作记录）- 更多日志
INSERT INTO operation_log (username, method, path, status, created_at) VALUES
('admin', 'POST', '/api/auth/login', 200, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('admin', 'GET', '/api/activities', 200, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('admin', 'POST', '/api/activities', 200, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
('admin', 'GET', '/api/terminals', 200, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('org1', 'POST', '/api/org/login', 200, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
('org1', 'GET', '/api/org/activities', 200, DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
('admin', 'PUT', '/api/volunteers/1/approve', 200, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('admin', 'GET', '/api/volunteers', 200, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('admin', 'DELETE', '/api/content/5', 200, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('admin', 'POST', '/api/content', 200, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('org1', 'POST', '/api/org/activities', 200, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
('org1', 'GET', '/api/org/volunteers', 200, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
('org2', 'POST', '/api/org/login', 200, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('org2', 'GET', '/api/org/activities', 200, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('org2', 'POST', '/api/org/help-requests/reply', 200, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('admin', 'GET', '/api/users', 200, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('admin', 'PUT', '/api/users/3', 200, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('org3', 'POST', '/api/org/login', 200, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('org3', 'POST', '/api/org/activities', 200, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('admin', 'GET', '/api/operation-logs', 200, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('admin', 'POST', '/api/broadcasts', 200, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('admin', 'GET', '/api/playlists', 200, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('admin', 'POST', '/api/playlists', 200, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('admin', 'PUT', '/api/terminals/1', 200, DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ============================================
-- 第四部分：验证数据
-- ============================================

SELECT '========================================' AS '';
SELECT '数据库初始化完成！验证结果如下：' AS message;
SELECT '========================================' AS '';
SELECT CONCAT('角色数量: ', COUNT(*)) AS result FROM role;
SELECT CONCAT('用户数量: ', COUNT(*)) AS result FROM user;
SELECT CONCAT('组织数量: ', COUNT(*)) AS result FROM volunteer_org;
SELECT CONCAT('志愿者数量: ', COUNT(*)) AS result FROM volunteer;
SELECT CONCAT('终端数量: ', COUNT(*)) AS result FROM terminal;
SELECT CONCAT('分类数量: ', COUNT(*)) AS result FROM menu_category;
SELECT CONCAT('内容数量: ', COUNT(*)) AS result FROM content_item;
SELECT CONCAT('活动数量: ', COUNT(*)) AS result FROM activity;
SELECT CONCAT('活动报名数: ', COUNT(*)) AS result FROM activity_signup;
SELECT CONCAT('求助信息数: ', COUNT(*)) AS result FROM help_request;
SELECT CONCAT('服务记录数: ', COUNT(*)) AS result FROM volunteer_service_record;
SELECT CONCAT('布局数量: ', COUNT(*)) AS result FROM layout;
SELECT CONCAT('播放列表数: ', COUNT(*)) AS result FROM playlist;
SELECT CONCAT('广播任务数: ', COUNT(*)) AS result FROM broadcast_job;
SELECT CONCAT('布局模板数: ', COUNT(*)) AS result FROM layout_template;
SELECT CONCAT('站内消息数: ', COUNT(*)) AS result FROM volunteer_message;
SELECT CONCAT('操作日志数: ', COUNT(*)) AS result FROM operation_log;
SELECT '========================================' AS '';
SELECT '默认账号信息：' AS message;
SELECT '----------------------------------------' AS '';
SELECT '【管理员端】 /login' AS portal;
SELECT '  admin / admin123' AS account;
SELECT '----------------------------------------' AS '';
SELECT '【组织端】 /org/login' AS portal;
SELECT '  org1 / admin123 (阳光志愿服务队)' AS account;
SELECT '  org2 / admin123 (爱心公益协会)' AS account;
SELECT '  org3 / admin123 (青年志愿者联盟)' AS account;
SELECT '----------------------------------------' AS '';
SELECT '【志愿者端】 /portal' AS portal;
SELECT '  13800001111 / admin123 (张三-已审核)' AS account;
SELECT '  13800002222 / admin123 (李四-已审核)' AS account;
SELECT '  13800003333 / admin123 (王五-待审核)' AS account;
SELECT '  13800004444 / admin123 (赵六-已审核)' AS account;
SELECT '  13800005555 / admin123 (孙七-已审核)' AS account;
SELECT '  13800006666 / admin123 (周八-已审核)' AS account;
SELECT '  13800007777 / admin123 (吴九-已拒绝)' AS account;
SELECT '  13800008888 / admin123 (郑十-已审核)' AS account;
SELECT '  13800009999 / admin123 (陈一-已审核)' AS account;
SELECT '  13800010000 / admin123 (林二-已审核)' AS account;
SELECT '  13800011111 / admin123 (黄三-已审核)' AS account;
SELECT '  13800012222 / admin123 (刘四-待审核)' AS account;
SELECT '  13800013333 / admin123 (杨五-待审核)' AS account;
SELECT '  13800014444 / admin123 (何六-待审核)' AS account;
SELECT '  13800015555 / admin123 (罗七-待审核)' AS account;
SELECT '  13800016666 / admin123 (高八-待审核)' AS account;
SELECT '----------------------------------------' AS '';
SELECT '【用户端】 /user-portal' AS portal;
SELECT '  user1 / admin123 (小明)' AS account;
SELECT '  user2 / admin123 (小红)' AS account;
SELECT '  user3 / admin123 (小刚)' AS account;
SELECT '  user4 / admin123 (小丽)' AS account;
SELECT '  user5 / admin123 (小华)' AS account;
SELECT '  user6 / admin123 (小强)' AS account;
SELECT '========================================' AS '';
