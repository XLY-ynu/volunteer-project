<template>
  <div class="page">
    <h2>志愿者注册</h2>
    <el-form :model="form" label-width="90px" class="form">
      <el-form-item label="姓名">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="组织">
        <el-input v-model="form.organization" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit">提交</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import http from '../api/http';
import { ElMessage } from 'element-plus';

const form = reactive({ name: '', phone: '', email: '', organization: '' });

const submit = async () => {
  if (!form.name) {
    ElMessage.warning('请填写姓名');
    return;
  }
  await http.post('/public/volunteer/register', form);
  ElMessage.success('提交成功');
  form.name = '';
  form.phone = '';
  form.email = '';
  form.organization = '';
};
</script>

<style scoped>
.page {
  max-width: 520px;
}
.form {
  margin-top: 12px;
}
</style>
