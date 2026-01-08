<!--
 * @Author: 曹宇涵
 * @Module: 活动管理 - 组织端登录
-->
<template>
  <div class="login-page">
    <div class="login-container">
      <el-card class="login-card" shadow="hover">
        <h2>志愿者组织端</h2>
        <p class="subtitle">活动管理模块</p>
        <el-form @submit.prevent="onSubmit" :model="form" class="login-form">
          <el-form-item><el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" /></el-form-item>
          <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password /></el-form-item>
          <el-form-item><el-button type="primary" size="large" @click="onSubmit" :loading="loading" class="login-btn">登录</el-button></el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { loginApi } from '../api';
import { useUserStore } from '../stores/user';

const router = useRouter();
const userStore = useUserStore();
const form = reactive({ username: 'org1', password: 'admin123' });
const loading = ref(false);

const onSubmit = async () => {
  if (!form.username || !form.password) { ElMessage.warning('请输入用户名和密码'); return; }
  loading.value = true;
  try {
    const resp = await loginApi(form.username, form.password);
    const data = (resp.data as any).data;
    userStore.setToken(data.token);
    userStore.setUsername(data.username || form.username);
    ElMessage.success('登录成功');
    router.push('/activities');
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '登录失败');
  } finally { loading.value = false; }
};
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #67c23a 0%, #409eff 100%); }
.login-container { width: 400px; }
.login-card { border-radius: 16px; padding: 20px; }
.login-card h2 { margin: 0 0 8px; font-size: 24px; color: #303133; text-align: center; }
.login-card .subtitle { text-align: center; color: #909399; margin-bottom: 32px; }
.login-form { width: 100%; }
.login-btn { width: 100%; border-radius: 8px; font-size: 16px; height: 44px; }
</style>
