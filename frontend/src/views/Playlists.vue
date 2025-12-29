<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>播放列表</h3>
          <span class="subtitle">创建和管理媒体播放列表</span>
        </div>
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>
          新建列表
        </el-button>
      </div>
    </el-card>

    <!-- 播放列表卡片展示 -->
    <div class="playlist-grid" v-if="playlists.length > 0">
      <el-card v-for="p in playlists" :key="p.id" class="playlist-card" shadow="hover" @click="preview(p.id)">
        <div class="playlist-cover">
          <el-image v-if="p.coverUrl" :src="p.coverUrl" fit="cover" class="cover-img" />
          <div v-else class="cover-placeholder">
            <el-icon><VideoPlay /></el-icon>
          </div>
          <div class="playlist-badge">{{ p.itemCount || 0 }} 项</div>
        </div>
        <div class="playlist-info">
          <div class="playlist-name">{{ p.name }}</div>
          <div class="playlist-desc">{{ p.description || '暂无描述' }}</div>
          <div class="playlist-meta">
            <el-tag v-if="p.layoutId" size="small">{{ getLayoutName(p.layoutId) }}</el-tag>
            <span class="playlist-date">{{ formatDate(p.createdAt) }}</span>
          </div>
        </div>
        <div class="playlist-actions" @click.stop>
          <el-button size="small" @click="edit(p)" title="编辑">
            <el-icon><Edit /></el-icon>
          </el-button>
          <el-button size="small" type="danger" @click="remove(p.id)" title="删除">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </el-card>
    </div>
    <el-empty v-else description="暂无播放列表，点击上方按钮创建" />

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑播放列表' : '新建播放列表'" width="800px" destroy-on-close>
      <el-form :model="form" label-width="80px" class="dialog-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="名称" required>
              <el-input v-model="form.name" placeholder="播放列表名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="布局">
              <el-select v-model="form.layoutId" placeholder="选择布局（可选）" clearable style="width: 100%" @change="onLayoutChange">
                <el-option v-for="l in layouts" :key="l.id" :label="l.name" :value="l.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="描述信息（可选）" />
        </el-form-item>
      </el-form>

      <!-- 资源选择区域 -->
      <div class="resource-section">
        <div class="section-header">
          <span class="section-title">选择资源</span>
          <el-radio-group v-model="resourceType" size="small">
            <el-radio-button label="media"><el-icon><Picture /></el-icon> 媒体</el-radio-button>
            <el-radio-button label="content"><el-icon><Document /></el-icon> 内容</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 媒体资源列表 -->
        <el-table v-show="resourceType === 'media'" :data="mediaItems" stripe @selection-change="onMediaSelectChange" max-height="200" ref="mediaTableRef" size="small">
          <el-table-column type="selection" width="45" />
          <el-table-column label="预览" width="70">
            <template #default="scope">
              <el-image v-if="scope.row.thumbUrl || scope.row.type === 'image'" :src="scope.row.thumbUrl || scope.row.url" fit="cover" class="thumb-img" />
              <div v-else class="thumb-placeholder"><el-icon><VideoPlay /></el-icon></div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="名称" min-width="150" show-overflow-tooltip />
          <el-table-column prop="type" label="类型" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.type === 'video' ? 'warning' : 'success'" size="small">{{ scope.row.type === 'video' ? '视频' : '图片' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="时长(秒)" width="110">
            <template #default="scope">
              <el-input-number v-model="mediaDurations[scope.row.id]" :min="3" :max="600" size="small" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="分区" width="110" v-if="layoutAreas.length">
            <template #default="scope">
              <el-select v-model="mediaAreas[scope.row.id]" size="small" placeholder="自动" style="width: 90px">
                <el-option label="自动" :value="undefined" />
                <el-option v-for="(area, idx) in layoutAreas" :key="idx" :label="'区域 ' + (idx + 1)" :value="idx + 1" />
              </el-select>
            </template>
          </el-table-column>
        </el-table>

        <!-- 内容资源列表 -->
        <el-table v-show="resourceType === 'content'" :data="contentItems" stripe @selection-change="onContentSelectChange" max-height="200" ref="contentTableRef" size="small">
          <el-table-column type="selection" width="45" />
          <el-table-column label="封面" width="70">
            <template #default="scope">
              <el-image v-if="scope.row.coverUrl" :src="scope.row.coverUrl" fit="cover" class="thumb-img" />
              <div v-else class="thumb-placeholder"><el-icon><Document /></el-icon></div>
            </template>
          </el-table-column>
          <el-table-column label="标题" min-width="150" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.title }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.published ? 'success' : 'info'" size="small">{{ scope.row.published ? '已发布' : '草稿' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="时长(秒)" width="110">
            <template #default="scope">
              <el-input-number v-model="contentDurations[scope.row.id]" :min="5" :max="600" size="small" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="分区" width="110" v-if="layoutAreas.length">
            <template #default="scope">
              <el-select v-model="contentAreas[scope.row.id]" size="small" placeholder="自动" style="width: 90px">
                <el-option label="自动" :value="undefined" />
                <el-option v-for="(area, idx) in layoutAreas" :key="idx" :label="'区域 ' + (idx + 1)" :value="idx + 1" />
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 已选资源 -->
      <div class="selected-section">
        <div class="section-header">
          <span class="section-title">已选资源</span>
          <el-tag type="info" size="small">{{ selectedItems.length }} 项，可拖拽排序</el-tag>
        </div>
        <div class="selected-list" v-if="selectedItems.length">
          <draggable v-model="selectedItems" item-key="key" class="drag-list" handle=".drag-handle">
            <template #item="{ element, index }">
              <div class="selected-item">
                <el-icon class="drag-handle"><Rank /></el-icon>
                <span class="item-order">{{ index + 1 }}</span>
                <el-image v-if="element.thumb" :src="element.thumb" fit="cover" class="item-thumb" />
                <div v-else class="item-thumb-placeholder"><el-icon><Document /></el-icon></div>
                <div class="item-info">
                  <span class="item-name">{{ element.name }}</span>
                  <el-tag :type="element.type === 'content' ? 'primary' : 'success'" size="small">{{ element.type === 'content' ? '内容' : '媒体' }}</el-tag>
                </div>
                <span class="item-duration">{{ element.duration }}秒</span>
                <el-tag v-if="element.areaIndex" type="info" size="small">区域 {{ element.areaIndex }}</el-tag>
                <el-button type="danger" link size="small" @click="removeSelected(index)"><el-icon><Close /></el-icon></el-button>
              </div>
            </template>
          </draggable>
        </div>
        <div v-else class="empty-selected">请从上方表格勾选资源</div>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSave">{{ editingId ? '保存修改' : '创建列表' }}</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewDialog" title="播放列表预览" width="650px">
      <div v-if="previewData" class="preview-content">
        <div class="preview-header">
          <el-image v-if="previewData.playlist?.coverUrl" :src="previewData.playlist.coverUrl" fit="cover" class="preview-cover" />
          <div v-else class="preview-cover-placeholder"><el-icon><VideoPlay /></el-icon></div>
          <div class="preview-info">
            <h3>{{ previewData.playlist?.name }}</h3>
            <p>{{ previewData.playlist?.description || '暂无描述' }}</p>
            <div class="preview-tags">
              <el-tag v-if="previewData.layout" size="small">布局: {{ previewData.layout.name }}</el-tag>
              <el-tag type="info" size="small">{{ previewData.items?.length || 0 }} 个资源</el-tag>
            </div>
          </div>
        </div>
        <el-divider />
        <el-table :data="previewData.items" size="small" max-height="300">
          <el-table-column prop="sortOrder" label="#" width="50" />
          <el-table-column label="资源" min-width="200">
            <template #default="scope">
              <div class="media-row">
                <template v-if="scope.row.mediaId">
                  <el-image v-if="getMediaThumb(scope.row.mediaId)" :src="getMediaThumb(scope.row.mediaId)" fit="cover" class="preview-thumb" />
                  <span>{{ getMediaName(scope.row.mediaId) }}</span>
                  <el-tag size="small" type="success">媒体</el-tag>
                </template>
                <template v-else-if="scope.row.contentId">
                  <el-image v-if="getContentCover(scope.row.contentId)" :src="getContentCover(scope.row.contentId)" fit="cover" class="preview-thumb" />
                  <span>{{ getContentTitle(scope.row.contentId) }}</span>
                  <el-tag size="small" type="primary">内容</el-tag>
                </template>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="displayDuration" label="时长(秒)" width="90" />
          <el-table-column prop="areaIndex" label="分区" width="70">
            <template #default="scope">
              <span v-if="scope.row.areaIndex">区域 {{ scope.row.areaIndex }}</span>
              <span v-else>自动</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>


