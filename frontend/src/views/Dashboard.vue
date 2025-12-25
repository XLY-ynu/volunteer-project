<template>
  <div class="page">
    <div class="header">
      <h2>仪表盘</h2>
    </div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card>
          <div class="card-title">{{ card.title }}</div>
          <div class="stat">{{ card.value }}</div>
          <div class="desc">{{ card.desc }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { fetchSummary } from '../api';

const cards = ref([
  { title: '终端数', value: 0, desc: '注册的显示终端' },
  { title: '资源数', value: 0, desc: '媒体资源总数' },
  { title: '播放列表', value: 0, desc: '可分发的列表' },
  { title: '活动数', value: 0, desc: '志愿活动' }
]);

const load = async () => {
  const resp = await fetchSummary();
  // @ts-ignore
  const d = resp.data?.data || {};
  cards.value = [
    { title: '终端数', value: d.terminalTotal || 0, desc: '注册的显示终端' },
    { title: '资源数', value: d.mediaTotal || 0, desc: '媒体资源总数' },
    { title: '播放列表', value: d.playlistTotal || 0, desc: '可分发的列表' },
    { title: '活动数', value: d.activityTotal || 0, desc: '志愿活动' }
  ];
};

onMounted(load);
</script>

<style scoped>
.page {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.card-title {
  font-weight: bold;
  margin-bottom: 6px;
}
.stat {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 4px;
}
.desc {
  color: #909399;
}
</style>
