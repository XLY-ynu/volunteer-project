<!--
 * @Author: 曹宇涵
 * @Module: 活动管理（管理员端）
 * @Description: 活动管理页面，创建和管理志愿者活动
-->
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

    <!-- 活动列表 - 卡片式展示 -->
    <div class="activity-grid" v-if="list.length">
      <el-card v-for="item in list" :key="item.id" class="activity-card" shadow="hover">
        <div class="card-header">
          <div class="activity-title">{{ item.title }}</div>
          <el-tag v-if="getActivityStatus(item) === 'ongoing'" type="success" size="small">进行中</el-tag>
          <el-tag v-else-if="getActivityStatus(item) === 'upcoming'" type="warning" size="small">未开始</el-tag>
          <el-tag v-else type="info" size="small">已结束</el-tag>
        </div>
        
        <div class="card-body">
          <div class="info-item">
            <el-icon><Location /></el-icon>
            <span>{{ item.location || '未设置地点' }}</span>
          </div>
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDate(item.startTime) }} ~ {{ formatDate(item.endTime) }}</span>
          </div>
          <div class="info-item">
            <el-icon><User /></el-icon>
            <span>人数上限: {{ item.capacity || '不限' }}</span>
          </div>
        </div>

        <div class="card-footer">
          <div class="checkin-code" v-if="item.checkinCode">
            <el-tag type="success" effect="dark">{{ item.checkinCode }}</el-tag>
            <el-button link type="primary" size="small" @click="copyCode(item.checkinCode)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
          </div>
          <div class="action-buttons">
            <el-button-group>
              <el-button size="small" type="primary" @click="viewSignups(item.id)" title="统计">
                <el-icon><DataAnalysis /></el-icon>
              </el-button>
              <el-button size="small" @click="edit(item)" title="编辑">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button size="small" type="danger" @click="remove(item.id)" title="删除">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-button-group>
          </div>
        </div>
      </el-card>
    </div>
    <el-card v-else class="content-card" shadow="never">
      <el-empty description="暂无活动，点击上方按钮创建" />
    </el-card>

    <div class="pagination-wrapper" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>

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
                value-format="YYYY-MM-DD HH:mm:ss"
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
                value-format="YYYY-MM-DD HH:mm:ss"
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
          <el-button type="success" @click="sendReminder(currentActivityId)">
            <el-icon><Message /></el-icon>
            发送提醒
          </el-button>
          <el-button type="warning" @click="viewReminderLogs(currentActivityId)">
            <el-icon><Bell /></el-icon>
            提醒日志
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
    <el-dialog v-model="reminderDialog" title="提醒日志" width="900px">
      <div class="reminder-filter">
        <el-select v-model="reminderStatus" placeholder="状态" size="small" style="width: 120px" @change="loadReminderLogs">
          <el-option label="全部" value="" />
          <el-option label="已发送" value="sent" />
          <el-option label="失败" value="failed" />
          <el-option label="已放弃" value="abandoned" />
        </el-select>
        <el-select v-model="reminderType" placeholder="类型" size="small" style="width: 120px" @change="loadReminderLogs">
          <el-option label="全部" value="" />
          <el-option label="签到提醒" value="checkin" />
          <el-option label="报名确认" value="signup" />
          <el-option label="自定义" value="custom" />
        </el-select>
        <el-button size="small" @click="loadReminderLogs">刷新</el-button>
      </div>
      <el-table :data="reminderLogs" size="small">
        <el-table-column prop="createdAt" label="时间" width="150">
          <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="volunteerName" label="姓名" width="90" />
        <el-table-column prop="volunteerPhone" label="手机号" width="120" />
        <el-table-column prop="reminderType" label="类型" width="100">
          <template #default="scope">
            <el-tag size="small" :type="getReminderTypeTag(scope.row.reminderType)">
              {{ getReminderTypeLabel(scope.row.reminderType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="channel" label="通道" width="90" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag size="small" :type="reminderStatusType(scope.row.status)">
              {{ getReminderStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="内容" min-width="200" show-overflow-tooltip />
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

    <!-- 发送提醒弹窗 -->
    <el-dialog v-model="reminderSendDialog" title="发送提醒" width="550px">
      <el-form :model="reminderForm" label-width="100px">
        <el-form-item label="提醒类型">
          <el-radio-group v-model="reminderForm.type">
            <el-radio label="checkin">签到提醒</el-radio>
            <el-radio label="signup">报名确认</el-radio>
            <el-radio label="custom">自定义</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发送对象">
          <el-radio-group v-model="reminderForm.targetStatus">
            <el-radio label="all">全部报名者</el-radio>
            <el-radio label="applied">仅未签到</el-radio>
            <el-radio label="checked_in">仅已签到</el-radio>
            <el-radio label="selected">指定人员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="reminderForm.targetStatus === 'selected'" label="选择人员">
          <el-select v-model="reminderForm.volunteerIds" multiple placeholder="选择志愿者" style="width: 100%">
            <el-option 
              v-for="s in signups" 
              :key="s.volunteerId" 
              :label="`${s.volunteerName || '未知'} (${s.volunteerPhone || '-'})`"
              :value="s.volunteerId"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="reminderForm.type === 'custom'" label="消息内容">
          <el-input 
            v-model="reminderForm.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入自定义消息内容"
          />
        </el-form-item>
        <el-form-item label="预览">
          <div class="reminder-preview">
            <div class="preview-title">{{ previewTitle }}</div>
            <div class="preview-content">{{ previewContent }}</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reminderSendDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmSendReminder" :loading="reminderSending">
          发送 ({{ targetCount }}人)
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import QRCode from 'qrcode';
import http from '../api/http';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchActivityStats, fetchReminderLogs } from '../api';
import { Plus, CopyDocument, DataAnalysis, Edit, Delete, Iphone, Download, Bell, Location, Calendar, User, Message } from '@element-plus/icons-vue';

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
  // 验证开始时间和结束时间
  if (form.value.startTime && form.value.endTime) {
    const start = new Date(form.value.startTime);
    const end = new Date(form.value.endTime);
    if (end <= start) {
      ElMessage.warning('结束时间必须晚于开始时间');
      return;
    }
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

const viewReminderLogs = (id: number | null) => {
  if (!id) return;
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

const getReminderTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    'checkin': '签到提醒',
    'signup': '报名确认',
    'custom': '自定义'
  };
  return map[type] || type || '提醒';
};

const getReminderTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'checkin': 'primary',
    'signup': 'success',
    'custom': 'warning'
  };
  return map[type] || 'info';
};

const getReminderStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    'sent': '已发送',
    'failed': '失败',
    'abandoned': '已放弃'
  };
  return map[status] || status || '未知';
};

