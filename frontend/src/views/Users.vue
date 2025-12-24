<template>
  <div>
    <div class="header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="onCreate">新增用户</el-button>
    </div>

    <el-form :inline="true" :model="filter" class="filter" @submit.prevent>
      <el-form-item label="用户名">
        <el-input v-model="filter.username" placeholder="模糊查询" />
      </el-form-item>
      <el-form-item>
        <el-button @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" style="width:100%">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="roleCode" label="角色" width="120" />
      <el-table-column prop="enabled" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" @click="edit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="onPage" />
    </div>

    <el-dialog v-model="dialogVisible" title="用户" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="留空不改" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCode" placeholder="选择角色">
            <el-option v-for="r in roles" :key="r.code" :label="r.name || r.code" :value="r.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import http from '../api/http';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(20);
const total = ref(0);
const filter = ref({ username: '' });
const roles = ref<any[]>([]);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const form = ref({ username: '', password: '', nickname: '', roleCode: '', enabled: true });

const load = async () => {
  const resp = await http.get('/users', { params: { page: page.value, size: size.value, username: filter.value.username } });
  // @ts-ignore
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};

const loadRoles = async () => {
  const resp = await http.get('/users/roles');
  // @ts-ignore
  roles.value = resp.data?.data || [];
};

const onCreate = () => {
  editingId.value = null;
  form.value = { username: '', password: '', nickname: '', roleCode: '', enabled: true };
  dialogVisible.value = true;
};

const edit = (row: any) => {
  editingId.value = row.id;
  form.value = { username: row.username, password: '', nickname: row.nickname, roleCode: row.roleCode, enabled: row.enabled };
  dialogVisible.value = true;
};

const submit = async () => {
  if (!form.value.username || !form.value.roleCode) {
    ElMessage.warning('请填写用户名和角色');
    return;
  }
  if (editingId.value) {
    await http.put(`/users/${editingId.value}`, form.value);
    ElMessage.success('已更新');
  } else {
    if (!form.value.password) {
      ElMessage.warning('新增用户需填写密码');
      return;
    }
    await http.post('/users', form.value);
    ElMessage.success('已创建');
  }
  dialogVisible.value = false;
  load();
};

const remove = async (id: number) => {
  await http.delete(`/users/${id}`);
  ElMessage.success('已删除');
  load();
};

const onPage = (p: number) => {
  page.value = p;
  load();
};

onMounted(() => {
  load();
  loadRoles();
});
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter {
  margin: 10px 0;
}
.pager {
  margin-top: 10px;
  text-align: right;
}
</style>
