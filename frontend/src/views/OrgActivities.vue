<!--
 * @Author: 曹宇涵
 * @Module: 活动管理（组织端）
 * @Description: 组织端活动管理页面，发布活动、查看报名名单、签到统计
-->
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
      <el-table-column label="仅限成员" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.membersOnly" type="warning" size="small">是</el-tag>
          <el-tag v-else type="info" size="small">否</el-tag>
        </template>
      </el-table-column>
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
        <el-form-item label="活动封面">
          <div class="cover-upload-wrapper">
            <el-upload
              class="cover-uploader"
              action="/api/media/upload-cover"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
              :before-upload="beforeCoverUpload"
              accept="image/*"
            >
              <div v-if="form.coverUrl" class="cover-preview">
                <img :src="form.coverUrl" alt="封面" />
                <div class="cover-actions">
                  <el-icon @click.stop="form.coverUrl = ''"><Delete /></el-icon>
                </div>
              </div>
              <div v-else class="cover-placeholder">
                <el-icon><Plus /></el-icon>
                <span>上传封面</span>
              </div>
            </el-upload>
            <div class="cover-tip">建议尺寸: 800x450px，支持 JPG/PNG 格式</div>
          </div>
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="活动地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="人数上限">
          <el-input-number v-model="form.capacity" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="签到码">
          <el-input v-model="form.checkinCode" placeholder="6位数字签到码" />
          <el-button @click="generateCode" style="margin-left: 10px;">随机生成</el-button>
        </el-form-item>
        <el-form-item label="仅限成员">
          <el-switch v-model="form.membersOnly" active-text="是" inactive-text="否" />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">开启后仅本组织成员可报名</span>
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
    <el-dialog v-model="checkinVisible" title="签到统计" width="600px">
      <div class="checkin-stats-container" v-if="checkinStats">
        <!-- 环形图表区域 -->
        <div class="chart-section">
          <div class="ring-chart">
            <svg viewBox="0 0 100 100" class="progress-ring">
              <circle class="ring-bg" cx="50" cy="50" r="40" />
              <circle 
                class="ring-progress" 
                cx="50" cy="50" r="40"
                :style="{ strokeDasharray: `${checkinStats.rate * 2.51} 251` }"
              />
            </svg>
            <div class="ring-center">
              <div class="ring-value">{{ checkinStats.rate }}%</div>
              <div class="ring-label">签到率</div>
            </div>
          </div>
          <div class="chart-legend">
            <div class="legend-item">
              <span class="legend-dot checked"></span>
              <span class="legend-text">已签到 {{ checkinStats.checkedIn }} 人</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot unchecked"></span>
              <span class="legend-text">未签到 {{ checkinStats.total - checkinStats.checkedIn }} 人</span>
            </div>
          </div>
        </div>
        
        <!-- 统计卡片区域 -->
        <div class="stats-grid">
          <div class="stat-card signup-card">
            <div class="stat-icon">
              <el-icon :size="28" color="#409EFF"><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ checkinStats.total }}</div>
              <div class="stat-label">报名人数</div>
            </div>
          </div>
          <div class="stat-card checkin-card">
            <div class="stat-icon">
              <el-icon :size="28" color="#67C23A"><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ checkinStats.checkedIn }}</div>
              <div class="stat-label">已签到</div>
            </div>
          </div>
          <div class="stat-card rate-card">
            <div class="stat-icon">
              <el-icon :size="28" color="#E6A23C"><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ checkinStats.rate }}%</div>
              <div class="stat-label">签到率</div>
            </div>
          </div>
        </div>
        
        <!-- 进度条 -->
        <div class="progress-section">
          <div class="progress-header">
            <span>签到进度</span>
            <span>{{ checkinStats.checkedIn }} / {{ checkinStats.total }}</span>
          </div>
          <el-progress 
            :percentage="checkinStats.rate" 
            :stroke-width="12"
            :color="getProgressColor(checkinStats.rate)"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { User, CircleCheck, TrendCharts, Plus, Delete } from '@element-plus/icons-vue';
import axios from 'axios';

const loading = ref(false);
const activities = ref<any[]>([]);
const formVisible = ref(false);
const editingId = ref<number | null>(null);
const form = ref({ title: '', description: '', location: '', startTime: '', endTime: '', capacity: 50, checkinCode: '', membersOnly: false, coverUrl: '' });

const signupVisible = ref(false);
const signups = ref<any[]>([]);
const currentActivity = ref<any>(null);

const checkinVisible = ref(false);
const checkinStats = ref<any>(null);

// 动态获取 headers，确保每次请求都使用最新的 token
const getHeaders = () => {
  const token = sessionStorage.getItem('org_token');
  return { Authorization: `Bearer ${token}` };
};

// 上传组件使用的 headers（计算属性）
const uploadHeaders = computed(() => {
  const token = sessionStorage.getItem('org_token');
  return { Authorization: `Bearer ${token}` };
});

