<!--
 * @Author: 谢龙洋
 * @Module: 普通用户端模块三 - 发布求助
 * @Description: 普通用户发布求助功能
 *   - 选择组织：从组织列表中选择要求助的志愿者组织
 *   - 填写求助：填写求助标题、详细内容、联系人、联系电话、地址
 *   - 提交求助：向指定组织发送求助请求
 *   - 我的求助：查看已提交的求助记录及处理状态
-->
<template>
  <div class="help-portal">
    <!-- 发布求助表单 -->
    <el-card class="help-card">
      <template #header>
        <div class="card-header">
          <el-icon><QuestionFilled /></el-icon>
          <span>向志愿者组织发布求助</span>
        </div>
      </template>
      
      <el-form :model="helpForm" label-width="100px" class="help-form">
        <!-- 选择组织 -->
        <el-form-item label="选择组织" required>
          <el-select v-model="helpForm.orgId" placeholder="请选择求助的志愿者组织" style="width: 100%">
            <el-option v-for="org in orgs" :key="org.id" :label="org.name" :value="org.id">
              <div class="org-option">
                <span class="org-name">{{ org.name }}</span>
                <span class="org-desc">{{ org.description }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <!-- 填写求助 -->
        <el-form-item label="求助标题" required>
          <el-input v-model="helpForm.title" placeholder="简要描述您的需求" maxlength="100" show-word-limit />
        </el-form-item>
        
        <el-form-item label="详细内容" required>
          <el-input v-model="helpForm.content" type="textarea" :rows="5" 
                    placeholder="详细描述您需要的帮助，包括时间、地点、具体需求等" 
                    maxlength="1000" show-word-limit />
        </el-form-item>
        
        <el-form-item label="联系人">
          <el-input v-model="helpForm.contactName" placeholder="您的姓名" />
        </el-form-item>
        
        <el-form-item label="联系电话">
          <el-input v-model="helpForm.contactPhone" placeholder="您的电话" />
        </el-form-item>
        
        <el-form-item label="地址">
          <el-input v-model="helpForm.address" placeholder="您的地址（可选）" />
        </el-form-item>
        
        <!-- 提交求助 -->
        <el-form-item>
          <el-button type="primary" @click="submitHelp" :loading="submitting" :disabled="!isLoggedIn">
            <el-icon><Position /></el-icon>
            提交求助
          </el-button>
          <span v-if="!isLoggedIn" class="login-tip">请先登录后再提交</span>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 我的求助记录 -->
    <el-card v-if="isLoggedIn" class="my-requests-card">
      <template #header>
        <div class="card-header">
          <el-icon><List /></el-icon>
          <span>我的求助记录</span>
          <el-button text type="primary" @click="loadMyHelpRequests">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="myHelpRequests" stripe v-loading="loading">
        <el-table-column prop="title" label="求助标题" min-width="200" />
        <el-table-column prop="orgName" label="求助组织" width="150" />
        <el-table-column prop="status" label="处理状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="回复内容" min-width="200">
          <template #default="{ row }">
            <span v-if="row.reply">{{ row.reply }}</span>
            <span v-else class="no-reply">暂无回复</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="viewDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrapper" v-if="total > pageSize">
        <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
    
    <!-- 求助详情弹窗 -->
    <el-dialog v-model="detailVisible" title="求助详情" width="600px">
      <el-descriptions :column="1" border v-if="currentRequest">
        <el-descriptions-item label="求助标题">{{ currentRequest.title }}</el-descriptions-item>
        <el-descriptions-item label="求助组织">{{ currentRequest.orgName }}</el-descriptions-item>
        <el-descriptions-item label="详细内容">{{ currentRequest.content }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentRequest.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRequest.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ currentRequest.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusType(currentRequest.status)">
            {{ getStatusText(currentRequest.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="回复内容" v-if="currentRequest.reply">
          {{ currentRequest.reply }}
        </el-descriptions-item>
        <el-descriptions-item label="回复时间" v-if="currentRequest.repliedAt">
          {{ formatTime(currentRequest.repliedAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatTime(currentRequest.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { QuestionFilled, Position, List, Refresh } from '@element-plus/icons-vue';
import http from '../api/http';

// 登录状态
const isLoggedIn = computed(() => !!localStorage.getItem('token'));

// 组织列表
const orgs = ref<any[]>([]);

// 求助表单
const helpForm = ref({
  orgId: null as number | null,
  title: '',
  content: '',
  contactName: '',
  contactPhone: '',
  address: ''
});
const submitting = ref(false);

// 我的求助记录
const myHelpRequests = ref<any[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 详情弹窗
const detailVisible = ref(false);
const currentRequest = ref<any>(null);

// 加载组织列表
const loadOrgs = async () => {
  try {
    const resp = await http.get('/user-portal/orgs');
    orgs.value = resp.data.data || [];
  } catch (e) {
    console.error('加载组织列表失败', e);
  }
};

// 加载我的求助记录
const loadMyHelpRequests = async () => {
  if (!isLoggedIn.value) return;
  loading.value = true;
  try {
    const resp = await http.get('/user-portal/help-requests', {
      params: { page: currentPage.value, size: pageSize.value }
    });
    const data = resp.data.data || {};
    myHelpRequests.value = data.records || data || [];
    total.value = data.total || myHelpRequests.value.length;
  } catch (e) {
    console.error('加载求助记录失败', e);
  } finally {
    loading.value = false;
  }
};

// 提交求助
const submitHelp = async () => {
  if (!helpForm.value.orgId) {
    ElMessage.warning('请选择求助的组织');
    return;
  }
  if (!helpForm.value.title.trim()) {
    ElMessage.warning('请填写求助标题');
    return;
  }
  if (!helpForm.value.content.trim()) {
    ElMessage.warning('请填写详细内容');
    return;
  }
  
  submitting.value = true;
  try {
    await http.post('/user-portal/help-requests', helpForm.value);
    ElMessage.success('求助已提交，请等待组织处理');
    // 重置表单
    helpForm.value = {
      orgId: null,
      title: '',
      content: '',
      contactName: '',
      contactPhone: '',
      address: ''
    };
    // 刷新列表
    loadMyHelpRequests();
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '提交失败');
  } finally {
    submitting.value = false;
  }
};

// 查看详情
const viewDetail = (row: any) => {
  currentRequest.value = row;
  detailVisible.value = true;
};

// 分页
const onPageChange = (page: number) => {
  currentPage.value = page;
  loadMyHelpRequests();
};

// 状态显示
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    pending: 'warning',
    processing: 'primary',
    completed: 'success'
  };
  return map[status] || 'info';
};

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成'
  };
  return map[status] || status;
};

const formatTime = (t: string) => {
  if (!t) return '-';
  return t.replace('T', ' ').substring(0, 16);
};

onMounted(() => {
  loadOrgs();
  if (isLoggedIn.value) {
    loadMyHelpRequests();
  }
});
</script>

<style scoped>
.help-portal {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.help-card, .my-requests-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.card-header .el-button {
  margin-left: auto;
}

.help-form {
  max-width: 600px;
}

.org-option {
  display: flex;
  flex-direction: column;
}

.org-option .org-name {
  font-weight: 500;
}

.org-option .org-desc {
  font-size: 12px;
  color: #909399;
}

.login-tip {
  color: #909399;
  margin-left: 12px;
  font-size: 13px;
}

.no-reply {
  color: #c0c4cc;
  font-style: italic;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
