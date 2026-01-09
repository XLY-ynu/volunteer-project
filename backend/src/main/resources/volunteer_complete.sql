-- ============================================
-- 志愿者服务活动中心多媒体展示系统
-- 完整数据库初始化脚本（一键执行版）
-- 更新日期：2026-01-09
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

-- 3. 示例志愿者组织账号 (密码: admin123)
INSERT INTO user (username, password, nickname, role_code, enabled, created_at, updated_at) VALUES
('org1', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '阳光志愿服务队', 'ORG', 1, NOW(), NOW());

-- 创建对应的组织信息（org1 的 user_id = 2）
INSERT INTO volunteer_org (id, name, code, description, contact_name, contact_phone, user_id, created_at) VALUES
(1, '阳光志愿服务队', 'sunshine', '致力于社区服务和公益活动的志愿者组织', '张队长', '13800138001', 2, NOW());

-- 4. 六大主菜单分类
INSERT INTO menu_category (id, name, code, parent_id, sort_order) VALUES
(1, '文明XX', 'wenming', NULL, 1),
(2, 'XX志愿者APP', 'app', NULL, 2),
(3, 'XX志愿者网', 'web', NULL, 3),
(4, '雷锋热线', 'leifeng', NULL, 4),
(5, '公益活动', 'gongyi', NULL, 5),
(6, '公益广告', 'ad', NULL, 6);

-- 5. 布局模板 (3个基础布局)
INSERT INTO layout (id, name, layout_json, created_at, updated_at) VALUES
(1, '单屏全屏', '{"areas":[{"x":0,"y":0,"w":100,"h":100}]}', NOW(), NOW()),
(2, '左右分屏', '{"areas":[{"x":0,"y":0,"w":50,"h":100},{"x":50,"y":0,"w":50,"h":100}]}', NOW(), NOW()),
(3, '上下分屏', '{"areas":[{"x":0,"y":0,"w":100,"h":50},{"x":0,"y":50,"w":100,"h":50}]}', NOW(), NOW());

-- 6. 内容配置
INSERT INTO content_config (id, recommend_interval_sec, preview_interval_sec, recommend_count, recommend_strategy, updated_at) VALUES
(1, 6, 10, 10, 'weight_only', NOW());

-- 7. 示例内容数据 (每个分类3条，共18条)

