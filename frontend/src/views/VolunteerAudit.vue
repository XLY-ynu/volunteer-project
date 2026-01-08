<!--
 * @Author: 系统管理
 * @Module: 志愿者审核
 * @Description: 管理员审核用户的志愿者申请
-->
<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>志愿者申请审核</span>
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="badge">
            <el-tag type="warning" size="small">待审核</el-tag>
          </el-badge>
        </div>
      </template>

      <!-- 筛选 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 120px" @change="load">
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="filterName" placeholder="搜索姓名" clearable style="width: 150px" @keyup.enter="load" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 列表 -->
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="organization" label="所属单位" min-width="150">
          <template #default="{ row }">{{ row.organization || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'pending'">
              <el-button type="success" size="small" @click="audit(row.id, 'approved')">
                <el-icon><Check /></el-icon> 通过
              </el-button>
              <el-button type="danger" size="small" @click="audit(row.id, 'rejected')">
                <el-icon><Close /></el-icon> 拒绝
              </el-button>
            </template>
            <span v-else style="color: #999;">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Check, Close } from '@element-plus/icons-vue';
import axios from 'axios';

const loading = ref(false);
const list = ref<any[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const filterStatus = ref('');
const filterName = ref('');

const token = localStorage.getItem('token');
const headers = { Authorization: `Bearer ${token}` };

const pendingCount = computed(() => list.value.filter(v => v.status === 'pending').length);

const load = async () => {
  loading.value = true;
  try {
    let url = `/api/volunteers?page=${page.value}&size=${size.value}`;
    if (filterStatus.value) url += `&status=${filterStatus.value}`;
    if (filterName.value) url += `&name=${filterName.value}`;
    
    const resp = await axios.get(url, { headers });
    const data = resp.data.data;
    list.value = data.records || data || [];
    total.value = data.total || list.value.length;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const audit = async (id: number, status: string) => {
  const action = status === 'approved' ? '通过' : '拒绝';
  try {
    await ElMessageBox.confirm(`确定${action}该志愿者申请？`, '确认', { type: 'warning' });
    await axios.put(`/api/volunteers/${id}/audit`, { status }, { headers });
    ElMessage.success(`已${action}`);
    load();
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '操作失败');
    }
  }
};

const onPageChange = (p: number) => {
  page.value = p;
  load();
};

const getStatusType = (status: string) => {
  return { pending: 'warning', approved: 'success', rejected: 'danger' }[status] || 'info';
};

const getStatusText = (status: string) => {
  return { pending: '待审核', approved: '已通过', rejected: '已拒绝' }[status] || status;
};

const formatTime = (t: string) => t ? new Date(t).toLocaleString() : '';

onMounted(load);
</script>

<style scoped>
.page { padding: 0; }
.card-header { display: flex; align-items: center; gap: 12px; font-weight: 600; }
.filter-form { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.badge :deep(.el-badge__content) { top: 8px; }
</style>
