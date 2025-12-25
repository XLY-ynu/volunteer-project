<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>媒体资源</h3>
          <span class="subtitle">管理图片、视频等媒体文件</span>
        </div>
        <el-upload
          :headers="uploadHeaders"
          action="/api/media/upload"
          :show-file-list="false"
          :on-success="onUploaded"
          :before-upload="beforeUpload"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            上传资源
          </el-button>
        </el-upload>
      </div>
    </el-card>

    <!-- 资源列表 -->
    <el-card class="content-card" shadow="never">
      <el-table :data="list" style="width: 100%" stripe>
        <el-table-column label="缩略图" width="100">
          <template #default="scope">
            <div class="thumb-wrapper">
              <el-image
                v-if="scope.row.thumbUrl || scope.row.type === 'image'"
                :src="scope.row.thumbUrl || scope.row.url"
                :preview-src-list="[scope.row.url]"
                fit="cover"
                class="thumb-img"
              />
              <div v-else-if="scope.row.type === 'video'" class="thumb-placeholder video">
                <el-icon><VideoPlay /></el-icon>
              </div>
              <div v-else class="thumb-placeholder file">
                <el-icon><Document /></el-icon>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="文件名" min-width="200">
          <template #default="scope">
            <div class="file-name">{{ scope.row.name }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)" size="small">
              {{ scope.row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="100">
          <template #default="scope">
            {{ formatSize(scope.row.sizeBytes) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" @click="onPreview(scope.row)">
                <el-icon><View /></el-icon>
                预览
              </el-button>
              <el-button size="small" @click="onDownload(scope.row.id, scope.row.name)">
                <el-icon><Download /></el-icon>
                下载
              </el-button>
              <el-dropdown trigger="click">
                <el-button size="small">
                  更多
                  <el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="openThumbUpload(scope.row)">
                      <el-icon><Picture /></el-icon>
                      上传封面
                    </el-dropdown-item>
                    <el-dropdown-item @click="onDelete(scope.row.id)" divided>
                      <el-icon color="#f56c6c"><Delete /></el-icon>
                      <span style="color: #f56c6c">删除</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="load"
        />
      </div>
    </el-card>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="预览" width="800px" destroy-on-close>
      <div class="preview-content">
        <img v-if="previewItem?.type === 'image'" :src="previewItem?.url" class="preview-img" />
        <video v-else-if="previewItem?.type === 'video'" :src="previewItem?.url" controls class="preview-video" />
        <div v-else class="preview-file">
          <el-icon size="64"><Document /></el-icon>
          <p>{{ previewItem?.name }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- 上传封面弹窗 -->
    <el-dialog v-model="thumbDialogVisible" title="上传封面" width="400px">
      <el-upload
        :headers="uploadHeaders"
        :action="`/api/media/${currentMediaId}/thumb`"
        :show-file-list="false"
        :on-success="onThumbUploaded"
        accept="image/*"
        drag
      >
        <el-icon class="el-icon--upload"><Upload /></el-icon>
        <div class="el-upload__text">拖拽图片到此处，或<em>点击上传</em></div>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { deleteMedia, downloadMedia, fetchMedia } from '../api';
import { useUserStore } from '../stores/user';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Upload, VideoPlay, Document, View, Download, Delete, Picture, ArrowDown } from '@element-plus/icons-vue';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const user = useUserStore();
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${user.token}` }));
const previewVisible = ref(false);
const previewItem = ref<any>(null);
const thumbDialogVisible = ref(false);
const currentMediaId = ref<number | null>(null);

const load = async () => {
  const resp = await fetchMedia(page.value, size.value);
  // @ts-ignore
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};

const beforeUpload = (file: File) => {
  const isLt50M = file.size / 1024 / 1024 < 50;
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过 50MB');
  }
  return isLt50M;
};

const onUploaded = () => {
  ElMessage.success('上传成功');
  load();
};

const onThumbUploaded = () => {
  ElMessage.success('封面已更新');
  thumbDialogVisible.value = false;
  load();
};

const openThumbUpload = (row: any) => {
  currentMediaId.value = row.id;
  thumbDialogVisible.value = true;
};

const onDelete = async (id: number) => {
  await ElMessageBox.confirm('确定要删除这个资源吗？', '提示', { type: 'warning' });
  await deleteMedia(id);
  ElMessage.success('已删除');
  load();
};

const onDownload = async (id: number, name: string) => {
  const resp = await downloadMedia(id);
  const blob = new Blob([resp.data]);
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = name || 'file';
  a.click();
  window.URL.revokeObjectURL(url);
};

const onPreview = (row: any) => {
  previewItem.value = row;
  previewVisible.value = true;
};

const getTypeTag = (type: string) => {
  const map: Record<string, string> = { image: 'success', video: 'warning', audio: 'info' };
  return map[type] || '';
};

const formatSize = (bytes: number) => {
  if (!bytes) return '-';
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / 1024 / 1024).toFixed(1) + ' MB';
};

const formatDate = (date: string) => {
  if (!date) return '-';
  return date.replace('T', ' ').substring(0, 19);
};

onMounted(load);
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  border-radius: 12px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.subtitle {
  font-size: 13px;
  color: #909399;
}

.content-card {
  border-radius: 12px;
}

.thumb-wrapper {
  width: 60px;
  height: 45px;
  border-radius: 6px;
  overflow: hidden;
}

.thumb-img {
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.thumb-placeholder.video {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
  color: #fff;
}

.thumb-placeholder.file {
  background: #f5f7fa;
  color: #909399;
}

.file-name {
  font-weight: 500;
  color: #303133;
  word-break: break-all;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.preview-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.preview-img {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
}

.preview-video {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
}

.preview-file {
  text-align: center;
  color: #909399;
}
</style>
