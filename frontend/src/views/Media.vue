<!--
 * @Author: 孔令超
 * @Module: 视频展示管理 - 媒体资源库
 * @Description: 媒体资源管理页面，管理图片、视频等媒体文件
-->
<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>媒体资源</h3>
          <span class="subtitle">管理图片、视频等媒体文件</span>
        </div>
        <el-upload :headers="uploadHeaders" action="/api/media/upload" :show-file-list="false" :on-success="onUploaded" :before-upload="beforeUpload" accept="image/*,video/*">
          <el-button type="primary"><el-icon><Upload /></el-icon>上传资源</el-button>
        </el-upload>
      </div>
    </el-card>

    <el-card class="content-card" shadow="never">
      <el-table :data="list" stripe>
        <el-table-column label="预览" width="100">
          <template #default="scope">
            <div class="preview-cell" @click="onPreview(scope.row)">
              <el-image v-if="scope.row.type === 'image'" :src="scope.row.url" fit="cover" class="preview-img" />
              <el-image v-else-if="scope.row.thumbUrl" :src="scope.row.thumbUrl" fit="cover" class="preview-img" />
              <div v-else-if="scope.row.type === 'video'" class="preview-placeholder video"><el-icon><VideoPlay /></el-icon></div>
              <div v-else class="preview-placeholder file"><el-icon><Document /></el-icon></div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="文件名" min-width="200">
          <template #default="scope">
            <div class="file-info">
              <span class="file-name">{{ scope.row.name }}</span>
              <span class="file-meta">{{ formatSize(scope.row.sizeBytes) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="90">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)" size="small">{{ getTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="160">
          <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" @click="onPreview(scope.row)" title="预览"><el-icon><View /></el-icon></el-button>
              <el-button size="small" @click="onDownload(scope.row.id, scope.row.name)" title="下载"><el-icon><Download /></el-icon></el-button>
              <el-button v-if="scope.row.type === 'video'" size="small" type="primary" @click="openThumbDialog(scope.row)" title="封面"><el-icon><PictureFilled /></el-icon></el-button>
              <el-button size="small" type="danger" @click="onDelete(scope.row.id)" title="删除"><el-icon><Delete /></el-icon></el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @current-change="load" @size-change="load" />
      </div>
    </el-card>

    <el-dialog v-model="previewVisible" title="媒体预览" width="800px" destroy-on-close>
      <div class="preview-dialog-content">
        <img v-if="previewItem?.type === 'image'" :src="previewItem?.url" class="preview-full-img" />
        <video v-else-if="previewItem?.type === 'video'" :src="previewItem?.url" controls class="preview-video" />
        <div v-else class="preview-file-info"><el-icon size="64"><Document /></el-icon><p>{{ previewItem?.name }}</p></div>
      </div>
    </el-dialog>

    <el-dialog v-model="thumbDialogVisible" title="设置视频封面" width="450px">
      <div class="thumb-content">
        <p class="thumb-label">当前封面</p>
        <div class="thumb-preview">
          <el-image v-if="currentMedia?.thumbUrl" :src="currentMedia.thumbUrl" fit="cover" class="thumb-img" />
          <div v-else class="thumb-empty"><el-icon><VideoPlay /></el-icon><span>暂无</span></div>
        </div>
        <el-divider />
        <p class="thumb-label">上传新封面</p>
        <el-upload :headers="uploadHeaders" :action="`/api/media/${currentMedia?.id}/thumb`" :show-file-list="false" :on-success="onThumbUploaded" :before-upload="beforeThumbUpload" accept="image/*" drag>
          <el-icon class="el-icon--upload"><Upload /></el-icon>
          <div class="el-upload__text">拖拽或<em>点击上传</em></div>
        </el-upload>
      </div>
      <template #footer><el-button @click="thumbDialogVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { deleteMedia, downloadMedia, fetchMedia } from '../api';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Upload, VideoPlay, Document, View, Download, Delete, PictureFilled } from '@element-plus/icons-vue';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${sessionStorage.getItem('org_token') || ''}` }));
const previewVisible = ref(false);
const previewItem = ref<any>(null);
const thumbDialogVisible = ref(false);
const currentMedia = ref<any>(null);

const load = async () => {
  const resp = await fetchMedia(page.value, size.value);
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};

const beforeUpload = (file: File) => {
  // 验证文件类型：只允许图片和视频
  const isImage = file.type.startsWith('image/');
  const isVideo = file.type.startsWith('video/');
  if (!isImage && !isVideo) {
    ElMessage.error('只能上传图片或视频文件');
    return false;
  }
  if (file.size / 1024 / 1024 > 500) { ElMessage.error('文件不能超过500MB'); return false; }
  return true;
};
const beforeThumbUpload = (file: File) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片'); return false; }
  if (file.size / 1024 / 1024 > 5) { ElMessage.error('封面不能超过5MB'); return false; }
  return true;
};
const onUploaded = () => { ElMessage.success('上传成功'); load(); };
const onThumbUploaded = (res: any) => {
  if (res.success) { ElMessage.success('封面已更新'); if (currentMedia.value) currentMedia.value.thumbUrl = res.data?.thumbUrl; load(); }
  else ElMessage.error(res.message || '上传失败');
};
const openThumbDialog = (row: any) => { currentMedia.value = { ...row }; thumbDialogVisible.value = true; };
const onDelete = async (id: number) => {
  await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' });
  await deleteMedia(id); ElMessage.success('已删除'); load();
};
const onDownload = async (id: number, name: string) => {
  const resp = await downloadMedia(id);
  const url = window.URL.createObjectURL(new Blob([resp.data]));
  const a = document.createElement('a'); a.href = url; a.download = name || 'file'; a.click();
  window.URL.revokeObjectURL(url);
};
const onPreview = (row: any) => { previewItem.value = row; previewVisible.value = true; };
const getTypeTag = (t: string) => ({ image: 'success', video: 'warning', audio: 'info' }[t] || '');
const getTypeLabel = (t: string) => ({ image: '图片', video: '视频', audio: '音频' }[t] || t);
const formatSize = (b: number) => !b ? '-' : b < 1024 ? b + 'B' : b < 1048576 ? (b/1024).toFixed(1) + 'KB' : (b/1048576).toFixed(1) + 'MB';
const formatDate = (d: string) => d ? d.replace('T', ' ').substring(0, 16) : '-';

