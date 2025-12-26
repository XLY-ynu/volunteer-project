<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-left">
        <div class="brand">
          <span class="brand-icon">📺</span>
          <h1>志愿者多媒体平台</h1>
        </div>
        <p class="tagline">高效管理媒体资源，智能调度终端播放</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Monitor /></el-icon>
            <span>终端管理</span>
          </div>
          <div class="feature-item">
            <el-icon><VideoPlay /></el-icon>
            <span>播放调度</span>
          </div>
          <div class="feature-item">
            <el-icon><Calendar /></el-icon>
            <span>活动管理</span>
          </div>
        </div>
        <div class="portal-entry">
          <el-button type="success" plain @click="goPortal">访问门户</el-button>
        </div>
      </div>
      <div class="login-right">
        <el-card class="login-card" shadow="never">
          <h2>欢迎登录</h2>
          <p class="subtitle">请输入您的账号信息</p>
          <el-form @submit.prevent="onSubmit" :model="form" class="login-form">
            <el-form-item>
              <el-input
                v-model="form.username"
                placeholder="用户名"
                size="large"
                prefix-icon="User"
              />
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="form.password"
                type="password"
                placeholder="密码"
                size="large"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" @click="onSubmit" :loading="loading" class="login-btn">
                登录
              </el-button>
            </el-form-item>
          </el-form>
          <div class="login-footer">
            <span>© 2025 志愿者多媒体平台</span>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { loginApi } from '../api';
import { useUserStore } from '../stores/user';
import { Monitor, VideoPlay, Calendar } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();
const form = reactive({ username: 'admin', password: 'admin123' });
const loading = ref(false);

const onSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }
  loading.value = true;
  try {
    const resp = await loginApi(form.username, form.password);
    // @ts-ignore
    const data = resp.data.data;
    userStore.setToken(data.token);
    userStore.setUsername(data.username);
    if (data.role) userStore.setRole(data.role);
    ElMessage.success('登录成功');
    router.push('/dashboard');
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '登录失败');
  } finally {
    loading.value = false;
  }
};

const goPortal = () => {
  router.push('/portal');
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3a5f 0%, #0f172a 100%);
}

.login-container {
  display: flex;
  width: 900px;
  min-height: 500px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.brand-icon {
  font-size: 40px;
}

.brand h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.tagline {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 40px;
}
.portal-entry {
  margin-top: 20px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  opacity: 0.9;
}

.feature-item .el-icon {
  font-size: 20px;
}

.login-right {
  flex: 1;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 360px;
  border: none;
  box-shadow: none;
}

.login-card h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #303133;
  text-align: center;
}

.login-card .subtitle {
  text-align: center;
  color: #909399;
  margin-bottom: 32px;
}

.login-form {
  width: 100%;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.login-btn {
  width: 100%;
  border-radius: 8px;
  font-size: 16px;
  height: 44px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  color: #c0c4cc;
  font-size: 12px;
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    width: 90%;
    max-width: 400px;
  }
  
  .login-left {
    padding: 40px 30px;
  }
  
  .features {
    display: none;
  }
}
</style>
