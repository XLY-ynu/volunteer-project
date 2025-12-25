<template>
  <div class="page">
    <h2>活动报名</h2>
    <el-table :data="list" style="width:100%">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="location" label="地点" width="160" />
      <el-table-column prop="startTime" label="开始" width="170" />
      <el-table-column prop="endTime" label="结束" width="170" />
      <el-table-column label="操作" width="160">
        <template #default="scope">
          <el-button size="small" @click="openSignup(scope.row.id)">报名</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="onPage" />
    </div>

    <el-dialog v-model="dialogVisible" title="活动报名" width="520px">
      <el-form :model="form" label-width="90px">
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
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="signup">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import http from '../api/http';
import { ElMessage } from 'element-plus';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const dialogVisible = ref(false);
const currentActivity = ref<number | null>(null);
const form = ref({ name: '', phone: '', email: '', organization: '' });

const load = async () => {
  const resp = await http.get('/public/activities', { params: { page: page.value, size: size.value } });
  // @ts-ignore
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};

const onPage = (p: number) => {
  page.value = p;
  load();
};

const openSignup = (activityId: number) => {
  currentActivity.value = activityId;
  dialogVisible.value = true;
};

const signup = async () => {
  if (!form.value.name || currentActivity.value == null) {
    ElMessage.warning('请填写姓名');
    return;
  }
  await http.post('/public/activities/signup-public', { ...form.value, activityId: currentActivity.value });
  ElMessage.success('报名成功');
  dialogVisible.value = false;
};

onMounted(load);
</script>

<style scoped>
.page {
  max-width: 900px;
}
.pager {
  margin-top: 10px;
  text-align: right;
}
</style>