<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createPlaylist, deletePlaylist, fetchContent, fetchLayouts, fetchMedia, fetchPlaylistItems, fetchPlaylistPreview, fetchPlaylists, updatePlaylist, fetchLayout } from '../api';
import { Plus, Picture, Document, VideoPlay, Delete, Edit, Rank, Close } from '@element-plus/icons-vue';
import draggable from 'vuedraggable';

interface SelectedItem {
  key: string;
  type: 'media' | 'content';
  id: number;
  name: string;
  thumb: string;
  duration: number;
  areaIndex?: number;
}

const dialogVisible = ref(false);
const resourceType = ref<'media' | 'content'>('media');
const mediaItems = ref<any[]>([]);
const contentItems = ref<any[]>([]);
const playlists = ref<any[]>([]);
const layouts = ref<any[]>([]);
const selectedItems = ref<SelectedItem[]>([]);
const mediaDurations = reactive<Record<number, number>>({});
const contentDurations = reactive<Record<number, number>>({});
const form = reactive<{ name: string; description: string; coverUrl?: string; layoutId?: number }>({ name: '', description: '', coverUrl: '', layoutId: undefined });
const editingId = ref<number | null>(null);
const previewDialog = ref(false);
const previewData = ref<any | null>(null);
const mediaTableRef = ref<any>(null);
const contentTableRef = ref<any>(null);
const layoutAreas = ref<any[]>([]);
const mediaAreas = reactive<Record<number, number | undefined>>({});
const contentAreas = reactive<Record<number, number | undefined>>({});

