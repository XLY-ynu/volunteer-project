-- ============================================
-- 志愿者系统完整数据初始化脚本
-- 包含：基础数据 + 示例内容
-- 执行方式: mysql -u root -p volunteer < init-data.sql
-- ============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE volunteer;

-- ============================================
-- 1. 基础角色数据
-- ============================================
INSERT INTO role (code, name, description) VALUES
('ADMIN', '管理员', '系统管理员'),
('VOLUNTEER', '志愿者', '普通志愿者')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================
-- 2. 管理员账号 (密码: admin123)
-- ============================================
INSERT INTO user (username, password, nickname, role_code, enabled, created_at, updated_at) VALUES
('admin', '$2a$10$z/YRhCxrFtHwHkDHjNbEeeM4oMhPszzZSgPokP7qzX0WhonkfWKSO', '系统管理员', 'ADMIN', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE nickname=VALUES(nickname);

-- ============================================
-- 3. 六大主菜单分类
-- ============================================
INSERT INTO menu_category (id, name, code, parent_id, sort_order) VALUES
(1, '文明XX', 'wenming', NULL, 1),
(2, 'XX志愿者APP', 'app', NULL, 2),
(3, 'XX志愿者网', 'web', NULL, 3),
(4, '雷锋热线', 'leifeng', NULL, 4),
(5, '公益活动', 'gongyi', NULL, 5),
(6, '公益广告', 'ad', NULL, 6)
ON DUPLICATE KEY UPDATE name=VALUES(name), sort_order=VALUES(sort_order);

-- ============================================
-- 4. 布局模板 (3个基础布局)
-- ============================================
INSERT INTO layout (id, name, layout_json, created_at, updated_at) VALUES
(1, '单屏全屏', '{"areas":[{"x":0,"y":0,"w":100,"h":100}]}', NOW(), NOW()),
(2, '左右分屏', '{"areas":[{"x":0,"y":0,"w":50,"h":100},{"x":50,"y":0,"w":50,"h":100}]}', NOW(), NOW()),
(3, '上下分屏', '{"areas":[{"x":0,"y":0,"w":100,"h":50},{"x":0,"y":50,"w":100,"h":50}]}', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), layout_json=VALUES(layout_json);

-- ============================================
-- 5. 内容配置
-- ============================================
INSERT INTO content_config (id, recommend_interval_sec, preview_interval_sec, recommend_count, recommend_strategy, updated_at) VALUES
(1, 6, 10, 10, 'weight_only', NOW())
ON DUPLICATE KEY UPDATE updated_at=NOW();

-- ============================================
-- 6. 示例内容数据 (每个分类3条，共18条)
-- ============================================

-- 先清理可能存在的示例数据，避免重复
DELETE FROM content_item WHERE id BETWEEN 1 AND 18;

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

-- ============================================
-- 7. 示例志愿活动 (2个示例活动)
-- ============================================

-- 先清理可能存在的示例活动，避免重复
DELETE FROM activity WHERE id BETWEEN 1 AND 2;

INSERT INTO activity (id, title, description, location, start_time, end_time, capacity, checkin_code, created_at, updated_at) VALUES
(1, '情暖三湘·志愿同行', '聚焦老年人尤其是空巢、独居老人的急难愁盼问题，开展慰问陪护、清洁卫生等志愿服务。', 
'敬老院、养老院等', '2026-01-15 09:00:00', '2026-01-15 17:00:00', 50, '382388', NOW(), NOW()),
(2, '环保志愿行动', '组织志愿者参与城市环境清洁、垃圾分类宣传等活动。', 
'市民广场', '2026-02-01 08:30:00', '2026-02-01 12:00:00', 30, '123456', NOW(), NOW());

-- ============================================
-- 完成提示
-- ============================================
SELECT '数据初始化完成！' AS message;
SELECT COUNT(*) AS content_count FROM content_item;
SELECT COUNT(*) AS activity_count FROM activity;
