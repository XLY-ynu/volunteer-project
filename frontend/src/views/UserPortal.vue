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
          <el-row :gutter="12">
            <el-col :span="12" v-for="item in contentList" :key="item.id">
              <el-card class="content-card" shadow="hover" @click="openContent(item.id)">
                <div class="content-cover" v-if="item.coverUrl">
                  <img :src="item.coverUrl" :alt="item.title" />
                </div>
                <h4>{{ item.title }}</h4>
                <p class="summary">{{ item.summary || '查看详情' }}</p>
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
          <el-input v-model="terminalCode" placeholder="终端代码，例如 public-screen" style="width: 260px" @change="loadPlayback" />
          <el-button @click="loadPlayback">刷新</el-button>
        </div>
      </div>
      <el-row :gutter="12">
        <el-col :span="12">
          <video v-if="currentMedia?.url" controls autoplay style="width: 100%; border-radius: 8px" :src="currentMedia.url"></video>
          <div v-else class="video-placeholder">暂无可播媒体</div>
        </el-col>
        <el-col :span="12">
          <el-table :data="mediaAssets" size="small" @row-click="playMedia">
            <el-table-column prop="name" label="媒体" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column label="时长" width="80">
              <template #default="scope">
                <span>{{ scope.row.durationSeconds ? scope.row.durationSeconds + 's' : '-' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
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
      <div v-html="contentDetail?.body || contentDetail?.summary"></div>
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
const contentPage = ref(1);
const contentSize = ref(6);
const contentTotal = ref(0);
const contentDialog = ref(false);
const contentDetail = ref<any | null>(null);

const terminalCode = ref('public-screen');
const playback = ref<any[]>([]);
const mediaAssets = ref<any[]>([]);
const currentMedia = ref<any | null>(null);
const playerTimer = ref<number | null>(null);

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
  const resp = await fetchPublicContent(contentPage.value, contentSize.value, activeChild.value?.id, keyword.value);
  const data = resp.data?.data || {};
  contentList.value = data.records || [];
  contentTotal.value = data.total || 0;
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
  const resp = await fetchPlaybackPublic(terminalCode.value);
  playback.value = resp.data?.data || [];
  const first = playback.value[0];
  mediaAssets.value = first?.mediaAssets || [];
  currentMedia.value = mediaAssets.value[0] || null;
  scheduleNext();
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

onMounted(async () => {
  await loadCategories();
  await loadContent();
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

.section-head { margin: 16px 0 10px; }
.sub { color: #909399; margin: 4px 0 0; }
.menu, .submenu { border-radius: 8px; margin-bottom: 10px; }
.content-card { cursor: pointer; margin-bottom: 12px; min-height: 140px; }
.content-cover { height: 120px; overflow: hidden; border-radius: 6px; margin-bottom: 8px; }
.content-cover img { width: 100%; height: 100%; object-fit: cover; }
.summary { color: #606266; }
.pager { margin-top: 10px; text-align: right; }
.video-placeholder { height: 260px; border: 1px dashed #dcdfe6; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #909399; }
.register-form { max-width: 420px; }
.query { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; }
.terminal-input { display: flex; gap: 8px; align-items: center; }
</style>
