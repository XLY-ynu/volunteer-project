<template>
  <div>
    <div class="header">
      <h2>内容管理</h2>
      <el-button type="primary" @click="onCreate">发布内容</el-button>
    </div>

    <el-form :inline="true" :model="filter" class="filter" @submit.prevent>
      <el-form-item label="分类">
        <el-select v-model="filter.categoryId" placeholder="全部" clearable style="width: 180px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="filter.published" placeholder="全部" clearable style="width: 140px">
          <el-option :value="true" label="已发布" />
          <el-option :value="false" label="草稿" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <el-dialog v-model="dialogVisible" title="发布内容" width="640px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" />
        </el-form-item>
        <el-form-item label="封面">
          <el-input v-model="form.coverUrl" placeholder="http://..." />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.body" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="发布">
          <el-switch v-model="form.published" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </template>
    </el-dialog>

    <el-table :data="list" style="width: 100%">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="categoryId" label="分类ID" width="100" />
      <el-table-column prop="published" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.published ? 'success' : 'info'">{{ scope.row.published ? '已发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="180" />
      <el-table-column label="操作" width="140">
        <template #default="scope">
          <el-button size="small" @click="edit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="onDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { createCategory, createContent, deleteContent, fetchCategories, fetchContent, updateContent } from '../api';

const list = ref<any[]>([]);
const categories = ref<any[]>([]);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);

const form = ref({
  title: '',
  categoryId: undefined as number | undefined,
  summary: '',
  coverUrl: '',
  body: '',
  published: true
});

const filter = ref<{ categoryId?: number; published?: boolean }>({});

const load = async () => {
  const resp = await fetchContent(1, 50, filter.value.categoryId, filter.value.published);
  // @ts-ignore
  list.value = resp.data?.data?.records || [];
};

const loadCategories = async () => {
  const resp = await fetchCategories();
  // @ts-ignore
  categories.value = resp.data?.data || [];
};

const onCreate = () => {
  editingId.value = null;
  form.value = { title: '', categoryId: undefined, summary: '', coverUrl: '', body: '', published: true };
  dialogVisible.value = true;
};

const edit = (row: any) => {
  editingId.value = row.id;
  form.value = { title: row.title, categoryId: row.categoryId, summary: row.summary, coverUrl: row.coverUrl, body: row.body, published: row.published };
  dialogVisible.value = true;
};

const submit = async () => {
  if (!form.value.title || !form.value.categoryId) {
    ElMessage.warning('标题和分类必填');
    return;
  }
  if (editingId.value) {
    await updateContent(editingId.value, form.value);
    ElMessage.success('已更新');
  } else {
    await createContent(form.value);
    ElMessage.success('已创建');
  }
  dialogVisible.value = false;
  load();
};

const onDelete = async (id: number) => {
  await deleteContent(id);
  ElMessage.success('已删除');
  load();
};

onMounted(() => {
  loadCategories();
  load();
});
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter {
  margin: 12px 0;
}
</style>
