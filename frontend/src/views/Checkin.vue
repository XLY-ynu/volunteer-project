<template>
  <div class="page">
    <h2>活动签到</h2>
    <p class="hint">请输入姓名和手机号完成签到</p>
    <el-form label-width="90px" class="form">
      <el-form-item label="姓名">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="机构">
        <el-input v-model="form.organization" />
      </el-form-item>
      <el-form-item label="签到码">
        <el-input v-model="checkinCode" disabled />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit">提交签到</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import http from '../api/http';

const route = useRoute();
const activityId = ref<number | null>(null);
const checkinCode = ref('');
const form = ref({ name: '', phone: '', email: '', organization: '' });

onMounted(() => {
  const id = route.query.activityId;
  const code = route.query.code;
  activityId.value = id ? Number(id) : null;
  checkinCode.value = (code as string) || '';
});

const submit = async () => {
  if (!activityId.value || !checkinCode.value) {
    ElMessage.error('链接无效，请联系管理员');
    return;
  }
  if (!form.value.name) {
    ElMessage.warning('请输入姓名');
    return;
  }
  try {
    await http.post('/public/activities/checkin', {
      activityId: activityId.value,
      checkinCode: checkinCode.value,
      ...form.value
    });
    ElMessage.success('签到成功');
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '签到失败');
  }
};
</script>

<style scoped>
.page {
  max-width: 480px;
  margin: 40px auto;
  padding: 24px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}
.hint {
  color: #909399;
  margin-bottom: 12px;
}
</style>
