<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>内容管理</h3>
          <span class="subtitle">发布和管理图文内容</span>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="onCreate"><el-icon><Plus /></el-icon>发布内容</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="content-card" shadow="never">
      <el-form :inline="true" :model="filter" class="filter" @submit.prevent>
        <el-form-item label="分类">
          <el-select v-model="filter.categoryId" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.published" placeholder="全部" clearable style="width: 120px">
            <el-option :value="true" label="已发布" />
            <el-option :value="false" label="草稿" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filter.keyword" placeholder="标题/摘要" style="width: 180px" clearable @keyup.enter="load" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load"><el-icon><Search /></el-icon>查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" stripe>
        <el-table-column label="封面" width="90">
          <template #default="scope">
            <div class="cover-cell">
              <el-image v-if="scope.row.coverUrl" :src="scope.row.coverUrl" fit="cover" class="cover-img" :preview-src-list="[scope.row.coverUrl]" />
              <div v-else class="cover-empty"><el-icon><Picture /></el-icon></div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="240">
          <template #default="scope">
            <div class="title-cell">
              <span class="title-text">{{ scope.row.title }}</span>
              <span class="summary-text">{{ scope.row.summary || '暂无摘要' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template #default="scope">
            <el-tag size="small">{{ getCategoryName(scope.row.categoryId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.published ? 'success' : 'info'" size="small">
              {{ scope.row.published ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160">
          <template #default="scope">{{ formatDate(scope.row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-space>
              <el-button size="small" @click="edit(scope.row)"><el-icon><Edit /></el-icon>编辑</el-button>
              <el-button size="small" type="danger" plain @click="onDelete(scope.row.id)"><el-icon><Delete /></el-icon></el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination 
          layout="total, prev, pager, next" 
          :total="total" 
          :page-size="size" 
          :current-page="page" 
          @current-change="onPage" 
        />
      </div>
    </el-card>

    <!-- 发布/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑内容' : '发布内容'" width="640px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入内容标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" placeholder="简短描述内容（可选）" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="封面">
          <div class="cover-upload-area">
            <div class="cover-preview" v-if="form.coverUrl">
              <el-image :src="form.coverUrl" fit="cover" class="preview-img" />
              <div class="cover-actions">
                <el-button size="small" type="danger" @click="form.coverUrl = ''"><el-icon><Delete /></el-icon></el-button>
              </div>
            </div>
            <el-upload 
              v-else 
              :headers="uploadHeaders" 
              action="/api/media/upload-cover" 
              :show-file-list="false" 
              :on-success="onCoverUploaded" 
              :before-upload="beforeCoverUpload" 
              accept="image/*" 
              class="cover-uploader"
            >
              <div class="upload-trigger">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span>上传封面</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.body" type="textarea" :rows="6" placeholder="请输入正文内容" />
        </el-form-item>
        <el-form-item label="发布状态">
          <el-switch v-model="form.published" active-text="发布" inactive-text="草稿" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitting">{{ editingId ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createContent, deleteContent, fetchCategories, fetchContent, updateContent } from '../api';
import { useUserStore } from '../stores/user';
import { Plus, Search, Edit, Delete, Picture } from '@element-plus/icons-vue';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const categories = ref<any[]>([]);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const submitting = ref(false);
const user = useUserStore();
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${user.token}` }));

const form = ref({
  title: '',
  categoryId: undefined as number | undefined,
  summary: '',
  coverUrl: '',
  body: '',
  published: true
});

const filter = ref<{ categoryId?: number; published?: boolean; keyword?: string }>({});

const load = async () => {
  const resp = await fetchContent(page.value, size.value, filter.value.categoryId, filter.value.published, filter.value.keyword);
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};

const loadCategories = async () => {
  const resp = await fetchCategories();
  categories.value = resp.data?.data || [];
};

const getCategoryName = (id: number) => {
  const cat = categories.value.find(c => c.id === id);
  return cat?.name || '未分类';
};

const formatDate = (d: string) => d ? d.replace('T', ' ').substring(0, 16) : '-';

const onCreate = () => {
  editingId.value = null;
  form.value = { title: '', categoryId: undefined, summary: '', coverUrl: '', body: '', published: true };
  dialogVisible.value = true;
};

const edit = (row: any) => {
  editingId.value = row.id;
  form.value = {
    title: row.title,
    categoryId: row.categoryId,
    summary: row.summary,
    coverUrl: row.coverUrl,
    body: row.body,
    published: row.published
  };
  dialogVisible.value = true;
};

const beforeCoverUpload = (file: File) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片'); return false; }
  if (file.size / 1024 / 1024 > 10) { ElMessage.error('图片不能超过10MB'); return false; }
  return true;
};

const onCoverUploaded = (res: any) => {
  if (res.success && res.data?.url) {
    form.value.coverUrl = res.data.url;
    ElMessage.success('封面上传成功');
  } else {
    ElMessage.error(res.message || '上传失败');
  }
};

const submit = async () => {
  if (!form.value.title || !form.value.categoryId) {
    ElMessage.warning('标题和分类必填');
    return;
  }
  submitting.value = true;
  try {
    if (editingId.value) {
      await updateContent(editingId.value, form.value);
      ElMessage.success('已更新');
    } else {
      await createContent(form.value);
      ElMessage.success('已发布');
    }
    dialogVisible.value = false;
    load();
  } finally {
    submitting.value = false;
  }
};

const onDelete = async (id: number) => {
  await ElMessageBox.confirm('确定删除该内容？', '提示', { type: 'warning' });
  await deleteContent(id);
  ElMessage.success('已删除');
  load();
};

const onPage = (p: number) => {
  page.value = p;
  load();
};

onMounted(() => {
  loadCategories();
  load();
});
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .content-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; margin-left: 12px; }
.header-actions { display: flex; gap: 10px; }
.filter { margin-bottom: 16px; }

.cover-cell { width: 60px; height: 45px; border-radius: 6px; overflow: hidden; }
.cover-img { width: 100%; height: 100%; cursor: pointer; }
.cover-empty { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #f5f7fa; color: #c0c4cc; font-size: 18px; }

.title-cell { display: flex; flex-direction: column; gap: 4px; }
.title-text { font-weight: 500; color: #303133; }
.summary-text { font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 300px; }

.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }

/* 封面上传 */
.cover-upload-area { width: 180px; }
.cover-preview { position: relative; width: 180px; height: 110px; border-radius: 8px; overflow: hidden; border: 1px solid #ebeef5; }
.cover-preview .preview-img { width: 100%; height: 100%; }
.cover-actions { position: absolute; top: 4px; right: 4px; }
.cover-uploader { width: 180px; }
.upload-trigger { width: 180px; height: 110px; border: 2px dashed #dcdfe6; border-radius: 8px; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; transition: border-color 0.3s; }
.upload-trigger:hover { border-color: #409eff; }
.upload-icon { font-size: 28px; color: #c0c4cc; margin-bottom: 6px; }
.upload-trigger span { font-size: 13px; color: #909399; }
</style>
