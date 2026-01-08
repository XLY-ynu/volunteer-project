<!--
 * @Author: 陈力宏
 * @Module: 系统管理 - 操作日志
 * @Description: 记录管理员的所有操作行为，支持日志查询/审计/清理
-->
<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left"><h3>操作日志</h3><span class="subtitle">系统操作记录，共 {{ total }} 条</span></div>
        <el-button type="danger" @click="showCleanDialog = true"><el-icon><Delete /></el-icon>清理日志</el-button>
      </div>
    </el-card>
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filter">
        <el-form-item label="用户"><el-input v-model="filter.username" placeholder="用户名" clearable style="width: 140px" /></el-form-item>
        <el-form-item label="方法">
          <el-select v-model="filter.method" placeholder="全部" clearable style="width: 120px">
            <el-option label="GET" value="GET" /><el-option label="POST" value="POST" /><el-option label="PUT" value="PUT" /><el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="路径"><el-input v-model="filter.path" placeholder="请求路径" clearable style="width: 180px" /></el-form-item>
        <el-form-item><el-button type="primary" @click="search"><el-icon><Search /></el-icon>查询</el-button><el-button @click="resetFilter">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="content-card" shadow="never">
      <el-table :data="list" stripe>
        <el-table-column prop="username" label="用户" width="120"><template #default="scope"><el-tag size="small">{{ scope.row.username || '匿名' }}</el-tag></template></el-table-column>
        <el-table-column prop="method" label="方法" width="90"><template #default="scope"><el-tag :type="getMethodType(scope.row.method)" size="small">{{ scope.row.method }}</el-tag></template></el-table-column>
        <el-table-column prop="path" label="路径" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80"><template #default="scope"><el-tag :type="scope.row.status >= 200 && scope.row.status < 300 ? 'success' : 'danger'" size="small">{{ scope.row.status }}</el-tag></template></el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180"><template #default="scope">{{ formatTime(scope.row.createdAt) }}</template></el-table-column>
      </el-table>
      <div class="pagination-wrapper"><el-pagination layout="total, sizes, prev, pager, next" :total="total" :page-size="size" :page-sizes="[20, 50, 100]" :current-page="page" @current-change="onPage" @size-change="onSizeChange" /></div>
    </el-card>
    <el-dialog v-model="showCleanDialog" title="清理日志" width="400px">
      <el-alert type="warning" :closable="false" style="margin-bottom: 16px"><template #title>此操作不可恢复，请谨慎操作</template></el-alert>
      <el-form label-width="100px"><el-form-item label="保留天数"><el-input-number v-model="cleanDays" :min="1" :max="365" /><span style="margin-left: 8px; color: #909399">天前的日志将被删除</span></el-form-item></el-form>
      <template #footer><el-button @click="showCleanDialog = false">取消</el-button><el-button type="danger" @click="cleanLogs">确认清理</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import http from '../api/http';
import { Delete, Search } from '@element-plus/icons-vue';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(20);
const total = ref(0);
const showCleanDialog = ref(false);
const cleanDays = ref(30);
const filter = ref({ username: '', method: '', path: '', dateRange: null as [string, string] | null });

const load = async () => {
  const params: any = { page: page.value, size: size.value };
  if (filter.value.username) params.username = filter.value.username;
  if (filter.value.method) params.method = filter.value.method;
  if (filter.value.path) params.path = filter.value.path;
  const resp = await http.get('/ops/logs', { params });
  const data = (resp.data as any)?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};
const search = () => { page.value = 1; load(); };
const resetFilter = () => { filter.value = { username: '', method: '', path: '', dateRange: null }; page.value = 1; load(); };
const onPage = (p: number) => { page.value = p; load(); };
const onSizeChange = (s: number) => { size.value = s; page.value = 1; load(); };
const formatTime = (t: string) => t ? t.replace('T', ' ').substring(0, 19) : '-';
const getMethodType = (method: string) => {
  switch (method) { case 'GET': return 'success'; case 'POST': return 'primary'; case 'PUT': return 'warning'; case 'DELETE': return 'danger'; default: return 'info'; }
};
const cleanLogs = async () => {
  await ElMessageBox.confirm(`确定删除 ${cleanDays.value} 天前的所有日志？`, '确认清理', { type: 'warning' });
  try {
    const resp = await http.delete('/ops/logs/clean', { params: { days: cleanDays.value } });
    const data = (resp.data as any)?.data || {};
    ElMessage.success(`已清理 ${data.deleted || 0} 条日志`);
    showCleanDialog.value = false;
    load();
  } catch (e) { ElMessage.error('清理失败'); }
};
onMounted(load);
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .filter-card, .content-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
