<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>终端播放预览</h3>
          <span class="subtitle">查看终端当前播放内容和插播</span>
        </div>
        <div class="header-actions">
          <el-select v-model="code" placeholder="选择终端" style="width: 220px" filterable clearable>
            <el-option v-for="t in terminals" :key="t.code" :label="`${t.name} (${t.code})`" :value="t.code">
              <div class="terminal-option">
                <span>{{ t.name }}</span>
                <el-tag :type="t.status === 'online' ? 'success' : 'danger'" size="small">{{ t.status === 'online' ? '在线' : '离线' }}</el-tag>
              </div>
            </el-option>
          </el-select>
          <el-button type="primary" @click="load" :disabled="!code">
            <el-icon><Search /></el-icon>
            获取播放
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 插播提示 -->
    <el-alert v-if="broadcasts.length > 0" type="error" :closable="false" class="broadcast-alert">
      <template #title>
        <div class="broadcast-alert-title">
          <el-icon><Bell /></el-icon>
          <span>该终端有 {{ broadcasts.length }} 个插播正在进行</span>
        </div>
      </template>
      <div class="broadcast-alert-content">
        插播内容将打断正常播放，优先显示
      </div>
    </el-alert>

    <!-- 插播列表 -->
    <el-card v-if="broadcasts.length > 0" class="content-card broadcast-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Bell /></el-icon>
          <span>当前插播</span>
          <el-tag type="danger" size="small">优先播放</el-tag>
        </div>
      </template>
      <el-table :data="broadcasts" size="small">
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column label="内容" width="150">
          <template #default="scope">
            <div class="content-cell">
              <el-tag v-if="scope.row.mediaId" type="success" size="small">媒体</el-tag>
              <el-tag v-else-if="scope.row.contentId" type="primary" size="small">内容</el-tag>
              <span>{{ getResourceName(scope.row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="时间范围" min-width="180">
          <template #default="scope">
            <span>{{ formatTime(scope.row.startTime) || '立即' }} ~ {{ formatTime(scope.row.endTime) || '永久' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag type="danger" size="small" effect="dark">{{ scope.row.status === 'active' ? '播放中' : '待播放' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 正常播放列表 -->
    <el-card class="content-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><VideoPlay /></el-icon>
          <span>播放列表</span>
          <el-tag v-if="broadcasts.length > 0" type="info" size="small">插播结束后播放</el-tag>
        </div>
      </template>
      
      <el-empty v-if="!code" description="请选择终端查看播放内容" />
      <el-empty v-else-if="playbacks.length === 0" description="该终端暂无绑定播放列表" />
      
      <el-collapse v-else accordion>
        <el-collapse-item v-for="p in playbacks" :key="p.playlist?.id">
          <template #title>
            <div class="playlist-title">
              <span class="playlist-name">{{ p.playlist?.name }}</span>
              <el-tag v-if="p.layout" size="small">{{ p.layout.name }}</el-tag>
              <span class="playlist-count">{{ p.items?.length || 0 }} 项</span>
            </div>
          </template>
          <el-table :data="p.items" size="small">
            <el-table-column prop="sortOrder" label="#" width="60" />
            <el-table-column label="资源" min-width="200">
              <template #default="scope">
                <div class="resource-cell">
                  <el-tag v-if="scope.row.mediaId" type="success" size="small">媒体#{{ scope.row.mediaId }}</el-tag>
                  <el-tag v-else-if="scope.row.contentId" type="primary" size="small">内容#{{ scope.row.contentId }}</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="displayDuration" label="时长(秒)" width="100" />
          </el-table>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import http from '../api/http';
import { fetchTerminals, fetchActiveBroadcasts } from '../api';
import { ElMessage } from 'element-plus';
import { Search, Bell, VideoPlay } from '@element-plus/icons-vue';

const code = ref('');
const terminals = ref<any[]>([]);
const playbacks = ref<any[]>([]);
const broadcasts = ref<any[]>([]);

const loadTerminals = async () => {
  const resp = await fetchTerminals(1, 200);
  terminals.value = resp.data?.data?.records || [];
};

const load = async () => {
  if (!code.value) {
    ElMessage.warning('请选择终端');
    return;
  }
  
  // 获取正常播放列表
  const resp = await http.get('/public/playback', { params: { terminalCode: code.value } });
  playbacks.value = resp.data?.data || [];
  
  // 获取当前插播
  const terminal = terminals.value.find(t => t.code === code.value);
  const groupName = terminal?.groupName || '';
  const broadcastResp = await fetchActiveBroadcasts(code.value, groupName);
  broadcasts.value = broadcastResp.data?.data?.records || [];
  
  if (!playbacks.value.length && !broadcasts.value.length) {
    ElMessage.info('该终端暂无有效播放内容');
  }
};

const formatTime = (t: string) => t ? t.replace('T', ' ').substring(0, 16) : '';

const getResourceName = (row: any) => {
  if (row.mediaId) return `#${row.mediaId}`;
  if (row.contentId) return `#${row.contentId}`;
  return '-';
};

// 选择终端后自动加载
watch(code, (val) => {
  if (val) load();
});

onMounted(() => {
  loadTerminals();
});
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .content-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; }
.header-actions { display: flex; gap: 8px; align-items: center; }

.terminal-option { display: flex; justify-content: space-between; align-items: center; width: 100%; }

.broadcast-alert { border-radius: 12px; }
.broadcast-alert-title { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.broadcast-alert-content { margin-top: 4px; font-size: 13px; }

.broadcast-card { border: 2px solid #f56c6c; }
.broadcast-card :deep(.el-card__header) { background: linear-gradient(135deg, #fef0f0, #fde2e2); }

.card-header { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.card-header .el-icon { font-size: 18px; }

.content-cell { display: flex; align-items: center; gap: 6px; }

.playlist-title { display: flex; align-items: center; gap: 12px; width: 100%; }
.playlist-name { font-weight: 500; }
.playlist-count { margin-left: auto; color: #909399; font-size: 13px; }

.resource-cell { display: flex; align-items: center; gap: 8px; }
</style>
