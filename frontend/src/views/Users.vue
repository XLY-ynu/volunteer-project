<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>用户管理</h3>
          <span class="subtitle">管理系统用户账号</span>
        </div>
        <el-button type="primary" @click="onCreate">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>
    </el-card>

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
import { Plus, User, CircleCheck, CircleClose, Edit, Delete } from '@element-plus/icons-vue';

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

onMounted(() => {
  load();
  loadRoles();
});
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .filter-card, .content-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; }

.status-row { margin-bottom: 0; }
.status-card { border-radius: 12px; }
.status-card :deep(.el-card__body) { display: flex; align-items: center; gap: 16px; padding: 20px; }
.status-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #fff; }
.status-card.total .status-icon { background: linear-gradient(135deg, #409eff, #66b1ff); }
.status-card.enabled .status-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.status-card.disabled .status-icon { background: linear-gradient(135deg, #909399, #b1b3b8); }
.status-value { font-size: 28px; font-weight: 700; }
.status-label { font-size: 14px; color: #909399; }

.filter-card :deep(.el-form-item) { margin-bottom: 0; margin-right: 12px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }

.user-cell { display: flex; align-items: center; gap: 8px; }
.user-avatar { background: linear-gradient(135deg, #409eff, #66b1ff); font-size: 14px; }
.current-tag { margin-left: 4px; }

.user-form { padding: 0 20px; }
.switch-tip { margin-left: 12px; font-size: 12px; color: #e6a23c; }
</style>
