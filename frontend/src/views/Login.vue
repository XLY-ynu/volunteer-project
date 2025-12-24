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

const router = useRouter();
const form = reactive({ username: '', password: '' });

const onSubmit = () => {
  // TODO: 调用登录接口
  router.push('/dashboard');
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
