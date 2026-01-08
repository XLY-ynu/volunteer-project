<template>
  <div class="page">
    <div class="header-row">
      <h2>活动管理</h2>
      <el-button type="primary" @click="showCreate">发布活动</el-button>
    </div>
    
    <el-table :data="activities" v-loading="loading">
      <el-table-column prop="title" label="活动名称" min-width="180" />
      <el-table-column prop="location" label="地点" min-width="120" />
      <el-table-column label="时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.startTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="capacity" label="人数上限" width="90" />
      <el-table-column label="报名人数" width="90">
        <template #default="{ row }">{{ row.signupCount || 0 }}</template>
      </el-table-column>
      <el-table-column prop="checkinCode" label="签到码" width="100" />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button size="small" @click="showSignups(row)">名单</el-button>
          <el-button size="small" @click="showCheckins(row)">统计</el-button>
          <el-button size="small" type="primary" @click="editActivity(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteActivity(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 创建/编辑活动弹窗 -->
    <el-dialog v-model="formVisible" :title="editingId ? '编辑活动' : '发布活动'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.title" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="活动地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" />
        </el-form-item>
        <el-form-item label="人数上限">
          <el-input-number v-model="form.capacity" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="签到码">
          <el-input v-model="form.checkinCode" placeholder="6位数字签到码" />
          <el-button @click="generateCode" style="margin-left: 10px;">随机生成</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="saveActivity">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 报名名单弹窗 -->
    <el-dialog v-model="signupVisible" title="报名名单" width="700px">
      <div style="margin-bottom: 10px;">
        <el-button type="success" size="small" @click="exportSignups">导出名单</el-button>
      </div>
      <el-table :data="signups" max-height="400">
        <el-table-column prop="volunteerName" label="姓名" />
        <el-table-column prop="volunteerPhone" label="电话" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'checked_in' ? 'success' : 'info'">
              {{ row.status === 'checked_in' ? '已签到' : '已报名' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名时间">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <div v-if="signups.length === 0" style="text-align: center; padding: 20px; color: #999;">
        暂无报名数据
      </div>
    </el-dialog>
    
    <!-- 签到统计弹窗 -->
    <el-dialog v-model="checkinVisible" title="签到统计" width="500px">
      <div class="stats-grid" v-if="checkinStats">
        <div class="stat-card">
          <div class="stat-value">{{ checkinStats.total }}</div>
          <div class="stat-label">报名人数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ checkinStats.checkedIn }}</div>
          <div class="stat-label">已签到</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ checkinStats.rate }}%</div>
          <div class="stat-label">签到率</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';

const loading = ref(false);
const activities = ref<any[]>([]);
const formVisible = ref(false);
const editingId = ref<number | null>(null);
const form = ref({ title: '', description: '', location: '', startTime: '', endTime: '', capacity: 50, checkinCode: '' });

const signupVisible = ref(false);
const signups = ref<any[]>([]);
const currentActivity = ref<any>(null);

const checkinVisible = ref(false);
const checkinStats = ref<any>(null);

// 动态获取 headers，确保每次请求都使用最新的 token
const getHeaders = () => {
  const token = localStorage.getItem('token');
  return { Authorization: `Bearer ${token}` };
};

const loadActivities = async () => {
  loading.value = true;
  try {
    const resp = await axios.get('/api/activities', { headers: getHeaders() });
    // API 返回分页数据，需要取 records 字段
    activities.value = resp.data.data?.records || resp.data.data || [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const showCreate = () => {
  editingId.value = null;
  form.value = { title: '', description: '', location: '', startTime: '', endTime: '', capacity: 50, checkinCode: '' };
  generateCode();
  formVisible.value = true;
};

const editActivity = (row: any) => {
  editingId.value = row.id;
  form.value = { ...row };
  formVisible.value = true;
};

const generateCode = () => {
  form.value.checkinCode = String(Math.floor(100000 + Math.random() * 900000));
};

const saveActivity = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入活动名称');
    return;
  }
  try {
    if (editingId.value) {
      await axios.put(`/api/activities/${editingId.value}`, form.value, { headers: getHeaders() });
    } else {
      await axios.post('/api/activities', form.value, { headers: getHeaders() });
    }
    ElMessage.success('保存成功');
    formVisible.value = false;
    loadActivities();
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '保存失败');
  }
};

const deleteActivity = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该活动？', '提示', { type: 'warning' });
    await axios.delete(`/api/activities/${id}`, { headers: getHeaders() });
    ElMessage.success('删除成功');
    loadActivities();
  } catch (e) {
    // cancelled
  }
};

const showSignups = async (row: any) => {
  currentActivity.value = row;
  try {
    const resp = await axios.get(`/api/activities/${row.id}/signups`, { headers: getHeaders() });
    // API 返回分页数据 { records: [...] }
    signups.value = resp.data.data?.records || resp.data.data || [];
    signupVisible.value = true;
  } catch (e) {
    console.error(e);
    ElMessage.error('获取报名名单失败');
  }
};

const exportSignups = () => {
  const csv = ['姓名,电话,状态,报名时间'];
  signups.value.forEach(s => {
    csv.push(`${s.volunteerName || ''},${s.volunteerPhone || ''},${s.status === 'checked_in' ? '已签到' : '已报名'},${s.createdAt || ''}`);
  });
  const blob = new Blob([csv.join('\n')], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `${currentActivity.value?.title || '活动'}_报名名单.csv`;
  link.click();
};

const showCheckins = async (row: any) => {
  try {
    const resp = await axios.get(`/api/activities/${row.id}/signups`, { headers: getHeaders() });
    // API 返回分页数据 { records: [...] }
    const list = resp.data.data?.records || resp.data.data || [];
    const total = list.length;
    const checkedIn = list.filter((s: any) => s.status === 'checked_in').length;
    const rate = total > 0 ? Math.round(checkedIn / total * 100) : 0;
    checkinStats.value = { total, checkedIn, rate };
    checkinVisible.value = true;
  } catch (e) {
    console.error(e);
    ElMessage.error('获取签到统计失败');
  }
};

const formatDate = (d: string) => d ? new Date(d).toLocaleString() : '';

onMounted(loadActivities);
</script>

<style scoped>
.page h2 { margin: 0; color: #2c5282; }
.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.action-buttons { display: flex; gap: 4px; flex-wrap: nowrap; }
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.stat-card { text-align: center; padding: 20px; background: #f5f7fa; border-radius: 8px; }
.stat-value { font-size: 32px; font-weight: bold; color: #409eff; }
.stat-label { color: #666; margin-top: 8px; }
</style>
