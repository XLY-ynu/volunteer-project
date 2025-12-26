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
              <el-slider v-model="progress" :min="0" :max="100" @change="seek" />
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
          <el-select v-model="multiSelected" multiple placeholder="选择终端" size="small" style="min-width: 280px" @change="loadMultiPreviews">
            <el-option v-for="t in favoriteTerminals" :key="t" :label="t" :value="t" />
          </el-select>
        </div>
        <el-row :gutter="10">
          <el-col :span="8" v-for="p in multiPreviews" :key="p.terminal">
            <el-card shadow="hover">
              <div class="mini-title">{{ p.terminal }}</div>
              <div class="mini-body">
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
      <div v-html="contentDetail?.body || contentDetail?.summary"></div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
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

const terminalCode = ref('public-screen');
const playback = ref<any[]>([]);
const mediaAssets = ref<any[]>([]);
const currentMedia = ref<any | null>(null);
const playerTimer = ref<number | null>(null);
const autoPlay = ref(true);
const activePlaylistId = ref<number | null>(null);
const playbackLoading = ref(false);
const playbackRate = ref(1);
const rates = [0.75, 1, 1.25, 1.5];
const volume = ref(0.8);
const muted = ref(false);
const videoRef = ref<HTMLVideoElement>();
const favoriteTerminals = ref<string[]>(() => {
  const saved = localStorage.getItem('portal_fav_terminals');
  return saved ? JSON.parse(saved) : [];
} as any);
const multiSelected = ref<string[]>([]);
const multiPreviews = ref<any[]>([]);
const currentTime = ref(0);
const duration = ref(0);
const progress = ref(0);

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
    headline.value = contentPage.value === 1 && contentList.value.length ? contentList.value[0] : headline.value;
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
      return;
    }
    const first = playback.value[0];
    activePlaylistId.value = first?.playlist?.id || null;
    mediaAssets.value = first?.mediaAssets || [];
    currentMedia.value = mediaAssets.value[0] || null;
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
  mediaAssets.value = target?.mediaAssets || [];
  currentMedia.value = mediaAssets.value[0] || null;
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
  ElMessageBox.prompt('输入新终端名称以重命名，或留空删除当前选择', '管理收藏', {
    inputPlaceholder: '如需删除，请留空',
    inputValue: terminalCode.value
  })
    .then(({ value }) => {
      const idx = favoriteTerminals.value.indexOf(terminalCode.value);
      if (idx === -1) return;
      if (!value) {
        favoriteTerminals.value.splice(idx, 1);
        ElMessage.success('已删除收藏');
      } else {
        favoriteTerminals.value.splice(idx, 1, value);
        terminalCode.value = value;
        ElMessage.success('已重命名');
      }
      localStorage.setItem('portal_fav_terminals', JSON.stringify(favoriteTerminals.value));
    })
    .catch(() => {});
};

const onTimeUpdate = () => {
  if (!videoRef.value) return;
  currentTime.value = videoRef.value.currentTime;
  duration.value = videoRef.value.duration || 0;
  progress.value = duration.value ? (currentTime.value / duration.value) * 100 : 0;
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
    results.push({ terminal: t, playlists: resp.data?.data || [] });
  }
  multiPreviews.value = results;
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
.preview-card { margin-top: 12px; }
.preview-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.mini-title { font-weight: 600; margin-bottom: 6px; }
.mini-body { min-height: 60px; color: #606266; }
.mini-empty { color: #c0c4cc; }
.mini-playlist { padding: 6px 8px; border: 1px dashed #e4e7ed; border-radius: 6px; margin-bottom: 6px; }
.mini-name { font-weight: 600; }
.mini-count { font-size: 12px; color: #909399; }
.progress-row { display: flex; align-items: center; gap: 6px; width: 100%; }
.progress-row .time { font-size: 12px; color: #909399; width: 40px; text-align: center; }
</style>
