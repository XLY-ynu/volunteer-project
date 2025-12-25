<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in cards" :key="card.title">
        <el-card class="stat-card" :class="card.type" shadow="hover">
          <div class="stat-icon">
            <component :is="card.icon" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-title">{{ card.title }}</div>
            <div class="stat-desc">{{ card.desc }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" class="quick-actions">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="action-buttons">
            <el-button type="primary" @click="$router.push('/media')">
              <el-icon><Upload /></el-icon>
              上传资源
            </el-button>
            <el-button type="success" @click="$router.push('/playlists')">
              <el-icon><VideoPlay /></el-icon>
              创建播放列表
            </el-button>
            <el-button type="warning" @click="$router.push('/terminals')">
              <el-icon><Monitor /></el-icon>
              管理终端
            </el-button>
            <el-button type="info" @click="$router.push('/activities')">
              <el-icon><Calendar /></el-icon>
              创建活动
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 终端状态 -->
    <el-row :gutter="20">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="status-card">
          <template #header>
            <div class="card-header">
              <span>终端状态</span>
              <el-button text type="primary" @click="$router.push('/terminals')">查看全部</el-button>
            </div>
          </template>
          <div class="terminal-status">
            <div class="status-item online">
              <div class="status-dot"></div>
              <span class="status-label">在线</span>
              <span class="status-count">{{ terminalStatus.online }}</span>
            </div>
            <div class="status-item offline">
              <div class="status-dot"></div>
              <span class="status-label">离线</span>
              <span class="status-count">{{ terminalStatus.offline }}</span>
            </div>
          </div>
          <el-alert
            v-if="terminalStatus.offline > 0"
            type="warning"
            :closable="false"
            show-icon
            style="margin-top: 16px"
          >
            <template #title>
              {{ terminalStatus.offline }} 台终端离线，请检查网络连接
            </template>
          </el-alert>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="status-card">
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <div class="system-info">
            <div class="info-item">
              <span class="info-label">系统版本</span>
              <span class="info-value">v1.0.0</span>
            </div>
            <div class="info-item">
              <span class="info-label">当前用户</span>
              <span class="info-value">{{ userStore.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">登录时间</span>
              <span class="info-value">{{ loginTime }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, markRaw } from 'vue';
import { fetchSummary, fetchTerminalStatus } from '../api';
import { useUserStore } from '../stores/user';
import { Monitor, Picture, VideoPlay, Calendar, Upload } from '@element-plus/icons-vue';

const userStore = useUserStore();
const loginTime = ref(new Date().toLocaleString());

const cards = ref([
  { title: '终端数', value: 0, desc: '注册的显示终端', type: 'primary', icon: markRaw(Monitor) },
  { title: '资源数', value: 0, desc: '媒体资源总数', type: 'success', icon: markRaw(Picture) },
  { title: '播放列表', value: 0, desc: '可分发的列表', type: 'warning', icon: markRaw(VideoPlay) },
  { title: '活动数', value: 0, desc: '志愿活动', type: 'info', icon: markRaw(Calendar) }
]);

const terminalStatus = ref({ online: 0, offline: 0, offlineTerminals: [] });

const load = async () => {
  const resp = await fetchSummary();
  // @ts-ignore
  const d = resp.data?.data || {};
  cards.value = [
    { title: '终端数', value: d.terminalTotal || 0, desc: '注册的显示终端', type: 'primary', icon: markRaw(Monitor) },
    { title: '资源数', value: d.mediaTotal || 0, desc: '媒体资源总数', type: 'success', icon: markRaw(Picture) },
    { title: '播放列表', value: d.playlistTotal || 0, desc: '可分发的列表', type: 'warning', icon: markRaw(VideoPlay) },
    { title: '活动数', value: d.activityTotal || 0, desc: '志愿活动', type: 'info', icon: markRaw(Calendar) }
  ];

  const statusResp = await fetchTerminalStatus();
  // @ts-ignore
  terminalStatus.value = statusResp.data?.data || { online: 0, offline: 0, offlineTerminals: [] };
};

onMounted(load);
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
}

.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 24px;
  gap: 20px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.stat-card.primary .stat-icon {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.stat-card.success .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.stat-card.warning .stat-icon {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
}

.stat-card.info .stat-icon {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-title {
  font-size: 16px;
  color: #606266;
  margin-top: 4px;
}

.stat-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.quick-actions {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  padding: 20px 24px;
  font-size: 14px;
}

.status-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.terminal-status {
  display: flex;
  gap: 40px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.status-item.online .status-dot {
  background: #67c23a;
  box-shadow: 0 0 8px rgba(103, 194, 58, 0.5);
}

.status-item.offline .status-dot {
  background: #f56c6c;
  box-shadow: 0 0 8px rgba(245, 108, 108, 0.5);
}

.status-label {
  color: #606266;
}

.status-count {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.system-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #909399;
}

.info-value {
  color: #303133;
  font-weight: 500;
}
</style>
