<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>活动管理</h3>
          <span class="subtitle">创建和管理志愿者活动</span>
        </div>
        <el-button type="primary" @click="onCreate">
          <el-icon><Plus /></el-icon>
          新增活动
        </el-button>
      </div>
    </el-card>

    <!-- 活动列表 -->
    <el-card class="content-card" shadow="never">
      <el-table :data="list" stripe>
        <el-table-column prop="title" label="活动名称" min-width="180">
          <template #default="scope">
            <div class="activity-title">{{ scope.row.title }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="150" />
        <el-table-column label="时间" width="200">
          <template #default="scope">
            <div class="time-info">
              <div>{{ formatDate(scope.row.startTime) }}</div>
              <div class="time-to">至</div>
              <div>{{ formatDate(scope.row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="人数上限" width="100" align="center" />
        <el-table-column label="签到码" width="140">
          <template #default="scope">
            <div class="checkin-code" v-if="scope.row.checkinCode">
              <el-tag type="success" effect="dark" size="large">
                {{ scope.row.checkinCode }}
              </el-tag>
              <el-button link type="primary" size="small" @click="copyCode(scope.row.checkinCode)">
                <el-icon><CopyDocument /></el-icon>
              </el-button>
            </div>
            <span v-else class="no-code">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" type="primary" @click="viewSignups(scope.row.id)">
                <el-icon><DataAnalysis /></el-icon>
                统计
              </el-button>
              <el-button size="small" type="warning" @click="viewReminderLogs(scope.row.id)">
                <el-icon><Bell /></el-icon>
                提醒日志
              </el-button>
              <el-button size="small" @click="edit(scope.row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="remove(scope.row.id)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          :total="total"
          :page-size="size"
          layout="total, prev, pager, next"
          @current-change="load"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑活动' : '新增活动'" width="600px">
      <el-form :model="form" label-width="100px" class="activity-form">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.title" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动地点">
          <el-input v-model="form.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="人数上限">
              <el-input-number v-model="form.capacity" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签到码">
              <el-input v-model="form.checkinCode" placeholder="留空自动生成" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动描述">
          <el-input type="textarea" v-model="form.description" :rows="4" placeholder="请输入活动描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 报名统计弹窗 -->
    <el-dialog v-model="signupDialog" title="报名统计" width="700px">
      <div class="stats-header">
        <div class="stats-cards">
          <div class="stats-card">
            <div class="stats-value">{{ stats.total }}</div>
            <div class="stats-label">报名人数</div>
          </div>
          <div class="stats-card success">
            <div class="stats-value">{{ stats.checkedIn }}</div>
            <div class="stats-label">已签到</div>
          </div>
          <div class="stats-card warning">
            <div class="stats-value">{{ stats.total - stats.checkedIn }}</div>
            <div class="stats-label">未签到</div>
          </div>
        </div>
        <el-progress
          :percentage="progressPercent"
          :stroke-width="20"
          :text-inside="true"
          :format="() => `签到率 ${progressPercent}%`"
          style="margin: 16px 0"
        />
      </div>

      <el-divider />

      <div class="checkin-info">
        <div class="info-row">
          <span class="info-label">签到码：</span>
          <el-tag type="success" effect="dark" size="large">{{ stats.checkinCode || '-' }}</el-tag>
          <el-button link type="primary" @click="copyCode(stats.checkinCode)">复制</el-button>
        </div>
        <div class="info-row">
          <span class="info-label">签到链接：</span>
          <el-link type="primary" :href="checkinUrl" target="_blank">{{ checkinUrl }}</el-link>
        </div>
        <div class="action-row">
          <el-button type="primary" @click="showQr">
            <el-icon><Iphone /></el-icon>
            显示二维码
          </el-button>
          <el-button @click="exportCsv(currentActivityId)">
            <el-icon><Download /></el-icon>
            导出报名
          </el-button>
        </div>
      </div>

      <el-divider />

      <el-table :data="signups" max-height="300">
        <el-table-column prop="volunteerName" label="姓名" min-width="100">
          <template #default="scope">
            {{ scope.row.volunteerName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="volunteerPhone" label="电话" min-width="120">
          <template #default="scope">
            {{ scope.row.volunteerPhone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="volunteerOrganization" label="组织" min-width="120">
          <template #default="scope">
            {{ scope.row.volunteerOrganization || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'checked_in' ? 'success' : 'info'" size="small">
              {{ scope.row.status === 'checked_in' ? '已签到' : '已报名' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="报名时间" width="150">
          <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="checkinTime" label="签到时间" width="150">
          <template #default="scope">{{ formatDate(scope.row.checkinTime) || '-' }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 提醒日志弹窗 -->
    <el-dialog v-model="reminderDialog" title="提醒日志" width="760px">
      <div class="reminder-filter">
        <el-select v-model="reminderStatus" placeholder="状态" size="small" style="width: 120px" @change="loadReminderLogs">
          <el-option label="全部" value="" />
          <el-option label="sent" value="sent" />
          <el-option label="failed" value="failed" />
          <el-option label="abandoned" value="abandoned" />
        </el-select>
        <el-select v-model="reminderType" placeholder="类型" size="small" style="width: 120px" @change="loadReminderLogs">
          <el-option label="全部" value="" />
          <el-option label="checkin" value="checkin" />
        </el-select>
        <el-button size="small" @click="loadReminderLogs">刷新</el-button>
      </div>
      <el-table :data="reminderLogs" size="small">
        <el-table-column prop="createdAt" label="时间" width="160">
          <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="volunteerName" label="姓名" width="100" />
        <el-table-column prop="volunteerPhone" label="手机号" width="140" />
        <el-table-column prop="channel" label="通道" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag size="small" :type="reminderStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="内容" min-width="200" />
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="reminderPage"
          :total="reminderTotal"
          :page-size="reminderSize"
          layout="total, prev, pager, next"
          @current-change="loadReminderLogs"
        />
      </div>
    </el-dialog>

    <!-- 二维码弹窗 -->
    <el-dialog v-model="qrDialog" title="签到二维码" width="360px">
      <div class="qr-wrapper">
        <img v-if="qrImage" :src="qrImage" alt="签到二维码" class="qr-image" />
        <p class="qr-tip">志愿者扫描二维码即可进入签到页面</p>
        <el-button type="primary" @click="downloadQr">下载二维码</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import QRCode from 'qrcode';
import http from '../api/http';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchActivityStats, fetchReminderLogs } from '../api';
import { Plus, CopyDocument, DataAnalysis, Edit, Delete, Iphone, Download, Bell } from '@element-plus/icons-vue';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const dialogVisible = ref(false);
const signupDialog = ref(false);
const signups = ref<any[]>([]);
const stats = ref<{ total: number; checkedIn: number; checkinCode?: string }>({ total: 0, checkedIn: 0 });
const currentActivityId = ref<number | null>(null);
const editingId = ref<number | null>(null);
const form = ref({ title: '', description: '', location: '', startTime: '', endTime: '', capacity: 0, checkinCode: '' });
const qrDialog = ref(false);
const qrImage = ref('');
const checkinUrl = ref('');
const reminderDialog = ref(false);
const reminderLogs = ref<any[]>([]);
const reminderPage = ref(1);
const reminderSize = ref(10);
const reminderTotal = ref(0);
const reminderStatus = ref('');
const reminderType = ref('');
const reminderActivityId = ref<number | null>(null);

const progressPercent = computed(() => {
  if (!stats.value.total) return 0;
  return Math.round(((stats.value.checkedIn || 0) / stats.value.total) * 100);
});

const load = async () => {
  const resp = await http.get('/activities', { params: { page: page.value, size: size.value } });
  // @ts-ignore
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};

const onCreate = () => {
  editingId.value = null;
  form.value = { title: '', description: '', location: '', startTime: '', endTime: '', capacity: 0, checkinCode: '' };
  dialogVisible.value = true;
};

const edit = (row: any) => {
  editingId.value = row.id;
  form.value = { ...row };
  dialogVisible.value = true;
};

const submit = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入活动名称');
    return;
  }
  if (editingId.value) {
    await http.put(`/activities/${editingId.value}`, form.value);
    ElMessage.success('已更新');
  } else {
    await http.post('/activities', form.value);
    ElMessage.success('已创建');
  }
  dialogVisible.value = false;
  load();
};

const remove = async (id: number) => {
  await ElMessageBox.confirm('确定要删除这个活动吗？', '提示', { type: 'warning' });
  await http.delete(`/activities/${id}`);
  ElMessage.success('已删除');
  load();
};

const viewSignups = async (id: number) => {
  currentActivityId.value = id;
  const resp = await http.get(`/activities/${id}/signups`, { params: { page: 1, size: 100 } });
  // @ts-ignore
  signups.value = resp.data?.data?.records || [];
  const statResp = await fetchActivityStats(id);
  // @ts-ignore
  stats.value = statResp.data?.data || { total: 0, checkedIn: 0 };
  checkinUrl.value = `${window.location.origin}/checkin?activityId=${id}&code=${stats.value.checkinCode || ''}`;
  signupDialog.value = true;
};

const viewReminderLogs = (id: number) => {
  reminderActivityId.value = id;
  reminderPage.value = 1;
  reminderDialog.value = true;
  loadReminderLogs();
};

const loadReminderLogs = async () => {
  if (!reminderActivityId.value) return;
  const resp = await fetchReminderLogs(reminderPage.value, reminderSize.value, reminderActivityId.value, reminderStatus.value || undefined, reminderType.value || undefined);
  const data = resp.data?.data || {};
  reminderLogs.value = data.records || [];
  reminderTotal.value = data.total || 0;
};

const reminderStatusType = (status: string) => {
  if (status === 'sent') return 'success';
  if (status === 'failed') return 'danger';
  if (status === 'abandoned') return 'warning';
  return 'info';
};

const copyCode = (code?: string) => {
  if (!code) {
    ElMessage.info('暂无签到码');
    return;
  }
  navigator.clipboard.writeText(code);
  ElMessage.success('已复制到剪贴板');
};

const exportCsv = async (id: number | null) => {
  if (!id) return;
  const resp = await fetch(`/api/activities/${id}/signups/export`, {
    headers: { Authorization: `Bearer ${localStorage.getItem('token') || ''}` }
  });
  const blob = await resp.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `activity-${id}-signups.csv`;
  a.click();
  URL.revokeObjectURL(url);
};

const showQr = async () => {
  if (!currentActivityId.value || !stats.value.checkinCode) {
    ElMessage.info('请选择活动');
    return;
  }
  const data = `${window.location.origin}/checkin?activityId=${currentActivityId.value}&code=${stats.value.checkinCode}`;
  qrImage.value = await QRCode.toDataURL(data, { width: 280, margin: 2 });
  qrDialog.value = true;
};

const downloadQr = () => {
  if (!qrImage.value) return;
  const a = document.createElement('a');
  a.href = qrImage.value;
  a.download = `checkin-qr-${currentActivityId.value}.png`;
  a.click();
};

const formatDate = (date: string) => {
  if (!date) return '';
  return date.replace('T', ' ').substring(0, 16);
};

onMounted(load);
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  border-radius: 12px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.subtitle {
  font-size: 13px;
  color: #909399;
}

.content-card {
  border-radius: 12px;
}

.activity-title {
  font-weight: 600;
  color: #303133;
}

.time-info {
  font-size: 13px;
  color: #606266;
}

.time-to {
  color: #909399;
  font-size: 12px;
}

.checkin-code {
  display: flex;
  align-items: center;
  gap: 8px;
}

.no-code {
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.activity-form {
  padding: 0 20px;
}

.stats-header {
  padding: 0 16px;
}

.stats-cards {
  display: flex;
  gap: 20px;
}

.stats-card {
  flex: 1;
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stats-card.success {
  background: #f0f9eb;
}

.stats-card.warning {
  background: #fdf6ec;
}

.stats-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
}

.stats-card.success .stats-value {
  color: #67c23a;
}

.stats-card.warning .stats-value {
  color: #e6a23c;
}

.stats-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.checkin-info {
  padding: 0 16px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.info-label {
  color: #606266;
  min-width: 70px;
}

.action-row {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.qr-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.qr-image {
  width: 240px;
  height: 240px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.qr-tip {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.reminder-filter {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}
</style>
