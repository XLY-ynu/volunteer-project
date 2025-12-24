<template>
  <el-container style="min-height: 100vh">
    <el-aside width="220px" class="aside">
      <div class="logo">志愿者多媒体</div>
      <el-menu :default-active="active" class="menu" @select="onSelect">
        <el-menu-item index="/dashboard">仪表盘</el-menu-item>
        <el-menu-item index="/media">资源管理</el-menu-item>
        <el-menu-item index="/categories">分类管理</el-menu-item>
        <el-menu-item index="/playlists">播放列表</el-menu-item>
        <el-menu-item index="/terminals">终端管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div />
        <div class="user">
          <el-button link @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { computed } from 'vue';
import { useUserStore } from '../stores/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const active = computed(() => route.path);

const onSelect = (path: string) => {
  router.push(path);
};

const logout = () => {
  userStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.aside {
  background: #0f172a;
  color: #fff;
}
.logo {
  padding: 20px 16px;
  font-weight: bold;
}
.menu {
  border-right: none;
  background: #0f172a;
  color: #fff;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
