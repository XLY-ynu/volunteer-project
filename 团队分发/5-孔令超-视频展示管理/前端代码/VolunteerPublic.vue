<!--
 * @Author: 孔令超
 * @Module: 活动参与 - 志愿者公开页面
 * @Description: 志愿者管理页面，支持志愿者注册信息管理和审核
-->
<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>志愿者管理</h3>
          <span class="subtitle">管理志愿者注册信息</span>
        </div>
        <el-button type="primary" @click="openRegister">
          <el-icon><Plus /></el-icon>
          新增志愿者
        </el-button>
      </div>
    </el-card>

    <!-- 状态统计 -->
    <el-row :gutter="16" class="status-row">
      <el-col :xs="12" :sm="6">
        <el-card class="status-card total" shadow="hover">
          <div class="status-icon"><el-icon><User /></el-icon></div>
          <div class="status-info">
            <div class="status-value">{{ statusCount.total }}</div>
            <div class="status-label">总人数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="status-card approved" shadow="hover">
          <div class="status-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="status-info">
            <div class="status-value">{{ statusCount.approved }}</div>
            <div class="status-label">已审核</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="status-card pending" shadow="hover">
          <div class="status-icon"><el-icon><Clock /></el-icon></div>
          <div class="status-info">
            <div class="status-value">{{ statusCount.pending }}</div>
            <div class="status-label">待审核</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true">
        <el-form-item label="姓名">
          <el-input v-model="filterName" placeholder="搜索姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="待审核" value="pending" />
            <el-option label="已审核" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 志愿者列表 -->
    <el-card class="content-card" shadow="never">
      <el-table :data="list" stripe>
        <el-table-column prop="userId" label="用户ID" width="80">
          <template #default="scope">{{ scope.row.userId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="电话" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="organization" label="组织" min-width="150">
          <template #default="scope">
            <span>{{ scope.row.organization || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170">
          <template #default="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button v-if="scope.row.status === 'pending'" size="small" type="success" @click="approve(scope.row)" title="审核通过">
                <el-icon><Check /></el-icon>
              </el-button>
              <el-button v-if="scope.row.status === 'pending'" size="small" type="warning" @click="reject(scope.row)" title="拒绝">
                <el-icon><Close /></el-icon>
              </el-button>
              <el-button size="small" @click="edit(scope.row)" title="编辑">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button size="small" type="danger" @click="remove(scope.row.id)" title="删除">
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
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑志愿者' : '新增志愿者'" width="500px">
      <el-form :model="form" label-width="80px" class="volunteer-form">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="电话" required>
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="组织">
          <el-input v-model="form.organization" placeholder="请输入所属组织" />
        </el-form-item>
        <el-form-item v-if="editingId" label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="待审核" value="pending" />
            <el-option label="已审核" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
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
import http from '../api/http';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, User, CircleCheck, Clock, Check, Close, Edit, Delete } from '@element-plus/icons-vue';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const filterName = ref('');
const filterStatus = ref('');
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const form = ref({ name: '', phone: '', email: '', organization: '', status: 'pending' });

const statusCount = computed(() => {
  const approved = list.value.filter(v => v.status === 'approved').length;
  const pending = list.value.filter(v => v.status === 'pending').length;
  return { total: total.value, approved, pending };
});

const load = async () => {
  const params: any = { page: page.value, size: size.value };
  if (filterName.value) params.name = filterName.value;
  if (filterStatus.value) params.status = filterStatus.value;
  const resp = await http.get('/volunteers', { params });
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};

const onPage = (p: number) => {
  page.value = p;
  load();
};

const getStatusType = (status: string) => {
  switch (status) {
    case 'approved': return 'success';
    case 'pending': return 'warning';
    case 'rejected': return 'danger';
    default: return 'info';
  }
};

const getStatusText = (status: string) => {
  switch (status) {
    case 'approved': return '已审核';
    case 'pending': return '待审核';
    case 'rejected': return '已拒绝';
    default: return status;
  }
};

const formatTime = (t: string) => t ? t.replace('T', ' ').substring(0, 16) : '-';

const openRegister = () => {
  editingId.value = null;
  form.value = { name: '', phone: '', email: '', organization: '', status: 'pending' };
  dialogVisible.value = true;
};

const edit = (row: any) => {
  editingId.value = row.id;
  form.value = { ...row };
  dialogVisible.value = true;
};

const submit = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入姓名');
    return;
  }
  if (!form.value.phone) {
    ElMessage.warning('请输入电话');
    return;
  }
  try {
    let res;
    if (editingId.value) {
      res = await http.put(`/volunteers/${editingId.value}`, form.value);
    } else {
      res = await http.post('/volunteers', form.value);
    }
    if (res.data.success) {
      ElMessage.success(editingId.value ? '已更新' : '已添加');
      dialogVisible.value = false;
      load();
    } else {
      ElMessage.error(res.data.message || '操作失败');
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败');
  }
};

const approve = async (row: any) => {
  await http.put(`/volunteers/${row.id}`, { ...row, status: 'approved' });
  ElMessage.success('已审核通过');
  load();
};

const reject = async (row: any) => {
  await ElMessageBox.confirm('确定拒绝该志愿者？', '提示', { type: 'warning' });
  await http.put(`/volunteers/${row.id}`, { ...row, status: 'rejected' });
  ElMessage.success('已拒绝');
  load();
};

const remove = async (id: number) => {
  await ElMessageBox.confirm('确定删除该志愿者？', '提示', { type: 'warning' });
  await http.delete(`/volunteers/${id}`);
  ElMessage.success('已删除');
  load();
};

onMounted(load);
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
.status-card.approved .status-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.status-card.pending .status-icon { background: linear-gradient(135deg, #e6a23c, #f0c78a); }
.status-value { font-size: 28px; font-weight: 700; }
.status-label { font-size: 14px; color: #909399; }

.filter-card :deep(.el-form-item) { margin-bottom: 0; margin-right: 12px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
.volunteer-form { padding: 0 20px; }
</style>
