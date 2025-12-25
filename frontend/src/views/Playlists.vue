<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>播放列表</h3>
          <span class="subtitle">创建和管理媒体播放列表</span>
        </div>
        <div class="header-actions">
          <el-button @click="resetForm">新建列表</el-button>
          <el-button type="primary" @click="onSave">{{ editingId ? '保存修改' : '创建列表' }}</el-button>
          <el-button type="success" @click="openPublish">发布到分组</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="content-card" shadow="never">
      <el-form :inline="true" class="playlist-form">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="播放列表名称" style="width: 200px" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="描述信息" style="width: 240px" />
        </el-form-item>
        <el-form-item label="布局">
          <el-select v-model="form.layoutId" placeholder="选择布局" clearable style="width: 160px">
            <el-option v-for="l in layouts" :key="l.id" :label="l.name" :value="l.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-divider content-position="left">选择媒体资源</el-divider>

      <el-table :data="mediaItems" stripe @selection-change="onSelectChange" max-height="300">
        <el-table-column type="selection" width="50" />
        <el-table-column label="缩略图" width="80">
          <template #default="scope">
            <el-image v-if="scope.row.thumbUrl || scope.row.type === 'image'" :src="scope.row.thumbUrl || scope.row.url" fit="cover" style="width: 50px; height: 35px; border-radius: 4px" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="资源名称" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="播放时长(秒)" width="160">
          <template #default="scope">
            <el-input-number v-model="durations[scope.row.id]" :min="3" :max="600" size="small" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="content-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>已有播放列表</span>
          <el-tag>{{ playlists.length }} 个</el-tag>
        </div>
      </template>
      <el-table :data="playlists" stripe>
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="布局" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.layoutId" size="small">ID: {{ scope.row.layoutId }}</el-tag>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="edit(scope.row)">编辑</el-button>
            <el-button size="small" type="primary" @click="preview(scope.row.id)">预览</el-button>
            <el-button size="small" @click="viewItems(scope.row.id)">条目</el-button>
            <el-button size="small" type="danger" @click="remove(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="itemsDialogVisible" title="播放条目" width="520px">
      <el-table :data="currentItems" size="small">
        <el-table-column prop="sortOrder" label="#" width="60" />
        <el-table-column prop="mediaId" label="媒体ID" />
        <el-table-column prop="displayDuration" label="时长(秒)" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="publishDialog" title="发布到分组" width="480px">
      <el-form :model="publishForm" label-width="80px">
        <el-form-item label="分组名">
          <el-input v-model="publishForm.groupName" placeholder="输入终端分组名称" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="publishRange" type="datetimerange" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialog = false">取消</el-button>
        <el-button type="primary" @click="publish">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewDialog" title="播放列表预览" width="700px">
      <div v-if="previewData" class="preview-content">
        <div class="preview-header">
          <el-image v-if="previewData.playlist?.coverUrl" :src="previewData.playlist.coverUrl" fit="cover" class="preview-cover" />
          <div class="preview-info">
            <h3>{{ previewData.playlist?.name }}</h3>
            <p>{{ previewData.playlist?.description || '暂无描述' }}</p>
            <el-tag v-if="previewData.layout">布局: {{ previewData.layout.name }}</el-tag>
          </div>
        </div>
        <el-divider />
        <el-table :data="previewData.items" size="small">
          <el-table-column prop="sortOrder" label="#" width="60" />
          <el-table-column label="媒体">
            <template #default="scope">
              <div class="media-row">
                <el-image v-if="mediaThumb(scope.row.mediaId)" :src="mediaThumb(scope.row.mediaId)" fit="cover" style="width: 50px; height: 35px; border-radius: 4px" />
                <span>{{ mediaName(scope.row.mediaId) || '内容ID:' + scope.row.contentId }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="displayDuration" label="时长(秒)" width="100" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createPlaylist, deletePlaylist, fetchLayouts, fetchMedia, fetchPlaylistItems, fetchPlaylistPreview, fetchPlaylists, updatePlaylist } from '../api';

const mediaItems = ref<any[]>([]);
const playlists = ref<any[]>([]);
const layouts = ref<any[]>([]);
const selectedIds = ref<number[]>([]);
const durations = reactive<Record<number, number>>({});
const form = reactive<{ name: string; description: string; coverUrl?: string; layoutId?: number }>({ name: '', description: '', coverUrl: '', layoutId: undefined });
const editingId = ref<number | null>(null);
const itemsDialogVisible = ref(false);
const currentItems = ref<any[]>([]);
const publishDialog = ref(false);
const publishForm = reactive({ groupName: '', startTime: '', endTime: '' });
const publishRange = ref<[string, string] | null>(null);
const previewDialog = ref(false);
const previewData = ref<any | null>(null);
const previewAssets = ref<any[]>([]);

