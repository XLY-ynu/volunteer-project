<template>
  <div>
    <div class="header">
      <h2>终端播放预览</h2>
      <div class="actions">
        <el-input v-model="code" placeholder="输入终端代码" style="width:220px" />
        <el-button type="primary" @click="load">获取播放</el-button>
      </div>
    </div>
    <el-empty v-if="playbacks.length === 0" description="暂无绑定" />
    <el-collapse v-else>
      <el-collapse-item v-for="p in playbacks" :key="p.playlist?.id" :title="p.playlist?.name">
        <p>布局: {{ p.layout?.name || '默认' }}</p>
        <el-table :data="p.items" size="small">
          <el-table-column prop="mediaId" label="媒体ID" width="120" />
          <el-table-column prop="contentId" label="内容ID" width="120" />
          <el-table-column prop="displayDuration" label="时长(秒)" width="120" />
          <el-table-column prop="sortOrder" label="排序" width="100" />
        </el-table>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import http from '../api/http';
import { ElMessage } from 'element-plus';

const code = ref('');
const playbacks = ref<any[]>([]);

const load = async () => {
  if (!code.value) {
    ElMessage.warning('请输入终端代码');
    return;
  }
  const resp = await http.get('/public/playback', { params: { terminalCode: code.value } });
  // @ts-ignore
  playbacks.value = resp.data?.data || [];
  if (!playbacks.value.length) ElMessage.info('该终端暂无有效播放绑定');
};
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