const loadMedia = async () => {
  const resp = await fetchMedia(1, 200);
  mediaItems.value = resp.data?.data?.records || [];
  mediaItems.value.forEach((m: any) => { mediaDurations[m.id] = mediaDurations[m.id] || 10; });
};

const loadContent = async () => {
  const resp = await fetchContent(1, 200, undefined, true);
  contentItems.value = resp.data?.data?.records || [];
  contentItems.value.forEach((c: any) => { contentDurations[c.id] = contentDurations[c.id] || 15; });
};

const loadPlaylists = async () => {
  const resp = await fetchPlaylists();
  playlists.value = resp.data?.data || [];
};

const loadLayouts = async () => {
  const resp = await fetchLayouts();
  layouts.value = resp.data?.data || [];
};

const loadLayoutDetail = async (id?: number) => {
  if (!id) {
    layoutAreas.value = [];
    return;
  }
  try {
    const resp = await fetchLayout(id);
    const json = resp.data?.data?.layoutJson;
    if (json) {
      const obj = JSON.parse(json);
      layoutAreas.value = obj.areas || [];
    } else {
      layoutAreas.value = [];
    }
  } catch {
    layoutAreas.value = [];
  }
};

const getLayoutName = (id: number) => layouts.value.find(l => l.id === id)?.name || `ID:${id}`;

const openCreate = () => {
  resetForm();
  dialogVisible.value = true;
};

const onLayoutChange = async (val: number) => {
  await loadLayoutDetail(val);
  const count = layoutAreas.value.length;
  if (count === 0) return;
  Object.keys(mediaAreas).forEach((k) => {
    const v = mediaAreas[Number(k)];
    if (v && v > count) mediaAreas[Number(k)] = undefined;
  });
  Object.keys(contentAreas).forEach((k) => {
    const v = contentAreas[Number(k)];
    if (v && v > count) contentAreas[Number(k)] = undefined;
  });
};

const onMediaSelectChange = (rows: any[]) => {
  selectedItems.value = selectedItems.value.filter(item => item.type !== 'media');
  rows.forEach(row => {
    selectedItems.value.push({
      key: `media-${row.id}`,
      type: 'media',
      id: row.id,
      name: row.name,
      thumb: row.thumbUrl || row.url || '',
      duration: mediaDurations[row.id] || 10,
      areaIndex: mediaAreas[row.id]
    });
  });
};

const onContentSelectChange = (rows: any[]) => {
  selectedItems.value = selectedItems.value.filter(item => item.type !== 'content');
  rows.forEach(row => {
    selectedItems.value.push({
      key: `content-${row.id}`,
      type: 'content',
      id: row.id,
      name: row.title,
      thumb: row.coverUrl || '',
      duration: contentDurations[row.id] || 15,
      areaIndex: contentAreas[row.id]
    });
  });
};

const removeSelected = (index: number) => {
  const item = selectedItems.value[index];
  selectedItems.value.splice(index, 1);
  if (item.type === 'media' && mediaTableRef.value) {
    const row = mediaItems.value.find(m => m.id === item.id);
    if (row) mediaTableRef.value.toggleRowSelection(row, false);
  } else if (item.type === 'content' && contentTableRef.value) {
    const row = contentItems.value.find(c => c.id === item.id);
    if (row) contentTableRef.value.toggleRowSelection(row, false);
  }
};

