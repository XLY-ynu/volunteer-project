<template>
  <div>
    <div class="header">
      <h2>分类管理</h2>
      <el-form :inline="true" :model="form" @submit.prevent>
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="文明XX" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="form.code" placeholder="code" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onCreate">新增</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table :data="list" style="width: 100%">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="code" label="编码" />
      <el-table-column prop="parentId" label="父级ID" width="100" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { createCategory, fetchCategories } from '../api';
import { ElMessage } from 'element-plus';

const list = ref<any[]>([]);
const form = ref({ name: '', code: '', parentId: undefined as number | undefined, sortOrder: 0 });

const load = async () => {
  const resp = await fetchCategories();
  // @ts-ignore
  list.value = resp.data?.data || [];
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

onMounted(load);
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
