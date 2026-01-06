-- ============================================
-- Volunteer 数据库初始化脚本
-- 使用方法: mysql -u root -p < init-database.sql
-- ============================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS volunteer 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 2. 切换到 volunteer 数据库
USE volunteer;

-- 3. 创建表结构
-- 用户表
CREATE TABLE IF NOT EXISTS user (
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
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    description VARCHAR(255)
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 菜单分类表
CREATE TABLE IF NOT EXISTS menu_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    parent_id BIGINT,
    sort_order INT DEFAULT 0
);

-- 内容项表
CREATE TABLE IF NOT EXISTS content_item (
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
CREATE TABLE IF NOT EXISTS content_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recommend_interval_sec INT DEFAULT 6,
    preview_interval_sec INT DEFAULT 10,
    recommend_count INT DEFAULT 6,
    recommend_strategy VARCHAR(16) DEFAULT 'prefer',
    updated_at DATETIME
);


-- 媒体资源表
CREATE TABLE IF NOT EXISTS media_asset (
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
CREATE TABLE IF NOT EXISTS playlist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    cover_url VARCHAR(255),
    layout_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME
);

-- 播放列表项表
CREATE TABLE IF NOT EXISTS playlist_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    playlist_id BIGINT NOT NULL,
    media_id BIGINT,
    content_id BIGINT,
    display_duration INT DEFAULT 10,
    sort_order INT DEFAULT 0
);

-- 布局表
CREATE TABLE IF NOT EXISTS layout (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    layout_json TEXT NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
);

-- 终端表
CREATE TABLE IF NOT EXISTS terminal (
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
CREATE TABLE IF NOT EXISTS terminal_playlist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    terminal_id BIGINT NOT NULL,
    playlist_id BIGINT NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    active TINYINT(1) DEFAULT 1
);

-- 广播任务表
CREATE TABLE IF NOT EXISTS broadcast_job (
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

CREATE TABLE IF NOT EXISTS terminal_heartbeat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    terminal_id BIGINT NOT NULL,
    status VARCHAR(32),
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64),
    method VARCHAR(16),
    path VARCHAR(255),
    status INT,
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS volunteer (
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

CREATE TABLE IF NOT EXISTS activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    start_time DATETIME,
    end_time DATETIME,
    capacity INT,
    checkin_code VARCHAR(32),
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS activity_signup (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    status VARCHAR(32) DEFAULT 'applied',
    checkin_time DATETIME,
    checked_in TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 志愿者站内消息表
CREATE TABLE IF NOT EXISTS volunteer_message (
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
CREATE TABLE IF NOT EXISTS portal_message_read (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    volunteer_id BIGINT NOT NULL,
    message_key VARCHAR(128) NOT NULL,
    read_at DATETIME,
    INDEX idx_volunteer_id (volunteer_id),
    UNIQUE KEY uk_volunteer_message (volunteer_id, message_key)
);

-- ============================================
-- 以下为扩展功能表
-- ============================================

-- 活动签到日志表
CREATE TABLE IF NOT EXISTS activity_checkin_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 活动提醒日志表
CREATE TABLE IF NOT EXISTS activity_reminder_log (
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

-- 告警订阅表
CREATE TABLE IF NOT EXISTS alert_subscription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(64),
    channel VARCHAR(32),
    target VARCHAR(128),
    enabled TINYINT(1) DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME
);

-- 布局区域内容池表
CREATE TABLE IF NOT EXISTS layout_area_pool (
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
CREATE TABLE IF NOT EXISTS layout_template (
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

-- 通知渠道配置表
CREATE TABLE IF NOT EXISTS notification_channel_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel VARCHAR(32) NOT NULL,
    config_json TEXT,
    enabled TINYINT(1) DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME
);

-- 通知日志表
CREATE TABLE IF NOT EXISTS notification_log (
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

-- 终端分组规则表
CREATE TABLE IF NOT EXISTS terminal_group_rule (
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
CREATE TABLE IF NOT EXISTS volunteer_reminder_setting (
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
CREATE TABLE IF NOT EXISTS volunteer_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    volunteer_id BIGINT NOT NULL,
    status VARCHAR(32),
    remark VARCHAR(255),
    created_at DATETIME
);
