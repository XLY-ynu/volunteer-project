<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>系统设置</h3>
          <span class="subtitle">系统信息与数据备份</span>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Monitor /></el-icon>
              <span>系统信息</span>
            </div>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="应用名称">{{ info.app }}</el-descriptions-item>
            <el-descriptions-item label="服务器时间">{{ formatTime(info.time) }}</el-descriptions-item>
            <el-descriptions-item label="Java版本">{{ info.java }}</el-descriptions-item>
            <el-descriptions-item label="操作系统">{{ info.os }}</el-descriptions-item>
            <el-descriptions-item label="存储目录">
              <el-tag size="small" type="info">{{ info.storageRoot }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="backup-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Download /></el-icon>
              <span>数据备份</span>
            </div>
          </template>
          
          <div class="backup-info">
            <el-alert type="info" :closable="false" style="margin-bottom: 16px">
              <template #title>备份包含以下内容</template>
            </el-alert>
            
            <div class="backup-items">
              <div class="backup-item">
                <el-icon><Document /></el-icon>
                <div class="item-info">
                  <div class="item-title">数据库数据</div>
                  <div class="item-desc">用户、角色、内容、媒体、播放列表、终端、志愿者、活动等</div>
                </div>
              </div>
              <div class="backup-item">
                <el-icon><Picture /></el-icon>
                <div class="item-info">
                  <div class="item-title">媒体文件</div>
                  <div class="item-desc">上传的图片、视频、封面、缩略图等</div>
                </div>
              </div>
              <div class="backup-item">
                <el-icon><Files /></el-icon>
                <div class="item-info">
                  <div class="item-title">数据库结构</div>
                  <div class="item-desc">schema.sql 表结构定义</div>
                </div>
              </div>
              <div class="backup-item">
                <el-icon><InfoFilled /></el-icon>
                <div class="item-info">
                  <div class="item-title">备份信息</div>
                  <div class="item-desc">备份时间、数据统计等元信息</div>
                </div>
              </div>
            </div>
            
            <el-button type="primary" size="large" @click="onBackup" :loading="backupLoading" class="backup-btn">
              <el-icon><Download /></el-icon>
              下载完整备份
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { downloadBackup, fetchSystemInfo } from '../api';
import { ElMessage } from 'element-plus';
import { Monitor, Download, Document, Picture, Files, InfoFilled } from '@element-plus/icons-vue';

const info = ref<any>({});
const backupLoading = ref(false);

const load = async () => {
  const resp = await fetchSystemInfo();
  info.value = (resp.data as any)?.data || {};
};

const formatTime = (t: string) => t ? t.replace('T', ' ').substring(0, 19) : '-';

const onBackup = async () => {
  backupLoading.value = true;
  try {
    const resp = await downloadBackup();
    const blob = new Blob([resp.data]);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    // 使用时间戳命名
    const timestamp = new Date().toISOString().replace(/[-:T]/g, '').substring(0, 14);
    a.download = `backup_${timestamp}.zip`;
    a.click();
    window.URL.revokeObjectURL(url);
    ElMessage.success('备份下载已开始');
  } catch (e) {
    ElMessage.error('备份下载失败');
  } finally {
    backupLoading.value = false;
  }
};

onMounted(load);
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .info-card, .backup-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; }

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
.card-header .el-icon { font-size: 18px; color: #409eff; }

.backup-info { padding: 8px 0; }

.backup-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.backup-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.backup-item .el-icon {
  font-size: 24px;
  color: #409eff;
  margin-top: 2px;
}

.item-info { flex: 1; }
.item-title { font-weight: 500; color: #303133; margin-bottom: 4px; }
.item-desc { font-size: 12px; color: #909399; }

.backup-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
}
</style>