onMounted(load);
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .content-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; }
.preview-cell { width: 70px; height: 50px; border-radius: 6px; overflow: hidden; cursor: pointer; }
.preview-cell:hover { transform: scale(1.05); }
.preview-img { width: 100%; height: 100%; }
.preview-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 22px; background: #f5f7fa; color: #909399; }
.preview-placeholder.video { background: linear-gradient(135deg, #e6a23c, #ebb563); color: #fff; }
.file-info { display: flex; flex-direction: column; gap: 4px; }
.file-name { font-weight: 500; color: #303133; }
.file-meta { font-size: 12px; color: #909399; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
.preview-dialog-content { display: flex; justify-content: center; align-items: center; min-height: 300px; }
.preview-full-img, .preview-video { max-width: 100%; max-height: 500px; border-radius: 8px; }
.preview-file-info { text-align: center; color: #909399; }
.thumb-content { padding: 0 8px; }
.thumb-label { margin: 0 0 10px; font-size: 14px; color: #606266; }
.thumb-preview { width: 160px; height: 100px; border-radius: 8px; overflow: hidden; border: 1px solid #ebeef5; }
.thumb-img { width: 100%; height: 100%; }
.thumb-empty { width: 100%; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; background: #f5f7fa; color: #c0c4cc; gap: 4px; }
.thumb-empty .el-icon { font-size: 24px; }
:deep(.el-upload-dragger) { padding: 16px; }
:deep(.el-icon--upload) { font-size: 32px; color: #c0c4cc; margin-bottom: 4px; }
</style>
