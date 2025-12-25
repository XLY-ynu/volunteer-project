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
