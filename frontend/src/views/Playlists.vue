<template>
  <div>
    <div class="header">
      <div>
        <h2>播放列表</h2>
        <p class="sub">选择资源并设置时长即可创建播放列表</p>
      </div>
      <el-button type="primary" @click="onCreate">创建</el-button>
    </div>

    <el-form :inline="true" class="form">
      <el-form-item label="名称">
        <el-input v-model="form.name" placeholder="轮播列表" style="width: 220px" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" placeholder="描述" style="width: 260px" />
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
      <el-table-column label="操作" width="160">
        <template #default="scope">
          <el-button size="small" @click="viewItems(scope.row.id)">查看条目</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { createPlaylist, fetchMedia, fetchPlaylistItems, fetchPlaylists } from '../api';

const mediaItems = ref<any[]>([]);
const playlists = ref<any[]>([]);
const selectedIds = ref<number[]>([]);
const durations = reactive<Record<number, number>>({});
const form = reactive({ name: '', description: '' });

const itemsDialogVisible = ref(false);
const currentItems = ref<any[]>([]);

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

const onSelectChange = (rows: any[]) => {
  selectedIds.value = rows.map((r) => r.id);
};

const onCreate = async () => {
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
  await createPlaylist({ ...form, items });
  ElMessage.success('创建成功');
  form.name = '';
  form.description = '';
  selectedIds.value = [];
  await loadPlaylists();
};

const viewItems = async (playlistId: number) => {
  const resp = await fetchPlaylistItems(playlistId);
  // @ts-ignore
  currentItems.value = resp.data?.data || [];
  itemsDialogVisible.value = true;
};

onMounted(() => {
  loadMedia();
  loadPlaylists();
});
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