watch(mediaDurations, () => {
  selectedItems.value.forEach(item => {
    if (item.type === 'media') item.duration = mediaDurations[item.id] || 10;
  });
}, { deep: true });

watch(contentDurations, () => {
  selectedItems.value.forEach(item => {
    if (item.type === 'content') item.duration = contentDurations[item.id] || 15;
  });
}, { deep: true });

watch(mediaAreas, () => {
  selectedItems.value.forEach(item => {
    if (item.type === 'media') item.areaIndex = mediaAreas[item.id];
  });
}, { deep: true });

watch(contentAreas, () => {
  selectedItems.value.forEach(item => {
    if (item.type === 'content') item.areaIndex = contentAreas[item.id];
  });
}, { deep: true });

const onSave = async () => {
  if (!form.name) { ElMessage.warning('请输入列表名称'); return; }
  if (selectedItems.value.length === 0) { ElMessage.warning('请选择至少一个资源'); return; }
  const areaCount = layoutAreas.value.length;
  
  const items = selectedItems.value.map((item, idx) => ({
    mediaId: item.type === 'media' ? item.id : null,
    contentId: item.type === 'content' ? item.id : null,
    displayDuration: item.duration,
    sortOrder: idx,
    areaIndex: areaCount > 0 && item.areaIndex && item.areaIndex <= areaCount ? item.areaIndex : null
  }));
  
  if (!form.coverUrl && selectedItems.value.length > 0) {
    form.coverUrl = selectedItems.value[0].thumb;
  }
  
  if (editingId.value) {
    await updatePlaylist(editingId.value, { ...form, items });
    ElMessage.success('已保存');
  } else {
    await createPlaylist({ ...form, items });
    ElMessage.success('创建成功');
  }
  dialogVisible.value = false;
  resetForm();
  await loadPlaylists();
};

const edit = async (row: any) => {
  editingId.value = row.id;
  form.name = row.name;
  form.description = row.description;
  form.coverUrl = row.coverUrl || '';
  form.layoutId = row.layoutId;
  await loadLayoutDetail(row.layoutId);
  dialogVisible.value = true;
  
  const resp = await fetchPlaylistItems(row.id);
  const items = resp.data?.data || [];
  
  selectedItems.value = [];
  
  setTimeout(() => {
    if (mediaTableRef.value) mediaTableRef.value.clearSelection();
    if (contentTableRef.value) contentTableRef.value.clearSelection();
    
    items.forEach((item: any) => {
      if (item.mediaId) {
        const media = mediaItems.value.find(m => m.id === item.mediaId);
        if (media) {
          mediaDurations[item.mediaId] = item.displayDuration || 10;
          mediaAreas[item.mediaId] = item.areaIndex;
          selectedItems.value.push({
            key: `media-${item.mediaId}`,
            type: 'media',
            id: item.mediaId,
            name: media.name,
            thumb: media.thumbUrl || media.url || '',
            duration: item.displayDuration || 10,
            areaIndex: item.areaIndex
          });
          mediaTableRef.value?.toggleRowSelection(media, true);
        }
      } else if (item.contentId) {
        const content = contentItems.value.find(c => c.id === item.contentId);
        if (content) {
          contentDurations[item.contentId] = item.displayDuration || 15;
          contentAreas[item.contentId] = item.areaIndex;
          selectedItems.value.push({
            key: `content-${item.contentId}`,
            type: 'content',
            id: item.contentId,
            name: content.title,
            thumb: content.coverUrl || '',
            duration: item.displayDuration || 15,
            areaIndex: item.areaIndex
          });
          contentTableRef.value?.toggleRowSelection(content, true);
        }
      }
    });
  }, 200);
};

const remove = async (id: number) => {
  await ElMessageBox.confirm('确定删除此播放列表？', '提示', { type: 'warning' });
  await deletePlaylist(id);
  ElMessage.success('已删除');
  loadPlaylists();
};

const resetForm = () => {
  editingId.value = null;
  form.name = '';
  form.description = '';
  form.coverUrl = '';
  form.layoutId = undefined;
  selectedItems.value = [];
  resourceType.value = 'media';
  layoutAreas.value = [];
  Object.keys(mediaAreas).forEach(k => delete mediaAreas[Number(k)]);
  Object.keys(contentAreas).forEach(k => delete contentAreas[Number(k)]);
};

const preview = async (id: number) => {
  const resp = await fetchPlaylistPreview(id);
  previewData.value = resp.data?.data || null;
  previewDialog.value = true;
};

