<template>
  <div class="page">
    <h2>志愿者管理</h2>
    
    <el-tabs v-model="activeTab" @tab-change="loadData">
      <el-tab-pane label="待审核" name="pending">
        <el-table :data="pendingList" v-loading="loading">
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="phone" label="电话" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="createdAt" label="申请时间">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="audit(row.id, 'approve')">通过</el-button>
              <el-button type="danger" size="small" @click="audit(row.id, 'reject')">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="已通过" name="approved">
        <el-table :data="approvedList" v-loading="loading">
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="phone" label="电话" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="joinedAt" label="加入时间">
            <template #default="{ row }">{{ formatTime(row.joinedAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';

const activeTab = ref('pending');
const loading = ref(false);
const pendingList = ref<any[]>([]);
const approvedList = ref<any[]>([]);

// 动态获取 headers
const getHeaders = () => {
  const token = localStorage.getItem('token');
  return { Authorization: `Bearer ${token}` };
};

const loadData = async () => {
  loading.value = true;
  try {
    if (activeTab.value === 'pending') {
      const resp = await axios.get('/api/org/volunteers/pending', { headers: getHeaders() });
      pendingList.value = resp.data.data || [];
    } else {
      const resp = await axios.get('/api/org/volunteers', { headers: getHeaders() });
      approvedList.value = resp.data.data || [];
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const audit = async (id: number, action: string) => {
  try {
    await axios.post(`/api/org/volunteers/${id}/audit?action=${action}`, {}, { headers: getHeaders() });
    ElMessage.success(action === 'approve' ? '已通过' : '已拒绝');
    loadData();
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败');
  }
};

const formatTime = (t: string) => t ? new Date(t).toLocaleString() : '';

onMounted(loadData);
</script>

<style scoped>
.page h2 { margin: 0 0 20px; color: #2c5282; }
</style>
