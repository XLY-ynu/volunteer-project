<!--
 * @Author: 贺嘉伟
 * @Module: 加入志愿者组织
 * @Description: 志愿者端 - 加入组织功能部分代码
 * 
 * 功能说明：
 * 1. 组织列表：浏览平台上的志愿者组织
 * 2. 申请加入：向心仪的组织提交加入申请
 * 3. 我的组织：查看已加入的组织及申请状态
-->

<!-- 以下是 VolunteerPortal.vue 中"加入组织"相关的代码片段 -->

<!-- ========== 模板部分 ========== -->
<!-- 加入组织区域 -->
<section class="orgs-section" id="orgs">
  <div class="section-header">
    <h2>志愿者组织</h2>
    <p>加入志愿者组织，参与更多公益活动</p>
  </div>
  <div class="orgs-grid">
    <el-card v-for="org in orgList" :key="org.id" class="org-card" shadow="hover">
      <div class="org-header">
        <div class="org-logo">{{ org.name?.charAt(0) || '组' }}</div>
        <div class="org-info">
          <h3>{{ org.name }}</h3>
          <p class="org-contact">{{ org.contactName }} · {{ org.contactPhone }}</p>
        </div>
      </div>
      <p class="org-desc">{{ org.description || '暂无简介' }}</p>
      <div class="org-footer">
        <el-button 
          v-if="getOrgJoinStatus(org.id) === 'approved'"
          type="success" 
          size="small"
          disabled
        >
          已加入
        </el-button>
        <el-button 
          v-else-if="getOrgJoinStatus(org.id) === 'pending'"
          type="warning" 
          size="small"
          disabled
        >
          审核中
        </el-button>
        <el-button 
          v-else
          type="primary" 
          size="small"
          @click="handleJoinOrg(org)"
        >
          申请加入
        </el-button>
      </div>
    </el-card>
    <el-empty v-if="!orgList.length" description="暂无志愿者组织" />
  </div>
  
  <!-- 我加入的组织 -->
  <div v-if="isLoggedIn && myOrgs.length > 0" class="my-orgs-section">
    <h3>我加入的组织</h3>
    <el-table :data="myOrgs" size="small">
      <el-table-column prop="orgName" label="组织名称" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'approved' ? 'success' : row.status === 'pending' ? 'warning' : 'danger'" size="small">
            {{ row.status === 'approved' ? '已通过' : row.status === 'pending' ? '审核中' : '已拒绝' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="joinedAt" label="加入时间" width="160">
        <template #default="{ row }">{{ row.joinedAt ? formatDateTime(row.joinedAt) : '-' }}</template>
      </el-table-column>
    </el-table>
  </div>
</section>

<!-- ========== 脚本部分 ========== -->
<script setup lang="ts">
// 组织相关数据
const orgList = ref<any[]>([]);
const myOrgs = ref<any[]>([]);

// 加载组织列表
const loadOrgs = async () => {
  try {
    const resp = await axios.get('/api/user-portal/orgs');
    orgList.value = resp.data.data || [];
  } catch (e) {
    console.error('加载组织列表失败', e);
  }
};

// 加载我加入的组织
const loadMyOrgs = async () => {
  if (!isLoggedIn.value) return;
  try {
    const resp = await axios.get('/api/portal/my-orgs', {
      headers: { Authorization: `Bearer ${portalToken.value}` }
    });
    myOrgs.value = resp.data.data || [];
  } catch (e) {
    console.error('加载我的组织失败', e);
  }
};

// 获取组织加入状态
const getOrgJoinStatus = (orgId: number) => {
  const membership = myOrgs.value.find(m => m.orgId === orgId);
  return membership?.status || null;
};

// 申请加入组织
const handleJoinOrg = async (org: any) => {
  if (!isLoggedIn.value) {
    showLoginDialog.value = true;
    return;
  }
  try {
    await ElMessageBox.confirm(`确定申请加入「${org.name}」？`, '申请加入', { type: 'info' });
    await axios.post(`/api/user-portal/join-org/${org.id}`, {}, {
      headers: { Authorization: `Bearer ${portalToken.value}` }
    });
    ElMessage.success('申请已提交，请等待审核');
    loadMyOrgs();
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '申请失败');
    }
  }
};
</script>

<!-- ========== 样式部分 ========== -->
<style scoped>
.orgs-section {
  padding: 60px 40px;
  background: #f8fafc;
}

.orgs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.org-card {
  border-radius: 12px;
}

.org-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.org-logo {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
}

.org-info h3 {
  margin: 0;
  font-size: 16px;
}

.org-contact {
  margin: 4px 0 0;
  font-size: 13px;
  color: #909399;
}

.org-desc {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
}

.org-footer {
  display: flex;
  justify-content: flex-end;
}

.my-orgs-section {
  margin-top: 40px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
}

.my-orgs-section h3 {
  margin: 0 0 16px;
  font-size: 16px;
}
</style>