const loadActivities = async () => {
  loading.value = true;
  try {
    const resp = await axios.get('/api/org/activities', { headers: getHeaders() });
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
  form.value = { title: '', description: '', location: '', startTime: '', endTime: '', capacity: 50, checkinCode: '', membersOnly: false, coverUrl: '' };
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
  // 验证开始时间和结束时间
  if (form.value.startTime && form.value.endTime) {
    const start = new Date(form.value.startTime);
    const end = new Date(form.value.endTime);
    if (end <= start) {
      ElMessage.warning('结束时间必须晚于开始时间');
      return;
    }
  }
  try {
    if (editingId.value) {
      await axios.put(`/api/org/activities/${editingId.value}`, form.value, { headers: getHeaders() });
    } else {
      await axios.post('/api/org/activities', form.value, { headers: getHeaders() });
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
    await axios.delete(`/api/org/activities/${id}`, { headers: getHeaders() });
    ElMessage.success('删除成功');
    loadActivities();
  } catch (e) {
    // cancelled
  }
};

const showSignups = async (row: any) => {
  currentActivity.value = row;
  try {
    const resp = await axios.get(`/api/org/activities/${row.id}/signups`, { headers: getHeaders() });
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
    const resp = await axios.get(`/api/org/activities/${row.id}/signups`, { headers: getHeaders() });
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

// 根据签到率返回进度条颜色
const getProgressColor = (rate: number) => {
  if (rate >= 80) return '#67C23A';
  if (rate >= 50) return '#E6A23C';
  return '#F56C6C';
};

const formatDate = (d: string) => d ? new Date(d).toLocaleString() : '';

// 封面上传相关
const handleCoverSuccess = (response: any) => {
  // 后端返回格式: { success: true, message: "ok", data: { url: "..." } }
  // upload-cover 接口只保存文件，不创建媒体资源记录
  if (response.success && response.data?.url) {
    form.value.coverUrl = response.data.url;
    ElMessage.success('封面上传成功');
  } else {
    ElMessage.error(response.message || '上传失败');
  }
};

const beforeCoverUpload = (file: File) => {
  const isImage = file.type.startsWith('image/');
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isImage) {
    ElMessage.error('只能上传图片文件');
    return false;
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB');
    return false;
  }
  return true;
};

onMounted(loadActivities);
</script>

<style scoped>
.page h2 { margin: 0; color: #2c5282; }
.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.action-buttons { display: flex; gap: 4px; flex-wrap: nowrap; }

/* 签到统计容器 */
.checkin-stats-container {
  padding: 10px 0;
}

/* 图表区域 */
.chart-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #fff 100%);
  border-radius: 16px;
}

/* 环形图 */
.ring-chart {
  position: relative;
  width: 140px;
  height: 140px;
}
.progress-ring {
  transform: rotate(-90deg);
  width: 100%;
  height: 100%;
}
.ring-bg {
  fill: none;
  stroke: #e8e8e8;
  stroke-width: 8;
}
.ring-progress {
  fill: none;
  stroke: #67C23A;
  stroke-width: 8;
  stroke-linecap: round;
  transition: stroke-dasharray 0.6s ease;
}
.ring-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}
.ring-value {
  font-size: 28px;
  font-weight: bold;
  color: #67C23A;
}
.ring-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

/* 图例 */
.chart-legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}
.legend-dot.checked { background: #67C23A; }
.legend-dot.unchecked { background: #e8e8e8; }
.legend-text {
  font-size: 14px;
  color: #606266;
}

/* 统计卡片 */
.stats-grid { 
  display: grid; 
  grid-template-columns: repeat(3, 1fr); 
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card { 
  display: flex; 
  align-items: center; 
  padding: 16px; 
  background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%); 
  border-radius: 12px; 
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}
.signup-card .stat-icon { background: rgba(64, 158, 255, 0.1); }
.checkin-card .stat-icon { background: rgba(103, 194, 58, 0.1); }
.rate-card .stat-icon { background: rgba(230, 162, 60, 0.1); }
.stat-content { text-align: left; }
.stat-value { font-size: 24px; font-weight: bold; color: #303133; line-height: 1.2; }
.stat-label { color: #909399; margin-top: 2px; font-size: 13px; }

/* 进度条区域 */
.progress-section {
  background: #f8fafc;
  padding: 16px 20px;
  border-radius: 12px;
}
.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

/* 封面上传样式 */
.cover-upload-wrapper {
  width: 100%;
}

.cover-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.cover-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.cover-preview {
  width: 200px;
  height: 112px;
  position: relative;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.cover-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 8px;
}

.cover-preview:hover .cover-actions {
  opacity: 1;
}

.cover-actions .el-icon {
  font-size: 24px;
  color: #fff;
  cursor: pointer;
}

.cover-placeholder {
  width: 200px;
  height: 112px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  background: #fafafa;
  border-radius: 8px;
}

.cover-placeholder .el-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.cover-placeholder span {
  font-size: 12px;
}

.cover-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>
