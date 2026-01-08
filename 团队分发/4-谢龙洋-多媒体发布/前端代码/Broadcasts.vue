<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>插播管理</h3>
          <span class="subtitle">创建紧急插播，打断正常播放内容</span>
        </div>
        <el-button type="danger" @click="openCreate">
          <el-icon><Bell /></el-icon>
          新建插播
        </el-button>
      </div>
    </el-card>

    <!-- 状态统计 -->
    <el-row :gutter="16" class="status-row">
      <el-col :span="8">
        <el-card class="status-card active" shadow="hover">
          <div class="status-icon"><el-icon><VideoPlay /></el-icon></div>
          <div class="status-info">
            <div class="status-value">{{ statusCount.active }}</div>
            <div class="status-label">进行中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="status-card pending" shadow="hover">
          <div class="status-icon"><el-icon><Clock /></el-icon></div>
          <div class="status-info">
            <div class="status-value">{{ statusCount.pending }}</div>
            <div class="status-label">待执行</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="status-card completed" shadow="hover">
          <div class="status-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="status-info">
            <div class="status-value">{{ statusCount.completed }}</div>
            <div class="status-label">已完成</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 插播列表 -->
    <el-card class="content-card" shadow="never">
      <el-table :data="list" stripe>
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column label="插播内容" min-width="200">
          <template #default="scope">
            <div class="content-cell">
              <template v-if="scope.row.mediaId">
                <el-tag type="success" size="small">媒体</el-tag>
                <span>{{ getMediaName(scope.row.mediaId) }}</span>
              </template>
              <template v-else-if="scope.row.contentId">
                <el-tag type="primary" size="small">内容</el-tag>
                <span>{{ getContentTitle(scope.row.contentId) }}</span>
              </template>
              <span v-else class="text-muted">未指定</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="目标" width="160">
          <template #default="scope">
            <div v-if="scope.row.targetGroup" class="target-cell">
              <el-icon><Folder /></el-icon>
              <span>{{ scope.row.targetGroup }}</span>
            </div>
            <div v-else-if="scope.row.targetTerminalCode" class="target-cell">
              <el-icon><Monitor /></el-icon>
              <span>{{ scope.row.targetTerminalCode }}</span>
            </div>
            <el-tag v-else size="small">全部终端</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="scope">
            <div class="time-cell">
              <span>{{ formatTime(scope.row.startTime) }}</span>
              <span class="time-sep">~</span>
              <span>{{ formatTime(scope.row.endTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row)" size="small">
              {{ getStatusText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="danger" 
              plain
              @click="cancelBroadcast(scope.row.id)" 
              :disabled="calculateStatus(scope.row) === 'completed'"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination layout="total, prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="onPage" />
      </div>
    </el-card>

    <!-- 新建插播对话框 -->
    <el-dialog v-model="dialogVisible" title="新建插播" width="500px" destroy-on-close>
      <el-alert type="warning" :closable="false" class="broadcast-alert">
        <template #title>插播将立即打断目标终端的正常播放</template>
      </el-alert>

      <el-form :model="form" label-width="90px" class="broadcast-form">
        <el-form-item label="插播标题" required>
          <el-input v-model="form.title" placeholder="如：紧急通知、活动预告" />
        </el-form-item>

        <el-form-item label="插播内容" required>
          <el-radio-group v-model="contentType" class="content-type-radio">
            <el-radio-button label="media">媒体资源</el-radio-button>
            <el-radio-button label="content">图文内容</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="contentType === 'media'" label="选择媒体">
          <el-select v-model="form.mediaId" placeholder="选择要插播的媒体" style="width: 100%" filterable>
            <el-option v-for="m in mediaList" :key="m.id" :label="m.name" :value="m.id">
              <div class="media-option">
                <el-tag :type="m.type === 'video' ? 'warning' : 'success'" size="small">{{ m.type === 'video' ? '视频' : '图片' }}</el-tag>
                <span>{{ m.name }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item v-if="contentType === 'content'" label="选择内容">
          <el-select v-model="form.contentId" placeholder="选择要插播的内容" style="width: 100%" filterable>
            <el-option v-for="c in contentList" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="目标范围">
          <el-radio-group v-model="targetType">
            <el-radio label="all">全部终端</el-radio>
            <el-radio label="group">指定分组</el-radio>
            <el-radio label="terminal">指定终端</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="targetType === 'group'" label="目标分组">
          <el-select v-model="form.targetGroup" placeholder="选择终端分组" style="width: 100%">
            <el-option v-for="g in groupList" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="targetType === 'terminal'" label="目标终端">
          <el-select v-model="form.targetTerminalCode" placeholder="选择终端" style="width: 100%" filterable>
            <el-option v-for="t in terminalList" :key="t.code" :label="`${t.name} (${t.code})`" :value="t.code" />
          </el-select>
        </el-form-item>

        <el-form-item label="播放时间">
          <el-date-picker v-model="startEnd" type="datetimerange" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submit">
          <el-icon><Bell /></el-icon>
          立即发布
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createBroadcast, fetchBroadcasts, deleteBroadcast, fetchBroadcastStatusCount, fetchMedia, fetchContent, fetchTerminals } from '../api';
import { Bell, VideoPlay, Clock, CircleCheck, Folder, Monitor } from '@element-plus/icons-vue';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const dialogVisible = ref(false);
const contentType = ref<'media' | 'content'>('media');
const targetType = ref<'all' | 'group' | 'terminal'>('all');
const mediaList = ref<any[]>([]);
const contentList = ref<any[]>([]);
const terminalList = ref<any[]>([]);
const groupList = ref<string[]>([]);

const form = ref({
  title: '',
  mediaId: undefined as number | undefined,
  contentId: undefined as number | undefined,
  targetGroup: '',
  targetTerminalCode: ''
});
const startEnd = ref<[string, string] | null>(null);
const statusCount = ref({ active: 0, pending: 0, completed: 0 });

const load = async () => {
  const resp = await fetchBroadcasts(page.value, size.value);
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};

const loadStatusCount = async () => {
  try {
    const resp = await fetchBroadcastStatusCount();
    statusCount.value = resp.data?.data || { active: 0, pending: 0, completed: 0 };
  } catch (e) { /* ignore */ }
};

const loadMedia = async () => {
  const resp = await fetchMedia(1, 200);
  mediaList.value = resp.data?.data?.records || [];
};

const loadContent = async () => {
  const resp = await fetchContent(1, 200, undefined, true);
  contentList.value = resp.data?.data?.records || [];
};

const loadTerminals = async () => {
  const resp = await fetchTerminals(1, 200);
  terminalList.value = resp.data?.data?.records || [];
  const groups = new Set<string>();
  terminalList.value.forEach((t: any) => { if (t.groupName) groups.add(t.groupName); });
  groupList.value = Array.from(groups);
};

const getMediaName = (id: number) => mediaList.value.find(m => m.id === id)?.name || `媒体#${id}`;
const getContentTitle = (id: number) => contentList.value.find(c => c.id === id)?.title || `内容#${id}`;
const formatTime = (t: string) => t ? t.replace('T', ' ').substring(5, 16) : '-';

const calculateStatus = (row: any): string => {
  const now = new Date();
  const start = row.startTime ? new Date(row.startTime) : null;
  const end = row.endTime ? new Date(row.endTime) : null;
  if (end && end < now) return 'completed';
  if (start && start > now) return 'pending';
  return 'active';
};

const getStatusType = (row: any) => {
  const s = calculateStatus(row);
  return s === 'active' ? 'danger' : s === 'pending' ? 'warning' : 'info';
};

const getStatusText = (row: any) => {
  const s = calculateStatus(row);
  return s === 'active' ? '进行中' : s === 'pending' ? '待执行' : '已完成';
};

const openCreate = () => {
  form.value = { title: '', mediaId: undefined, contentId: undefined, targetGroup: '', targetTerminalCode: '' };
  startEnd.value = null;
  contentType.value = 'media';
  targetType.value = 'all';
  dialogVisible.value = true;
};

const onPage = (p: number) => { page.value = p; load(); };

const submit = async () => {
  if (!form.value.title) { ElMessage.warning('请输入插播标题'); return; }
  if (contentType.value === 'media' && !form.value.mediaId) { ElMessage.warning('请选择要插播的媒体'); return; }
  if (contentType.value === 'content' && !form.value.contentId) { ElMessage.warning('请选择要插播的内容'); return; }

  const payload: any = { title: form.value.title, queueMode: 'interrupt', priority: 9 };
  if (contentType.value === 'media') payload.mediaId = form.value.mediaId;
  else payload.contentId = form.value.contentId;
  if (targetType.value === 'group') payload.targetGroup = form.value.targetGroup;
  else if (targetType.value === 'terminal') payload.targetTerminalCode = form.value.targetTerminalCode;
  if (startEnd.value) { payload.startTime = startEnd.value[0]; payload.endTime = startEnd.value[1]; }

  await createBroadcast(payload);
  ElMessage.success('插播已发布');
  dialogVisible.value = false;
  load();
  loadStatusCount();
};

const cancelBroadcast = async (id: number) => {
  await ElMessageBox.confirm('确定取消此插播？', '提示', { type: 'warning' });
  await deleteBroadcast(id);
  ElMessage.success('已取消');
  load();
  loadStatusCount();
};

onMounted(() => {
  load();
  loadStatusCount();
  loadMedia();
  loadContent();
  loadTerminals();
});
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .content-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; margin-left: 12px; }

.status-row { margin-bottom: 0; }
.status-card { border-radius: 12px; }
.status-card :deep(.el-card__body) { display: flex; align-items: center; gap: 16px; padding: 20px; }
.status-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #fff; }
.status-card.active .status-icon { background: linear-gradient(135deg, #f56c6c, #f78989); }
.status-card.pending .status-icon { background: linear-gradient(135deg, #e6a23c, #f0c78a); }
.status-card.completed .status-icon { background: linear-gradient(135deg, #909399, #b1b3b8); }
.status-value { font-size: 28px; font-weight: 700; }
.status-label { font-size: 14px; color: #909399; }

.content-cell { display: flex; align-items: center; gap: 8px; }
.target-cell { display: flex; align-items: center; gap: 4px; color: #606266; }
.time-cell { font-size: 13px; }
.time-sep { color: #c0c4cc; margin: 0 4px; }
.text-muted { color: #c0c4cc; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }

.broadcast-alert { margin-bottom: 16px; }
.broadcast-form { margin-top: 16px; }
.content-type-radio { width: 100%; }
.content-type-radio :deep(.el-radio-button) { width: 50%; }
.content-type-radio :deep(.el-radio-button__inner) { width: 100%; }
.media-option { display: flex; align-items: center; gap: 8px; }
</style>
