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
      <el-carousel v-if="recommendedList.length > 1" height="200px" indicator-position="outside" class="recommend-carousel">
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
            <el-col :span="12" v-for="m in mediaAssets" :key="m.id">
              <el-card class="media-card" shadow="hover" @click="playMedia(m)">
                <div class="media-thumb">
                  <img v-if="m.thumbUrl" :src="m.thumbUrl" />
                  <div v-else class="thumb-placeholder">{{ m.type }}</div>
                  <span class="badge">{{ m.durationSeconds ? m.durationSeconds + 's' : '—' }}</span>
                  <span class="badge type">{{ m.type?.toUpperCase() || 'MEDIA' }}</span>
                  <span v-if="m.type === 'video'" class="badge quality" :class="qualityClass(m)">{{ qualityLabel(m) }}</span>
                </div>
                <p class="media-title">{{ m.name }}</p>
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
        </div>
        <el-row :gutter="10">
          <el-col :span="8" v-for="p in multiPreviews" :key="p.terminal">
            <el-card shadow="hover">
              <div class="mini-title">{{ p.terminal }}</div>
              <div class="mini-body">
                <div class="mini-now" v-if="p.currentMedia">
                  <div class="mini-name">播放中：{{ p.currentMedia.name }}</div>
                  <div class="mini-count">时长：{{ p.currentMedia.durationSeconds || '-' }}s</div>
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
        <p class="sub">留下联系方式方便报名与签到</p>
      </div>
      <el-form label-width="90px" class="register-form">
        <el-form-item label="姓名">
          <el-input v-model="volunteerForm.name" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="volunteerForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="volunteerForm.email" />
        </el-form-item>
        <el-form-item label="所属组织">
          <el-input v-model="volunteerForm.organization" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="registerVolunteer">提交</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="my-signups">
      <div class="section-head">
        <h3>我的报名/签到</h3>
        <p class="sub">输入手机号查看状态</p>
      </div>
      <div class="query">
        <el-input v-model="queryPhone" placeholder="输入手机号查询" style="width: 240px" />
        <el-button @click="loadSignups">查询</el-button>
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
import { onMounted, onBeforeUnmount, ref } from 'vue';
import { ElMessage } from 'element-plus';
import {
  fetchActivitiesPublic,
  fetchPlaybackPublic,
  fetchPublicCategories,
  fetchPublicContent,
  fetchPublicContentById,
  fetchVolunteerSignups,
  registerVolunteerPublic,
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

const activities = ref<any[]>([]);
const activityPage = ref(1);
const activitySize = ref(8);
const activityTotal = ref(0);
const dialogVisible = ref(false);
const signupForm = ref({ name: '', phone: '', email: '', organization: '' });
const currentActivity = ref<number | null>(null);

const volunteerForm = ref({ name: '', phone: '', email: '', organization: '' });
const signups = ref<any[]>([]);
const queryPhone = ref('');

const stats = ref<{ playlistTotal: number; activityTotal: number; mediaTotal: number }>({
  playlistTotal: 0,
  activityTotal: 0,
  mediaTotal: 0
});
const isAdmin = ref(!!localStorage.getItem('token'));

const scrollTo = (id: string) => {
  const el = document.getElementById(id);
  if (el) el.scrollIntoView({ behavior: 'smooth' });
};

const loadCategories = async () => {
  const resp = await fetchPublicCategories();
  parents.value = resp.data?.data || [];
  if (parents.value.length) selectParent(parents.value[0]);
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
    recommendedList.value = contentList.value
      .filter((i: any) => i.recommended)
      .sort((a: any, b: any) => {
        const sa = a.sortOrder ?? 0;
        const sb = b.sortOrder ?? 0;
        if (sa !== sb) return sa - sb;
        return new Date(b.publishTime || b.createdAt || 0).getTime() - new Date(a.publishTime || a.createdAt || 0).getTime();
      });
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
  dialogVisible.value = true;
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

const registerVolunteer = async () => {
  if (!volunteerForm.value.name || !volunteerForm.value.phone) {
    ElMessage.warning('请输入姓名和手机号');
    return;
  }
  try {
    await registerVolunteerPublic(volunteerForm.value);
    ElMessage.success('提交成功，等待审核');
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '提交失败');
  }
};

const loadSignups = async () => {
  if (!queryPhone.value) {
    ElMessage.warning('请输入手机号');
    return;
  }
  const resp = await fetchVolunteerSignups(queryPhone.value);
  signups.value = resp.data?.data || [];
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

const onMultiSelectedChange = () => {
  loadMultiPreviews();
  startMultiPolling();
};

const startMultiPolling = () => {
  if (multiTimer.value) {
    clearInterval(multiTimer.value);
  }
  if (!multiSelected.value.length || !previewPollingEnabled.value) {
    stopPreviewClock();
    return;
  }
  startPreviewClock();
  multiTimer.value = window.setInterval(() => {
    loadMultiPreviews();
  }, previewInterval.value * 1000);
};

const stopMultiPolling = () => {
  if (multiTimer.value) {
    clearInterval(multiTimer.value);
    multiTimer.value = null;
  }
  stopPreviewClock();
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
  const hd =
    (m.width && m.width >= 1280) ||
    (m.height && m.height >= 720) ||
    (m.bitrateKbps && m.bitrateKbps >= 2500) ||
    (m.frameRate && m.frameRate >= 30) ||
    (m.sizeBytes && m.sizeBytes > 50 * 1024 * 1024);
  return hd ? 'HD' : 'SD';
};

const qualityClass = (m: any) => {
  return qualityLabel(m) === 'HD' ? 'hd' : 'sd';
};

onMounted(async () => {
  await loadCategories();
  await loadContent();
  const saved = localStorage.getItem('portal_terminal_code');
  if (saved) terminalCode.value = saved;
  await loadPlayback();
  await loadActivities();
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
.terminal-input { display: flex; gap: 8px; align-items: center; }
.media-card { margin-bottom: 10px; }
.media-thumb { position: relative; height: 120px; border-radius: 8px; overflow: hidden; background: #f5f7fa; display: flex; align-items: center; justify-content: center; }
.media-thumb img { width: 100%; height: 100%; object-fit: cover; }
.thumb-placeholder { color: #909399; text-transform: uppercase; }
.badge { position: absolute; right: 6px; bottom: 6px; background: rgba(0,0,0,0.6); color: #fff; padding: 2px 6px; border-radius: 6px; font-size: 12px; }
.badge.type { left: 6px; right: auto; top: 6px; bottom: auto; background: #409eff; }
.media-title { margin: 6px 0 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
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
.interval-text { color: #909399; font-size: 12px; }
.mini-title { font-weight: 600; margin-bottom: 6px; }
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
.preview-time { text-align: center; font-size: 12px; color: #606266; margin-top: 4px; }
.badge.quality { left: auto; right: 6px; top: 6px; bottom: auto; background: #67c23a; }
.badge.quality.hd { background: #67c23a; }
.badge.quality.sd { background: #909399; }
</style>
