<!--
 * @Author: 梁玉杰
 * @Module: 内容展示管理 - 分类管理
 * @Description: 分类管理页面，管理内容分类
-->
<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>分类管理</h3>
          <span class="subtitle">管理内容分类</span>
        </div>
        <el-form :inline="true" :model="form" @submit.prevent class="header-form">
          <el-form-item label="名称">
            <el-input v-model="form.name" placeholder="分类名称" style="width: 140px" />
          </el-form-item>
          <el-form-item label="编码">
            <el-input v-model="form.code" placeholder="分类编码" style="width: 140px" />
          </el-form-item>
          <el-form-item label="父级">
            <el-select v-model="form.parentId" placeholder="无" clearable style="width: 140px">
              <el-option v-for="c in list" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="onCreate">
              <el-icon><Plus /></el-icon>
              新增
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="content-card" shadow="never">
      <el-table :data="list" stripe>
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="code" label="编码" min-width="120" />
        <el-table-column label="父级" width="150">
          <template #default="scope">
            <span v-if="scope.row.parentId">{{ getParentName(scope.row.parentId) }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button size="small" type="danger" @click="onDelete(scope.row)" title="删除">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { createCategory, deleteCategory, fetchCategories } from '../api';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Delete } from '@element-plus/icons-vue';

const list = ref<any[]>([]);
const form = ref({ name: '', code: '', parentId: undefined as number | undefined, sortOrder: 0 });

const load = async () => {
  const resp = await fetchCategories();
  list.value = resp.data?.data || [];
};

const getParentName = (parentId: number) => {
  const parent = list.value.find(c => c.id === parentId);
  return parent?.name || `#${parentId}`;
};

const onCreate = async () => {
  if (!form.value.name || !form.value.code) {
    ElMessage.warning('请填写名称和编码');
    return;
  }
  await createCategory(form.value);
  ElMessage.success('创建成功');
  form.value = { name: '', code: '', parentId: undefined, sortOrder: 0 };
  load();
};

const onDelete = async (row: any) => {
  // 检查是否有子分类
  const hasChildren = list.value.some(c => c.parentId === row.id);
  if (hasChildren) {
    ElMessage.warning('该分类下有子分类，无法删除');
    return;
  }
  
  await ElMessageBox.confirm(`确定删除分类「${row.name}」？`, '提示', { type: 'warning' });
  await deleteCategory(row.id);
  ElMessage.success('删除成功');
  load();
};

onMounted(load);
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .content-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; }
.header-form { display: flex; align-items: center; flex-wrap: wrap; }
.header-form :deep(.el-form-item) { margin-bottom: 0; margin-right: 12px; }
.text-muted { color: #c0c4cc; }
</style>
