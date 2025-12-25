<template>
  <div>
    <div class="header">
      <h2>资源管理</h2>
      <el-upload
        :headers="uploadHeaders"
        action="/api/media/upload"
        :show-file-list="false"
        :on-success="onUploaded"
      >
        <el-button type="primary">上传资源</el-button>
      </el-upload>
    </div>
    <el-table :data="list" style="width: 100%">
      <el-table-column label="缩略图" width="120">
        <template #default="scope">
          <img v-if="scope.row.thumbUrl" :src="scope.row.thumbUrl" style="width:80px;height:60px;object-fit:cover;" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column prop="url" label="访问路径">
        <template #default="scope">
          <a :href="scope.row.url" target="_blank">{{ scope.row.url }}</a>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="onDownload(scope.row.id, scope.row.name)">下载</el-button>
          <el-button type="danger" size="small" @click="onDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
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
import { onMounted, ref, computed } from 'vue';
import { deleteMedia, downloadMedia, fetchMedia } from '../api';
import { useUserStore } from '../stores/user';
import { ElMessage } from 'element-plus';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const user = useUserStore();
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${user.token}` }));

const load = async () => {
  const resp = await fetchMedia(page.value, size.value);
  // @ts-ignore
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};

const onUploaded = () => {
  ElMessage.success('上传成功');
  load();
};

const onDelete = async (id: number) => {
  await deleteMedia(id);
  ElMessage.success('已删除');
  load();
};

const onDownload = async (id: number, name: string) => {
  const resp = await downloadMedia(id);
  const blob = new Blob([resp.data]);
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = name || 'file';
  a.click();
  window.URL.revokeObjectURL(url);
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
  margin-bottom: 12px;
}
.pager {
  margin-top: 10px;
  text-align: right;
}
</style>
