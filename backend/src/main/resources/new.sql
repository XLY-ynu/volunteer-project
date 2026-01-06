USE volunteer;

CREATE TABLE IF NOT EXISTS activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    location VARCHAR(256),
    start_time DATETIME,
    end_time DATETIME,
    capacity INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS activity_signup (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    status VARCHAR(32) DEFAULT 'applied',
    checked_in BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS volunteer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    phone VARCHAR(32),
    email VARCHAR(128),
    id_card VARCHAR(32),
    status VARCHAR(32) DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================
-- 增量更新（请按需执行，若字段已存在会报错可忽略）
-- ============================================
-- 1) 播放列表新增布局字段
-- ALTER TABLE playlist ADD COLUMN layout_id BIGINT NULL;

-- 2) 终端心跳表
CREATE TABLE IF NOT EXISTS terminal_heartbeat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    terminal_id BIGINT NOT NULL,
    status VARCHAR(32),
    created_at DATETIME
);

-- 3) 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64),
    method VARCHAR(16),
    path VARCHAR(255),
    status INT,
    created_at DATETIME
);

-- 4) 志愿者表补充字段
-- ALTER TABLE volunteer ADD COLUMN organization VARCHAR(128) AFTER email;

-- 5) 活动报名表补充签到时间（若字段已存在会报错可忽略）
-- ALTER TABLE activity_signup ADD COLUMN checkin_time DATETIME NULL AFTER status;

-- 6) 初始化六大菜单分类
INSERT IGNORE INTO menu_category (name, code, parent_id, sort_order) VALUES
('文明XX', 'wenming', NULL, 0),
('XX志愿者APP', 'app', NULL, 0),
('XX志愿者网', 'web', NULL, 0),
('雷锋热线', 'leifeng', NULL, 0),
('公益活动', 'gongyi', NULL, 0),
('公益广告', 'ad', NULL, 0);


-- 删除重复记录，保留id最小的
DELETE t1 FROM menu_category t1
INNER JOIN menu_category t2 
WHERE t1.id > t2.id AND t1.code = t2.code;

-- 7) 媒体资源增加缩略图（若字段已存在会报错可忽略）
-- ALTER TABLE media_asset ADD COLUMN thumb_url VARCHAR(255) NULL AFTER url;

-- 8) 活动签到日志
CREATE TABLE IF NOT EXISTS activity_checkin_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 9) 活动增加签到码（若字段已存在会报错可忽略）
-- ALTER TABLE activity ADD COLUMN checkin_code VARCHAR(32) NULL AFTER capacity;

-- 10) 播放列表增加封面
-- ALTER TABLE playlist ADD COLUMN cover_url VARCHAR(255) NULL AFTER description;

-- 11) 内容头条/推荐标记
-- ALTER TABLE content_item ADD COLUMN headline TINYINT(1) DEFAULT 0;
-- ALTER TABLE content_item ADD COLUMN recommended TINYINT(1) DEFAULT 0;
-- ALTER TABLE content_item ADD COLUMN sort_order INT DEFAULT 0;

-- 12) 媒体元数据增强
-- ALTER TABLE media_asset ADD COLUMN bitrate_kbps INT NULL;
-- ALTER TABLE media_asset ADD COLUMN frame_rate DECIMAL(6,2) NULL;

-- 13) 内容配置表（推荐轮播/预览轮询间隔）
-- ALTER TABLE content_config
-- ADD COLUMN recommend_count INT DEFAULT 6;

-- 14) 推荐轮播数量配置（若字段已存在会报错可忽略）
-- ALTER TABLE content_config ADD COLUMN recommend_count INT DEFAULT 6 AFTER recommend_interval_sec;

ALTER TABLE content_config ADD COLUMN recommend_strategy VARCHAR(16) DEFAULT 'prefer' AFTER recommend_count;
ALTER TABLE content_item ADD COLUMN recommend_weight INT DEFAULT 0 AFTER recommended;

ALTER TABLE volunteer ADD COLUMN user_id BIGINT NULL AFTER id;

CREATE TABLE IF NOT EXISTS terminal_group_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(64) NOT NULL,
    offline_threshold INT DEFAULT 1,
    enabled TINYINT(1) DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME
);

ALTER TABLE terminal_group_rule ADD COLUMN notify_channel VARCHAR(32) NULL AFTER enabled;
ALTER TABLE terminal_group_rule ADD COLUMN notify_target VARCHAR(128) NULL AFTER notify_channel;

-- 18) 告警历史与通知日志
CREATE TABLE IF NOT EXISTS terminal_alert_history (
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

CREATE TABLE IF NOT EXISTS notification_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel VARCHAR(32),
    target VARCHAR(128),
    title VARCHAR(128),
    content TEXT,
    status VARCHAR(32),
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS alert_subscription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(64),
    channel VARCHAR(32),
    target VARCHAR(128),
    enabled TINYINT(1) DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS alert_silence (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(64),
    channel VARCHAR(32),
    start_time DATETIME,
    end_time DATETIME,
    enabled TINYINT(1) DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME
);

-- 19) 志愿者审核状态日志
CREATE TABLE IF NOT EXISTS volunteer_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    volunteer_id BIGINT NOT NULL,
    status VARCHAR(32),
    remark VARCHAR(255),
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS layout_template_history (
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
