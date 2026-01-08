<!--
 * @Author: 贺嘉伟
 * @Module: 活动参与 - 签到页面
 * @Description: 志愿者活动签到页面，支持签到码验证
-->
<template>
  <div class="page">
    <div class="checkin-card">
      <div class="card-header">
        <el-icon class="header-icon"><Checked /></el-icon>
        <h2>活动签到</h2>
      </div>
      
      <el-alert type="info" :closable="false" class="info-alert">
        <template #title>请输入签到码和您注册时填写的姓名、手机号</template>
      </el-alert>

      <el-form label-width="80px" class="checkin-form">
        <el-form-item label="签到码" required>
          <el-input v-model="checkinCode" placeholder="请输入活动签到码" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
      <el-form-item label="手机号" required>
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" @click="submit" :loading="loading" style="width: 100%">
          <el-icon><Check /></el-icon>
          提交签到
        </el-button>
        <div class="back-portal">
          <el-button text type="primary" @click="goPortal">返回门户</el-button>
        </div>
      </el-form-item>
    </el-form>

      <div class="tip-text">
        <el-icon><InfoFilled /></el-icon>
        <span>如未注册，请先联系管理员或在志愿者端完成注册</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import http from '../api/http';
import { Checked, Check, InfoFilled } from '@element-plus/icons-vue';

const route = useRoute();
const checkinCode = ref('');
const form = ref({ name: '', phone: '' });
const loading = ref(false);

onMounted(() => {
  // 如果 URL 带参数，自动填入签到码
  const code = route.query.code;
  if (code) {
    checkinCode.value = code as string;
  }
});

const submit = async () => {
  if (!checkinCode.value.trim()) {
    ElMessage.warning('请输入签到码');
    return;
  }
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入姓名');
    return;
  }
  if (!form.value.phone.trim()) {
    ElMessage.warning('请输入手机号');
    return;
  }
  
  loading.value = true;
  try {
    const res = await http.post('/public/activities/checkin', {
      checkinCode: checkinCode.value.trim(),
      name: form.value.name.trim(),
      phone: form.value.phone.trim()
    });
    // 检查响应中的 success 字段
    if (res.data.success) {
      ElMessage.success('签到成功！');
    } else {
      ElMessage.error(res.data.message || '签到失败');
    }
  } catch (e: any) {
    const msg = e?.response?.data?.message || '签到失败';
    ElMessage.error(msg);
  } finally {
    loading.value = false;
  }
};

const goPortal = () => {
  window.location.href = '/portal';
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.checkin-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.card-header {
  text-align: center;
  margin-bottom: 24px;
}

.header-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 12px;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.info-alert {
  margin-bottom: 20px;
  border-radius: 8px;
}

.checkin-form {
  margin-top: 20px;
}

.checkin-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.tip-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 20px;
  color: #909399;
  font-size: 13px;
}
.back-portal {
  text-align: center;
  margin-top: 8px;
}
</style>
