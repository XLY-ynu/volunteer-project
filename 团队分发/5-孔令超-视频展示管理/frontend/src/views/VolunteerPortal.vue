<template>
  <div class="volunteer-portal">
    <!-- 顶部导航栏 -->
    <header class="portal-header">
      <div class="header-left">
        <div class="logo">
          <span class="logo-icon">🤝</span>
          <span class="logo-text">志愿者服务中心</span>
        </div>
        <nav class="nav-links">
          <a href="#home" @click.prevent="scrollTo('home')">首页</a>
          <a href="#activities" @click.prevent="scrollTo('activities')">活动报名</a>
          <a href="#content" @click.prevent="scrollTo('content')">资讯中心</a>
          <a href="#videos" @click.prevent="scrollTo('videos')">媒体展示</a>
          <a href="#orgs" @click.prevent="scrollTo('orgs')">加入组织</a>
        </nav>
      </div>
      <div class="header-right">
        <template v-if="!isLoggedIn">
          <el-button text @click="showLoginDialog = true">登录</el-button>
          <el-button type="primary" @click="showRegisterDialog = true">注册</el-button>
        </template>
        <template v-else>
          <el-badge :value="unreadCount" :hidden="!unreadCount" type="danger">
            <el-button text @click="scrollTo('messages')">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>
          <el-dropdown @command="handleUserCommand">
            <span class="user-info">
              <el-avatar :size="32" class="user-avatar">{{ userInitial }}</el-avatar>
              <span class="user-name">{{ userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="signups">我的报名</el-dropdown-item>
                <el-dropdown-item command="checkin">活动签到</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </header>

    <!-- Hero 区域 -->
    <section class="hero" id="home">
      <div class="hero-content">
        <h1>让志愿服务更有温度</h1>
        <p class="hero-desc">参与公益活动，传递爱心力量，共建美好社会</p>
        <div class="hero-stats">
          <div class="stat-item">
            <div class="stat-value">{{ stats.volunteerCount || 0 }}</div>
            <div class="stat-label">注册志愿者</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.activityCount || 0 }}</div>
            <div class="stat-label">公益活动</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.serviceHours || 0 }}</div>
            <div class="stat-label">服务时长</div>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="scrollTo('activities')">
            <el-icon><Calendar /></el-icon>
            立即报名
          </el-button>
          <el-button size="large" @click="isLoggedIn ? showCheckinDialog = true : (showLoginDialog = true)">
            <el-icon><Location /></el-icon>
            活动签到
          </el-button>
        </div>
      </div>
      <div class="hero-image">
        <div class="hero-card">
          <div class="card-icon">🌟</div>
          <div class="card-text">奉献 · 友爱 · 互助 · 进步</div>
        </div>
      </div>
    </section>

    <!-- 活动报名区域 -->
    <section class="activities-section" id="activities">
      <div class="section-header">
        <h2>热门活动</h2>
        <p>参与志愿服务，贡献你的力量</p>
      </div>
      <div class="activities-grid">
        <el-card v-for="act in activities" :key="act.id" class="activity-card" shadow="hover">
          <div class="activity-cover" @click="openActivityDetail(act)">
            <img v-if="act.coverUrl" :src="act.coverUrl" :alt="act.title" />
            <div v-else class="cover-placeholder">
              <el-icon :size="40"><Calendar /></el-icon>
            </div>
            <div class="activity-status" :class="getActivityStatus(act).class">
              {{ getActivityStatus(act).text }}
            </div>
          </div>
          <div class="activity-info">
            <h3 class="activity-title" @click="openActivityDetail(act)">{{ act.title }}</h3>
            <div class="activity-meta">
              <span><el-icon><Location /></el-icon>{{ act.location || '待定' }}</span>
              <span><el-icon><Clock /></el-icon>{{ formatDate(act.startTime) }}</span>
            </div>
            <p class="activity-desc">{{ act.description || '暂无描述' }}</p>
            <div class="activity-footer">
              <span class="capacity">
                <el-icon><User /></el-icon>
                {{ act.signupCount || 0 }}/{{ act.capacity || '不限' }}
              </span>
              <div class="action-buttons">
                <el-button 
                  size="small"
                  @click="openActivityDetail(act)"
                >
                  查看详情
                </el-button>
                <el-button 
                  v-if="canCancelSignup(act)"
                  type="danger" 
                  size="small"
                  plain
                  @click="handleCancelSignup(act)"
                >
                  取消报名
                </el-button>
                <el-button 
                  v-else
                  type="primary" 
                  size="small"
                  :disabled="!canSignup(act)"
                  @click="handleSignup(act)"
                >
                  {{ getSignupButtonText(act) }}
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      <div class="section-more" v-if="activityTotal > activities.length">
        <el-button @click="loadMoreActivities">加载更多</el-button>
      </div>
    </section>

    <!-- 资讯中心 -->
    <section class="content-section" id="content">
      <div class="section-header">
        <h2>资讯中心</h2>
        <p>了解最新志愿服务动态</p>
      </div>
      <div class="content-layout">
        <div class="category-sidebar">
          <div 
            v-for="cat in categories" 
            :key="cat.id" 
            class="category-item"
            :class="{ active: activeCategory === cat.id }"
            @click="selectCategory(cat)"
          >
            {{ cat.name }}
          </div>
        </div>
        <div class="content-list">
          <el-skeleton v-if="contentLoading" :rows="4" animated />
          <template v-else>
            <div v-for="item in contentList" :key="item.id" class="content-item" @click="openContentDetail(item)">
              <div class="content-thumb" v-if="item.coverUrl">
                <img :src="item.coverUrl" :alt="item.title" />
              </div>
              <div class="content-info">
                <h4>{{ item.title }}</h4>
                <p>{{ item.summary || '点击查看详情' }}</p>
                <div class="content-meta">
                  <span>{{ formatDate(item.publishTime || item.createdAt) }}</span>
                </div>
              </div>
            </div>
            <el-empty v-if="!contentList.length" description="暂无内容" />
          </template>
        </div>
      </div>
    </section>

    <!-- 媒体展示 -->
    <section class="media-section" id="videos">
      <div class="section-header">
        <h2>媒体展示</h2>
        <p>精彩志愿服务图片与视频</p>
      </div>
      <div class="media-filter">
        <el-radio-group v-model="mediaTypeFilter" size="small" @change="loadMedia">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="image">图片</el-radio-button>
          <el-radio-button label="video">视频</el-radio-button>
        </el-radio-group>
      </div>
      <div class="media-grid">
        <div v-for="media in mediaList" :key="media.id" class="media-card" @click="openMedia(media)">
          <div class="media-thumb">
            <img v-if="media.thumbUrl || media.coverUrl || (media.type === 'image' && media.url)" 
                 :src="media.thumbUrl || media.coverUrl || media.url" 
                 :alt="media.name" />
            <div v-else class="thumb-placeholder">
              <el-icon :size="40"><VideoPlay v-if="media.type === 'video'" /><Picture v-else /></el-icon>
            </div>
            <div v-if="media.type === 'video'" class="media-duration">{{ formatDuration(media.durationSeconds) }}</div>
            <div class="media-type-badge" :class="media.type">
              <el-icon v-if="media.type === 'video'"><VideoPlay /></el-icon>
              <el-icon v-else><Picture /></el-icon>
            </div>
            <div v-if="media.type === 'video'" class="play-overlay">
              <el-icon :size="48"><VideoPlay /></el-icon>
            </div>
          </div>
          <div class="media-info">
            <h4>{{ media.name }}</h4>
          </div>
        </div>
      </div>
      <div class="section-more" v-if="mediaTotal > mediaList.length">
        <el-button @click="loadMoreMedia">加载更多</el-button>
      </div>
    </section>

    <!-- 加入组织区域 -->
    <section class="orgs-section" id="orgs">
      <div class="section-header">
        <h2>志愿者组织</h2>
        <p>加入志愿者组织，参与更多公益活动</p>
      </div>
      <div class="orgs-grid">
        <el-card v-for="org in orgList" :key="org.id" class="org-card" shadow="hover">
          <div class="org-header">
            <div class="org-logo">{{ org.name?.charAt(0) || '组' }}</div>
            <div class="org-info">
              <h3>{{ org.name }}</h3>
              <p class="org-contact">{{ org.contactName }} · {{ org.contactPhone }}</p>
            </div>
          </div>
          <p class="org-desc">{{ org.description || '暂无简介' }}</p>
          <div class="org-footer">
            <el-button 
              v-if="getOrgJoinStatus(org.id) === 'approved'"
              type="success" 
              size="small"
              disabled
            >
              已加入
            </el-button>
            <el-button 
              v-else-if="getOrgJoinStatus(org.id) === 'pending'"
              type="warning" 
              size="small"
              disabled
            >
              审核中
            </el-button>
            <el-button 
              v-else
              type="primary" 
              size="small"
              @click="handleJoinOrg(org)"
            >
              申请加入
            </el-button>
          </div>
        </el-card>
        <el-empty v-if="!orgList.length" description="暂无志愿者组织" />
      </div>
      
      <!-- 我加入的组织 -->
      <div v-if="isLoggedIn && myOrgs.length > 0" class="my-orgs-section">
        <h3>我加入的组织</h3>
        <el-table :data="myOrgs" size="small">
          <el-table-column prop="orgName" label="组织名称" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'approved' ? 'success' : row.status === 'pending' ? 'warning' : 'danger'" size="small">
                {{ row.status === 'approved' ? '已通过' : row.status === 'pending' ? '审核中' : '已拒绝' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="joinedAt" label="加入时间" width="160">
            <template #default="{ row }">{{ row.joinedAt ? formatDateTime(row.joinedAt) : '-' }}</template>
          </el-table-column>
        </el-table>
      </div>
    </section>

    <!-- 个人中心区域（登录后显示） -->
    <section v-if="isLoggedIn" class="profile-section" id="messages">
      <div class="section-header">
        <h2>个人中心</h2>
        <p>管理您的志愿服务信息</p>
      </div>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="profile-card">
            <div class="profile-header">
              <el-avatar :size="64" class="profile-avatar">{{ userInitial }}</el-avatar>
              <div class="profile-info">
                <h3>{{ profile?.name || '志愿者' }}</h3>
                <p>{{ profile?.phone }}</p>
                <el-tag size="small" :type="statusTagType(profile?.status)">
                  {{ statusLabel(profile?.status) }}
                </el-tag>
              </div>
            </div>
            <div class="profile-stats">
              <div class="p-stat">
                <div class="p-stat-value">{{ myStats.total || 0 }}</div>
                <div class="p-stat-label">报名活动</div>
              </div>
              <div class="p-stat">
                <div class="p-stat-value">{{ myStats.checkedIn || 0 }}</div>
                <div class="p-stat-label">已签到</div>
              </div>
              <div class="p-stat">
                <div class="p-stat-value">{{ myStats.hours || 0 }}</div>
                <div class="p-stat-label">服务时长</div>
              </div>
            </div>
            <div class="profile-actions">
              <el-button size="small" @click="openProfileDialog">编辑资料</el-button>
              <el-button size="small" @click="showPasswordDialog = true">修改密码</el-button>
            </div>
          </el-card>
        </el-col>
        <el-col :span="16">
          <el-card class="message-card">
            <template #header>
              <div class="card-header">
                <span>消息通知</span>
                <el-badge :value="unreadCount" :hidden="!unreadCount" type="danger" />
                <el-button text size="small" @click="markAllRead">全部已读</el-button>
              </div>
            </template>
            <el-table :data="messages" size="small" max-height="300" @row-click="openMessageDetail" style="cursor: pointer">
              <el-table-column prop="title" label="标题" min-width="180">
                <template #default="{ row }">
                  <span :class="{ 'unread-title': !row.read }">{{ row.title }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="type" label="类型" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :type="getMessageTypeTag(row.type)">
                    {{ getMessageTypeLabel(row.type) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="时间" width="140">
                <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column prop="read" label="状态" width="70">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.read ? 'info' : 'warning'">
                    {{ row.read ? '已读' : '未读' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
          <el-card class="signup-card" style="margin-top: 16px">
            <template #header>
              <span>我的报名</span>
            </template>
            <el-table :data="mySignups" size="small" max-height="250">
              <el-table-column prop="title" label="活动" min-width="180" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :type="signupStatusType(row.status)">
                    {{ formatSignupStatus(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="报名时间" width="160">
                <template #default="{ row }">{{ formatDateTime(row.signupTime) }}</template>
              </el-table-column>
              <el-table-column label="签到时间" width="160">
                <template #default="{ row }">{{ row.checkinTime ? formatDateTime(row.checkinTime) : '-' }}</template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 页脚 -->
    <footer class="portal-footer">
      <div class="footer-content">
        <div class="footer-brand">
          <span class="logo-icon">🤝</span>
          <span>志愿者服务中心</span>
        </div>
        <div class="footer-links">
          <a href="#home" @click.prevent="scrollTo('home')">首页</a>
          <a href="#activities" @click.prevent="scrollTo('activities')">活动报名</a>
          <a href="#content" @click.prevent="scrollTo('content')">资讯中心</a>
          <a href="#videos" @click.prevent="scrollTo('videos')">媒体展示</a>
        </div>
        <div class="footer-copyright">
          © 2025 志愿者多媒体平台 · 让志愿服务更有温度
        </div>
      </div>
    </footer>

    <!-- 登录对话框 -->
    <el-dialog v-model="showLoginDialog" title="志愿者登录" width="400px" :close-on-click-modal="false">
      <el-form :model="loginForm" label-width="80px">
        <el-form-item label="手机号">
          <el-input v-model="loginForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLoginDialog = false">取消</el-button>
        <el-button type="primary" @click="handleLogin" :loading="loginLoading">登录</el-button>
      </template>
      <div class="dialog-footer-link">
        还没有账号？<el-button text type="primary" @click="showLoginDialog = false; showRegisterDialog = true">立即注册</el-button>
      </div>
    </el-dialog>

    <!-- 注册对话框 -->
    <el-dialog v-model="showRegisterDialog" title="志愿者注册" width="450px" :close-on-click-modal="false">
      <el-form :model="registerForm" label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="registerForm.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="registerForm.password" type="password" placeholder="8-20位，包含字母和数字" show-password />
          <div class="form-hint">密码需8-20位，且必须同时包含字母和数字</div>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="registerForm.email" placeholder="选填" />
        </el-form-item>
        <el-form-item label="所属组织">
          <el-input v-model="registerForm.organization" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取消</el-button>
        <el-button type="primary" @click="handleRegister" :loading="registerLoading">注册</el-button>
      </template>
      <div class="dialog-footer-link">
        已有账号？<el-button text type="primary" @click="showRegisterDialog = false; showLoginDialog = true">立即登录</el-button>
      </div>
    </el-dialog>

    <!-- 签到对话框 -->
    <el-dialog v-model="showCheckinDialog" title="活动签到" width="400px">
      <el-form :model="checkinForm" label-width="80px">
        <el-form-item label="签到码">
          <el-input v-model="checkinForm.code" placeholder="请输入活动签到码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCheckinDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCheckin" :loading="checkinLoading">签到</el-button>
      </template>
    </el-dialog>

    <!-- 报名对话框 -->
    <el-dialog v-model="showSignupDialog" title="活动报名" width="450px">
      <div class="signup-activity-info" v-if="currentActivity">
        <h4>{{ currentActivity.title }}</h4>
        <p><el-icon><Location /></el-icon>{{ currentActivity.location || '待定' }}</p>
        <p><el-icon><Clock /></el-icon>{{ formatDate(currentActivity.startTime) }} - {{ formatDate(currentActivity.endTime) }}</p>
      </div>
      <el-form v-if="!isLoggedIn" :model="signupForm" label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="signupForm.name" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="signupForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="signupForm.email" />
        </el-form-item>
      </el-form>
      <div v-else class="signup-confirm">
        <p>确认以 <strong>{{ profile?.name }}</strong> ({{ profile?.phone }}) 的身份报名此活动？</p>
      </div>
      <template #footer>
        <el-button @click="showSignupDialog = false">取消</el-button>
        <el-button type="primary" @click="submitSignup" :loading="signupLoading">确认报名</el-button>
      </template>
    </el-dialog>

    <!-- 视频播放对话框 -->
    <el-dialog v-model="showVideoDialog" :title="currentVideo?.name || '视频播放'" width="800px" destroy-on-close>
      <video v-if="currentVideo" controls autoplay style="width: 100%; border-radius: 8px" :src="currentVideo.url"></video>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="showImageDialog" :title="currentImage?.name || '图片预览'" width="800px" class="image-preview-dialog">
      <div class="image-preview-container" v-if="currentImage">
        <el-image 
          :src="currentImage.url" 
          :preview-src-list="[currentImage.url]"
          fit="contain"
          class="preview-image"
        />
      </div>
    </el-dialog>

    <!-- 内容详情对话框 -->
    <el-dialog v-model="showContentDialog" :title="currentContent?.title || '详情'" width="800px" class="content-detail-dialog">
      <div class="content-detail" v-if="currentContent">
        <div class="content-detail-header">
          <div class="content-detail-time">
            <el-icon><Clock /></el-icon>
            {{ formatDateTime(currentContent.publishTime || currentContent.createdAt) }}
          </div>
        </div>
        <div v-if="currentContent.coverUrl" class="content-detail-cover" @click="showImagePreview = true">
          <el-image 
            :src="currentContent.coverUrl" 
            :preview-src-list="[currentContent.coverUrl]"
            fit="cover"
            class="detail-cover-img"
          >
            <template #placeholder>
              <div class="image-loading">加载中...</div>
            </template>
          </el-image>
          <div class="cover-zoom-hint">
            <el-icon><ZoomIn /></el-icon>
            点击放大
          </div>
        </div>
        <div class="content-detail-body" v-html="currentContent.body || currentContent.summary || '暂无内容'"></div>
      </div>
    </el-dialog>

    <!-- 活动详情对话框 -->
    <el-dialog v-model="showActivityDetailDialog" :title="currentActivityDetail?.title || '活动详情'" width="700px">
      <div class="activity-detail" v-if="currentActivityDetail">
        <div class="activity-detail-cover" v-if="currentActivityDetail.coverUrl">
          <el-image 
            :src="currentActivityDetail.coverUrl" 
            :preview-src-list="[currentActivityDetail.coverUrl]"
            fit="cover"
            :alt="currentActivityDetail.title"
            class="activity-cover-img"
          />
        </div>
        <div class="activity-detail-header">
          <div class="activity-detail-status" :class="getActivityStatus(currentActivityDetail).class">
            {{ getActivityStatus(currentActivityDetail).text }}
          </div>
          <h2>{{ currentActivityDetail.title }}</h2>
        </div>
        <div class="activity-detail-info">
          <div class="info-row">
            <el-icon><Location /></el-icon>
            <span class="info-label">活动地点：</span>
            <span>{{ currentActivityDetail.location || '待定' }}</span>
          </div>
          <div class="info-row">
            <el-icon><Clock /></el-icon>
            <span class="info-label">开始时间：</span>
            <span>{{ formatDateTime(currentActivityDetail.startTime) }}</span>
          </div>
          <div class="info-row">
            <el-icon><Clock /></el-icon>
            <span class="info-label">结束时间：</span>
            <span>{{ formatDateTime(currentActivityDetail.endTime) }}</span>
          </div>
          <div class="info-row">
            <el-icon><User /></el-icon>
            <span class="info-label">报名人数：</span>
            <span>{{ currentActivityDetail.signupCount || 0 }} / {{ currentActivityDetail.capacity || '不限' }}</span>
          </div>
        </div>
        <div class="activity-detail-desc">
          <h4>活动介绍</h4>
          <p>{{ currentActivityDetail.description || '暂无活动介绍' }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="showActivityDetailDialog = false">关闭</el-button>
        <el-button 
          v-if="canCancelSignup(currentActivityDetail)"
          type="danger" 
          plain
          @click="handleCancelSignupFromDetail"
        >
          取消报名
        </el-button>
        <el-button 
          v-else-if="canSignup(currentActivityDetail)"
          type="primary" 
          @click="handleSignupFromDetail"
        >
          立即报名
        </el-button>
        <el-button v-else type="info" disabled>
          {{ getSignupButtonText(currentActivityDetail) }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="showProfileDialog" title="编辑资料" width="450px">
      <el-form :model="profileForm" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="profileForm.name" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profileForm.email" />
        </el-form-item>
        <el-form-item label="所属组织">
          <el-input v-model="profileForm.organization" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showProfileDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="90px">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="changePassword">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 消息详情对话框 -->
    <el-dialog v-model="showMessageDetailDialog" :title="currentMessage?.title || '消息详情'" width="500px" class="message-detail-dialog">
      <div class="message-detail" v-if="currentMessage">
        <div class="message-detail-header">
          <el-tag size="small" :type="getMessageTypeTag(currentMessage.type)">
            {{ getMessageTypeLabel(currentMessage.type) }}
          </el-tag>
          <span class="message-time">{{ formatDateTime(currentMessage.createdAt) }}</span>
        </div>
        <div class="message-detail-content">
          {{ currentMessage.message || '暂无详细内容' }}
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showMessageDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Bell, ArrowDown, Calendar, Location, Clock, User, VideoPlay, ZoomIn, Picture } from '@element-plus/icons-vue';
import {
  getPublicActivities,
  getPublicCategories,
  getPublicContent,
  getPublicMedia,
  portalLoginApi as portalLogin,
  portalRegisterApi as portalRegister,
  portalProfile as fetchProfile,
  portalSignups,
  portalCheckin,
  activitySignup,
  getPortalMessages,
  markPortalMessagesReadApi as markPortalMessagesRead,
  cancelActivitySignup
} from '../api';

// 状态
const isLoggedIn = ref(false);
const profile = ref<any>(null);
const portalToken = ref('');

// 对话框
const showLoginDialog = ref(false);
const showRegisterDialog = ref(false);
const showCheckinDialog = ref(false);
const showSignupDialog = ref(false);
const showVideoDialog = ref(false);
const showContentDialog = ref(false);
const showActivityDetailDialog = ref(false);
const showImagePreview = ref(false);
const showImageDialog = ref(false);
const showProfileDialog = ref(false);
const showPasswordDialog = ref(false);
const showMessageDetailDialog = ref(false);

// 表单
const loginForm = ref({ phone: '', password: '' });
const registerForm = ref({ name: '', phone: '', password: '', email: '', organization: '' });
const checkinForm = ref({ code: '' });
const signupForm = ref({ name: '', phone: '', email: '' });
const profileForm = ref({ name: '', email: '', organization: '' });
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' });

// 加载状态
const loginLoading = ref(false);
const registerLoading = ref(false);
const checkinLoading = ref(false);
const signupLoading = ref(false);
const contentLoading = ref(false);

// 数据
const stats = ref({ volunteerCount: 1280, activityCount: 56, serviceHours: 8640 });
const activities = ref<any[]>([]);
const activityTotal = ref(0);
const activityPage = ref(1);
const categories = ref<any[]>([]);
const activeCategory = ref<number | null>(null);
const contentList = ref<any[]>([]);
const mediaList = ref<any[]>([]);
const mediaTypeFilter = ref('');
const mediaPage = ref(1);
const mediaTotal = ref(0);
const messages = ref<any[]>([]);
const mySignups = ref<any[]>([]);
const myStats = ref({ total: 0, checkedIn: 0, hours: 0 });
const unreadCount = ref(0);
const orgList = ref<any[]>([]);
const myOrgs = ref<any[]>([]);

const currentActivity = ref<any>(null);
const currentVideo = ref<any>(null);
const currentContent = ref<any>(null);
const currentActivityDetail = ref<any>(null);
const currentImage = ref<any>(null);
const currentMessage = ref<any>(null);

// 计算属性
const userName = computed(() => profile.value?.name || '志愿者');
const userInitial = computed(() => (profile.value?.name || '志')[0]);

// 方法
const scrollTo = (id: string) => {
  const el = document.getElementById(id);
  if (el) el.scrollIntoView({ behavior: 'smooth' });
};

const handleUserCommand = (cmd: string) => {
  if (cmd === 'logout') {
    isLoggedIn.value = false;
    profile.value = null;
    portalToken.value = '';
    localStorage.removeItem('portalToken');
    stopStatusCheck(); // 退出时停止检查
    ElMessage.success('已退出登录');
  } else if (cmd === 'profile') {
    showProfileDialog.value = true;
    profileForm.value = { 
      name: profile.value?.name || '', 
      email: profile.value?.email || '', 
      organization: profile.value?.organization || '' 
    };
  } else if (cmd === 'signups') {
    scrollTo('messages');
  } else if (cmd === 'checkin') {
    showCheckinDialog.value = true;
  }
};

const handleLogin = async () => {
  if (!loginForm.value.phone || !loginForm.value.password) {
    ElMessage.warning('请输入手机号和密码');
    return;
  }
  // 验证手机号格式
  if (!/^1[3-9]\d{9}$/.test(loginForm.value.phone)) {
    ElMessage.warning('请输入正确的手机号格式');
    return;
  }
  loginLoading.value = true;
  try {
    const res: any = await portalLogin(loginForm.value.phone, loginForm.value.password);
    if (res.data?.success) {
      portalToken.value = res.data.data.token;
      localStorage.setItem('portalToken', portalToken.value);
      isLoggedIn.value = true;
      showLoginDialog.value = false;
      ElMessage.success('登录成功');
      await loadProfile();
      await loadMyData();
      startStatusCheck(); // 登录成功后开始定时检查
    } else {
      // 显示后端返回的具体错误信息
      const msg = res.data?.message || '登录失败';
      ElMessage.error(msg);
    }
  } catch (e: any) {
    // 解析错误信息，提供友好提示
    const errMsg = e?.response?.data?.message || e?.response?.data?.error || '';
    if (errMsg.includes('用户不存在')) {
      ElMessage.error('该手机号未注册，请先注册');
    } else if (errMsg.includes('密码错误') || errMsg.includes('密码不正确')) {
      ElMessage.error('密码错误，请重新输入');
    } else if (errMsg.includes('审核中')) {
      ElMessage.warning('账号审核中，请等待管理员审核');
    } else if (errMsg.includes('审核未通过') || errMsg.includes('拒绝')) {
      ElMessage.error('账号审核未通过，请联系管理员');
    } else if (errMsg.includes('禁用')) {
      ElMessage.error('账号已被禁用，请联系管理员');
    } else if (errMsg.includes('无法登录志愿者端')) {
      ElMessage.error('该账号不是志愿者账号');
    } else if (errMsg.includes('手机号格式')) {
      ElMessage.warning('手机号格式不正确');
    } else {
      ElMessage.error(errMsg || '登录失败，请检查手机号和密码');
    }
  } finally {
    loginLoading.value = false;
  }
};

const handleRegister = async () => {
  if (!registerForm.value.name || !registerForm.value.phone || !registerForm.value.password) {
    ElMessage.warning('请填写必填项');
    return;
  }
  // 验证手机号格式
  if (!/^1[3-9]\d{9}$/.test(registerForm.value.phone)) {
    ElMessage.warning('请输入正确的手机号');
    return;
  }
  // 验证密码格式：8-20位，包含字母和数字
  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,20}$/.test(registerForm.value.password)) {
    ElMessage.warning('密码需8-20位，且必须同时包含字母和数字');
    return;
  }
  registerLoading.value = true;
  try {
    const res: any = await portalRegister(registerForm.value);
    if (res.data?.success) {
      ElMessage.success('注册成功，请登录');
      showRegisterDialog.value = false;
      showLoginDialog.value = true;
      loginForm.value.phone = registerForm.value.phone;
    } else {
      ElMessage.error(res.data?.message || '注册失败');
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '注册失败');
  } finally {
    registerLoading.value = false;
  }
};

const handleCheckin = async () => {
  if (!checkinForm.value.code) {
    ElMessage.warning('请输入签到码');
    return;
  }
  checkinLoading.value = true;
  try {
    const res: any = await portalCheckin(checkinForm.value.code, portalToken.value);
    if (res.data?.success) {
      ElMessage.success('签到成功');
      showCheckinDialog.value = false;
      checkinForm.value.code = '';
      await loadMyData();
    } else {
      ElMessage.error(res.data?.message || '签到失败');
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '签到失败');
  } finally {
    checkinLoading.value = false;
  }
};

const handleSignup = (act: any) => {
  currentActivity.value = act;
  // 未登录时先弹出登录框
  if (!isLoggedIn.value) {
    showLoginDialog.value = true;
    return;
  }
  signupForm.value = { name: profile.value?.name || '', phone: profile.value?.phone || '', email: profile.value?.email || '' };
  showSignupDialog.value = true;
};

const submitSignup = async () => {
  signupLoading.value = true;
  try {
    const payload = isLoggedIn.value 
      ? { activityId: currentActivity.value.id }
      : { activityId: currentActivity.value.id, ...signupForm.value };
    const res: any = await activitySignup(payload, portalToken.value);
    if (res.data?.success) {
      ElMessage.success('报名成功');
      showSignupDialog.value = false;
      // 刷新活动列表以更新报名人数
      await loadActivities();
      if (isLoggedIn.value) await loadMyData();
    } else {
      ElMessage.error(res.data?.message || '报名失败');
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '报名失败');
  } finally {
    signupLoading.value = false;
  }
};

const loadProfile = async () => {
  try {
    const res: any = await fetchProfile(portalToken.value);
    if (res.data?.success) {
      profile.value = res.data.data;
      // 检查账号状态
      if (profile.value?.status === 'rejected') {
        handleAccountDisabled('账号审核未通过');
      }
    }
  } catch (e: any) {
    // 检查是否是账号被禁用
    const errMsg = e?.response?.data?.message || '';
    if (e?.response?.status === 401 || e?.response?.status === 403 || errMsg.includes('禁用') || errMsg.includes('权限')) {
      handleAccountDisabled('账号异常，请重新登录');
    }
  }
};

// 处理账号被禁用的情况
const handleAccountDisabled = (msg: string) => {
  ElMessage.error(msg);
  isLoggedIn.value = false;
  profile.value = null;
  portalToken.value = '';
  localStorage.removeItem('portalToken');
  stopStatusCheck(); // 停止状态检查
};

const loadMyData = async () => {
  try {
    const [signupsRes, msgRes]: any[] = await Promise.all([
      portalSignups(portalToken.value),
      getPortalMessages(portalToken.value)
    ]);
    if (signupsRes.data?.success) {
      mySignups.value = signupsRes.data.data || [];
      myStats.value.total = mySignups.value.length;
      myStats.value.checkedIn = mySignups.value.filter((s: any) => s.checkinTime).length;
    }
    if (msgRes.data?.success) {
      messages.value = msgRes.data.data?.records || msgRes.data.data || [];
      unreadCount.value = messages.value.filter((m: any) => !m.read).length;
    }
  } catch (e) { /* ignore */ }
};

const markAllRead = async () => {
  try {
    // 使用消息的 key 而不是 id
    const unreadKeys = messages.value.filter(m => !m.read).map(m => m.key);
    if (unreadKeys.length === 0) {
      ElMessage.info('没有未读消息');
      return;
    }
    await markPortalMessagesRead(unreadKeys, portalToken.value);
    messages.value.forEach(m => m.read = true);
    unreadCount.value = 0;
    ElMessage.success('已全部标记为已读');
  } catch (e) { /* ignore */ }
};

const loadActivities = async () => {
  try {
    const res: any = await getPublicActivities({ page: activityPage.value, size: 6 });
    if (res.data?.success) {
      activities.value = res.data.data?.records || res.data.data || [];
      activityTotal.value = res.data.data?.total || activities.value.length;
    }
  } catch (e) { /* ignore */ }
};

const loadMoreActivities = () => {
  activityPage.value++;
  loadActivities();
};

const loadCategories = async () => {
  try {
    const res: any = await getPublicCategories();
    if (res.data?.success) {
      categories.value = res.data.data || [];
      if (categories.value.length) {
        activeCategory.value = categories.value[0].id;
        loadContent();
      }
    }
  } catch (e) { /* ignore */ }
};

const selectCategory = (cat: any) => {
  activeCategory.value = cat.id;
  loadContent();
};

const loadContent = async () => {
  contentLoading.value = true;
  try {
    const res: any = await getPublicContent({ categoryId: activeCategory.value || undefined, page: 1, size: 10 });
    if (res.data?.success) {
      contentList.value = res.data.data?.records || res.data.data || [];
    }
  } catch (e) { /* ignore */ }
  contentLoading.value = false;
};

const loadMedia = async (resetOrEvent: boolean | string = true) => {
  // @change事件会传递选中的值(字符串)，需要判断是否为布尔值
  const reset = typeof resetOrEvent === 'boolean' ? resetOrEvent : true;
  try {
    if (reset) {
      mediaPage.value = 1;
    }
    const params: any = { page: mediaPage.value, size: 8 };
    if (mediaTypeFilter.value) {
      params.type = mediaTypeFilter.value;
    }
    const res: any = await getPublicMedia(params);
    if (res.data?.success) {
      const records = res.data.data?.records || res.data.data || [];
      if (reset) {
        mediaList.value = records;
      } else {
        mediaList.value = [...mediaList.value, ...records];
      }
      mediaTotal.value = res.data.data?.total || records.length;
    }
  } catch (e) { /* ignore */ }
};

const loadMoreMedia = () => {
  mediaPage.value++;
  loadMedia(false);
};

// 加载志愿者组织列表
const loadOrgs = async () => {
  try {
    const res = await fetch('/api/user-portal/orgs');
    const data = await res.json();
    if (data.success) {
      orgList.value = data.data || [];
    }
  } catch (e) { /* ignore */ }
};

// 加载我加入的组织
const loadMyOrgs = async () => {
  if (!isLoggedIn.value) return;
  try {
    const res = await fetch('/api/user-portal/my-orgs', {
      headers: { Authorization: `Bearer ${portalToken.value}` }
    });
    const data = await res.json();
    if (data.success) {
      myOrgs.value = data.data || [];
    }
  } catch (e) { /* ignore */ }
};

// 获取组织加入状态
const getOrgJoinStatus = (orgId: number) => {
  const found = myOrgs.value.find((o: any) => o.orgId === orgId);
  return found?.status || null;
};

// 申请加入组织
const handleJoinOrg = async (org: any) => {
  if (!isLoggedIn.value) {
    showLoginDialog.value = true;
    ElMessage.warning('请先登录');
    return;
  }
  try {
    const res = await fetch(`/api/user-portal/join-org/${org.id}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${portalToken.value}` }
    });
    const data = await res.json();
    if (data.success) {
      ElMessage.success('申请已提交，请等待审核');
      await loadMyOrgs();
    } else {
      ElMessage.error(data.message || '申请失败');
    }
  } catch (e) {
    ElMessage.error('申请失败');
  }
};

// 打开媒体（视频播放或图片预览）
const openMedia = (media: any) => {
  if (media.type === 'video') {
    currentVideo.value = media;
    showVideoDialog.value = true;
  } else {
    // 图片预览
    currentImage.value = media;
    showImageDialog.value = true;
  }
};

const playVideo = (media: any) => {
  currentVideo.value = media;
  showVideoDialog.value = true;
};

const openContentDetail = (item: any) => {
  currentContent.value = item;
  showContentDialog.value = true;
};

// 打开活动详情
const openActivityDetail = (act: any) => {
  currentActivityDetail.value = act;
  showActivityDetailDialog.value = true;
};

// 从详情页报名
const handleSignupFromDetail = () => {
  showActivityDetailDialog.value = false;
  handleSignup(currentActivityDetail.value);
};

// 从详情页取消报名
const handleCancelSignupFromDetail = async () => {
  await handleCancelSignup(currentActivityDetail.value);
  showActivityDetailDialog.value = false;
};

const openProfileDialog = () => {
  profileForm.value = {
    name: profile.value?.name || '',
    email: profile.value?.email || '',
    organization: profile.value?.organization || ''
  };
  showProfileDialog.value = true;
};

const saveProfile = async () => {
  try {
    const res: any = await fetch(`http://localhost:8080/api/portal/me`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${portalToken.value}`
      },
      body: JSON.stringify(profileForm.value)
    });
    const data = await res.json();
    if (data.success) {
      profile.value = { ...profile.value, ...data.data };
      ElMessage.success('资料已保存');
      showProfileDialog.value = false;
    } else {
      ElMessage.error(data.message || '保存失败');
    }
  } catch (e) {
    ElMessage.error('保存失败，请重试');
  }
};

const changePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning('请填写完整');
    return;
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次密码不一致');
    return;
  }
  // 验证新密码格式：8-20位，包含字母和数字
  const pwdRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,20}$/;
  if (!pwdRegex.test(passwordForm.value.newPassword)) {
    ElMessage.warning('新密码需8-20位，且必须同时包含字母和数字');
    return;
  }
  try {
    const res: any = await fetch(`http://localhost:8080/api/portal/auth/change-password`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${portalToken.value}`
      },
      body: JSON.stringify({
        oldPassword: passwordForm.value.oldPassword,
        newPassword: passwordForm.value.newPassword
      })
    });
    const data = await res.json();
    if (data.success) {
      ElMessage.success('密码修改成功');
      showPasswordDialog.value = false;
      passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' };
    } else {
      ElMessage.error(data.message || '修改失败');
    }
  } catch (e) {
    ElMessage.error('修改失败，请重试');
  }
};

// 工具函数
const formatDate = (date: string) => {
  if (!date) return '-';
  return new Date(date).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
};

const formatDateTime = (date: string) => {
  if (!date) return '-';
  return new Date(date).toLocaleString('zh-CN');
};

const formatDuration = (seconds: number) => {
  if (!seconds) return '--:--';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${s.toString().padStart(2, '0')}`;
};

const getActivityStatus = (act: any) => {
  const now = Date.now();
  const start = new Date(act.startTime).getTime();
  const end = new Date(act.endTime).getTime();
  // 检查是否已报名
  if (isLoggedIn.value && isSignedUp(act.id)) {
    return { text: '已报名', class: 'status-signed' };
  }
  if (now < start) return { text: '报名中', class: 'status-open' };
  if (now >= start && now <= end) return { text: '进行中', class: 'status-ongoing' };
  return { text: '已结束', class: 'status-ended' };
};

// 检查是否已报名某活动
const isSignedUp = (activityId: number) => {
  return mySignups.value.some((s: any) => s.activityId === activityId);
};

// 检查是否已签到某活动
const isCheckedIn = (activityId: number) => {
  const signup = mySignups.value.find((s: any) => s.activityId === activityId);
  return signup && signup.status === 'checked_in';
};

// 检查是否可以取消报名
const canCancelSignup = (act: any) => {
  if (!isLoggedIn.value) return false;
  if (!isSignedUp(act.id)) return false;
  if (isCheckedIn(act.id)) return false; // 已签到不能取消
  const now = Date.now();
  const start = new Date(act.startTime).getTime();
  return now < start; // 活动开始前可以取消
};

// 取消报名
const handleCancelSignup = async (act: any) => {
  try {
    await ElMessageBox.confirm(`确定取消报名活动"${act.title}"吗？`, '取消报名', { type: 'warning' });
    const res: any = await cancelActivitySignup(act.id, portalToken.value);
    if (res.data?.success) {
      ElMessage.success('已取消报名');
      await loadActivities();
      await loadMyData();
    } else {
      ElMessage.error(res.data?.message || '取消失败');
    }
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '取消失败');
    }
  }
};

const canSignup = (act: any) => {
  // 已报名则不能再报名
  if (isLoggedIn.value && isSignedUp(act.id)) {
    return false;
  }
  const now = Date.now();
  const start = new Date(act.startTime).getTime();
  return now < start;
};

const getSignupButtonText = (act: any) => {
  // 已报名显示"已报名"
  if (isLoggedIn.value && isSignedUp(act.id)) {
    return '已报名';
  }
  const now = Date.now();
  const start = new Date(act.startTime).getTime();
  const end = new Date(act.endTime).getTime();
  if (now >= end) return '已结束';
  if (now >= start && now < end) return '进行中';
  return '立即报名';
};

const statusTagType = (status: string) => {
  if (status === 'approved') return 'success';
  if (status === 'pending') return 'warning';
  if (status === 'rejected') return 'danger';
  return 'info';
};

const statusLabel = (status: string) => {
  const map: Record<string, string> = { approved: '已审核', pending: '待审核', rejected: '已拒绝' };
  return map[status] || status || '未知';
};

const signupStatusType = (status: string) => {
  if (status === 'checked_in' || status === 'checkedIn') return 'success';
  if (status === 'applied') return 'warning';
  return 'info';
};

const formatSignupStatus = (status: string) => {
  const map: Record<string, string> = {
    'applied': '已报名',
    'checked_in': '已签到',
    'checkedIn': '已签到',
    'cancelled': '已取消'
  };
  return map[status] || status || '未知';
};

// 消息类型标签
const getMessageTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    'signup': '报名',
    'checkin': '签到',
    'reminder': '提醒',
    'custom': '通知'
  };
  return map[type] || '消息';
};

const getMessageTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'signup': 'success',
    'checkin': 'primary',
    'reminder': 'warning',
    'custom': 'info'
  };
  return map[type] || 'info';
};

// 打开消息详情
const openMessageDetail = async (row: any) => {
  currentMessage.value = row;
  showMessageDetailDialog.value = true;
  // 标记为已读
  if (!row.read && row.key) {
    try {
      await markPortalMessagesRead([row.key], portalToken.value);
      row.read = true;
      // 更新未读数
      unreadCount.value = Math.max(0, unreadCount.value - 1);
    } catch (e) {
      console.error('标记已读失败', e);
    }
  }
};

// 定时检查账号状态（每10秒）
let statusCheckInterval: any = null;

const startStatusCheck = () => {
  if (statusCheckInterval) clearInterval(statusCheckInterval);
  statusCheckInterval = setInterval(async () => {
    if (isLoggedIn.value && portalToken.value) {
      try {
        const res: any = await fetchProfile(portalToken.value);
        if (res.data?.success) {
          const newProfile = res.data.data;
          // 检查账号是否被禁用（用户表enabled字段）
          if (newProfile?.enabled === false) {
            handleAccountDisabled('账号已被禁用，已被强制下线');
            return;
          }
          // 检查志愿者审核状态
          if (newProfile?.status === 'rejected') {
            handleAccountDisabled('账号审核未通过，已被强制下线');
            return;
          }
          // 更新本地profile
          profile.value = newProfile;
        }
      } catch (e: any) {
        if (e?.response?.status === 401 || e?.response?.status === 403) {
          handleAccountDisabled('账号已被禁用，已被强制下线');
        }
      }
    }
  }, 10000); // 10秒检查一次
};

const stopStatusCheck = () => {
  if (statusCheckInterval) {
    clearInterval(statusCheckInterval);
    statusCheckInterval = null;
  }
};

// 初始化
onMounted(async () => {
  const savedToken = localStorage.getItem('portalToken');
  if (savedToken) {
    portalToken.value = savedToken;
    isLoggedIn.value = true;
    await loadProfile();
    if (isLoggedIn.value) { // 如果loadProfile没有因为账号异常而退出
      await loadMyData();
      await loadMyOrgs();
      startStatusCheck(); // 开始定时检查
    }
  }
  await Promise.all([loadActivities(), loadCategories(), loadMedia(), loadOrgs()]);
});

// 组件卸载时清理定时器
onUnmounted(() => {
  stopStatusCheck();
});
</script>

<style scoped>
.volunteer-portal {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 顶部导航 */
.portal-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.logo-icon {
  font-size: 28px;
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-links a {
  color: #606266;
  text-decoration: none;
  font-size: 15px;
  transition: color 0.2s;
}

.nav-links a:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f7fa;
}

.user-avatar {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
}

.user-name {
  font-size: 14px;
  color: #303133;
}

/* Hero 区域 */
.hero {
  padding: 100px 60px 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
}

.hero-content {
  flex: 1;
  color: #fff;
}

.hero-content h1 {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 12px;
  line-height: 1.2;
}

.hero-desc {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 32px;
}

.hero-stats {
  display: flex;
  gap: 32px;
  margin-bottom: 32px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
}

.stat-label {
  font-size: 13px;
  opacity: 0.8;
  margin-top: 4px;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.hero-actions .el-button {
  height: 44px;
  padding: 0 28px;
  font-size: 15px;
  border-radius: 22px;
}

.hero-image {
  flex-shrink: 0;
}

.hero-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  color: #fff;
}

.card-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.card-text {
  font-size: 18px;
  letter-spacing: 2px;
}

/* 通用 Section */
.section-header {
  text-align: center;
  margin-bottom: 28px;
}

.section-header h2 {
  font-size: 26px;
  color: #303133;
  margin: 0 0 6px;
}

.section-header p {
  color: #909399;
  font-size: 14px;
}

.section-more {
  text-align: center;
  margin-top: 24px;
}

/* 活动区域 */
.activities-section {
  padding: 40px 60px;
  background: #fff;
}

.activities-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.activity-card {
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.activity-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.activity-card :deep(.el-card__body) {
  padding: 0;
}

.activity-cover {
  position: relative;
  height: 140px;
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.activity-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.6);
}

.activity-status {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-open {
  background: #67c23a;
  color: #fff;
}

.status-signed {
  background: #409eff;
  color: #fff;
}

.status-ongoing {
  background: #e6a23c;
  color: #fff;
}

.status-ended {
  background: #909399;
  color: #fff;
}

.activity-info {
  padding: 14px;
}

.activity-info h3 {
  margin: 0 0 6px;
  font-size: 15px;
  color: #303133;
}

.activity-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.activity-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.activity-desc {
  font-size: 12px;
  color: #606266;
  margin: 0 0 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.capacity {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

/* 资讯区域 */
.content-section {
  padding: 40px 60px;
  background: #f5f7fa;
}

.content-layout {
  display: flex;
  gap: 20px;
}

.category-sidebar {
  width: 180px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 10px;
  padding: 6px;
}

.category-item {
  padding: 10px 14px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  color: #606266;
  font-size: 14px;
}

.category-item:hover {
  background: #f5f7fa;
}

.category-item.active {
  background: #409eff;
  color: #fff;
}

.content-list {
  flex: 1;
  background: #fff;
  border-radius: 10px;
  padding: 12px;
}

.content-item {
  display: flex;
  gap: 14px;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background 0.2s;
}

.content-item:hover {
  background: #fafafa;
}

.content-item:last-child {
  border-bottom: none;
}

.content-thumb {
  width: 100px;
  height: 70px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.content-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.content-info {
  flex: 1;
}

.content-info h4 {
  margin: 0 0 8px;
  font-size: 15px;
  color: #303133;
}

.content-info p {
  margin: 0 0 8px;
  font-size: 13px;
  color: #909399;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.content-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #c0c4cc;
}

/* 媒体展示区域 */
.media-section {
  padding: 40px 60px;
  background: #fff;
}

.media-filter {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.media-card {
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
  background: #f5f7fa;
  transition: transform 0.3s, box-shadow 0.3s;
}

.media-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.media-card:hover .play-overlay {
  opacity: 1;
}

.media-thumb {
  position: relative;
  height: 140px;
  background: #f0f0f0;
}

.media-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: rgba(255, 255, 255, 0.6);
}

.media-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.media-type-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
}

.media-type-badge.video {
  background: rgba(103, 126, 234, 0.9);
}

.media-type-badge.image {
  background: rgba(103, 194, 58, 0.9);
}

.play-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}

.media-info {
  padding: 10px;
}

.media-info h4 {
  margin: 0;
  font-size: 13px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 图片预览对话框 */
.image-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.image-preview-container .preview-image {
  max-width: 100%;
  max-height: 500px;
}

.image-preview-container .preview-image :deep(.el-image__inner) {
  max-height: 500px;
  object-fit: contain;
}

/* 个人中心 */
.profile-section {
  padding: 40px 60px;
  background: #f5f7fa;
}

.profile-card {
  border-radius: 12px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}

.profile-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 22px;
}

.profile-info h3 {
  margin: 0 0 4px;
  font-size: 16px;
}

.profile-info p {
  margin: 0 0 6px;
  color: #909399;
  font-size: 13px;
}

.profile-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 14px;
  background: #f5f7fa;
  border-radius: 10px;
}

.p-stat {
  text-align: center;
  flex: 1;
}

.p-stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #409eff;
}

.p-stat-label {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}

.profile-actions {
  display: flex;
  gap: 8px;
}

.message-card, .signup-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 页脚 */
.portal-footer {
  background: #1f2937;
  color: #fff;
  padding: 30px 60px;
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.footer-links {
  display: flex;
  gap: 24px;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-size: 14px;
}

.footer-links a:hover {
  color: #fff;
}

.footer-copyright {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

/* 对话框 */
.dialog-footer-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #909399;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

.signup-activity-info {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.signup-activity-info h4 {
  margin: 0 0 8px;
}

.signup-activity-info p {
  margin: 4px 0;
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}

.signup-confirm {
  text-align: center;
  padding: 20px;
}

/* 内容详情样式 */
.content-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.content-detail-tags {
  display: flex;
  gap: 8px;
}

.content-detail-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.content-detail-cover {
  position: relative;
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.content-detail-cover .detail-cover-img {
  width: 100%;
  max-height: 400px;
  display: block;
}

.content-detail-cover .detail-cover-img :deep(.el-image__inner) {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
}

.cover-zoom-hint {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.content-detail-cover:hover .cover-zoom-hint {
  opacity: 1;
}

.image-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  background: #f5f7fa;
  color: #909399;
}

.content-detail-body {
  line-height: 1.8;
  color: #303133;
  font-size: 15px;
}

.content-detail-body p {
  margin-bottom: 12px;
}

.content-detail-body img {
  max-width: 100%;
  border-radius: 4px;
  margin: 12px 0;
}

/* 活动详情样式 */
.activity-detail-cover {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}

.activity-detail-cover .activity-cover-img {
  width: 100%;
  max-height: 300px;
}

.activity-detail-cover .activity-cover-img :deep(.el-image__inner) {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
}

.activity-detail-header {
  margin-bottom: 20px;
}

.activity-detail-header h2 {
  margin: 12px 0 0;
  font-size: 22px;
  color: #303133;
}

.activity-detail-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}

.activity-detail-info {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.activity-detail-info .info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  color: #606266;
  font-size: 14px;
}

.activity-detail-info .info-row .el-icon {
  color: #409eff;
}

.activity-detail-info .info-label {
  color: #909399;
  min-width: 70px;
}

.activity-detail-desc {
  margin-top: 20px;
}

.activity-detail-desc h4 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #303133;
}

.activity-detail-desc p {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

.activity-title {
  cursor: pointer;
  transition: color 0.2s;
}

.activity-title:hover {
  color: #409eff;
}

.activity-cover {
  cursor: pointer;
}

/* 响应式 */
@media (max-width: 1200px) {
  .activities-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .media-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .portal-header {
    padding: 0 16px;
  }
  .nav-links {
    display: none;
  }
  .hero {
    flex-direction: column;
    padding: 100px 20px 60px;
    text-align: center;
  }
  .hero-content h1 {
    font-size: 32px;
  }
  .hero-stats {
    justify-content: center;
  }
  .hero-actions {
    justify-content: center;
  }
  .hero-image {
    display: none;
  }
  .activities-grid {
    grid-template-columns: 1fr;
  }
  .content-layout {
    flex-direction: column;
  }
  .category-sidebar {
    width: 100%;
    display: flex;
    overflow-x: auto;
    padding: 8px;
  }
  .category-item {
    white-space: nowrap;
  }
  .media-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .footer-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}

/* 消息详情样式 */
.message-detail {
  padding: 8px 0;
}

.message-detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.message-time {
  color: #909399;
  font-size: 13px;
}

.message-detail-content {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  line-height: 1.8;
  color: #303133;
  font-size: 14px;
}

/* 未读消息标题加粗 */
.unread-title {
  font-weight: 600;
  color: #303133;
}

/* 组织列表样式 */
.orgs-section {
  padding: 40px 60px;
  background: #fff;
}

.orgs-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-top: 24px;
}

.org-card {
  border-radius: 12px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.org-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.org-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 12px;
}

.org-logo {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
}

.org-info h4 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #303133;
}

.org-info p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.org-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.org-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.my-orgs-section {
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid #ebeef5;
}

.my-orgs-section h3 {
  margin: 0 0 16px;
  font-size: 18px;
  color: #303133;
}

@media (max-width: 1200px) {
  .orgs-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .orgs-section {
    padding: 30px 16px;
  }
  .orgs-grid {
    grid-template-columns: 1fr;
  }
}
</style>