const loadMedia = async () => {
  const resp = await fetchMedia(1, 100);
  mediaItems.value = resp.data?.data?.records || [];
  mediaItems.value.forEach((m: any) => { durations[m.id] = durations[m.id] || 10; });
};
const loadPlaylists = async () => {
  const resp = await fetchPlaylists();
  playlists.value = resp.data?.data || [];
};
const loadLayouts = async () => {
  const resp = await fetchLayouts();
  layouts.value = resp.data?.data || [];
};
const onSelectChange = (rows: any[]) => {
  selectedIds.value = rows.map((r) => r.id);
  if (!form.coverUrl && rows.length > 0) form.coverUrl = rows[0].thumbUrl || '';
};
const onSave = async () => {
  if (!form.name) { ElMessage.warning('请输入列表名称'); return; }
  if (selectedIds.value.length === 0) { ElMessage.warning('请选择至少一个资源'); return; }
  const items = selectedIds.value.map((id, idx) => ({ mediaId: id, displayDuration: durations[id] || 10, sortOrder: idx }));
  if (editingId.value) {
    await updatePlaylist(editingId.value, { ...form, items });
    ElMessage.success('已保存');
  } else {
    await createPlaylist({ ...form, items });
    ElMessage.success('创建成功');
  }
  resetForm();
  await loadPlaylists();
};
const viewItems = async (playlistId: number) => {
  const resp = await fetchPlaylistItems(playlistId);
  currentItems.value = resp.data?.data || [];
  itemsDialogVisible.value = true;
};
const edit = async (row: any) => {
  editingId.value = row.id;
  form.name = row.name;
  form.description = row.description;
  form.coverUrl = row.coverUrl || '';
  form.layoutId = row.layoutId;
  const resp = await fetchPlaylistItems(row.id);
  const items = resp.data?.data || [];
  selectedIds.value = items.map((i: any) => i.mediaId).filter((id: number) => !!id);
  items.forEach((i: any) => { if (i.mediaId) durations[i.mediaId] = i.displayDuration || 10; });
};
const remove = async (id: number) => {
  await ElMessageBox.confirm('确定删除此播放列表？', '提示', { type: 'warning' });
  await deletePlaylist(id);
  ElMessage.success('已删除');
  if (editingId.value === id) resetForm();
  loadPlaylists();
};
const resetForm = () => {
  editingId.value = null;
  form.name = '';
  form.description = '';
  form.coverUrl = '';
  form.layoutId = undefined;
  selectedIds.value = [];
};
const openPublish = () => {
  if (!editingId.value) { ElMessage.warning('请先选择一个播放列表并编辑'); return; }
  publishDialog.value = true;
};
const publish = async () => {
  if (!publishForm.groupName) { ElMessage.warning('请输入分组名'); return; }
  if (publishRange.value) { publishForm.startTime = publishRange.value[0]; publishForm.endTime = publishRange.value[1]; }
  await fetch('/api/terminals/bind-playlist/group', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token') || ''}` },
    body: JSON.stringify({ playlistId: editingId.value, groupName: publishForm.groupName, startTime: publishForm.startTime || null, endTime: publishForm.endTime || null })
  });
  ElMessage.success('已发布到分组');
  publishDialog.value = false;
};
const preview = async (id: number) => {
  const resp = await fetchPlaylistPreview(id);
  previewData.value = resp.data?.data || null;
  previewAssets.value = previewData.value?.mediaAssets || [];
  previewDialog.value = true;
};
const mediaThumb = (id?: number) => previewAssets.value.find((a: any) => a.id === id)?.thumbUrl;
const mediaName = (id?: number) => previewAssets.value.find((a: any) => a.id === id)?.name;
const formatDate = (date: string) => date ? date.replace('T', ' ').substring(0, 19) : '-';

onMounted(() => { loadMedia(); loadPlaylists(); loadLayouts(); });
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; }
.header-actions { display: flex; gap: 8px; }
.content-card { border-radius: 12px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; }
.playlist-form { margin-bottom: 16px; }
.no-data { color: #c0c4cc; }
.preview-content { padding: 0 16px; }
.preview-header { display: flex; gap: 16px; align-items: center; }
.preview-cover { width: 120px; height: 80px; border-radius: 8px; }
.preview-info h3 { margin: 0 0 8px; }
.preview-info p { color: #909399; margin: 0 0 8px; }
.media-row { display: flex; align-items: center; gap: 8px; }
</style>
