<template>
  <div>
    <div class="header">
      <h2>布局管理</h2>
      <el-button type="primary" @click="onCreate">新增布局</el-button>
    </div>

    <el-table :data="list">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="layoutJson" label="布局JSON" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增布局" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="JSON">
          <el-input v-model="form.layoutJson" type="textarea" :rows="6" placeholder='{"areas":[{"x":0,"y":0,"w":1,"h":1}]}' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { createLayout, fetchLayouts } from '../api';
import { ElMessage } from 'element-plus';

const list = ref<any[]>([]);
const dialogVisible = ref(false);
const form = ref({ name: '', layoutJson: '' });

const load = async () => {
  const resp = await fetchLayouts();
  // @ts-ignore
  list.value = resp.data?.data || [];
};

const onCreate = () => {
  form.value = { name: '', layoutJson: '' };
  dialogVisible.value = true;
};

const submit = async () => {
  if (!form.value.name || !form.value.layoutJson) {
    ElMessage.warning('请填写名称与布局JSON');
    return;
  }
  await createLayout(form.value);
  ElMessage.success('已创建');
  dialogVisible.value = false;
  load();
};

onMounted(load);
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
