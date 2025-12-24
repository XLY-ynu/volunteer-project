<template>
  <div>
    <div class="header">
      <h2>插播管理</h2>
      <el-button type="primary" @click="dialogVisible = true">新增插播</el-button>
    </div>

    <el-table :data="list">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="mediaId" label="媒体ID" width="100" />
      <el-table-column prop="contentId" label="内容ID" width="100" />
      <el-table-column prop="targetGroup" label="目标分组" width="120" />
      <el-table-column prop="targetTerminalCode" label="目标终端" width="140" />
      <el-table-column prop="startTime" label="开始" width="170" />
      <el-table-column prop="endTime" label="结束" width="170" />
      <el-table-column prop="status" label="状态" width="100" />
    </el-table>

    <el-pagination
      layout="prev, pager, next"
      :total="total"
      :page-size="size"
      :current-page="page"
      @current-change="onPage"
      class="pager"
    />

    <el-dialog v-model="dialogVisible" title="新增插播" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="媒体ID">
          <el-input-number v-model="form.mediaId" :min="1" />
        </el-form-item>
        <el-form-item label="内容ID">
          <el-input-number v-model="form.contentId" :min="1" />
        </el-form-item>
        <el-form-item label="目标分组">
          <el-input v-model="form.targetGroup" placeholder="可选" />
        </el-form-item>
        <el-form-item label="目标终端">
          <el-input v-model="form.targetTerminalCode" placeholder="可选" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="startEnd"
            type="datetimerange"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
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
import { createBroadcast, fetchBroadcasts } from '../api';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const dialogVisible = ref(false);
const form = ref({
  title: '',
  mediaId: undefined as number | undefined,
  contentId: undefined as number | undefined,
  targetGroup: '',
  targetTerminalCode: ''
});
const startEnd = ref<[string, string] | null>(null);

const load = async () => {
  const resp = await fetchBroadcasts(page.value, size.value);
  // @ts-ignore
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};

const onPage = (p: number) => {
  page.value = p;
  load();
};

const submit = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入标题');
    return;
  }
  const payload: any = { ...form.value };
  if (startEnd.value) {
    payload.startTime = startEnd.value[0];
    payload.endTime = startEnd.value[1];
  }
  await createBroadcast(payload);
  ElMessage.success('已创建');
  dialogVisible.value = false;
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
