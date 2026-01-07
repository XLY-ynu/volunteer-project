<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in cards" :key="card.title">
        <el-card class="stat-card" :class="card.type" shadow="hover">
          <div class="stat-icon">
            <component :is="card.icon" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-title">{{ card.title }}</div>
            <div class="stat-desc">{{ card.desc }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" class="quick-actions">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="action-buttons">
            <el-button type="primary" @click="$router.push('/users')">
              <el-icon><UserFilled /></el-icon>
              用户管理
            </el-button>
            <el-button type="success" @click="$router.push('/ops')">
              <el-icon><List /></el-icon>
              操作日志
            </el-button>
            <el-button type="warning" @click="$router.push('/system')">
              <el-icon><Setting /></el-icon>
              系统设置
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统状态 -->
    <el-row :gutter="20">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="status-card">
          <template #header>
            <div class="card-header">
              <span>用户角色分布</span>
            </div>
          </template>
          <div class="role-stats">
            <div class="role-item">
              <div class="role-icon admin">👑</div>
              <span class="role-label">管理员</span>
              <span class="role-count">{{ roleStats.admin }}</span>
            </div>
            <div class="role-item">
              <div class="role-icon org">🏢</div>
              <span class="role-label">志愿者组织</span>
              <span class="role-count">{{ roleStats.org }}</span>
            </div>
            <div class="role-item">
              <div class="role-icon volunteer">🤝</div>
              <span class="role-label">志愿者</span>
              <span class="role-count">{{ roleStats.volunteer }}</span>
            </div>
            <div class="role-item">
              <div class="role-icon user">👤</div>
              <span class="role-label">普通用户</span>
              <span class="role-count">{{ roleStats.user }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="status-card">
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <div class="system-info">
            <div class="info-item">
              <span class="info-label">系统版本</span>
              <span class="info-value">v1.0.0</span>
            </div>
            <div class="info-item">
              <span class="info-label">当前用户</span>
              <span class="info-value">{{ userStore.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">用户角色</span>
              <span class="info-value">系统管理员</span>
            </div>
            <div class="info-item">
              <span class="info-label">登录时间</span>
              <span class="info-value">{{ loginTime }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近操作日志 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近操作日志</span>
              <el-button text type="primary" @click="$router.push('/ops')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentLogs" size="small">
            <el-table-column prop="username" label="用户" width="120" />
            <el-table-column prop="method" label="方法" width="80" />
            <el-table-column prop="path" label="路径" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status < 400 ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="180">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, markRaw } from 'vue';
import { useUserStore } from '../stores/user';
import { UserFilled, List, Setting, User } from '@element-plus/icons-vue';
import axios from 'axios';

const userStore = useUserStore();
const loginTime = ref(new Date().toLocaleString());

const cards = ref([
  { title: '总用户数', value: 0, desc: '系统注册用户', type: 'primary', icon: markRaw(UserFilled) },
  { title: '志愿者组织', value: 0, desc: '注册的组织', type: 'success', icon: markRaw(UserFilled) },
  { title: '志愿者', value: 0, desc: '注册志愿者', type: 'warning', icon: markRaw(User) },
  { title: '普通用户', value: 0, desc: '普通用户数', type: 'info', icon: markRaw(User) }
]);

const roleStats = ref({ admin: 0, org: 0, volunteer: 0, user: 0 });
const recentLogs = ref<any[]>([]);

const token = localStorage.getItem('token');
const headers = { Authorization: `Bearer ${token}` };

const load = async () => {
  try {
    // 获取用户统计 - API返回分页数据，用户列表在 data.records 中
    const usersResp = await axios.get('/api/users?page=1&size=1000', { headers });
    const users = usersResp.data?.data?.records || usersResp.data?.data || [];
    
    const adminCount = users.filter((u: any) => u.roleCode === 'ADMIN').length;
    const orgCount = users.filter((u: any) => u.roleCode === 'ORG').length;
    const volunteerCount = users.filter((u: any) => u.roleCode === 'VOLUNTEER').length;
    const userCount = users.filter((u: any) => u.roleCode === 'USER').length;
    
    roleStats.value = { admin: adminCount, org: orgCount, volunteer: volunteerCount, user: userCount };
    
    cards.value = [
      { title: '总用户数', value: users.length, desc: '系统注册用户', type: 'primary', icon: markRaw(UserFilled) },
      { title: '志愿者组织', value: orgCount, desc: '注册的组织', type: 'success', icon: markRaw(UserFilled) },
      { title: '志愿者', value: volunteerCount, desc: '注册志愿者', type: 'warning', icon: markRaw(User) },
      { title: '普通用户', value: userCount, desc: '普通用户数', type: 'info', icon: markRaw(User) }
    ];
    
    // 获取最近操作日志
    const logsResp = await axios.get('/api/ops?size=5', { headers });
    recentLogs.value = logsResp.data?.data?.records || logsResp.data?.data || [];
  } catch (e) {
    console.error(e);
  }
};

const formatTime = (t: string) => t ? new Date(t).toLocaleString() : '';

onMounted(load);
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
}

.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 24px;
  gap: 20px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.stat-card.primary .stat-icon {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.stat-card.success .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.stat-card.warning .stat-icon {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
}

.stat-card.info .stat-icon {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-title {
  font-size: 16px;
  color: #606266;
  margin-top: 4px;
}

.stat-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.quick-actions {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  padding: 20px 24px;
  font-size: 14px;
}

.status-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.terminal-status {
  display: flex;
  gap: 40px;
}

.role-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.role-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.role-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.role-icon.admin { background: linear-gradient(135deg, #f56c6c, #f78989); }
.role-icon.org { background: linear-gradient(135deg, #409eff, #66b1ff); }
.role-icon.volunteer { background: linear-gradient(135deg, #67c23a, #85ce61); }
.role-icon.user { background: linear-gradient(135deg, #909399, #a6a9ad); }

.role-label {
  flex: 1;
  color: #606266;
}

.role-count {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.status-item.online .status-dot {
  background: #67c23a;
  box-shadow: 0 0 8px rgba(103, 194, 58, 0.5);
}

.status-item.offline .status-dot {
  background: #f56c6c;
  box-shadow: 0 0 8px rgba(245, 108, 108, 0.5);
}

.status-label {
  color: #606266;
}

.status-count {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.system-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #909399;
}

.info-value {
  color: #303133;
  font-weight: 500;
}
</style>
