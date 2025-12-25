<template>
  <div>
    <div class="header">
      <div>
        <h2>播放列表</h2>
        <p class="sub">选择资源并设置时长即可创建/编辑播放列表</p>
      </div>
      <div class="actions">
        <el-button @click="resetForm">新建</el-button>
        <el-button type="primary" @click="onSave">{{ editingId ? '保存修改' : '创建' }}</el-button>
        <el-button type="success" @click="openPublish">发布到分组</el-button>
      </div>
    </div>

    <el-form :inline="true" class="form">
      <el-form-item label="名称">
        <el-input v-model="form.name" placeholder="轮播列表" style="width: 220px" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" placeholder="描述" style="width: 260px" />
      </el-form-item>
      <el-form-item label="布局">
        <el-select v-model="form.layoutId" placeholder="选择布局" clearable style="width: 200px">
          <el-option v-for="l in layouts" :key="l.id" :label="l.name" :value="l.id" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-table :data="mediaItems" style="width: 100%" @selection-change="onSelectChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="name" label="资源" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column label="播放时长(秒)" width="180">
        <template #default="scope">
          <el-input-number v-model="durations[scope.row.id]" :min="3" :max="600" />
        </template>
      </el-table-column>
    </el-table>

    <h3 style="margin-top:16px">已有播放列表</h3>
    <el-table :data="playlists" style="width: 100%">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button size="small" @click="edit(scope.row)">编辑</el-button>
          <el-button size="small" @click="viewItems(scope.row.id)">查看条目</el-button>
          <el-button size="small" type="danger" @click="remove(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="itemsDialogVisible" title="播放条目" width="520px">
      <el-table :data="currentItems">
        <el-table-column prop="mediaId" label="媒体ID" />
        <el-table-column prop="contentId" label="内容ID" />
        <el-table-column prop="displayDuration" label="时长" />
        <el-table-column prop="sortOrder" label="排序" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="publishDialog" title="发布到分组" width="520px">
      <el-form :model="publishForm" label-width="90px">
        <el-form-item label="分组名">
          <el-input v-model="publishForm.groupName" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="publishRange"
            type="datetimerange"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 320px"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialog = false">取消</el-button>
        <el-button type="primary" @click="publish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import {
  createPlaylist,
  deletePlaylist,
  fetchLayouts,
  fetchMedia,
  fetchPlaylistItems,
  fetchPlaylists,
  updatePlaylist
} from '../api';

const mediaItems = ref<any[]>([]);
const playlists = ref<any[]>([]);
const layouts = ref<any[]>([]);
const selectedIds = ref<number[]>([]);
const durations = reactive<Record<number, number>>({});
const form = reactive<{ name: string; description: string; layoutId?: number }>({ name: '', description: '', layoutId: undefined });
const editingId = ref<number | null>(null);

const itemsDialogVisible = ref(false);
const currentItems = ref<any[]>([]);
const publishDialog = ref(false);
const publishForm = reactive({ groupName: '', startTime: '', endTime: '' });
const publishRange = ref<[string, string] | null>(null);

const loadMedia = async () => {
  const resp = await fetchMedia(1, 100);
  // @ts-ignore
  mediaItems.value = resp.data?.data?.records || [];
  mediaItems.value.forEach((m: any) => {
    durations[m.id] = durations[m.id] || 10;
  });
};

const loadPlaylists = async () => {
  const resp = await fetchPlaylists();
  // @ts-ignore
  playlists.value = resp.data?.data || [];
};

const loadLayouts = async () => {
  const resp = await fetchLayouts();
  // @ts-ignore
  layouts.value = resp.data?.data || [];
};

const onSelectChange = (rows: any[]) => {
  selectedIds.value = rows.map((r) => r.id);
};

const onSave = async () => {
  if (!form.name) {
    ElMessage.warning('请输入列表名称');
    return;
  }
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择至少一个资源');
    return;
  }
  const items = selectedIds.value.map((id, idx) => ({
    mediaId: id,
    displayDuration: durations[id] || 10,
    sortOrder: idx
  }));
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
  // @ts-ignore
  currentItems.value = resp.data?.data || [];
  itemsDialogVisible.value = true;
};

const edit = async (row: any) => {
  editingId.value = row.id;
  form.name = row.name;
  form.description = row.description;
  form.layoutId = row.layoutId;
  const resp = await fetchPlaylistItems(row.id);
  // @ts-ignore
  const items = resp.data?.data || [];
  selectedIds.value = items.map((i: any) => i.mediaId).filter((id: number) => !!id);
  items.forEach((i: any) => {
    if (i.mediaId) durations[i.mediaId] = i.displayDuration || 10;
  });
};

const remove = async (id: number) => {
  await deletePlaylist(id);
  ElMessage.success('已删除');
  if (editingId.value === id) resetForm();
  loadPlaylists();
};

const resetForm = () => {
  editingId.value = null;
  form.name = '';
  form.description = '';
  form.layoutId = undefined;
  selectedIds.value = [];
};

onMounted(() => {
  loadMedia();
  loadPlaylists();
  loadLayouts();
});

// 发布到分组
const publishDialog = ref(false);
const publishForm = reactive({ groupName: '', startTime: '', endTime: '' });
const openPublish = () => {
  if (!editingId.value) {
    ElMessage.warning('请选择一个播放列表并点击编辑后再发布');
    return;
  }
  publishDialog.value = true;
};
const publish = async () => {
  if (!publishForm.groupName) {
    ElMessage.warning('请输入分组名');
    return;
  }
  if (publishRange.value) {
    publishForm.startTime = publishRange.value[0];
    publishForm.endTime = publishRange.value[1];
  } else {
    publishForm.startTime = '';
    publishForm.endTime = '';
  }
  await fetch('/api/terminals/bind-playlist/group', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token') || ''}` },
    body: JSON.stringify({
      playlistId: editingId.value,
      groupName: publishForm.groupName,
      startTime: publishForm.startTime || null,
      endTime: publishForm.endTime || null
    })
  });
  ElMessage.success('已发布到分组');
  publishDialog.value = false;
};
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.sub {
  color: #909399;
  margin: 4px 0 0;
}
</style>
