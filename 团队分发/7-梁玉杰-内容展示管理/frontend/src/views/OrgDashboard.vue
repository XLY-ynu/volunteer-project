<!--
 * @Author: 梁玉杰
 * @Module: 组织端仪表盘
 * @Description: 组织端工作台，展示志愿者数量、待审核申请、待处理求助等统计
-->
<template>
  <div class="dashboard">
    <h2>工作台</h2>
    
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon volunteer">👥</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.volunteerCount }}</div>
            <div class="stat-label">组织志愿者</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon pending">⏳</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingCount }}</div>
            <div class="stat-label">待审核申请</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon help">🆘</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.helpPendingCount }}</div>
            <div class="stat-label">待处理求助</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>待审核志愿者</span>
              <el-button type="primary" link @click="$router.push('/org/volunteers')">查看全部</el-button>
            </div>
          </template>
          <div v-if="pendingVolunteers.length === 0" class="empty">暂无待审核申请</div>
          <div v-else class="list">
            <div v-for="v in pendingVolunteers.slice(0, 5)" :key="v.id" class="list-item">
              <span class="name">{{ v.name }}</span>
              <span class="phone">{{ v.phone }}</span>
              <el-tag size="small" type="warning">待审核</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>待处理求助</span>
              <el-button type="primary" link @click="$router.push('/org/help-requests')">查看全部</el-button>
            </div>
          </template>
          <div v-if="pendingHelps.length === 0" class="empty">暂无待处理求助</div>
          <div v-else class="list">
            <div v-for="h in pendingHelps.slice(0, 5)" :key="h.id" class="list-item">
              <span class="title">{{ h.title }}</span>
              <span class="time">{{ formatTime(h.createdAt) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';

const stats = ref({ volunteerCount: 0, pendingCount: 0, helpPendingCount: 0 });
const pendingVolunteers = ref<any[]>([]);
const pendingHelps = ref<any[]>([]);

// 动态获取 headers
const getHeaders = () => {
  const token = localStorage.getItem('token');
  return { Authorization: `Bearer ${token}` };
};

onMounted(async () => {
  try {
    const headers = getHeaders();
    const [statsResp, volunteersResp, helpsResp] = await Promise.all([
      axios.get('/api/org/stats', { headers }),
      axios.get('/api/org/volunteers/pending', { headers }),
      axios.get('/api/org/help-requests?status=pending', { headers })
    ]);
    stats.value = statsResp.data.data;
    pendingVolunteers.value = volunteersResp.data.data || [];
    pendingHelps.value = helpsResp.data.data?.records || [];
  } catch (e) {
    console.error(e);
  }
});

const formatTime = (t: string) => {
  if (!t) return '';
  return new Date(t).toLocaleDateString();
};
</script>

<style scoped>
.dashboard h2 { margin: 0 0 20px; color: #2c5282; }

.stats-row { margin-bottom: 20px; }

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  font-size: 40px;
  margin-right: 16px;
}

.stat-value { font-size: 28px; font-weight: 600; color: #2c5282; }
.stat-label { color: #718096; font-size: 14px; }

.section-card { height: 300px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.empty { text-align: center; color: #a0aec0; padding: 40px 0; }

.list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #edf2f7;
}

.list-item:last-child { border-bottom: none; }
.list-item .name { font-weight: 500; }
.list-item .phone, .list-item .time { color: #718096; font-size: 13px; }
.list-item .title { flex: 1; }
</style>