const getMediaThumb = (id: number) => previewData.value?.mediaAssets?.find((a: any) => a.id === id)?.thumbUrl || previewData.value?.mediaAssets?.find((a: any) => a.id === id)?.url;
const getMediaName = (id: number) => previewData.value?.mediaAssets?.find((a: any) => a.id === id)?.name || `媒体#${id}`;
const getContentCover = (id: number) => previewData.value?.contentAssets?.find((a: any) => a.id === id)?.coverUrl;
const getContentTitle = (id: number) => previewData.value?.contentAssets?.find((a: any) => a.id === id)?.title || `内容#${id}`;

const formatDate = (date: string) => date ? date.replace('T', ' ').substring(0, 10) : '-';

onMounted(() => { loadMedia(); loadContent(); loadPlaylists(); loadLayouts(); });
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; }

/* 播放列表卡片网格 */
.playlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.playlist-card {
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  overflow: hidden;
}
.playlist-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}
.playlist-card :deep(.el-card__body) { padding: 0; }
.playlist-cover {
  position: relative;
  height: 140px;
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.cover-img { width: 100%; height: 100%; }
.cover-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  font-size: 48px; color: rgba(255,255,255,0.6);
}
.playlist-badge {
  position: absolute; bottom: 8px; right: 8px;
  background: rgba(0,0,0,0.6); color: #fff;
  padding: 2px 8px; border-radius: 10px; font-size: 12px;
}
.playlist-info { padding: 12px 16px; }
.playlist-name { font-weight: 600; font-size: 15px; color: #303133; margin-bottom: 4px; }
.playlist-desc { font-size: 13px; color: #909399; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.playlist-meta { display: flex; align-items: center; gap: 8px; }
.playlist-date { font-size: 12px; color: #c0c4cc; margin-left: auto; }
.playlist-actions {
  padding: 8px 16px 12px;
  display: flex; gap: 8px; justify-content: flex-end;
  border-top: 1px solid #f0f0f0;
}

/* 对话框样式 */
.dialog-form { margin-bottom: 16px; }
.resource-section { margin-bottom: 16px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.section-title { font-weight: 600; font-size: 14px; color: #303133; }
.thumb-img { width: 45px; height: 32px; border-radius: 4px; }
.thumb-placeholder { width: 45px; height: 32px; border-radius: 4px; background: #f5f7fa; display: flex; align-items: center; justify-content: center; color: #c0c4cc; }

.selected-section { background: #fafafa; border-radius: 8px; padding: 12px; }
.selected-list { max-height: 180px; overflow-y: auto; }
.drag-list { display: flex; flex-direction: column; gap: 6px; }
.selected-item {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 10px; background: #fff; border-radius: 6px;
  border: 1px solid #ebeef5;
}
.selected-item:hover { border-color: #409eff; }
.drag-handle { cursor: move; color: #c0c4cc; font-size: 16px; }
.drag-handle:hover { color: #409eff; }
.item-order {
  width: 20px; height: 20px; background: #409eff; color: #fff;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 600;
}
.item-thumb { width: 40px; height: 28px; border-radius: 4px; }
.item-thumb-placeholder { width: 40px; height: 28px; border-radius: 4px; background: #e4e7ed; display: flex; align-items: center; justify-content: center; color: #909399; font-size: 14px; }
.item-info { flex: 1; display: flex; align-items: center; gap: 6px; min-width: 0; }
.item-name { font-size: 13px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-duration { color: #909399; font-size: 12px; white-space: nowrap; }
.empty-selected { text-align: center; color: #c0c4cc; padding: 20px; font-size: 13px; }

/* 预览对话框 */
.preview-content { padding: 0 8px; }
.preview-header { display: flex; gap: 16px; align-items: flex-start; }
.preview-cover { width: 120px; height: 80px; border-radius: 8px; flex-shrink: 0; }
.preview-cover-placeholder {
  width: 120px; height: 80px; border-radius: 8px; flex-shrink: 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex; align-items: center; justify-content: center;
  font-size: 32px; color: rgba(255,255,255,0.6);
}
.preview-info { flex: 1; }
.preview-info h3 { margin: 0 0 8px; font-size: 16px; }
.preview-info p { color: #909399; margin: 0 0 8px; font-size: 13px; }
.preview-tags { display: flex; gap: 8px; }
.media-row { display: flex; align-items: center; gap: 8px; }
.preview-thumb { width: 45px; height: 32px; border-radius: 4px; }
</style>
