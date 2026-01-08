<template>
  <div class="page">
    <div class="menu">
      <el-menu :default-active="active" @select="onSelect">
        <el-menu-item v-for="c in categories" :key="c.id" :index="String(c.id)">{{ c.name }}</el-menu-item>
      </el-menu>
    </div>
    <div class="content">
      <el-input v-model="keyword" placeholder="搜索内容" style="width: 240px" @change="load" />
      <el-table :data="list" style="width: 100%">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="summary" label="摘要" />
        <el-table-column prop="publishTime" label="发布时间" width="180" />
      </el-table>
      <div class="pager">
        <el-pagination layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="onPage" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import http from '../api/http';

const categories = ref<any[]>([]);
const active = ref<string>('');
const list = ref<any[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const keyword = ref('');

const loadCategories = async () => {
  const resp = await http.get('/public/categories');
  // @ts-ignore
  categories.value = resp.data?.data || [];
  if (categories.value.length && !active.value) {
    active.value = String(categories.value[0].id);
  }
};

const load = async () => {
  const resp = await http.get('/public/content', {
    params: {
      page: page.value,
      size: size.value,
      categoryId: active.value ? Number(active.value) : undefined,
      keyword: keyword.value || undefined
    }
  });
  // @ts-ignore
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
};

const onSelect = (index: string) => {
  active.value = index;
  page.value = 1;
  load();
};

const onPage = (p: number) => {
  page.value = p;
  load();
};

onMounted(async () => {
  await loadCategories();
  load();
});
</script>

<style scoped>
.page {
  display: flex;
  gap: 16px;
}
.menu {
  width: 200px;
}
.content {
  flex: 1;
}
.pager {
  margin-top: 10px;
  text-align: right;
}
</style>
