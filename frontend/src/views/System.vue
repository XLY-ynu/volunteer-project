<template>
  <div>
    <div class="header">
      <h2>系统管理</h2>
      <el-button type="primary" @click="onBackup">下载备份</el-button>
    </div>
    <el-descriptions title="系统信息" :column="1" border>
      <el-descriptions-item label="应用">{{ info.app }}</el-descriptions-item>
      <el-descriptions-item label="时间">{{ info.time }}</el-descriptions-item>
      <el-descriptions-item label="Java">{{ info.java }}</el-descriptions-item>
      <el-descriptions-item label="OS">{{ info.os }}</el-descriptions-item>
      <el-descriptions-item label="存储目录">{{ info.storageRoot }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { downloadBackup, fetchSystemInfo } from '../api';
import { ElMessage } from 'element-plus';

const info = ref<any>({});

const load = async () => {
  const resp = await fetchSystemInfo();
  // @ts-ignore
  info.value = resp.data?.data || {};
};

const onBackup = async () => {
  const resp = await downloadBackup();
  const blob = new Blob([resp.data]);
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'backup.zip';
  a.click();
  window.URL.revokeObjectURL(url);
  ElMessage.success('备份下载开始');
};

onMounted(load);
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
</style>
