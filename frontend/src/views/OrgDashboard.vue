<!--
 * @Author: 梁玉杰
 * @Module: 组织端仪表盘
 * @Description: 组织端工作台，展示志愿者数量、待审核申请、待处理求助等统计
-->
<template>
  <div class="dashboard">
    <h2>工作台</h2>
    
    <!-- 组织信息卡片 -->
    <el-card class="org-info-card" shadow="hover">
      <div class="org-info-header">
        <div class="org-avatar">{{ orgInfo.name?.charAt(0) || '组' }}</div>
        <div class="org-details">
          <h3>{{ orgInfo.name || '未设置组织名称' }}</h3>
          <p class="org-desc">{{ orgInfo.description || '暂无简介，点击编辑添加组织简介' }}</p>
        </div>
        <el-button type="primary" @click="openEditDialog">
          <el-icon><Edit /></el-icon>编辑信息
        </el-button>
      </div>
      <div class="org-contact" v-if="orgInfo.contactName || orgInfo.contactPhone">
        <span v-if="orgInfo.contactName">联系人：{{ orgInfo.contactName }}</span>
        <span v-if="orgInfo.contactPhone">电话：{{ orgInfo.contactPhone }}</span>
        <span v-if="orgInfo.address">地址：{{ orgInfo.address }}</span>
      </div>
    </el-card>
    
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon volunteer">👥</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.volunteerCount }}</div>
            <div class="stat-label">组织志愿者</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon pending">⏳</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingCount }}</div>
            <div class="stat-label">待审核申请</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon help">🆘</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.helpPendingCount }}</div>
            <div class="stat-label">待处理求助</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>待审核志愿者</span>
              <el-button type="primary" link @click="$router.push('/org/volunteers')">查看全部</el-button>
            </div>
          </template>
          <div v-if="pendingVolunteers.length === 0" class="empty">暂无待审核申请</div>
          <div v-else class="list">
            <div v-for="v in pendingVolunteers.slice(0, 5)" :key="v.id" class="list-item">
              <span class="name">{{ v.name }}</span>
              <span class="phone">{{ v.phone }}</span>
              <el-tag size="small" type="warning">待审核</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>待处理求助</span>
              <el-button type="primary" link @click="$router.push('/org/help-requests')">查看全部</el-button>
            </div>
          </template>
          <div v-if="pendingHelps.length === 0" class="empty">暂无待处理求助</div>
          <div v-else class="list">
            <div v-for="h in pendingHelps.slice(0, 5)" :key="h.id" class="list-item">
              <span class="title">{{ h.title }}</span>
              <span class="time">{{ formatTime(h.createdAt) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑组织信息弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑组织信息" width="550px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="组织名称" required>
          <el-input v-model="editForm.name" placeholder="请输入组织名称" />
        </el-form-item>
        <el-form-item label="组织简介">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入组织简介，将展示在志愿者端" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="editForm.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="editForm.contactEmail" placeholder="请输入联系邮箱" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="editForm.address" placeholder="请输入组织地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveOrgInfo" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { Edit } from '@element-plus/icons-vue';

const stats = ref({ volunteerCount: 0, pendingCount: 0, helpPendingCount: 0 });
const pendingVolunteers = ref<any[]>([]);
const pendingHelps = ref<any[]>([]);
const orgInfo = ref<any>({});
const editDialogVisible = ref(false);
const editForm = ref<any>({});
const saving = ref(false);

// 动态获取 headers
const getHeaders = () => {
  const token = sessionStorage.getItem('org_token');
  return { Authorization: `Bearer ${token}` };
};

const loadOrgInfo = async () => {
  try {
    const resp = await axios.get('/api/org/info', { headers: getHeaders() });
    orgInfo.value = resp.data.data || {};
  } catch (e) {
    console.error(e);
  }
};

const openEditDialog = () => {
  editForm.value = { ...orgInfo.value };
  editDialogVisible.value = true;
};

const saveOrgInfo = async () => {
  if (!editForm.value.name?.trim()) {
    ElMessage.warning('请输入组织名称');
    return;
  }
  saving.value = true;
  try {
    await axios.put('/api/org/info', editForm.value, { headers: getHeaders() });
    ElMessage.success('保存成功');
    editDialogVisible.value = false;
    await loadOrgInfo();
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '保存失败');
  } finally {
    saving.value = false;
  }
};

onMounted(async () => {
  try {
    const headers = getHeaders();
    const [statsResp, volunteersResp, helpsResp] = await Promise.all([
      axios.get('/api/org/stats', { headers }),
      axios.get('/api/org/volunteers/pending', { headers }),
      axios.get('/api/org/help-requests?status=pending', { headers }),
      loadOrgInfo()
    ]);
    stats.value = statsResp.data.data;
    pendingVolunteers.value = volunteersResp.data.data || [];
    pendingHelps.value = helpsResp.data.data?.records || [];
  } catch (e) {
    console.error(e);
  }
});

const formatTime = (t: string) => {
  if (!t) return '';
  return new Date(t).toLocaleDateString();
};
</script>

<style scoped>
.dashboard h2 { margin: 0 0 20px; color: #2c5282; }

/* 组织信息卡片 */
.org-info-card { margin-bottom: 20px; }
.org-info-header { display: flex; align-items: center; gap: 16px; }
.org-avatar {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 28px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}
.org-details { flex: 1; }
.org-details h3 { margin: 0 0 6px; font-size: 18px; color: #2c5282; }
.org-desc { margin: 0; color: #718096; font-size: 14px; }
.org-contact {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #edf2f7;
  display: flex;
  gap: 24px;
  font-size: 13px;
  color: #718096;
}

.stats-row { margin-bottom: 20px; }

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  font-size: 40px;
  margin-right: 16px;
}

.stat-value { font-size: 28px; font-weight: 600; color: #2c5282; }
.stat-label { color: #718096; font-size: 14px; }

.section-card { height: 300px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.empty { text-align: center; color: #a0aec0; padding: 40px 0; }

.list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #edf2f7;
}

.list-item:last-child { border-bottom: none; }
.list-item .name { font-weight: 500; }
.list-item .phone, .list-item .time { color: #718096; font-size: 13px; }
.list-item .title { flex: 1; }
</style>