// 发送提醒相关
const reminderSendDialog = ref(false);
const reminderSending = ref(false);
const reminderForm = ref({
  type: 'checkin',
  targetStatus: 'all',
  volunteerIds: [] as number[],
  content: ''
});
const currentActivityForReminder = ref<any>(null);

const targetCount = computed(() => {
  if (reminderForm.value.targetStatus === 'selected') {
    return reminderForm.value.volunteerIds.length;
  }
  if (reminderForm.value.targetStatus === 'applied') {
    return signups.value.filter(s => s.status !== 'checked_in').length;
  }
  if (reminderForm.value.targetStatus === 'checked_in') {
    return signups.value.filter(s => s.status === 'checked_in').length;
  }
  return signups.value.length;
});

const previewTitle = computed(() => {
  const activity = currentActivityForReminder.value;
  if (!activity) return '';
  if (reminderForm.value.type === 'signup') return `报名确认 · ${activity.title}`;
  if (reminderForm.value.type === 'custom') return `活动通知 · ${activity.title}`;
  return `签到提醒 · ${activity.title}`;
});

const previewContent = computed(() => {
  const activity = currentActivityForReminder.value;
  if (!activity) return '';
  const startTime = activity.startTime ? activity.startTime.replace('T', ' ').substring(0, 16) : '待定';
  const location = activity.location || '待定';
  
  if (reminderForm.value.type === 'custom' && reminderForm.value.content) {
    return reminderForm.value.content;
  }
  if (reminderForm.value.type === 'signup') {
    return `您已成功报名活动【${activity.title}】，活动时间：${startTime}，地点：${location}，请准时参加！`;
  }
  return `您报名的活动【${activity.title}】即将开始，时间：${startTime}，地点：${location}，请准时到场签到！签到码：${activity.checkinCode || '现场获取'}`;
});

const sendReminder = async (id: number | null) => {
  if (!id) return;
  // 先获取活动信息
  const activity = list.value.find(a => a.id === id);
  currentActivityForReminder.value = activity;
  // 重置表单
  reminderForm.value = {
    type: 'checkin',
    targetStatus: 'all',
    volunteerIds: [],
    content: ''
  };
  reminderSendDialog.value = true;
};

const confirmSendReminder = async () => {
  if (!currentActivityId.value) return;
  if (targetCount.value === 0) {
    ElMessage.warning('没有符合条件的发送对象');
    return;
  }
  
  reminderSending.value = true;
  try {
    const payload: any = {
      type: reminderForm.value.type,
      targetStatus: reminderForm.value.targetStatus === 'selected' ? 'all' : reminderForm.value.targetStatus
    };
    if (reminderForm.value.targetStatus === 'selected') {
      payload.volunteerIds = reminderForm.value.volunteerIds;
    }
    if (reminderForm.value.type === 'custom' && reminderForm.value.content) {
      payload.content = reminderForm.value.content;
    }
    
    const resp = await http.post(`/activities/${currentActivityId.value}/send-reminder`, payload);
    // @ts-ignore
    const data = resp.data?.data || {};
    ElMessage.success(data.message || '发送成功');
    reminderSendDialog.value = false;
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '发送失败');
  } finally {
    reminderSending.value = false;
  }
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
    headers: { Authorization: `Bearer ${localStorage.getItem('admin_token') || ''}` }
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

const getActivityStatus = (item: any) => {
  const now = new Date();
  const start = item.startTime ? new Date(item.startTime) : null;
  const end = item.endTime ? new Date(item.endTime) : null;
  if (end && now > end) return 'ended';
  if (start && now < start) return 'upcoming';
  return 'ongoing';
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

/* 活动卡片网格 */
.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.activity-card {
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

.activity-card :deep(.el-card__body) {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.activity-title {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
  flex: 1;
  margin-right: 8px;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}

.info-item .el-icon {
  color: #909399;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.checkin-code {
  display: flex;
  align-items: center;
  gap: 4px;
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

.reminder-preview {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  width: 100%;
}

.preview-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.preview-content {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
}
</style>
