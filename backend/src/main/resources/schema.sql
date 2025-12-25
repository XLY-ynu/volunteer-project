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
    updated_at DATETIME
);


-- 媒体资源表
CREATE TABLE IF NOT EXISTS media_asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    url VARCHAR(255) NOT NULL,
    size_bytes BIGINT,
    duration_seconds INT,
    width INT,
    height INT,
    checksum VARCHAR(128),
    created_at DATETIME
);

-- 播放列表表
CREATE TABLE IF NOT EXISTS playlist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
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
    status VARCHAR(32) DEFAULT 'scheduled'
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
    name VARCHAR(64) NOT NULL,
    phone VARCHAR(32),
    email VARCHAR(128),
    organization VARCHAR(128),
    status VARCHAR(32) DEFAULT 'pending',
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    start_time DATETIME,
    end_time DATETIME,
    capacity INT,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS activity_signup (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    volunteer_id BIGINT NOT NULL,
    status VARCHAR(32) DEFAULT 'applied',
    checkin_time DATETIME,
    created_at DATETIME
);
