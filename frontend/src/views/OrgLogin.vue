<!--
 * @Author: 梁玉杰
 * @Module: 组织端登录
 * @Description: 志愿者组织端登录页面
-->
<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-left">
        <div class="brand">
          <span class="brand-icon">🏢</span>
          <h1>志愿者组织管理平台</h1>
        </div>
        <p class="tagline">管理志愿者、发布活动、处理求助</p>
      </div>
      <div class="login-right">
        <el-card class="login-card" shadow="never">
          <h2>组织登录</h2>
          <el-form @submit.prevent="onSubmit" :model="form" class="login-form">
            <el-form-item>
              <el-input v-model="form.username" placeholder="组织账号" size="large" prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" @click="onSubmit" :loading="loading" class="login-btn">登录</el-button>
            </el-form-item>
          </el-form>
          <div class="links">
            <router-link to="/login">管理员登录</router-link>
            <router-link to="/portal">志愿者/用户入口</router-link>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import axios from 'axios';

const router = useRouter();
const form = reactive({ username: 'org1', password: 'admin123' });
const loading = ref(false);

onMounted(() => {
  document.title = '组织端登录 - 志愿者多媒体平台';
});

const onSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码');
    return;
  }
  loading.value = true;
  try {
    const resp = await axios.post('/api/org/login', form);
    const data = resp.data.data;
    sessionStorage.setItem('org_token', data.token);
    sessionStorage.setItem('org_username', data.username);
    sessionStorage.setItem('org_role', data.role);
    ElMessage.success('登录成功');
    router.push('/org/dashboard');
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '登录失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2c5282 0%, #1a365d 100%);
}

.login-container {
  display: flex;
  width: 800px;
  min-height: 400px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #3182ce 0%, #63b3ed 100%);
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
}

.brand { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.brand-icon { font-size: 40px; }
.brand h1 { margin: 0; font-size: 22px; }
.tagline { font-size: 14px; opacity: 0.9; }

.login-right {
  flex: 1;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card { width: 100%; max-width: 320px; border: none; box-shadow: none; }
.login-card h2 { margin: 0 0 24px; font-size: 22px; text-align: center; color: #2c5282; }
.login-btn { width: 100%; border-radius: 8px; }

.links {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.links a { color: #3182ce; text-decoration: none; font-size: 13px; }
</style>
