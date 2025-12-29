<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>{{ activeTab === 'users' ? '用户管理' : '志愿者审核' }}</h3>
          <span class="subtitle">{{ activeTab === 'users' ? '管理系统用户账号' : '审核志愿者注册申请' }}</span>
        </div>
        <el-button v-if="activeTab === 'users'" type="primary" @click="onCreate">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>
    </el-card>

    <!-- Tab切换 -->
    <el-card class="tab-card" shadow="never">
      <el-radio-group v-model="activeTab" @change="onTabChange">
        <el-radio-button value="users">用户管理</el-radio-button>
        <el-radio-button value="volunteers">
          志愿者审核
          <el-badge v-if="pendingCount > 0" :value="pendingCount" class="pending-badge" />
        </el-radio-button>
      </el-radio-group>
    </el-card>

    <!-- 用户管理Tab -->
    <template v-if="activeTab === 'users'">
      <!-- 统计卡片 -->
      <el-row :gutter="16" class="status-row">
        <el-col :xs="12" :sm="6">
          <el-card class="status-card total" shadow="hover">
            <div class="status-icon"><el-icon><User /></el-icon></div>
            <div class="status-info">
              <div class="status-value">{{ total }}</div>
              <div class="status-label">总用户数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card class="status-card enabled" shadow="hover">
            <div class="status-icon"><el-icon><CircleCheck /></el-icon></div>
            <div class="status-info">
              <div class="status-value">{{ enabledCount }}</div>
              <div class="status-label">已启用</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card class="status-card disabled" shadow="hover">
            <div class="status-icon"><el-icon><CircleClose /></el-icon></div>
            <div class="status-info">
              <div class="status-value">{{ disabledCount }}</div>
              <div class="status-label">已禁用</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 筛选 -->
      <el-card class="filter-card" shadow="never">
        <el-form :inline="true">
          <el-form-item label="用户名">
            <el-input v-model="filter.username" placeholder="搜索用户名" clearable style="width: 180px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="load">查询</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 用户列表 -->
      <el-card class="content-card" shadow="never">
        <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="120">
          <template #default="scope">
            <div class="user-cell">
              <el-avatar :size="32" class="user-avatar">{{ scope.row.username?.charAt(0)?.toUpperCase() }}</el-avatar>
              <span>{{ scope.row.username }}</span>
              <el-tag v-if="scope.row.username === currentUser" type="info" size="small" class="current-tag">当前</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="120">
          <template #default="scope">
            <span>{{ scope.row.nickname || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="roleCode" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.roleCode === 'ADMIN' ? 'danger' : 'primary'" size="small">
              {{ getRoleName(scope.row.roleCode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'" size="small">
              {{ scope.row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" @click="edit(scope.row)" title="编辑">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button size="small" type="danger" @click="remove(scope.row)" title="删除" 
                         :disabled="scope.row.username === currentUser">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="onPage"
        />
      </div>
    </el-card>
    </template>

    <!-- 志愿者审核Tab -->
    <template v-if="activeTab === 'volunteers'">
      <!-- 统计卡片 -->
      <el-row :gutter="16" class="status-row">
        <el-col :xs="12" :sm="6">
          <el-card class="status-card pending" shadow="hover">
            <div class="status-icon"><el-icon><Clock /></el-icon></div>
            <div class="status-info">
              <div class="status-value">{{ pendingCount }}</div>
              <div class="status-label">待审核</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card class="status-card approved" shadow="hover">
            <div class="status-icon"><el-icon><Select /></el-icon></div>
            <div class="status-info">
              <div class="status-value">{{ approvedCount }}</div>
              <div class="status-label">已通过</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card class="status-card rejected" shadow="hover">
            <div class="status-icon"><el-icon><CloseBold /></el-icon></div>
            <div class="status-info">
              <div class="status-value">{{ rejectedCount }}</div>
              <div class="status-label">已拒绝</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 筛选 -->
      <el-card class="filter-card" shadow="never">
        <el-form :inline="true">
          <el-form-item label="姓名">
            <el-input v-model="volunteerFilter.name" placeholder="搜索姓名" clearable style="width: 150px" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="volunteerFilter.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="待审核" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadVolunteers">查询</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 志愿者列表 -->
      <el-card class="content-card" shadow="never">
        <el-table :data="volunteerList" stripe>
          <el-table-column prop="userId" label="用户ID" width="80">
            <template #default="scope">{{ scope.row.userId || '-' }}</template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" min-width="100">
            <template #default="scope">
              <div class="user-cell">
                <el-avatar :size="32" class="user-avatar volunteer-avatar">{{ scope.row.name?.charAt(0) }}</el-avatar>
                <span>{{ scope.row.name || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="email" label="邮箱" min-width="150">
            <template #default="scope">{{ scope.row.email || '-' }}</template>
          </el-table-column>
          <el-table-column prop="organization" label="组织" min-width="120">
            <template #default="scope">{{ scope.row.organization || '-' }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getVolunteerStatusType(scope.row.status)" size="small">
                {{ getVolunteerStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间" width="170">
            <template #default="scope">{{ formatTime(scope.row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <el-button-group>
                <el-button v-if="scope.row.status === 'pending'" size="small" type="success" @click="approveVolunteer(scope.row)" title="通过">
                  <el-icon><Select /></el-icon>
                </el-button>
                <el-button v-if="scope.row.status === 'pending'" size="small" type="danger" @click="rejectVolunteer(scope.row)" title="拒绝">
                  <el-icon><CloseBold /></el-icon>
                </el-button>
                <el-button size="small" @click="viewVolunteer(scope.row)" title="详情">
                  <el-icon><View /></el-icon>
                </el-button>
                <el-button size="small" type="danger" @click="removeVolunteer(scope.row)" title="删除">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            layout="total, prev, pager, next"
            :total="volunteerTotal"
            :page-size="volunteerSize"
            :current-page="volunteerPage"
            @current-change="onVolunteerPage"
          />
        </div>
      </el-card>
    </template>

    <!-- 志愿者详情弹窗 -->
    <el-dialog v-model="volunteerDetailVisible" title="志愿者详情" width="500px">
      <el-descriptions :column="1" border v-if="currentVolunteer">
        <el-descriptions-item label="用户ID">{{ currentVolunteer.userId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentVolunteer.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentVolunteer.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentVolunteer.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="组织">{{ currentVolunteer.organization || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getVolunteerStatusType(currentVolunteer.status)" size="small">
            {{ getVolunteerStatusText(currentVolunteer.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatTime(currentVolunteer.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(currentVolunteer.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="volunteerDetailVisible = false">关闭</el-button>
        <el-button v-if="currentVolunteer?.status === 'pending'" type="success" @click="approveVolunteer(currentVolunteer); volunteerDetailVisible = false">通过</el-button>
        <el-button v-if="currentVolunteer?.status === 'pending'" type="danger" @click="rejectVolunteer(currentVolunteer); volunteerDetailVisible = false">拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="80px" class="user-form">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="密码" :required="!editingId">
          <el-input v-model="form.password" type="password" show-password 
                    :placeholder="editingId ? '留空则不修改密码' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.roleCode" placeholder="选择角色" style="width: 100%">
            <el-option v-for="r in roles" :key="r.code" :label="r.name || r.code" :value="r.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用"
                     :disabled="editingId && form.username === currentUser" />
          <span v-if="editingId && form.username === currentUser" class="switch-tip">不能禁用自己</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import http from '../api/http';
import { Plus, User, CircleCheck, CircleClose, Edit, Delete, Clock, Select, CloseBold, View } from '@element-plus/icons-vue';

// Tab状态
const activeTab = ref('users');

// 用户管理相关
const list = ref<any[]>([]);
const page = ref(1);
const size = ref(20);
const total = ref(0);
const filter = ref({ username: '' });
const roles = ref<any[]>([]);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const form = ref({ username: '', password: '', nickname: '', roleCode: '', enabled: true });
const currentUser = ref(localStorage.getItem('username') || '');

const enabledCount = computed(() => list.value.filter(u => u.enabled).length);
const disabledCount = computed(() => list.value.filter(u => !u.enabled).length);

// 志愿者审核相关
const volunteerList = ref<any[]>([]);
const volunteerPage = ref(1);
const volunteerSize = ref(20);
const volunteerTotal = ref(0);
const volunteerFilter = ref({ name: '', status: '' });
const volunteerDetailVisible = ref(false);
const currentVolunteer = ref<any>(null);

const pendingCount = computed(() => volunteerList.value.filter(v => v.status === 'pending').length);
const approvedCount = computed(() => volunteerList.value.filter(v => v.status === 'approved').length);
const rejectedCount = computed(() => volunteerList.value.filter(v => v.status === 'rejected').length);

const load = async () => {
  const params: any = { page: page.value, size: size.value };
  if (filter.value.username) params.username = filter.value.username;
  const resp = await http.get('/users', { params });
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};

const loadRoles = async () => {
  const resp = await http.get('/users/roles');
  roles.value = resp.data?.data || [];
};

const getRoleName = (code: string) => {
  const role = roles.value.find(r => r.code === code);
  return role?.name || code;
};

const formatTime = (t: string) => t ? t.replace('T', ' ').substring(0, 16) : '-';

const onCreate = () => {
  editingId.value = null;
  form.value = { username: '', password: '', nickname: '', roleCode: 'VOLUNTEER', enabled: true };
  dialogVisible.value = true;
};

const edit = (row: any) => {
  editingId.value = row.id;
  form.value = { 
    username: row.username, 
    password: '', 
    nickname: row.nickname || '', 
    roleCode: row.roleCode, 
    enabled: row.enabled 
  };
  dialogVisible.value = true;
};

const submit = async () => {
  if (!form.value.username) {
    ElMessage.warning('请输入用户名');
    return;
  }
  if (!form.value.roleCode) {
    ElMessage.warning('请选择角色');
    return;
  }
  if (!editingId.value && !form.value.password) {
    ElMessage.warning('新增用户需填写密码');
    return;
  }
  
  try {
    let res;
    if (editingId.value) {
      res = await http.put(`/users/${editingId.value}`, form.value);
    } else {
      res = await http.post('/users', form.value);
    }
    if (res.data.success) {
      ElMessage.success(editingId.value ? '已更新' : '已创建');
      dialogVisible.value = false;
      load();
    } else {
      ElMessage.error(res.data.message || '操作失败');
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败');
  }
};

const remove = async (row: any) => {
  if (row.username === currentUser.value) {
    ElMessage.warning('不能删除自己的账号');
    return;
  }
  await ElMessageBox.confirm(`确定删除用户 "${row.username}" 吗？`, '提示', { type: 'warning' });
  try {
    const res = await http.delete(`/users/${row.id}`);
    if (res.data.success) {
      ElMessage.success('已删除');
      load();
    } else {
      ElMessage.error(res.data.message || '删除失败');
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '删除失败');
  }
};

const onPage = (p: number) => {
  page.value = p;
  load();
};

// Tab切换
const onTabChange = (tab: string) => {
  if (tab === 'volunteers') {
    loadVolunteers();
  } else {
    load();
  }
};

// 志愿者相关方法
const loadVolunteers = async () => {
  const params: any = { page: volunteerPage.value, size: volunteerSize.value };
  if (volunteerFilter.value.name) params.name = volunteerFilter.value.name;
  if (volunteerFilter.value.status) params.status = volunteerFilter.value.status;
  const resp = await http.get('/volunteers', { params });
  const data = resp.data?.data || {};
  volunteerList.value = data.records || [];
  volunteerTotal.value = data.total || 0;
};

const getVolunteerStatusType = (status: string) => {
  switch (status) {
    case 'pending': return 'warning';
    case 'approved': return 'success';
    case 'rejected': return 'danger';
    default: return 'info';
  }
};

const getVolunteerStatusText = (status: string) => {
  switch (status) {
    case 'pending': return '待审核';
    case 'approved': return '已通过';
    case 'rejected': return '已拒绝';
    default: return status || '-';
  }
};

const viewVolunteer = (row: any) => {
  currentVolunteer.value = row;
  volunteerDetailVisible.value = true;
};

const approveVolunteer = async (row: any) => {
  await ElMessageBox.confirm(`确定通过志愿者 "${row.name || row.phone}" 的申请吗？`, '审核确认', { type: 'warning' });
  try {
    const res = await http.put(`/volunteers/${row.id}`, { ...row, status: 'approved' });
    if (res.data.success) {
      ElMessage.success('已通过审核');
      loadVolunteers();
    } else {
      ElMessage.error(res.data.message || '操作失败');
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败');
  }
};

const rejectVolunteer = async (row: any) => {
  await ElMessageBox.confirm(`确定拒绝志愿者 "${row.name || row.phone}" 的申请吗？`, '审核确认', { type: 'warning' });
  try {
    const res = await http.put(`/volunteers/${row.id}`, { ...row, status: 'rejected' });
    if (res.data.success) {
      ElMessage.success('已拒绝申请');
      loadVolunteers();
    } else {
      ElMessage.error(res.data.message || '操作失败');
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败');
  }
};

const removeVolunteer = async (row: any) => {
  await ElMessageBox.confirm(`确定删除志愿者 "${row.name || row.phone}" 吗？`, '提示', { type: 'warning' });
  try {
    const res = await http.delete(`/volunteers/${row.id}`);
    if (res.data.success) {
      ElMessage.success('已删除');
      loadVolunteers();
    } else {
      ElMessage.error(res.data.message || '删除失败');
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '删除失败');
  }
};

const onVolunteerPage = (p: number) => {
  volunteerPage.value = p;
  loadVolunteers();
};

onMounted(() => {
  load();
  loadRoles();
  loadVolunteers(); // 预加载志愿者数据以显示待审核数量
});
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .filter-card, .content-card, .tab-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; }

.tab-card :deep(.el-card__body) { padding: 12px 20px; }
.pending-badge { margin-left: 6px; }
.pending-badge :deep(.el-badge__content) { top: -2px; }

.status-row { margin-bottom: 0; }
.status-card { border-radius: 12px; }
.status-card :deep(.el-card__body) { display: flex; align-items: center; gap: 16px; padding: 20px; }
.status-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #fff; }
.status-card.total .status-icon { background: linear-gradient(135deg, #409eff, #66b1ff); }
.status-card.enabled .status-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.status-card.disabled .status-icon { background: linear-gradient(135deg, #909399, #b1b3b8); }
.status-card.pending .status-icon { background: linear-gradient(135deg, #e6a23c, #f0c78a); }
.status-card.approved .status-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.status-card.rejected .status-icon { background: linear-gradient(135deg, #f56c6c, #f89898); }
.status-value { font-size: 28px; font-weight: 700; }
.status-label { font-size: 14px; color: #909399; }

.filter-card :deep(.el-form-item) { margin-bottom: 0; margin-right: 12px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }

.user-cell { display: flex; align-items: center; gap: 8px; }
.user-avatar { background: linear-gradient(135deg, #409eff, #66b1ff); font-size: 14px; }
.volunteer-avatar { background: linear-gradient(135deg, #67c23a, #85ce61); }
.current-tag { margin-left: 4px; }

.user-form { padding: 0 20px; }
.switch-tip { margin-left: 12px; font-size: 12px; color: #e6a23c; }
</style>
