-- ============================================
-- 志愿者系统内容示例数据（含封面图片）
-- 执行方式: 在MySQL Workbench中打开此文件执行
-- ============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE volunteer;

-- 清空现有内容
DELETE FROM content_item;

-- ============================================
-- 分类1: 文明XX (id=1) - 文明创建相关新闻资讯
-- ============================================
INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(1, '我市荣获全国文明城市称号', '经过全市人民共同努力，我市成功创建全国文明城市', 
'近日，中央文明办公布了新一届全国文明城市名单，我市凭借优异的创建成绩，成功入选全国文明城市。这是全市人民共同努力的结果，也是我市精神文明建设的重要里程碑。',
'https://picsum.photos/seed/city1/400/300',
1, NOW(), NOW(), NOW());

INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(1, '文明交通志愿服务在行动', '千名志愿者走上街头，倡导文明出行', 
'为进一步提升市民文明交通意识，我市组织开展"文明交通志愿服务月"活动。来自全市各行各业的1000余名志愿者走上街头，在主要交通路口开展文明劝导服务。',
'https://picsum.photos/seed/traffic1/400/300',
1, NOW(), NOW(), NOW());

INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(1, '社区文明实践站揭牌成立', '打通服务群众最后一公里', 
'今日，我市首批10个新时代文明实践站正式揭牌成立。文明实践站将整合各类资源，开展理论宣讲、文化服务等志愿服务活动。',
'https://picsum.photos/seed/community1/400/300',
1, NOW(), NOW(), NOW());

-- ============================================
-- 分类2: XX志愿者APP (id=2)
-- ============================================
INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(2, '志愿者APP全新上线', '一键报名，轻松参与志愿服务', 
'志愿者APP是我市官方志愿服务移动平台，集活动报名、签到打卡、时长统计于一体。下载APP，开启您的志愿服务之旅！',
'https://picsum.photos/seed/app1/400/300',
1, NOW(), NOW(), NOW());

INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(2, 'APP新功能：活动地图上线', '附近志愿活动一目了然', 
'志愿者APP新增活动地图功能，可以查看附近正在进行的志愿活动。点击地图标记即可查看活动详情并一键报名。',
'https://picsum.photos/seed/map1/400/300',
1, NOW(), NOW(), NOW());

INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(2, '本周热门志愿招募', '多个志愿岗位等你来', 
'本周新增志愿岗位：社区助老服务、图书馆导读、环保宣传等。欢迎广大志愿者踊跃报名参与！',
'https://picsum.photos/seed/recruit1/400/300',
1, NOW(), NOW(), NOW());

-- ============================================
-- 分类3: XX志愿者网 (id=3)
-- ============================================
INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(3, '志愿者网平台介绍', '官方志愿服务信息发布平台', 
'志愿者网是我市志愿服务工作的官方门户网站，提供志愿者注册、活动发布、新闻资讯等功能。',
'https://picsum.photos/seed/web1/400/300',
1, NOW(), NOW(), NOW());

INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(3, '2024年度优秀志愿者表彰', '百名优秀志愿者受表彰', 
'在2024年度志愿服务总结大会上，100名优秀志愿者受到表彰。他们用实际行动诠释了志愿精神。',
'https://picsum.photos/seed/award1/400/300',
1, NOW(), NOW(), NOW());

INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(3, '志愿服务专题：关爱空巢老人', '用爱心温暖每一位老人', 
'关爱空巢老人专题活动持续开展中，志愿者们定期走访社区空巢老人，提供生活照料服务。',
'https://picsum.photos/seed/elderly1/400/300',
1, NOW(), NOW(), NOW());

-- ============================================
-- 分类4: 雷锋热线 (id=4)
-- ============================================
INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(4, '雷锋热线服务指南', '24小时志愿服务热线', 
'雷锋热线是我市志愿服务求助与帮扶的桥梁，拨打热线即可获得志愿服务帮助。',
'https://picsum.photos/seed/hotline1/400/300',
1, NOW(), NOW(), NOW());

INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(4, '热线帮扶案例：为独居老人送温暖', '志愿者上门帮助独居老人', 
'张奶奶今年82岁，独居在家。通过雷锋热线求助后，志愿者定期上门帮助采购生活用品、打扫卫生。',
'https://picsum.photos/seed/help1/400/300',
1, NOW(), NOW(), NOW());

INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(4, '品牌活动：学雷锋志愿服务月', '传承雷锋精神，践行志愿服务', 
'每年3月是学雷锋志愿服务月，全市开展形式多样的志愿服务活动。',
'https://picsum.photos/seed/leifeng1/400/300',
1, NOW(), NOW(), NOW());

-- ============================================
-- 分类5: 公益活动 (id=5)
-- ============================================
INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(5, '让轮椅飞：无障碍出行公益行动', '帮助残障人士实现出行梦想', 
'"让轮椅飞"是我市品牌公益项目，旨在帮助残障人士无障碍出行，让他们感受城市的美好。',
'https://picsum.photos/seed/wheelchair1/400/300',
1, NOW(), NOW(), NOW());

INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(5, '名师一堂课：公益教育进社区', '优质教育资源惠及更多家庭', 
'"名师一堂课"邀请优秀教师走进社区，为孩子们提供免费辅导，帮助提升学习成绩。',
'https://picsum.photos/seed/teacher1/400/300',
1, NOW(), NOW(), NOW());

INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(5, '睡前讲故事：陪伴留守儿童成长', '用故事温暖童年', 
'"睡前讲故事"项目通过视频连线，为留守儿童讲述睡前故事，用温暖的声音陪伴他们成长。',
'https://picsum.photos/seed/story1/400/300',
1, NOW(), NOW(), NOW());

-- ============================================
-- 分类6: 公益广告 (id=6)
-- ============================================
INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(6, '图说我们的价值观：诚信篇', '诚信是金，立身之本', 
'诚信是中华民族的传统美德，是社会主义核心价值观的重要内容。让我们做诚实守信的人。',
'https://picsum.photos/seed/honest1/400/300',
1, NOW(), NOW(), NOW());

INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(6, '讲文明树新风：文明餐桌', '光盘行动，从我做起', 
'珍惜粮食，杜绝浪费。让我们一起践行"光盘行动"，做文明餐桌的倡导者。',
'https://picsum.photos/seed/food1/400/300',
1, NOW(), NOW(), NOW());

INSERT INTO content_item (category_id, title, summary, body, cover_url, published, publish_time, created_at, updated_at) VALUES
(6, '公益广告：关爱未成年人', '呵护成长，守护未来', 
'未成年人是祖国的花朵，让我们共同关爱未成年人健康成长，守护他们的美好未来。',
'https://picsum.photos/seed/children1/400/300',
1, NOW(), NOW(), NOW());

-- 验证插入结果
SELECT category_id, title, cover_url FROM content_item;
