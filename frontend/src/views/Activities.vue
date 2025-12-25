<template>
  <div>
    <div class="header">
      <h2>活动管理</h2>
      <el-button type="primary" @click="onCreate">新增活动</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="location" label="地点" />
      <el-table-column prop="startTime" label="开始" width="160" />
      <el-table-column prop="endTime" label="结束" width="160" />
      <el-table-column prop="checkinCode" label="签到码" width="120">
        <template #default="scope">
          <el-tag>{{ scope.row.checkinCode || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300">
        <template #default="scope">
          <el-button size="small" @click="edit(scope.row)">编辑</el-button>
          <el-button size="small" type="primary" @click="viewSignups(scope.row.id)">报名/统计</el-button>
          <el-button size="small" @click="copyCode(scope.row.checkinCode)">复制签到码</el-button>
          <el-button size="small" type="danger" @click="remove(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="onPage" />
    </div>

    <el-dialog v-model="dialogVisible" title="活动" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="开始">
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束">
          <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="人数">
          <el-input-number v-model="form.capacity" :min="0" />
        </el-form-item>
        <el-form-item label="签到码">
          <el-input v-model="form.checkinCode" placeholder="可留空自动生成" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="form.description" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="signupDialog" title="报名列表/统计" width="640px">
      <p>报名：{{ stats.total }} 人，已签到：{{ stats.checkedIn }} 人，签到码：{{ stats.checkinCode || '-' }}</p>
      <p>签到入口：<a :href="checkinUrl" target="_blank">{{ checkinUrl }}</a></p>
      <el-progress :percentage="progressPercent" :stroke-width="16" :text-inside="true" />
      <div class="signup-actions">
        <el-button size="small" @click="copyCode(stats.checkinCode)">复制签到码</el-button>
        <el-button size="small" @click="showQr">签到二维码</el-button>
        <el-button size="small" type="primary" @click="exportCsv(currentActivityId)">导出报名</el-button>
      </div>
      <el-table :data="signups">
        <el-table-column prop="volunteerId" label="志愿者ID" width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="createdAt" label="报名时间" />
        <el-table-column prop="checkinTime" label="签到时间" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="qrDialog" title="签到二维码" width="320px">
      <div class="qr-wrapper">
        <img v-if="qrImage" :src="qrImage" alt="签到二维码" />
        <p class="sub">志愿者扫描后直接进入签到页</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import QRCode from 'qrcode';
import http from '../api/http';
import { ElMessage } from 'element-plus';
import { fetchActivityStats } from '../api';

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

const onPage = (p: number) => {
  page.value = p;
  load();
};

const onCreate = () => {
  editingId.value = null;
  form.value = { title: '', description: '', location: '', startTime: '', endTime: '', capacity: 0, checkinCode: '' };
  dialogVisible.value = true;
};

const edit = (row: any) => {
  editingId.value = row.id;
  form.value = { title: row.title, description: row.description, location: row.location, startTime: row.startTime, endTime: row.endTime, capacity: row.capacity, checkinCode: row.checkinCode };
  dialogVisible.value = true;
};

const submit = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入标题');
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

const copyCode = (code?: string) => {
  if (!code) {
    ElMessage.info('暂无签到码');
    return;
  }
  navigator.clipboard.writeText(code);
  ElMessage.success('已复制');
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
  qrImage.value = await QRCode.toDataURL(data);
  qrDialog.value = true;
};

onMounted(load);
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pager {
  margin-top: 10px;
  text-align: right;
}
.signup-actions {
  margin: 8px 0;
  display: flex;
  gap: 8px;
}
.qr-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.qr-wrapper img {
  width: 220px;
  height: 220px;
}
</style>
