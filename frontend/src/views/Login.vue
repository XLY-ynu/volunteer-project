<template>
  <div class="login-page">
    <el-card class="card">
      <h2>志愿者多媒体展示管理</h2>
      <el-form @submit.prevent="onSubmit" :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="••••••" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit" block>登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { loginApi } from '../api';
import { useUserStore } from '../stores/user';

const router = useRouter();
const userStore = useUserStore();
const form = reactive({ username: 'admin', password: 'admin123' });

const onSubmit = async () => {
  try {
    const resp = await loginApi(form.username, form.password);
    // @ts-ignore
    const data = resp.data.data;
    userStore.setToken(data.token);
    userStore.setUsername(data.username);
    ElMessage.success('登录成功');
    router.push('/dashboard');
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '登录失败');
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(120deg, #f0f4ff, #f5f7fa);
}
.card {
  width: 420px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
}
h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #303133;
}
</style>
