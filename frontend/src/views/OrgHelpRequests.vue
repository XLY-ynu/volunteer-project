<!--
 * @Author: 曹宇涵
 * @Module: 求助处理
 * @Description: 组织端求助管理页面，处理普通用户的求助请求
-->
<template>
  <div class="page">
    <h2>求助管理</h2>
    
    <el-tabs v-model="activeTab" @tab-change="loadData">
      <el-tab-pane label="待处理" name="pending">
        <el-table :data="pendingList" v-loading="loading">
          <el-table-column prop="title" label="标题" min-width="150" />
          <el-table-column prop="contactName" label="联系人" width="100" />
          <el-table-column prop="contactPhone" label="电话" width="120" />
          <el-table-column prop="address" label="地址" min-width="150" />
          <el-table-column prop="createdAt" label="提交时间" width="160">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="showDetail(row)">处理</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="处理中" name="processing">
        <el-table :data="processingList" v-loading="loading">
          <el-table-column prop="title" label="标题" min-width="150" />
          <el-table-column prop="contactName" label="联系人" width="100" />
          <el-table-column prop="contactPhone" label="电话" width="120" />
          <el-table-column prop="createdAt" label="提交时间" width="160">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="showDetail(row)">完成</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="已完成" name="completed">
        <el-table :data="completedList" v-loading="loading">
          <el-table-column prop="title" label="标题" min-width="150" />
          <el-table-column prop="contactName" label="联系人" width="100" />
          <el-table-column prop="reply" label="回复内容" min-width="200" />
          <el-table-column prop="repliedAt" label="处理时间" width="160">
            <template #default="{ row }">{{ formatTime(row.repliedAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button type="info" size="small" @click="showDetail(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 详情弹窗 -->
    <el-dialog v-model="dialogVisible" :title="currentRequest?.title" width="650px">
      <div class="detail-content" v-if="currentRequest">
        <div class="detail-section">
          <div class="detail-row">
            <div class="detail-item">
              <span class="detail-label">联系人</span>
              <span class="detail-value">{{ currentRequest.contactName || '未填写' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">联系电话</span>
              <span class="detail-value">{{ currentRequest.contactPhone || '未填写' }}</span>
            </div>
          </div>
          <div class="detail-row">
            <div class="detail-item full">
              <span class="detail-label">地址</span>
              <span class="detail-value">{{ currentRequest.address || '未填写' }}</span>
            </div>
          </div>
        </div>
        
        <div class="detail-section">
          <div class="detail-item full">
            <span class="detail-label">求助内容</span>
            <div class="detail-content-box">{{ currentRequest.content }}</div>
          </div>
        </div>
        
        <div class="detail-section">
          <div class="detail-row">
            <div class="detail-item">
              <span class="detail-label">提交时间</span>
              <span class="detail-value">{{ formatTime(currentRequest.createdAt) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">状态</span>
              <el-tag :type="statusType(currentRequest.status)" size="small">{{ statusText(currentRequest.status) }}</el-tag>
            </div>
          </div>
        </div>
        
        <div class="detail-section" v-if="currentRequest.reply">
          <div class="detail-item full">
            <span class="detail-label">回复内容</span>
            <div class="detail-content-box reply">{{ currentRequest.reply }}</div>
          </div>
        </div>
      </div>
      
      <div v-if="currentRequest?.status !== 'completed'" class="reply-section">
        <div class="reply-label">回复内容</div>
        <el-input v-model="replyContent" type="textarea" :rows="3" placeholder="请输入回复内容..." />
      </div>
      
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <template v-if="currentRequest?.status === 'pending'">
          <el-button type="warning" @click="handleReply('processing')">开始处理</el-button>
          <el-button type="success" @click="handleReply('completed')">直接完成</el-button>
        </template>
        <template v-else-if="currentRequest?.status === 'processing'">
          <el-button type="success" @click="handleReply('completed')">标记完成</el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';

const activeTab = ref('pending');
const loading = ref(false);
const pendingList = ref<any[]>([]);
const processingList = ref<any[]>([]);
const completedList = ref<any[]>([]);
const dialogVisible = ref(false);
const currentRequest = ref<any>(null);
const replyContent = ref('');

// 动态获取 headers
const getHeaders = () => {
  const token = sessionStorage.getItem('org_token');
  return { Authorization: `Bearer ${token}` };
};

const loadData = async () => {
  loading.value = true;
  try {
    const status = activeTab.value;
    const resp = await axios.get(`/api/org/help-requests?status=${status}&size=100`, { headers: getHeaders() });
    const list = resp.data.data?.records || [];
    if (status === 'pending') pendingList.value = list;
    else if (status === 'processing') processingList.value = list;
    else completedList.value = list;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const showDetail = (row: any) => {
  currentRequest.value = row;
  replyContent.value = row.reply || '';
  dialogVisible.value = true;
};

const handleReply = async (status: string) => {
  if (!replyContent.value && status === 'completed') {
    ElMessage.warning('请输入回复内容');
    return;
  }
  try {
    await axios.post(`/api/org/help-requests/${currentRequest.value.id}/reply`, {
      reply: replyContent.value,
      status
    }, { headers: getHeaders() });
    ElMessage.success('操作成功');
    dialogVisible.value = false;
    loadData();
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败');
  }
};

const formatTime = (t: string) => t ? new Date(t).toLocaleString() : '';
const statusText = (s: string) => ({ pending: '待处理', processing: '处理中', completed: '已完成' }[s] || s);
const statusType = (s: string) => ({ pending: 'warning', processing: 'primary', completed: 'success' }[s] || 'info');

onMounted(loadData);
</script>

<style scoped>
.page h2 { margin: 0 0 20px; color: #2c5282; }

/* 详情弹窗样式 */
.detail-content {
  padding: 0;
}

.detail-section {
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.detail-section:last-child {
  border-bottom: none;
}

.detail-row {
  display: flex;
  gap: 24px;
}

.detail-item {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item.full {
  flex: 1 1 100%;
}

.detail-label {
  flex-shrink: 0;
  width: 70px;
  color: #909399;
  font-size: 14px;
  line-height: 22px;
}

.detail-value {
  flex: 1;
  color: #303133;
  font-size: 14px;
  line-height: 22px;
  word-break: break-all;
}

.detail-content-box {
  flex: 1;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-all;
}

.detail-content-box.reply {
  background: #f0f9eb;
  color: #67c23a;
}

.reply-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px dashed #dcdfe6;
}

.reply-label {
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}
</style>
