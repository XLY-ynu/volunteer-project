<!--
 * @Author: 贺嘉伟
 * @Module: 成为志愿者
 * @Description: 普通用户端 - 成为志愿者功能部分代码
 * 
 * 功能说明：
 * 1. 申请注册：普通用户可申请成为志愿者
 * 2. 填写资料：提交姓名、手机号、邮箱、所属单位等信息
 * 3. 等待审核：提交后等待志愿者组织审核通过
-->

<!-- 以下是 UserPublicPortal.vue 中"成为志愿者"相关的代码片段 -->

<!-- ========== 模板部分 ========== -->
<!-- 成为志愿者 -->
<div v-if="tab === 'volunteer'" class="content-area">
  <div class="volunteer-section">
    <div class="volunteer-banner">
      <h2>🌟 成为志愿者</h2>
      <p>加入我们，用爱心传递温暖，用行动改变世界</p>
    </div>
    
    <!-- 未登录提示 -->
    <div v-if="!isLoggedIn" class="login-prompt">
      <el-icon :size="48" color="#909399"><User /></el-icon>
      <p>请先登录后再申请成为志愿者</p>
      <el-button type="primary" @click="showLogin = true">立即登录</el-button>
    </div>
    
    <!-- 已是志愿者 -->
    <div v-else-if="volunteerStatus === 'approved'" class="volunteer-status success">
      <el-icon :size="48" color="#67c23a"><CircleCheck /></el-icon>
      <h3>您已经是志愿者了！</h3>
      <p>您可以前往志愿者端参与更多活动</p>
      <el-button type="primary" @click="goToVolunteerPortal">进入志愿者端</el-button>
    </div>
    
    <!-- 审核中 -->
    <div v-else-if="volunteerStatus === 'pending'" class="volunteer-status pending">
      <el-icon :size="48" color="#e6a23c"><Clock /></el-icon>
      <h3>您的申请正在审核中</h3>
      <p>请耐心等待管理员审核，审核通过后您将成为正式志愿者</p>
    </div>
    
    <!-- 申请表单 -->
    <div v-else class="volunteer-form-section">
      <h3>填写志愿者申请信息</h3>
      <el-form :model="volunteerForm" label-width="100px" style="max-width: 500px; margin: 0 auto;">
        <el-form-item label="姓名" required>
          <el-input v-model="volunteerForm.name" placeholder="请输入您的真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="volunteerForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="volunteerForm.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <el-form-item label="所属单位">
          <el-input v-model="volunteerForm.organization" placeholder="学校/公司/社区等（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitVolunteerApplication" :loading="applyLoading">
            提交申请
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</div>

<!-- ========== 脚本部分 ========== -->
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { User, CircleCheck, Clock } from '@element-plus/icons-vue';
import axios from 'axios';

// 志愿者申请相关
const volunteerStatus = ref<string | null>(null);
const volunteerForm = ref({ name: '', phone: '', email: '', organization: '' });
const applyLoading = ref(false);

// 获取请求头
const getHeaders = () => {
  const token = localStorage.getItem('userToken');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

// 检查志愿者状态
const checkVolunteerStatus = async () => {
  if (!isLoggedIn.value) {
    volunteerStatus.value = null;
    return;
  }
  try {
    const resp = await axios.get('/api/user-portal/volunteer-status', { headers: getHeaders() });
    volunteerStatus.value = resp.data.data?.status || null;
  } catch (e) {
    volunteerStatus.value = null;
  }
};

// 提交志愿者申请
const submitVolunteerApplication = async () => {
  if (!volunteerForm.value.name || !volunteerForm.value.phone) {
    ElMessage.warning('请填写姓名和手机号');
    return;
  }
  applyLoading.value = true;
  try {
    await axios.post('/api/user-portal/become-volunteer', volunteerForm.value, { headers: getHeaders() });
    ElMessage.success('申请已提交，请等待审核');
    volunteerStatus.value = 'pending';
    volunteerForm.value = { name: '', phone: '', email: '', organization: '' };
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '申请失败');
  } finally {
    applyLoading.value = false;
  }
};

// 跳转到志愿者端
const goToVolunteerPortal = () => {
  window.location.href = '/portal';
};

onMounted(() => {
  if (isLoggedIn.value) {
    checkVolunteerStatus();
  }
});
</script>

<!-- ========== 样式部分 ========== -->
<style scoped>
.volunteer-section {
  background: #fff;
  padding: 30px;
  border-radius: 12px;
}

.volunteer-banner {
  text-align: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #67c23a, #85ce61);
  color: #fff;
  border-radius: 12px;
  margin-bottom: 30px;
}

.volunteer-banner h2 {
  margin: 0 0 10px;
  font-size: 28px;
}

.volunteer-banner p {
  margin: 0;
  opacity: 0.9;
}

.login-prompt {
  text-align: center;
  padding: 60px 20px;
}

.login-prompt p {
  color: #909399;
  margin: 20px 0;
}

.volunteer-status {
  text-align: center;
  padding: 60px 20px;
}

.volunteer-status h3 {
  margin: 20px 0 10px;
  color: #303133;
}

.volunteer-status p {
  color: #909399;
  margin-bottom: 20px;
}

.volunteer-status.success {
  background: #f0f9eb;
  border-radius: 12px;
}

.volunteer-status.pending {
  background: #fdf6ec;
  border-radius: 12px;
}

.volunteer-form-section {
  padding: 20px 0;
}

.volunteer-form-section h3 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}
</style>
