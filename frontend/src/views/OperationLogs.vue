<template>
  <div>
    <div class="header">
      <h2>操作日志</h2>
    </div>
    <el-table :data="list" style="width: 100%">
      <el-table-column prop="username" label="用户" width="140" />
      <el-table-column prop="method" label="方法" width="100" />
      <el-table-column prop="path" label="路径" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createdAt" label="时间" width="180" />
    </el-table>
    <div class="pager">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import http from '../api/http';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(20);
const total = ref(0);

const load = async () => {
  const resp = await http.get('/ops/logs', { params: { page: page.value, size: size.value } });
  // @ts-ignore
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};

const onPage = (p: number) => {
  page.value = p;
  load();
};

onMounted(load);
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pager {
  margin-top: 10px;
  text-align: right;
}
</style>
