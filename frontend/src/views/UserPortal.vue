<template>
  <div class="portal">
    <section class="hero">
      <div>
        <p class="eyebrow">志愿者服务活动中心</p>
        <h1>融媒体展示 · 报名签到 · 多屏播放</h1>
        <p class="desc">文明资讯、公益活动、视频轮播与活动报名/签到一站式入口</p>
        <div class="hero-actions">
          <el-button type="primary" @click="scrollTo('activities')">我要报名</el-button>
          <el-button @click="scrollTo('videos')">观看视频</el-button>
          <el-button type="success" @click="scrollTo('register')">志愿者注册</el-button>
        </div>
      </div>
      <div class="hero-meta">
        <div class="meta-card">
          <div class="meta-value">{{ stats.playlistTotal }}</div>
          <div class="meta-label">播放列表</div>
        </div>
        <div class="meta-card">
          <div class="meta-value">{{ stats.activityTotal }}</div>
          <div class="meta-label">活动</div>
        </div>
        <div class="meta-card">
          <div class="meta-value">{{ stats.mediaTotal }}</div>
          <div class="meta-label">媒体</div>
        </div>
        <el-button v-if="isAdmin" type="warning" plain @click="goAdmin" class="admin-btn">返回管理后台</el-button>
      </div>
    </section>

    <section class="content" id="content">
      <div class="section-head">
        <h2>内容展示</h2>
        <p class="sub">六大主菜单 + 子菜单资讯/视频</p>
      </div>
      <el-card v-if="headline" class="headline-card" shadow="hover" @click="openContent(headline.id)">
        <div class="headline-cover">
          <img :src="headline.coverUrl" />
          <div class="headline-tag">{{ activeParent?.name || '资讯' }}</div>
          <div class="headline-info">
            <h3>{{ headline.title }}</h3>
            <p>{{ headline.summary || '点击查看详情' }}</p>
          </div>
        </div>
      </el-card>
      <div class="recommend-controls">
        <el-radio-group v-model="recommendStrategy" size="small" @change="loadRecommendations">
          <el-radio-button label="prefer">本栏目优先</el-radio-button>
          <el-radio-button label="filter">仅本栏目</el-radio-button>
          <el-radio-button label="global">全站推荐</el-radio-button>
        </el-radio-group>
      </div>
      <el-carousel
        v-if="recommendedList.length > 1"
        ref="recommendCarouselRef"
        height="200px"
        :interval="recommendIntervalSec * 1000"
        :autoplay="recommendAutoplay"
        indicator-position="outside"
        class="recommend-carousel"
      >
        <el-carousel-item v-for="item in recommendedList" :key="item.id">
          <div class="recommend-card" @click="openContent(item.id)">
            <img v-if="item.coverUrl" :src="item.coverUrl" />
            <div class="recommend-info">
              <span class="recommend-tag">推荐</span>
              <h4>{{ item.title }}</h4>
              <p>{{ item.summary || '查看详情' }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <div v-if="recommendedList.length > 1" class="carousel-controls">
        <el-button-group size="small">
          <el-button @click="toggleRecommendAutoplay">{{ recommendAutoplay ? '暂停轮播' : '继续轮播' }}</el-button>
          <el-button @click="fastForwardRecommend">快进</el-button>
        </el-button-group>
      </div>
      <el-row :gutter="12">
        <el-col :span="6">
          <el-menu :default-active="activeParent?.id?.toString()" class="menu">
            <el-menu-item v-for="p in parents" :key="p.id" :index="p.id.toString()" @click="selectParent(p)">
              {{ p.name }}
            </el-menu-item>
          </el-menu>
          <el-menu v-if="children.length" :default-active="activeChild?.id?.toString()" class="submenu">
            <el-menu-item v-for="c in children" :key="c.id" :index="c.id.toString()" @click="selectChild(c)">
              {{ c.name }}
            </el-menu-item>
          </el-menu>
        </el-col>
        <el-col :span="18">
          <el-input v-model="keyword" placeholder="搜索资讯" prefix-icon="Search" style="margin-bottom: 12px" @change="loadContent" />
          <el-empty v-if="contentList.length === 0 && !contentLoading" description="暂无内容" />
          <el-skeleton v-if="contentLoading" :rows="4" animated />
          <el-row :gutter="12">
            <el-col :span="12" v-for="item in contentList" :key="item.id">
              <el-card class="content-card" shadow="hover" @click="openContent(item.id)">
                <div class="content-cover" v-if="item.coverUrl">
                  <img :src="item.coverUrl" :alt="item.title" />
                  <div class="cover-tag primary">{{ activeParent?.name }}</div>
                </div>
                <div class="content-meta">
                  <h4>{{ item.title }}</h4>
                  <p class="summary">{{ item.summary || '查看详情' }}</p>
                  <div class="tags">
                    <el-tag size="small" :type="tagColor(activeChild?.name || activeParent?.name)">
                      {{ activeChild?.name || activeParent?.name }}
                    </el-tag>
                    <el-tag v-if="item.headline" size="small" type="danger">头条</el-tag>
                    <el-tag v-if="item.recommended" size="small" type="warning">推荐</el-tag>
                    <el-tag v-if="item.publishTime" size="small" type="info">{{ item.publishTime }}</el-tag>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <div class="pager">
            <el-pagination layout="prev, pager, next" :total="contentTotal" :page-size="contentSize" :current-page="contentPage" @current-change="onContentPage" />
          </div>
        </el-col>
      </el-row>
    </section>

    <section class="videos" id="videos">
      <div class="section-head">
        <h2>视频展示</h2>
        <p class="sub">终端播放拉取绑定的播放列表（默认 public-screen）</p>
        <div class="terminal-input">
          <el-select v-model="terminalCode" placeholder="输入或选择终端" filterable allow-create style="width: 260px" @change="loadPlayback">
            <el-option v-for="t in favoriteTerminals" :key="t" :label="t" :value="t" />
          </el-select>
          <el-button @click="loadPlayback" :loading="playbackLoading">刷新</el-button>
          <el-button type="primary" plain @click="saveFavorite">收藏终端</el-button>
          <el-button text type="primary" @click="manageFavorites">管理收藏</el-button>
          <el-button text type="primary" @click="topFavorite">置顶</el-button>
        </div>
      </div>
      <div class="video-filters">
        <el-select v-model="qualityFilter" placeholder="画质" size="small" style="width: 120px">
          <el-option label="全部" value="all" />
          <el-option label="超清(4K)" value="uhd" />
          <el-option label="1080p+" value="fhd" />
          <el-option label="720p" value="hd" />
          <el-option label="标清(SD)" value="sd" />
        </el-select>
        <el-select v-model="durationFilter" placeholder="时长" size="small" style="width: 140px">
          <el-option label="全部" value="all" />
          <el-option label="<=30s" value="short" />
          <el-option label="31-60s" value="medium" />
          <el-option label="61-120s" value="long" />
          <el-option label=">120s" value="xlong" />
        </el-select>
      </div>
      <el-row :gutter="12">
        <el-col :span="12">
          <video
            v-if="currentMedia?.url"
            ref="videoRef"
            controls
            autoplay
            style="width: 100%; border-radius: 8px"
            :src="currentMedia.url"
            :muted="muted"
            @timeupdate="onTimeUpdate"
            @loadedmetadata="onLoadedMeta"
          ></video>
          <div v-else class="video-placeholder">暂无可播媒体</div>
          <div class="player-controls" v-if="mediaAssets.length">
            <el-button size="small" @click="prevMedia">上一条</el-button>
            <el-button size="small" @click="togglePlay">{{ autoPlay ? '暂停轮播' : '继续轮播' }}</el-button>
            <el-button size="small" @click="nextMedia">下一条</el-button>
            <el-select v-model="playbackRate" size="small" style="width: 120px" @change="changeRate">
              <el-option v-for="r in rates" :key="r" :label="r + 'x'" :value="r" />
            </el-select>
            <el-switch v-model="muted" active-text="静音" @change="applyVolume" />
            <el-slider v-model="volume" :min="0" :max="1" :step="0.05" style="width: 120px" @change="applyVolume" />
            <div class="progress-row">
              <span class="time">{{ formatTime(currentTime) }}</span>
              <div class="progress-wrap" @mousemove="onProgressHover" @mouseleave="hidePreview">
                <div class="buffered" :style="{ width: bufferedProgress + '%' }"></div>
                <el-slider v-model="progress" :min="0" :max="100" @change="seek" />
                <div v-if="previewVisible" class="preview" :style="{ left: previewPercent + '%' }">
                  <img v-if="previewMedia?.thumbUrl || previewMedia?.coverUrl" :src="previewMedia.thumbUrl || previewMedia.coverUrl" />
                  <div v-else class="preview-fallback">{{ formatTime(previewTime) }}</div>
                  <div class="preview-title">{{ previewMedia?.name || '预览' }}</div>
                  <div class="preview-time">{{ formatTime(previewTime) }}</div>
                </div>
              </div>
              <span class="time">{{ formatTime(duration) }}</span>
            </div>
          </div>
          <div class="playlist-select" v-if="playback.length">
            <el-select v-model="activePlaylistId" placeholder="选择播放列表" size="small" @change="onPlaylistChange">
              <el-option v-for="p in playback" :key="p.playlist?.id" :label="p.playlist?.name" :value="p.playlist?.id" />
            </el-select>
          </div>
        </el-col>
        <el-col :span="12">
          <el-row :gutter="10">
            <el-col :span="12" v-for="m in visibleMediaAssets" :key="m.id">
              <el-card class="media-card" shadow="hover" @click="playMedia(m)">
                <div class="media-thumb">
                  <img v-if="m.thumbUrl" :src="m.thumbUrl" />
                  <div v-else class="thumb-placeholder">{{ m.type }}</div>
                  <span class="badge">{{ m.durationSeconds ? m.durationSeconds + 's' : '—' }}</span>
                  <span class="badge type">{{ m.type?.toUpperCase() || 'MEDIA' }}</span>
                  <span v-if="m.type === 'video'" class="badge quality" :class="qualityClass(m)">{{ qualityLabel(m) }}</span>
                  <span v-if="m.height" class="badge res">{{ resolutionLabel(m) }}</span>
                </div>
                <p class="media-title">{{ m.name }}</p>
                <p class="media-meta" v-if="mediaMeta(m)">{{ mediaMeta(m) }}</p>
              </el-card>
            </el-col>
          </el-row>
        </el-col>
      </el-row>

      <el-card v-if="favoriteTerminals.length" class="preview-card" shadow="never">
        <div class="preview-head">
          <h4>多终端同步预览</h4>
          <el-select v-model="multiSelected" multiple placeholder="选择终端" size="small" style="min-width: 280px" @change="onMultiSelectedChange">
            <el-option v-for="t in favoriteTerminals" :key="t" :label="t" :value="t" />
          </el-select>
          <el-input-number v-model="previewInterval" :min="5" :max="60" size="small" @change="startMultiPolling" />
          <span class="interval-text">刷新间隔(秒)</span>
          <el-switch v-model="previewPollingEnabled" active-text="实时轮询" @change="startMultiPolling" />
          <el-button v-if="isAdmin" size="small" @click="savePreviewIntervalAsDefault">设为全局默认</el-button>
        </div>
        <el-alert
          v-if="offlineTerminals.length"
          type="warning"
          show-icon
          :title="`发现 ${offlineTerminals.length} 个离线终端：${offlineTerminals.join('、')}`"
          class="offline-alert"
        />
        <el-row :gutter="10">
          <el-col :span="8" v-for="p in multiPreviews" :key="p.terminal">
            <el-card shadow="hover">
              <div class="mini-title">
                <span>{{ p.terminal }}</span>
                <el-tag size="small" :type="terminalStatusType(p.terminal)">{{ terminalStatusLabel(p.terminal) }}</el-tag>
              </div>
              <div class="mini-body">
                <div class="mini-now" v-if="p.currentMedia">
                  <div class="mini-name">播放中：{{ p.currentMedia.name }}</div>
                  <div class="mini-count">时长：{{ p.currentMedia.durationSeconds || '-' }}s</div>
                  <div class="mini-count" v-if="previewRemaining(p) !== null">剩余：{{ formatTime(previewRemaining(p) || 0) }}</div>
                  <el-progress :percentage="previewProgress(p)" :stroke-width="8" />
                </div>
                <div v-if="p.playlists.length === 0" class="mini-empty">无可播列表</div>
                <div v-else>
                  <div class="mini-playlist" v-for="pl in p.playlists" :key="pl.playlist?.id">
                    <div class="mini-name">{{ pl.playlist?.name || '未命名列表' }}</div>
                    <div class="mini-count">资源数：{{ pl.mediaAssets?.length || 0 }}</div>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-card>

      <el-card v-if="terminalGroups.length" class="group-card" shadow="never">
        <div class="group-head">
          <h4>终端分组视角</h4>
          <el-button size="small" @click="loadTerminalStatus">刷新状态</el-button>
        </div>
        <el-tabs v-model="activeGroupTab">
          <el-tab-pane
            v-for="group in terminalGroups"
            :key="group.name"
            :label="`${group.name} (${group.onlineCount}/${group.items.length})`"
            :name="group.name"
          >
            <div class="group-grid">
              <div v-for="t in group.items" :key="t.code" class="group-item">
                <div class="group-name">{{ t.name || t.code }}</div>
                <div class="group-meta">
                  <span class="group-code">{{ t.code }}</span>
                  <el-tag size="small" :type="t.status === 'offline' ? 'danger' : 'success'">
                    {{ t.status === 'offline' ? '离线' : '在线' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </section>

    <section class="activities" id="activities">
      <div class="section-head">
        <h2>活动报名</h2>
        <p class="sub">报名/查看活动信息</p>
      </div>
      <el-table :data="activities" style="width:100%">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="location" label="地点" width="160" />
        <el-table-column prop="startTime" label="开始" width="170" />
        <el-table-column prop="endTime" label="结束" width="170" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openSignup(scope.row.id)">报名</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination layout="prev, pager, next" :total="activityTotal" :page-size="activitySize" :current-page="activityPage" @current-change="onActivityPage" />
      </div>
    </section>

    <section class="register" id="register">
      <div class="section-head">
        <h2>志愿者注册</h2>
        <p class="sub">注册后可在个人中心查看报名/签到记录</p>
      </div>
      <el-form label-width="90px" class="register-form">
        <el-form-item label="姓名">
          <el-input v-model="portalRegisterForm.name" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="portalRegisterForm.phone" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="portalRegisterForm.password" show-password />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="portalRegisterForm.email" />
        </el-form-item>
        <el-form-item label="所属组织">
          <el-input v-model="portalRegisterForm.organization" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="registerVolunteer">提交</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="my-signups">
      <div class="section-head">
        <h3>我的报名/签到</h3>
        <p class="sub">{{ portalLoggedIn ? '已登录，报名/签到自动同步' : '登录后可自动同步报名/签到记录' }}</p>
      </div>
      <div v-if="!portalLoggedIn" class="portal-auth">
        <el-card class="auth-card" shadow="hover">
          <div class="auth-title">个人中心登录</div>
          <el-form label-width="70px">
            <el-form-item label="手机号">
              <el-input v-model="portalLoginForm.phone" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="portalLoginForm.password" type="password" show-password />
            </el-form-item>
          </el-form>
          <div class="auth-actions">
            <el-button type="primary" @click="portalLoginSubmit">登录</el-button>
            <el-button @click="scrollTo('register')">去注册</el-button>
          </div>
        </el-card>
        <div class="query">
          <el-input v-model="queryPhone" placeholder="未登录时可用手机号查询" style="width: 240px" />
          <el-button @click="loadSignups">查询</el-button>
        </div>
      </div>
      <div v-else class="portal-profile">
        <el-card class="profile-card" shadow="hover">
          <div class="profile-head">
            <div>
              <div class="profile-name">{{ portalProfile?.name || '志愿者' }}</div>
              <div class="profile-meta">{{ portalProfile?.phone }}</div>
            </div>
            <div class="profile-actions">
              <el-button size="small" @click="profileEditing = !profileEditing">{{ profileEditing ? '取消' : '编辑资料' }}</el-button>
              <el-button size="small" @click="portalLogout">退出</el-button>
            </div>
          </div>
          <el-form v-if="profileEditing" label-width="80px">
            <el-form-item label="姓名">
              <el-input v-model="profileForm.name" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" />
            </el-form-item>
            <el-form-item label="组织">
              <el-input v-model="profileForm.organization" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="savePortalProfile">保存</el-button>
            </el-form-item>
          </el-form>
          <div v-else class="profile-info">
            <div>邮箱：{{ portalProfile?.email || '未填写' }}</div>
            <div>组织：{{ portalProfile?.organization || '未填写' }}</div>
            <div>状态：{{ portalProfile?.status || '—' }}</div>
          </div>
        </el-card>
      </div>
      <el-table :data="signups" size="small">
        <el-table-column prop="title" label="活动" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signupTime" label="报名时间" width="160" />
        <el-table-column prop="checkinTime" label="签到时间" width="160" />
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" title="活动报名" width="520px">
      <el-form :model="signupForm" label-width="90px">
        <el-form-item label="姓名"><el-input v-model="signupForm.name" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="signupForm.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="signupForm.email" /></el-form-item>
        <el-form-item label="组织"><el-input v-model="signupForm.organization" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="signup">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="contentDialog" :title="contentDetail?.title" width="720px">
      <div class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>{{ activeParent?.name || '内容' }}</el-breadcrumb-item>
          <el-breadcrumb-item>{{ activeChild?.name || '子菜单' }}</el-breadcrumb-item>
          <el-breadcrumb-item>{{ contentDetail?.title }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="detail-cover" v-if="contentDetail?.coverUrl">
        <img :src="contentDetail.coverUrl" />
      </div>
      <div class="detail-body" v-html="contentDetail?.body || contentDetail?.summary"></div>
    </el-dialog>

    <el-dialog v-model="favDialog" title="收藏终端管理" width="520px">
      <el-table :data="favoriteTerminals.map((name, index) => ({ name, index }))" @selection-change="onFavSelect">
        <el-table-column type="selection" width="45" />
        <el-table-column label="终端名称">
          <template #default="scope">
            <el-input v-model="favoriteTerminals[scope.row.index]" size="small" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="favDialog = false">关闭</el-button>
        <el-button @click="batchDeleteFavorites">删除所选</el-button>
        <el-button @click="batchTopFavorites">置顶所选</el-button>
        <el-button type="primary" @click="saveFavorites">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import {
  fetchActivitiesPublic,
  fetchPlaybackPublic,
  fetchPublicCategories,
  fetchPublicContent,
  fetchPublicContentById,
  fetchPublicContentConfig,
  fetchPublicRecommendations,
  fetchPublicTerminals,
  portalLogin,
  portalRegister,
  fetchPortalMe,
  updatePortalMe,
  fetchPortalSignups,
  signupActivityPortal,
  updateContentConfig,
  fetchVolunteerSignups,
  signupActivityPublic
} from '../api';

const parents = ref<any[]>([]);
const children = ref<any[]>([]);
const activeParent = ref<any | null>(null);
const activeChild = ref<any | null>(null);
const keyword = ref('');
const contentList = ref<any[]>([]);
const contentLoading = ref(false);
const contentPage = ref(1);
const contentSize = ref(6);
const contentTotal = ref(0);
const contentDialog = ref(false);
const contentDetail = ref<any | null>(null);
const headline = ref<any | null>(null);
const headlinePool = ref<any[]>([]);
const headlineIndex = ref(0);
const headlineTimer = ref<number | null>(null);
const recommendedList = ref<any[]>([]);
const recommendIntervalSec = ref(6);
const recommendCount = ref(6);
const recommendCarouselRef = ref();
const recommendAutoplay = ref(true);
const recommendStrategy = ref('prefer');
const portalToken = ref(localStorage.getItem('portal_token') || '');
const portalProfile = ref<any | null>(null);
const portalLoginForm = ref({ phone: '', password: '' });
const portalRegisterForm = ref({ name: '', phone: '', password: '', email: '', organization: '' });
const profileForm = ref({ name: '', email: '', organization: '' });
const profileEditing = ref(false);
const portalLoggedIn = computed(() => !!portalToken.value);

const terminalCode = ref('public-screen');
const playback = ref<any[]>([]);
const mediaAssets = ref<any[]>([]);
const currentMedia = ref<any | null>(null);
const activePlayback = ref<any | null>(null);
const timelineItems = ref<any[]>([]);
const timelineTotal = ref(0);
const previewMedia = ref<any | null>(null);
const playerTimer = ref<number | null>(null);
const autoPlay = ref(true);
const activePlaylistId = ref<number | null>(null);
const playbackLoading = ref(false);
const playbackRate = ref(1);
const rates = [0.75, 1, 1.25, 1.5];
const volume = ref(0.8);
const muted = ref(false);
const videoRef = ref<HTMLVideoElement>();
const favoriteTerminals = ref<string[]>(
  (() => {
    const saved = localStorage.getItem('portal_fav_terminals');
    return saved ? JSON.parse(saved) : [];
  })()
);
const multiSelected = ref<string[]>([]);
const multiPreviews = ref<any[]>([]);
const currentTime = ref(0);
const duration = ref(0);
const progress = ref(0);
const bufferedProgress = ref(0);
const previewVisible = ref(false);
const previewPercent = ref(0);
const previewTime = ref(0);
const favDialog = ref(false);
const favSelection = ref<number[]>([]);
const multiTimer = ref<number | null>(null);
const previewInterval = ref(10);
const previewClock = ref(Date.now());
const previewClockTimer = ref<number | null>(null);
const previewPollingEnabled = ref(true);
const qualityFilter = ref('all');
const durationFilter = ref('all');
const terminalStatusList = ref<any[]>([]);
const activeGroupTab = ref('');

const visibleMediaAssets = computed(() => {
  let list = mediaAssets.value;
  if (qualityFilter.value !== 'all') {
    list = list.filter((m: any) => {
      const bucket = qualityBucket(m);
      if (qualityFilter.value === 'uhd') return bucket === 'uhd';
      if (qualityFilter.value === 'fhd') return bucket === 'fhd' || bucket === 'uhd';
      if (qualityFilter.value === 'hd') return bucket === 'hd';
      if (qualityFilter.value === 'sd') return bucket === 'sd';
      return true;
    });
  }
  if (durationFilter.value !== 'all') {
    list = list.filter((m: any) => {
      const d = m.durationSeconds || 0;
      if (durationFilter.value === 'short') return d > 0 && d <= 30;
      if (durationFilter.value === 'medium') return d > 30 && d <= 60;
      if (durationFilter.value === 'long') return d > 60 && d <= 120;
      if (durationFilter.value === 'xlong') return d > 120;
      return true;
    });
  }
  return list;
});

const terminalStatusMap = computed(() => {
  const map: Record<string, any> = {};
  terminalStatusList.value.forEach((t: any) => {
    map[t.code] = t;
  });
  return map;
});

const terminalGroups = computed(() => {
  const groups: Record<string, any[]> = {};
  terminalStatusList.value.forEach((t: any) => {
    const key = t.groupName || '未分组';
    if (!groups[key]) groups[key] = [];
    groups[key].push(t);
  });
  return Object.keys(groups).map((name) => {
    const items = groups[name];
    const offlineCount = items.filter((t) => t.status === 'offline').length;
    return {
      name,
      items,
      offlineCount,
      onlineCount: items.length - offlineCount
    };
  });
});

const offlineTerminals = computed(() => {
  return multiSelected.value.filter((code) => terminalStatusMap.value[code]?.status === 'offline');
});

const activities = ref<any[]>([]);
const activityPage = ref(1);
const activitySize = ref(8);
const activityTotal = ref(0);
const dialogVisible = ref(false);
const signupForm = ref({ name: '', phone: '', email: '', organization: '' });
const currentActivity = ref<number | null>(null);

const signups = ref<any[]>([]);
const queryPhone = ref('');

const stats = ref<{ playlistTotal: number; activityTotal: number; mediaTotal: number }>({
  playlistTotal: 0,
  activityTotal: 0,
  mediaTotal: 0
});
const isAdmin = ref(localStorage.getItem('role') === 'ADMIN');

const scrollTo = (id: string) => {
  const el = document.getElementById(id);
  if (el) el.scrollIntoView({ behavior: 'smooth' });
};

const loadCategories = async () => {
  const resp = await fetchPublicCategories();
  parents.value = resp.data?.data || [];
  if (parents.value.length) selectParent(parents.value[0]);
};

const loadContentConfig = async () => {
  try {
    const resp = await fetchPublicContentConfig();
    const data = resp.data?.data;
    if (data?.recommendIntervalSec) {
      recommendIntervalSec.value = data.recommendIntervalSec;
    }
    if (data?.recommendCount) {
      recommendCount.value = data.recommendCount;
    }
    if (data?.previewIntervalSec) {
      const local = localStorage.getItem('portal_preview_interval');
      if (!local) {
        previewInterval.value = data.previewIntervalSec;
      }
    }
    await loadRecommendations();
  } catch (e) {
    // ignore
  }
};

const selectParent = async (p: any) => {
  activeParent.value = p;
  const resp = await fetchPublicCategories(p.id);
  children.value = resp.data?.data || [];
  if (children.value.length) {
    selectChild(children.value[0]);
  } else {
    activeChild.value = null;
    loadContent();
  }
  await loadRecommendations();
};

const selectChild = (c: any) => {
  activeChild.value = c;
  contentPage.value = 1;
  loadContent();
};

const loadContent = async () => {
  contentLoading.value = true;
  try {
    const resp = await fetchPublicContent(contentPage.value, contentSize.value, activeChild.value?.id, keyword.value);
    const data = resp.data?.data || {};
    contentList.value = data.records || [];
    contentTotal.value = data.total || 0;
    if (contentPage.value === 1 && contentList.value.length) {
      const list = [...contentList.value];
      const pick = list.find((i: any) => i.headline) || list.find((i: any) => i.recommended);
      if (pick) {
        headlinePool.value = list
          .filter((i: any) => i.headline || i.recommended)
          .sort((a: any, b: any) => {
            const sa = a.sortOrder ?? 0;
            const sb = b.sortOrder ?? 0;
            if (sa !== sb) return sa - sb;
            return new Date(b.publishTime || b.createdAt || 0).getTime() - new Date(a.publishTime || a.createdAt || 0).getTime();
          });
        headline.value = pick;
        startHeadlineRotate();
      } else {
        headlinePool.value = list.sort((a: any, b: any) => {
          return (new Date(b.publishTime || b.createdAt || 0).getTime()) - (new Date(a.publishTime || a.createdAt || 0).getTime());
        });
        headline.value = headlinePool.value[0];
        stopHeadlineRotate();
      }
    }
  } finally {
    contentLoading.value = false;
  }
};

const loadRecommendations = async () => {
  try {
    const parentId = recommendStrategy.value === 'global' ? undefined : activeParent.value?.id;
    const strategy = recommendStrategy.value === 'global' ? 'prefer' : recommendStrategy.value;
    const resp = await fetchPublicRecommendations(parentId, recommendCount.value, strategy);
    recommendedList.value = resp.data?.data || [];
  } catch (e) {
    syncRecommendedList();
  }
};

const syncRecommendedList = () => {
  const sorted = contentList.value
    .filter((i: any) => i.recommended)
    .sort((a: any, b: any) => {
      const sa = a.sortOrder ?? 0;
      const sb = b.sortOrder ?? 0;
      if (sa !== sb) return sa - sb;
      return new Date(b.publishTime || b.createdAt || 0).getTime() - new Date(a.publishTime || a.createdAt || 0).getTime();
    });
  const limit = recommendCount.value || sorted.length;
  recommendedList.value = sorted.slice(0, limit);
};

const onContentPage = (p: number) => {
  contentPage.value = p;
  loadContent();
};

const openContent = async (id: number) => {
  const resp = await fetchPublicContentById(id);
  contentDetail.value = resp.data?.data || null;
  contentDialog.value = true;
};

const loadPlayback = async () => {
  playbackLoading.value = true;
  try {
    localStorage.setItem('portal_terminal_code', terminalCode.value);
    const resp = await fetchPlaybackPublic(terminalCode.value);
    playback.value = resp.data?.data || [];
    if (!playback.value.length) {
      mediaAssets.value = [];
      currentMedia.value = null;
      activePlayback.value = null;
      timelineItems.value = [];
      timelineTotal.value = 0;
      return;
    }
    const first = playback.value[0];
    activePlaylistId.value = first?.playlist?.id || null;
    activePlayback.value = first;
    mediaAssets.value = first?.mediaAssets || [];
    currentMedia.value = mediaAssets.value[0] || null;
    buildTimeline(first);
    scheduleNext();
    applyVolume();
    changeRate(playbackRate.value);
  } finally {
    playbackLoading.value = false;
  }
};

const playMedia = (row: any) => {
  currentMedia.value = row;
  scheduleNext();
};

const loadActivities = async () => {
  const resp = await fetchActivitiesPublic(activityPage.value, activitySize.value);
  const data = resp.data?.data || {};
  activities.value = data.records || [];
  activityTotal.value = data.total || 0;
};

const onActivityPage = (p: number) => {
  activityPage.value = p;
  loadActivities();
};

const openSignup = (id: number) => {
  currentActivity.value = id;
  if (portalLoggedIn.value) {
    signupWithAccount();
  } else {
    dialogVisible.value = true;
  }
};

const signup = async () => {
  if (!signupForm.value.name || !signupForm.value.phone) {
    ElMessage.warning('请填写姓名和电话');
    return;
  }
  await signupActivityPublic({ ...signupForm.value, activityId: currentActivity.value });
  ElMessage.success('报名成功');
  queryPhone.value = signupForm.value.phone;
  loadSignups();
  dialogVisible.value = false;
};

const signupWithAccount = async () => {
  if (!currentActivity.value) return;
  await signupActivityPortal({ activityId: currentActivity.value });
  ElMessage.success('报名成功');
  await loadSignups();
};

const registerVolunteer = async () => {
  if (!portalRegisterForm.value.name || !portalRegisterForm.value.phone || !portalRegisterForm.value.password) {
    ElMessage.warning('请输入姓名、手机号和密码');
    return;
  }
  try {
    const resp = await portalRegister(portalRegisterForm.value);
    const data = resp.data?.data;
    if (data?.token) {
      portalToken.value = data.token;
      localStorage.setItem('portal_token', data.token);
    }
    await loadPortalProfile();
    ElMessage.success('注册成功');
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '提交失败');
  }
};

const loadSignups = async () => {
  if (portalLoggedIn.value) {
    const resp = await fetchPortalSignups();
    signups.value = resp.data?.data || [];
    return;
  }
  if (!queryPhone.value) {
    ElMessage.warning('请输入手机号');
    return;
  }
  const resp = await fetchVolunteerSignups(queryPhone.value);
  signups.value = resp.data?.data || [];
};

const portalLoginSubmit = async () => {
  if (!portalLoginForm.value.phone || !portalLoginForm.value.password) {
    ElMessage.warning('请输入手机号和密码');
    return;
  }
  try {
    const resp = await portalLogin(portalLoginForm.value);
    const data = resp.data?.data;
    if (data?.token) {
      portalToken.value = data.token;
      localStorage.setItem('portal_token', data.token);
    }
    await loadPortalProfile();
    await loadSignups();
    ElMessage.success('登录成功');
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '登录失败');
  }
};

const loadPortalProfile = async () => {
  if (!portalLoggedIn.value) return;
  try {
    const resp = await fetchPortalMe();
    portalProfile.value = resp.data?.data || null;
    if (portalProfile.value) {
      localStorage.setItem('portal_profile', JSON.stringify(portalProfile.value));
      profileForm.value = {
        name: portalProfile.value.name || '',
        email: portalProfile.value.email || '',
        organization: portalProfile.value.organization || ''
      };
    }
  } catch (e) {
    portalLogout();
  }
};

const savePortalProfile = async () => {
  if (!portalLoggedIn.value) return;
  const resp = await updatePortalMe(profileForm.value);
  portalProfile.value = resp.data?.data || null;
  localStorage.setItem('portal_profile', JSON.stringify(portalProfile.value));
  profileEditing.value = false;
  ElMessage.success('已保存');
};

const portalLogout = () => {
  portalToken.value = '';
  portalProfile.value = null;
  localStorage.removeItem('portal_token');
  localStorage.removeItem('portal_profile');
  profileEditing.value = false;
  portalLoginForm.value = { phone: '', password: '' };
  signups.value = [];
};

const loadStats = async () => {
  // 复用 monitor/summary 需要鉴权，因此这里仅使用现有数据长度估算
  stats.value = {
    playlistTotal: playback.value?.length || 0,
    activityTotal: activities.value?.length || 0,
    mediaTotal: mediaAssets.value?.length || 0
  };
};

const scheduleNext = () => {
  if (playerTimer.value) {
    clearTimeout(playerTimer.value);
  }
  if (!mediaAssets.value.length || !currentMedia.value) return;
  const idx = mediaAssets.value.findIndex((m: any) => m.id === currentMedia.value.id);
  const duration = (currentMedia.value.durationSeconds || 10) * 1000;
  playerTimer.value = window.setTimeout(() => {
    const next = mediaAssets.value[(idx + 1) % mediaAssets.value.length];
    currentMedia.value = next;
    scheduleNext();
  }, duration);
};

const statusTag = (status: string) => {
  if (status === 'checked_in') return 'success';
  if (status === 'applied') return 'warning';
  return 'info';
};

const goAdmin = () => {
  window.location.href = '/dashboard';
};

const startHeadlineRotate = () => {
  stopHeadlineRotate();
  if (!headlinePool.value || headlinePool.value.length <= 1) return;
  headlineIndex.value = Math.max(
    0,
    headlinePool.value.findIndex((i: any) => i.id === headline.value?.id)
  );
  headlineTimer.value = window.setInterval(() => {
    headlineIndex.value = (headlineIndex.value + 1) % headlinePool.value.length;
    headline.value = headlinePool.value[headlineIndex.value];
  }, 6000);
};

const stopHeadlineRotate = () => {
  if (headlineTimer.value) {
    clearInterval(headlineTimer.value);
    headlineTimer.value = null;
  }
};

const togglePlay = () => {
  autoPlay.value = !autoPlay.value;
  if (autoPlay.value) {
    scheduleNext();
  } else if (playerTimer.value) {
    clearTimeout(playerTimer.value);
  }
};

const toggleRecommendAutoplay = () => {
  recommendAutoplay.value = !recommendAutoplay.value;
};

const fastForwardRecommend = () => {
  recommendCarouselRef.value?.next?.();
};

const nextMedia = () => {
  if (!mediaAssets.value.length || !currentMedia.value) return;
  const idx = mediaAssets.value.findIndex((m: any) => m.id === currentMedia.value.id);
  currentMedia.value = mediaAssets.value[(idx + 1) % mediaAssets.value.length];
  scheduleNext();
};

const prevMedia = () => {
  if (!mediaAssets.value.length || !currentMedia.value) return;
  const idx = mediaAssets.value.findIndex((m: any) => m.id === currentMedia.value.id);
  const prev = (idx - 1 + mediaAssets.value.length) % mediaAssets.value.length;
  currentMedia.value = mediaAssets.value[prev];
  scheduleNext();
};

const onPlaylistChange = (id: number) => {
  const target = playback.value.find((p: any) => p.playlist?.id === id);
  activePlayback.value = target || null;
  mediaAssets.value = target?.mediaAssets || [];
  currentMedia.value = mediaAssets.value[0] || null;
  buildTimeline(target);
  scheduleNext();
};

const changeRate = (rate: number) => {
  playbackRate.value = rate;
  if (videoRef.value) {
    videoRef.value.playbackRate = rate;
  }
};

const applyVolume = () => {
  if (videoRef.value) {
    videoRef.value.muted = muted.value;
    videoRef.value.volume = muted.value ? 0 : volume.value;
  }
};

const saveFavorite = () => {
  if (!terminalCode.value) return;
  if (!favoriteTerminals.value.includes(terminalCode.value)) {
    favoriteTerminals.value.push(terminalCode.value);
    localStorage.setItem('portal_fav_terminals', JSON.stringify(favoriteTerminals.value));
    ElMessage.success('已收藏终端');
  }
};

const manageFavorites = () => {
  favDialog.value = true;
};

const onFavSelect = (rows: any[]) => {
  favSelection.value = rows.map((r: any) => r.index);
};

const saveFavorites = () => {
  const cleaned = favoriteTerminals.value.filter((t) => t && t.trim());
  favoriteTerminals.value = Array.from(new Set(cleaned));
  localStorage.setItem('portal_fav_terminals', JSON.stringify(favoriteTerminals.value));
  ElMessage.success('已保存');
  favDialog.value = false;
};

const batchDeleteFavorites = () => {
  if (favSelection.value.length === 0) return;
  const sorted = [...favSelection.value].sort((a, b) => b - a);
  sorted.forEach((idx) => favoriteTerminals.value.splice(idx, 1));
  favSelection.value = [];
};

const batchTopFavorites = () => {
  if (favSelection.value.length === 0) return;
  const selected = favSelection.value.sort((a, b) => a - b).map((idx) => favoriteTerminals.value[idx]);
  const remain = favoriteTerminals.value.filter((_, idx) => !favSelection.value.includes(idx));
  favoriteTerminals.value = [...selected, ...remain];
  favSelection.value = [];
};

const topFavorite = () => {
  const idx = favoriteTerminals.value.indexOf(terminalCode.value);
  if (idx > 0) {
    favoriteTerminals.value.splice(idx, 1);
    favoriteTerminals.value.unshift(terminalCode.value);
    localStorage.setItem('portal_fav_terminals', JSON.stringify(favoriteTerminals.value));
    ElMessage.success('已置顶');
  }
};

const onTimeUpdate = () => {
  if (!videoRef.value) return;
  currentTime.value = videoRef.value.currentTime;
  duration.value = videoRef.value.duration || 0;
  progress.value = duration.value ? (currentTime.value / duration.value) * 100 : 0;
  const buf = videoRef.value.buffered;
  if (buf && buf.length) {
    const end = buf.end(buf.length - 1);
    bufferedProgress.value = duration.value ? (end / duration.value) * 100 : 0;
  }
};

const onProgressHover = (evt: MouseEvent) => {
  const target = evt.currentTarget as HTMLElement;
  const rect = target.getBoundingClientRect();
  const percent = Math.max(0, Math.min(100, ((evt.clientX - rect.left) / rect.width) * 100));
  previewPercent.value = percent;
  const total = timelineTotal.value || duration.value;
  previewTime.value = total ? (total * percent) / 100 : 0;
  previewMedia.value = pickTimelineMedia(previewTime.value);
  previewVisible.value = true;
};

const hidePreview = () => {
  previewVisible.value = false;
};

const buildTimeline = (playbackObj: any) => {
  if (!playbackObj?.items || !playbackObj?.mediaAssets) {
    timelineItems.value = [];
    timelineTotal.value = 0;
    return;
  }
  const assets = new Map<number, any>();
  playbackObj.mediaAssets.forEach((m: any) => assets.set(m.id, m));
  const items = [...playbackObj.items].sort((a: any, b: any) => (a.sortOrder || 0) - (b.sortOrder || 0));
  let cursor = 0;
  timelineItems.value = items.map((i: any) => {
    const duration = i.displayDuration || 10;
    const media = assets.get(i.mediaId);
    const start = cursor;
    const end = cursor + duration;
    cursor = end;
    return { start, end, media };
  });
  timelineTotal.value = cursor;
};

const pickTimelineMedia = (time: number) => {
  if (!timelineItems.value.length) return currentMedia.value;
  const item = timelineItems.value.find((t: any) => time >= t.start && time < t.end);
  return item?.media || currentMedia.value;
};

const onLoadedMeta = () => {
  onTimeUpdate();
};

const seek = (val: number) => {
  if (!videoRef.value || !duration.value) return;
  videoRef.value.currentTime = (val / 100) * duration.value;
  currentTime.value = videoRef.value.currentTime;
};

const formatTime = (sec: number) => {
  if (!sec || Number.isNaN(sec)) return '00:00';
  const m = Math.floor(sec / 60);
  const s = Math.floor(sec % 60);
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
};

const loadMultiPreviews = async () => {
  const results: any[] = [];
  for (const t of multiSelected.value) {
    const resp = await fetchPlaybackPublic(t);
    const playlists = resp.data?.data || [];
    const currentPlaying = playlists.flatMap((p: any) => p.mediaAssets || []).find(() => true) || null;
    const existing = multiPreviews.value.find((p: any) => p.terminal === t);
    const startedAt =
      existing && existing.currentMedia?.id === currentPlaying?.id && existing.startedAt
        ? existing.startedAt
        : Date.now();
    results.push({
      terminal: t,
      playlists,
      currentMedia: currentPlaying,
      totalCount: playlists.reduce((acc: number, p: any) => acc + (p.mediaAssets?.length || 0), 0),
      startedAt
    });
  }
  multiPreviews.value = results;
};

const loadTerminalStatus = async () => {
  try {
    const resp = await fetchPublicTerminals();
    terminalStatusList.value = resp.data?.data || [];
    if (!activeGroupTab.value && terminalGroups.value.length) {
      activeGroupTab.value = terminalGroups.value[0].name;
    }
  } catch (e) {
    // ignore
  }
};

const onMultiSelectedChange = () => {
  loadMultiPreviews();
  loadTerminalStatus();
  startMultiPolling();
};

const startMultiPolling = () => {
  if (multiTimer.value) {
    clearInterval(multiTimer.value);
  }
  localStorage.setItem('portal_preview_interval', String(previewInterval.value));
  if (!multiSelected.value.length || !previewPollingEnabled.value) {
    stopPreviewClock();
    return;
  }
  startPreviewClock();
  loadTerminalStatus();
  multiTimer.value = window.setInterval(() => {
    loadMultiPreviews();
    loadTerminalStatus();
  }, previewInterval.value * 1000);
};

const stopMultiPolling = () => {
  if (multiTimer.value) {
    clearInterval(multiTimer.value);
    multiTimer.value = null;
  }
  stopPreviewClock();
};

const savePreviewIntervalAsDefault = async () => {
  try {
    await updateContentConfig({ previewIntervalSec: previewInterval.value });
    ElMessage.success('已保存为全局默认');
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '保存失败');
  }
};

const startPreviewClock = () => {
  if (previewClockTimer.value) clearInterval(previewClockTimer.value);
  previewClockTimer.value = window.setInterval(() => {
    previewClock.value = Date.now();
  }, 1000);
};

const stopPreviewClock = () => {
  if (previewClockTimer.value) {
    clearInterval(previewClockTimer.value);
    previewClockTimer.value = null;
  }
};

const previewProgress = (p: any) => {
  if (!p?.currentMedia?.durationSeconds) return 0;
  const elapsed = (previewClock.value - p.startedAt) / 1000;
  return Math.round(((elapsed % p.currentMedia.durationSeconds) / p.currentMedia.durationSeconds) * 100);
};

const previewRemaining = (p: any) => {
  if (!p?.currentMedia?.durationSeconds) return null;
  const elapsed = (previewClock.value - p.startedAt) / 1000;
  const durationSec = p.currentMedia.durationSeconds;
  const remaining = durationSec - (elapsed % durationSec);
  return Math.max(0, Math.round(remaining));
};

const terminalStatusLabel = (code: string) => {
  const status = terminalStatusMap.value[code]?.status;
  if (status === 'offline') return '离线';
  if (status === 'online') return '在线';
  return '未知';
};

const terminalStatusType = (code: string) => {
  const status = terminalStatusMap.value[code]?.status;
  if (status === 'offline') return 'danger';
  if (status === 'online') return 'success';
  return 'info';
};

const tagColor = (name?: string) => {
  if (!name) return 'info';
  const map: Record<string, string> = {
    公益活动: 'success',
    公益广告: 'warning',
    雷锋热线: 'danger',
    文明XX: 'info'
  };
  return map[name] || 'primary';
};

const qualityLabel = (m: any) => {
  return qualityBucket(m) === 'sd' ? 'SD' : 'HD';
};

const qualityClass = (m: any) => {
  return qualityLabel(m) === 'HD' ? 'hd' : 'sd';
};

const qualityBucket = (m: any) => {
  const height = m.height || 0;
  const width = m.width || 0;
  if (height >= 2160 || width >= 3840) return 'uhd';
  if (height >= 1080 || width >= 1920) return 'fhd';
  if (height >= 720 || width >= 1280) return 'hd';
  if (m.bitrateKbps && m.bitrateKbps >= 2500) return 'hd';
  return 'sd';
};

const resolutionLabel = (m: any) => {
  if (!m.height) return '';
  if (m.height >= 1080) return '1080p';
  if (m.height >= 720) return '720p';
  if (m.height >= 480) return '480p';
  return m.height + 'p';
};

const frameRateLabel = (m: any) => {
  if (!m.frameRate) return '';
  const rounded = Number.isInteger(m.frameRate) ? m.frameRate : Number(m.frameRate).toFixed(1);
  return `${rounded}fps`;
};

const bitrateLabel = (m: any) => {
  if (!m.bitrateKbps) return '';
  if (m.bitrateKbps >= 1000) {
    return `${(m.bitrateKbps / 1000).toFixed(1)}Mbps`;
  }
  return `${Math.round(m.bitrateKbps)}kbps`;
};

const mediaMeta = (m: any) => {
  const parts: string[] = [];
  const res = resolutionLabel(m);
  const fps = frameRateLabel(m);
  const bitrate = bitrateLabel(m);
  if (res) parts.push(res);
  if (fps) parts.push(fps);
  if (bitrate) parts.push(bitrate);
  return parts.join(' · ');
};

onMounted(async () => {
  await loadContentConfig();
  await loadCategories();
  await loadContent();
  const cachedProfile = localStorage.getItem('portal_profile');
  if (cachedProfile) {
    portalProfile.value = JSON.parse(cachedProfile);
    profileForm.value = {
      name: portalProfile.value?.name || '',
      email: portalProfile.value?.email || '',
      organization: portalProfile.value?.organization || ''
    };
  }
  if (portalLoggedIn.value) {
    await loadPortalProfile();
    await loadSignups();
  }
  const saved = localStorage.getItem('portal_terminal_code');
  if (saved) terminalCode.value = saved;
  const intervalSaved = localStorage.getItem('portal_preview_interval');
  if (intervalSaved) previewInterval.value = Number(intervalSaved);
  await loadPlayback();
  await loadActivities();
  await loadTerminalStatus();
  loadStats();
});

onBeforeUnmount(() => {
  if (playerTimer.value) clearTimeout(playerTimer.value);
  stopMultiPolling();
  stopHeadlineRotate();
});
</script>

<style scoped>
.portal { padding: 20px; }
.hero { background: linear-gradient(135deg, #2c7be5, #5cc9f5); color: #fff; padding: 24px; border-radius: 12px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.eyebrow { margin: 0; opacity: 0.9; }
.desc { margin: 6px 0 12px; opacity: 0.9; }
.hero-actions :deep(.el-button) { margin-right: 8px; }
.hero-meta { display: flex; gap: 12px; align-items: center; }
.meta-card { background: rgba(255,255,255,0.12); padding: 10px 14px; border-radius: 10px; text-align: center; min-width: 90px; }
.meta-value { font-size: 24px; font-weight: 700; }
.meta-label { font-size: 13px; opacity: 0.9; }
.admin-btn { margin-left: 8px; }
.headline-card { margin-bottom: 12px; border-radius: 12px; overflow: hidden; }
.headline-cover { position: relative; }
.headline-cover img { width: 100%; height: 260px; object-fit: cover; display: block; }
.headline-tag { position: absolute; top: 12px; left: 12px; background: rgba(0,0,0,0.55); color: #fff; padding: 4px 10px; border-radius: 999px; }
.headline-info { position: absolute; bottom: 0; left: 0; right: 0; padding: 16px; background: linear-gradient(180deg, transparent 0%, rgba(0,0,0,0.65) 100%); color: #fff; }
.headline-info h3 { margin: 0 0 4px; }
.recommend-carousel { margin: 12px 0; }
.recommend-card { position: relative; height: 200px; border-radius: 10px; overflow: hidden; cursor: pointer; }
.recommend-card img { width: 100%; height: 100%; object-fit: cover; }
.recommend-info { position: absolute; inset: 0; padding: 14px; background: linear-gradient(180deg, rgba(0,0,0,0.25), rgba(0,0,0,0.7)); color: #fff; }
.recommend-info h4 { margin: 6px 0 4px; }
.recommend-tag { display: inline-block; background: #f59e0b; color: #fff; padding: 2px 8px; border-radius: 999px; font-size: 12px; }
.recommend-controls { display: flex; justify-content: flex-end; margin-bottom: 6px; }
.carousel-controls { display: flex; justify-content: flex-end; margin-bottom: 12px; }

.section-head { margin: 16px 0 10px; }
.sub { color: #909399; margin: 4px 0 0; }
.menu, .submenu { border-radius: 8px; margin-bottom: 10px; }
.content-card { cursor: pointer; margin-bottom: 12px; min-height: 140px; }
.content-cover { position: relative; height: 160px; overflow: hidden; border-radius: 6px; margin-bottom: 8px; }
.cover-tag { position: absolute; top: 8px; left: 8px; background: rgba(0,0,0,0.55); color: #fff; padding: 2px 8px; border-radius: 999px; font-size: 12px; }
.content-cover img { width: 100%; height: 100%; object-fit: cover; }
.content-meta { min-height: 110px; display: flex; flex-direction: column; gap: 6px; }
.summary { color: #606266; }
.tags { display: flex; gap: 6px; flex-wrap: wrap; }
.pager { margin-top: 10px; text-align: right; }
.video-placeholder { height: 260px; border: 1px dashed #dcdfe6; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #909399; }
.register-form { max-width: 420px; }
.query { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; }
.portal-auth { display: flex; gap: 12px; align-items: flex-start; }
.auth-card { width: 320px; }
.auth-title { font-weight: 600; margin-bottom: 8px; }
.auth-actions { display: flex; gap: 8px; }
.profile-card { margin-bottom: 10px; }
.profile-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.profile-name { font-size: 16px; font-weight: 600; }
.profile-meta { color: #909399; font-size: 12px; }
.profile-actions { display: flex; gap: 6px; }
.profile-info { color: #606266; display: flex; flex-direction: column; gap: 6px; }
.terminal-input { display: flex; gap: 8px; align-items: center; }
.media-card { margin-bottom: 10px; }
.media-thumb { position: relative; height: 120px; border-radius: 8px; overflow: hidden; background: #f5f7fa; display: flex; align-items: center; justify-content: center; }
.media-thumb img { width: 100%; height: 100%; object-fit: cover; }
.thumb-placeholder { color: #909399; text-transform: uppercase; }
.badge { position: absolute; right: 6px; bottom: 6px; background: rgba(0,0,0,0.6); color: #fff; padding: 2px 6px; border-radius: 6px; font-size: 12px; }
.badge.type { left: 6px; right: auto; top: 6px; bottom: auto; background: #409eff; }
.media-title { margin: 6px 0 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.media-meta { margin: 2px 0 0; font-size: 12px; color: #909399; }
.breadcrumb { margin-bottom: 10px; }
.detail-cover { margin-bottom: 12px; }
.detail-cover img { width: 100%; border-radius: 8px; object-fit: cover; }
.detail-body { line-height: 1.8; letter-spacing: 0.3px; color: #303133; }
.detail-body p { margin: 10px 0; }
.detail-body h2 { margin: 14px 0 8px; font-size: 18px; }
.detail-body ul { padding-left: 18px; }
.detail-body li { margin: 6px 0; }
.detail-body blockquote { border-left: 4px solid #409eff; padding: 6px 12px; color: #606266; background: #f5f7fa; margin: 10px 0; }
.preview-card { margin-top: 12px; }
.preview-head { display: flex; align-items: center; gap: 8px; justify-content: space-between; margin-bottom: 8px; }
.offline-alert { margin-bottom: 8px; }
.interval-text { color: #909399; font-size: 12px; }
.mini-title { display: flex; align-items: center; justify-content: space-between; font-weight: 600; margin-bottom: 6px; }
.mini-body { min-height: 60px; color: #606266; }
.mini-now { padding: 6px 8px; background: #f0f9eb; border-radius: 6px; margin-bottom: 6px; }
.mini-empty { color: #c0c4cc; }
.mini-playlist { padding: 6px 8px; border: 1px dashed #e4e7ed; border-radius: 6px; margin-bottom: 6px; }
.mini-name { font-weight: 600; }
.mini-count { font-size: 12px; color: #909399; }
.progress-row { display: flex; align-items: center; gap: 6px; width: 100%; }
.progress-row .time { font-size: 12px; color: #909399; width: 40px; text-align: center; }
.progress-wrap { position: relative; width: 100%; }
.buffered { position: absolute; left: 0; top: 50%; transform: translateY(-50%); height: 4px; background: #dcdfe6; width: 0; border-radius: 2px; z-index: 1; }
.progress-wrap :deep(.el-slider) { position: relative; z-index: 2; }
.preview { position: absolute; top: -90px; transform: translateX(-50%); width: 120px; background: #fff; border: 1px solid #e4e7ed; border-radius: 6px; padding: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.12); }
.preview img { width: 100%; height: 60px; object-fit: cover; border-radius: 4px; }
.preview-fallback { height: 60px; display: flex; align-items: center; justify-content: center; color: #909399; }
.preview-title { font-size: 12px; color: #303133; margin-top: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.preview-time { text-align: center; font-size: 12px; color: #606266; margin-top: 4px; }
.badge.quality { left: auto; right: 6px; top: 6px; bottom: auto; background: #67c23a; }
.badge.quality.hd { background: #67c23a; }
.badge.quality.sd { background: #909399; }
.badge.res { left: 6px; right: auto; bottom: 6px; top: auto; background: #111827; }
.video-filters { display: flex; gap: 8px; margin: 8px 0; }
.group-card { margin-top: 12px; }
.group-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.group-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 10px; }
.group-item { border: 1px solid #ebeef5; border-radius: 8px; padding: 10px; }
.group-name { font-weight: 600; margin-bottom: 6px; }
.group-meta { display: flex; justify-content: space-between; align-items: center; color: #909399; font-size: 12px; }
.group-code { color: #606266; }
</style>