-- 分类1: 文明XX
INSERT INTO content_item (id, category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(1, 1, '我市荣获全国文明城市称号', '经过全市人民共同努力，我市成功创建全国文明城市', 
'近日，中央文明办公布了新一届全国文明城市名单，我市凭借优异的创建成绩，成功入选全国文明城市。',
'https://picsum.photos/seed/city1/400/300', 1, NOW(), NOW(), NOW()),
(2, 1, '文明交通志愿服务在行动', '千名志愿者走上街头，倡导文明出行', 
'为进一步提升市民文明交通意识，我市组织开展文明交通志愿服务月活动。',
'https://picsum.photos/seed/traffic1/400/300', 1, NOW(), NOW(), NOW()),
(3, 1, '社区文明实践站揭牌成立', '打通服务群众最后一公里', 
'今日，我市首批10个新时代文明实践站正式揭牌成立。',
'https://picsum.photos/seed/community1/400/300', 1, NOW(), NOW(), NOW());

-- 分类2: XX志愿者APP
INSERT INTO content_item (id, category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(4, 2, '志愿者APP全新上线', '一键报名，轻松参与志愿服务', 
'志愿者APP是我市官方志愿服务移动平台，集活动报名、签到打卡、时长统计于一体。',
'https://picsum.photos/seed/app1/400/300', 1, NOW(), NOW(), NOW()),
(5, 2, 'APP新功能：活动地图上线', '附近志愿活动一目了然', 
'志愿者APP新增活动地图功能，可以查看附近正在进行的志愿活动。',
'https://picsum.photos/seed/map1/400/300', 1, NOW(), NOW(), NOW()),
(6, 2, '本周热门志愿招募', '多个志愿岗位等你来', 
'本周新增志愿岗位：社区助老服务、图书馆导读、环保宣传等。',
'https://picsum.photos/seed/recruit1/400/300', 1, NOW(), NOW(), NOW());

-- 分类3: XX志愿者网
INSERT INTO content_item (id, category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(7, 3, '志愿者网平台介绍', '官方志愿服务信息发布平台', 
'志愿者网是我市志愿服务工作的官方门户网站，提供志愿者注册、活动发布等功能。',
'https://picsum.photos/seed/web1/400/300', 1, NOW(), NOW(), NOW()),
(8, 3, '2024年度优秀志愿者表彰', '百名优秀志愿者受表彰', 
'在2024年度志愿服务总结大会上，100名优秀志愿者受到表彰。',
'https://picsum.photos/seed/award1/400/300', 1, NOW(), NOW(), NOW()),
(9, 3, '志愿服务专题：关爱空巢老人', '用爱心温暖每一位老人', 
'关爱空巢老人专题活动持续开展中，志愿者们定期走访社区空巢老人。',
'https://picsum.photos/seed/elderly1/400/300', 1, NOW(), NOW(), NOW());

-- 分类4: 雷锋热线
INSERT INTO content_item (id, category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(10, 4, '雷锋热线服务指南', '24小时志愿服务热线', 
'雷锋热线是我市志愿服务求助与帮扶的桥梁，拨打热线即可获得志愿服务帮助。',
'https://picsum.photos/seed/hotline1/400/300', 1, NOW(), NOW(), NOW()),
(11, 4, '热线帮扶案例：为独居老人送温暖', '志愿者上门帮助独居老人', 
'张奶奶今年82岁，独居在家。通过雷锋热线求助后，志愿者定期上门帮助。',
'https://picsum.photos/seed/help1/400/300', 1, NOW(), NOW(), NOW()),
(12, 4, '品牌活动：学雷锋志愿服务月', '传承雷锋精神，践行志愿服务', 
'每年3月是学雷锋志愿服务月，全市开展形式多样的志愿服务活动。',
'https://picsum.photos/seed/leifeng1/400/300', 1, NOW(), NOW(), NOW());

-- 分类5: 公益活动
INSERT INTO content_item (id, category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(13, 5, '让轮椅飞：无障碍出行公益行动', '帮助残障人士实现出行梦想', 
'让轮椅飞是我市品牌公益项目，旨在帮助残障人士无障碍出行。',
'https://picsum.photos/seed/wheelchair1/400/300', 1, NOW(), NOW(), NOW()),
(14, 5, '名师一堂课：公益教育进社区', '优质教育资源惠及更多家庭', 
'名师一堂课邀请优秀教师走进社区，为孩子们提供免费辅导。',
'https://picsum.photos/seed/teacher1/400/300', 1, NOW(), NOW(), NOW()),
(15, 5, '睡前讲故事：陪伴留守儿童成长', '用故事温暖童年', 
'睡前讲故事项目通过视频连线，为留守儿童讲述睡前故事。',
'https://picsum.photos/seed/story1/400/300', 1, NOW(), NOW(), NOW());

-- 分类6: 公益广告
INSERT INTO content_item (id, category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(16, 6, '图说我们的价值观：诚信篇', '诚信是金，立身之本', 
'诚信是中华民族的传统美德，是社会主义核心价值观的重要内容。',
'https://picsum.photos/seed/honest1/400/300', 1, NOW(), NOW(), NOW()),
(17, 6, '讲文明树新风：文明餐桌', '光盘行动，从我做起', 
'珍惜粮食，杜绝浪费。让我们一起践行光盘行动。',
'https://picsum.photos/seed/food1/400/300', 1, NOW(), NOW(), NOW()),
(18, 6, '公益广告：关爱未成年人', '呵护成长，守护未来', 
'未成年人是祖国的花朵，让我们共同关爱未成年人健康成长。',
'https://picsum.photos/seed/children1/400/300', 1, NOW(), NOW(), NOW());

-- 8. 示例志愿活动 (3个示例活动，关联组织)
INSERT INTO activity (id, title, description, location, start_time, end_time, capacity, checkin_code, org_id, members_only, created_at, updated_at) VALUES
(1, '情暖三湘·志愿同行', '聚焦老年人尤其是空巢、独居老人的急难愁盼问题，开展慰问陪护、清洁卫生等志愿服务。', 
'敬老院、养老院等', '2026-01-15 09:00:00', '2026-01-15 17:00:00', 50, '382388', 1, 0, NOW(), NOW()),
(2, '环保志愿行动', '组织志愿者参与城市环境清洁、垃圾分类宣传等活动。', 
'市民广场', '2026-02-01 08:30:00', '2026-02-01 12:00:00', 30, '123456', 1, 0, NOW(), NOW()),
(3, '组织内部培训', '仅限阳光志愿服务队成员参与的内部培训活动。', 
'组织会议室', '2026-02-15 14:00:00', '2026-02-15 17:00:00', 20, '888888', 1, 1, NOW(), NOW());

-- 9. 示例志愿者账号和数据 (密码: admin123)
-- 志愿者用户账号
INSERT INTO user (id, username, password, nickname, role_code, enabled, created_at, updated_at) VALUES
(3, '13800001111', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '张三', 'VOLUNTEER', 1, NOW(), NOW()),
(4, '13800002222', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '李四', 'VOLUNTEER', 1, NOW(), NOW()),
(5, '13800003333', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '王五', 'VOLUNTEER', 1, NOW(), NOW());

-- 志愿者详细信息
INSERT INTO volunteer (id, user_id, name, phone, email, organization, id_card, status, created_at) VALUES
(1, 3, '张三', '13800001111', 'zhangsan@example.com', '阳光志愿服务队', '430102199001011234', 'approved', NOW()),
(2, 4, '李四', '13800002222', 'lisi@example.com', '阳光志愿服务队', '430102199002022345', 'approved', NOW()),
(3, 5, '王五', '13800003333', 'wangwu@example.com', NULL, '430102199003033456', 'pending', NOW());

-- 志愿者加入组织
INSERT INTO volunteer_org_member (volunteer_id, org_id, status, joined_at, created_at) VALUES
(1, 1, 'approved', NOW(), NOW()),
(2, 1, 'approved', NOW(), NOW());

-- 活动报名记录
INSERT INTO activity_signup (activity_id, volunteer_id, status, created_at) VALUES
(1, 1, 'applied', NOW()),
(1, 2, 'applied', NOW()),
(2, 1, 'applied', NOW());

-- 10. 示例普通用户账号 (密码: admin123)
INSERT INTO user (id, username, password, nickname, role_code, enabled, created_at, updated_at) VALUES
(6, 'user1', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '普通用户小明', 'USER', 1, NOW(), NOW());

-- 示例求助信息
INSERT INTO help_request (user_id, org_id, title, content, contact_name, contact_phone, address, status, created_at) VALUES
(6, 1, '需要帮助搬运物资', '社区有一批捐赠物资需要搬运到仓库，希望能有志愿者帮忙。', '小明', '13900001111', '幸福社区3栋1单元', 'pending', NOW()),
(6, 1, '独居老人需要陪伴', '我家邻居是一位独居老人，希望能有志愿者定期上门陪伴聊天。', '小明', '13900001111', '幸福社区5栋2单元', 'replied', NOW());

-- 11. 示例终端数据
INSERT INTO terminal (id, code, name, group_name, status, last_heartbeat, created_at, updated_at) VALUES
(1, 'T001', '一楼大厅展示屏', '大厅组', 'online', NOW(), NOW(), NOW()),
(2, 'T002', '二楼走廊展示屏', '走廊组', 'online', NOW(), NOW(), NOW()),
(3, 'T003', '会议室展示屏', '会议室组', 'offline', DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), NOW());

-- 12. 示例志愿服务记录（时长统计）
INSERT INTO volunteer_service_record (volunteer_id, activity_id, org_id, service_hours, service_date, remark, created_at) VALUES
(1, 1, 1, 4.5, '2026-01-10', '参与敬老院慰问活动', NOW()),
(1, 2, 1, 3.0, '2026-01-08', '参与环保宣传活动', NOW()),
(2, 1, 1, 4.5, '2026-01-10', '参与敬老院慰问活动', NOW());

-- 13. 示例播放列表
INSERT INTO playlist (id, name, description, cover_url, layout_id, created_at, updated_at) VALUES
(1, '大厅宣传轮播', '一楼大厅展示屏播放列表', 'https://picsum.photos/seed/playlist1/400/300', 1, NOW(), NOW()),
(2, '志愿活动展示', '志愿活动相关内容轮播', 'https://picsum.photos/seed/playlist2/400/300', 2, NOW(), NOW());

-- 14. 播放列表项（关联内容）
INSERT INTO playlist_item (playlist_id, media_id, content_id, display_duration, sort_order, area_index) VALUES
(1, NULL, 1, 10, 1, 0),
(1, NULL, 2, 10, 2, 0),
(1, NULL, 3, 10, 3, 0),
(2, NULL, 13, 15, 1, 0),
(2, NULL, 14, 15, 2, 0),
(2, NULL, 15, 15, 3, 1);

-- 15. 终端播放列表关联
INSERT INTO terminal_playlist (terminal_id, playlist_id, start_time, end_time, active) VALUES
(1, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(2, 2, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1);

-- 16. 示例广播任务
INSERT INTO broadcast_job (id, title, media_id, content_id, target_group, target_terminal_code, start_time, end_time, status, priority, queue_mode) VALUES
(1, '紧急通知：志愿者大会', NULL, 1, '大厅组', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 2 HOUR), 'scheduled', 10, 'interrupt'),
(2, '公益广告轮播', NULL, 16, NULL, 'T001', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 'scheduled', 5, 'queue');

-- 17. 示例布局模板
INSERT INTO layout_template (id, name, description, layout_json, tags, cover_url, builtin, created_at, updated_at) VALUES
(1, '经典单屏', '适用于单一内容全屏展示', '{"areas":[{"x":0,"y":0,"w":100,"h":100}]}', '单屏,全屏,简洁', 'https://picsum.photos/seed/tpl1/400/300', 1, NOW(), NOW()),
(2, '左右双栏', '左侧主内容，右侧辅助信息', '{"areas":[{"x":0,"y":0,"w":70,"h":100},{"x":70,"y":0,"w":30,"h":100}]}', '双栏,左右,信息展示', 'https://picsum.photos/seed/tpl2/400/300', 1, NOW(), NOW()),
(3, '上下分屏', '上方标题区，下方内容区', '{"areas":[{"x":0,"y":0,"w":100,"h":30},{"x":0,"y":30,"w":100,"h":70}]}', '上下,标题,内容', 'https://picsum.photos/seed/tpl3/400/300', 1, NOW(), NOW()),
(4, '三分屏', '适用于多内容同时展示', '{"areas":[{"x":0,"y":0,"w":50,"h":100},{"x":50,"y":0,"w":50,"h":50},{"x":50,"y":50,"w":50,"h":50}]}', '三分,多内容,复杂', 'https://picsum.photos/seed/tpl4/400/300', 1, NOW(), NOW());

-- 18. 示例志愿者站内消息
INSERT INTO volunteer_message (volunteer_id, activity_id, title, content, type, is_read, created_at) VALUES
(1, 1, '活动报名成功', '您已成功报名"情暖三湘·志愿同行"活动，请准时参加。', 'signup', 1, NOW()),
(1, 1, '活动即将开始', '"情暖三湘·志愿同行"活动将于明天上午9点开始，请做好准备。', 'reminder', 0, NOW()),
(2, 1, '活动报名成功', '您已成功报名"情暖三湘·志愿同行"活动，请准时参加。', 'signup', 1, NOW()),
(1, 2, '活动报名成功', '您已成功报名"环保志愿行动"活动。', 'signup', 0, NOW());

-- 19. 示例活动签到记录
INSERT INTO activity_checkin_log (activity_id, volunteer_id, created_at) VALUES
(1, 1, '2026-01-15 08:55:00'),
(1, 2, '2026-01-15 08:58:00');

-- 更新活动报名表的签到状态
UPDATE activity_signup SET checked_in = 1, checkin_time = '2026-01-15 08:55:00' WHERE activity_id = 1 AND volunteer_id = 1;
UPDATE activity_signup SET checked_in = 1, checkin_time = '2026-01-15 08:58:00' WHERE activity_id = 1 AND volunteer_id = 2;

-- 20. 示例志愿者提醒设置
INSERT INTO volunteer_reminder_setting (volunteer_id, signup_reminder, checkin_reminder, channel, reminder_minutes, created_at, updated_at) VALUES
(1, 1, 1, 'sms', 30, NOW(), NOW()),
(2, 1, 1, 'sms', 60, NOW(), NOW());

-- 21. 示例操作日志（最近操作记录）
INSERT INTO operation_log (username, method, path, status, created_at) VALUES
('admin', 'POST', '/api/auth/login', 200, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('admin', 'GET', '/api/activities', 200, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('admin', 'POST', '/api/activities', 200, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
('admin', 'GET', '/api/terminals', 200, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('org1', 'POST', '/api/org/login', 200, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
('org1', 'GET', '/api/org/activities', 200, DATE_SUB(NOW(), INTERVAL 15 MINUTE));

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
SELECT '管理员端 (/login): admin / admin123' AS account;
SELECT '组织端 (/org/login): org1 / admin123' AS account;
SELECT '志愿者端 (/portal): 13800001111 / admin123' AS account;
SELECT '志愿者端 (/portal): 13800002222 / admin123' AS account;
SELECT '志愿者端 (/portal): 13800003333 / admin123 (待审核)' AS account;
SELECT '用户端 (/user-portal): user1 / admin123' AS account;
SELECT '========================================' AS '';
