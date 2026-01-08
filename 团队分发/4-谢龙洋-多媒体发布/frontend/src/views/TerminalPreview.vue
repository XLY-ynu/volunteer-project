<!--
 * @Author: 谢龙洋
 * @Module: 多媒体发布 - 终端预览
 * @Description: 终端播放预览页面，查看终端播放计划
-->
<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>终端播放预览</h3>
          <span class="subtitle">查看终端播放计划</span>
        </div>
        <div class="header-actions">
          <el-select v-model="code" placeholder="选择终端" style="width: 220px" filterable>
            <el-option v-for="t in terminals" :key="t.code" :label="`${t.name || t.code}`" :value="t.code">
              <div class="terminal-option">
                <span>{{ t.name || t.code }}</span>
                <el-tag :type="t.status === 'online' ? 'success' : 'danger'" size="small">
                  {{ t.status === 'online' ? '在线' : '离线' }}
                </el-tag>
              </div>
            </el-option>
          </el-select>
        </div>
      </div>
    </el-card>

    <!-- 插播优先级提示 -->
    <el-alert v-if="code && hasBroadcast" type="warning" :closable="false" class="priority-alert">
      <template #title>
        <span>插播内容优先级最高，会打断正常播放列表</span>
      </template>
    </el-alert>

    <el-card class="content-card" shadow="never">
      <el-empty v-if="!code" description="请选择终端查看播放计划" />
      <el-empty v-else-if="allItems.length === 0" description="该终端暂无播放计划" />
      
      <el-table v-else :data="allItems" :row-class-name="getRowClass">
        <el-table-column label="优先级" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isBroadcast" type="danger" size="small" effect="dark">高</el-tag>
            <el-tag v-else type="info" size="small">普通</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isBroadcast" type="danger" size="small">插播</el-tag>
            <el-tag v-else type="primary" size="small">播放列表</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="名称" min-width="120">
          <template #default="scope">{{ scope.row.name }}</template>
        </el-table-column>
        <el-table-column label="时间范围" min-width="200">
          <template #default="scope">
            <span>{{ formatTime(scope.row.startTime) || '立即' }} ~ {{ formatTime(scope.row.endTime) || '永久' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row)" size="small">{{ getStatusText(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容详情" min-width="200">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="showDetail(scope.row)">
              查看详情 ({{ scope.row.itemCount }}项)
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 内容详情对话框 -->
    <el-dialog v-model="detailVisible" :title="detailTitle" width="600px">
      <el-table :data="detailItems" size="small" max-height="400">
        <el-table-column label="序号" width="60" type="index" />
        <el-table-column label="类型" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.type === 'media'" type="success" size="small">媒体</el-tag>
            <el-tag v-else type="primary" size="small">内容</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="名称" min-width="200">
          <template #default="scope">{{ scope.row.name }}</template>
        </el-table-column>
        <el-table-column label="时长" width="80">
          <template #default="scope">{{ scope.row.duration ? scope.row.duration + '秒' : '-' }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, computed } from 'vue';
import http from '../api/http';
import { fetchTerminals } from '../api';

const code = ref('');
const terminals = ref<any[]>([]);
const playbacks = ref<any[]>([]);
const broadcasts = ref<any[]>([]);
const detailVisible = ref(false);
const detailTitle = ref('');
const detailItems = ref<any[]>([]);

const hasBroadcast = computed(() => broadcasts.value.length > 0);

const allItems = computed(() => {
  const items: any[] = [];
  
  // 播放列表
  playbacks.value.forEach(p => {
    items.push({
      isBroadcast: false,
      name: p.playlist?.name || '未知列表',
      startTime: p.startTime,
      endTime: p.endTime,
      itemCount: p.items?.length || 0,
      rawData: p
    });
  });
  
  // 插播 - 从 job 对象获取时间
  broadcasts.value.forEach(b => {
    const job = b.job || b;
    items.push({
      isBroadcast: true,
      name: job.title || '插播',
      startTime: job.startTime,
      endTime: job.endTime,
      itemCount: 1,
      rawData: b
    });
  });
  
  // 按开始时间排序
  items.sort((a, b) => {
    const timeA = a.startTime ? new Date(a.startTime).getTime() : 0;
    const timeB = b.startTime ? new Date(b.startTime).getTime() : 0;
    return timeA - timeB;
  });
  
  return items;
});

const loadTerminals = async () => {
  const resp = await fetchTerminals(1, 200);
  terminals.value = resp.data?.data?.records || [];
};

const load = async () => {
  if (!code.value) return;
  
  const resp = await http.get('/public/playback', { params: { terminalCode: code.value } });
  playbacks.value = resp.data?.data || [];
  
  const terminal = terminals.value.find(t => t.code === code.value);
  const broadcastResp = await http.get('/public/broadcasts/active', { 
    params: { terminalCode: code.value, groupName: terminal?.groupName || '' } 
  });
  broadcasts.value = broadcastResp.data?.data || [];
};

const formatTime = (t: string) => t ? t.replace('T', ' ').substring(0, 16) : '';

const getStatusType = (item: any) => {
  const now = new Date();
  const start = item.startTime ? new Date(item.startTime) : null;
  const end = item.endTime ? new Date(item.endTime) : null;
  if (end && now > end) return 'info';
  if (start && now < start) return 'warning';
  return 'success';
};

const getStatusText = (item: any) => {
  const now = new Date();
  const start = item.startTime ? new Date(item.startTime) : null;
  const end = item.endTime ? new Date(item.endTime) : null;
  if (end && now > end) return '已结束';
  if (start && now < start) return '未开始';
  return '进行中';
};

const getRowClass = ({ row }: any) => row.isBroadcast ? 'broadcast-row' : '';

const showDetail = (item: any) => {
  detailTitle.value = item.name + ' - 内容详情';
  
  if (item.isBroadcast) {
    const b = item.rawData;
    const job = b.job || b;
    detailItems.value = [{
      type: job.mediaId ? 'media' : 'content',
      name: b.media?.originalName || b.media?.name || b.content?.title || (job.mediaId ? `媒体#${job.mediaId}` : `内容#${job.contentId}`),
      duration: null
    }];
  } else {
    const p = item.rawData;
    detailItems.value = (p.items || []).map((it: any) => {
      if (it.mediaId) {
        const media = (p.mediaAssets || []).find((m: any) => m.id === it.mediaId);
        return { type: 'media', name: media?.originalName || media?.name || `媒体#${it.mediaId}`, duration: it.displayDuration };
      }
      const content = (p.contentAssets || []).find((c: any) => c.id === it.contentId);
      return { type: 'content', name: content?.title || `内容#${it.contentId}`, duration: it.displayDuration };
    });
  }
  
  detailVisible.value = true;
};

watch(code, (val) => { if (val) load(); });

onMounted(() => { loadTerminals(); });
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .content-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; margin-left: 12px; }
.terminal-option { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.priority-alert { border-radius: 8px; }
:deep(.broadcast-row) { background-color: #fef0f0 !important; }
:deep(.broadcast-row:hover > td) { background-color: #fde2e2 !important; }
</style>
