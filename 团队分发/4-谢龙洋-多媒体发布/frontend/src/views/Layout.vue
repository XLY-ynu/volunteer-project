<!--
 * @Author: 谢龙洋
 * @Module: 多媒体发布 - 布局框架
-->
<template>
  <el-container style="min-height: 100vh">
    <el-aside width="220px" class="aside">
      <div class="logo"><span class="logo-icon">📡</span><span class="logo-text">多媒体发布</span></div>
      <el-menu :default-active="active" class="menu" @select="onSelect">
        <el-menu-item index="/broadcasts"><el-icon><Promotion /></el-icon><span>广播推送</span></el-menu-item>
        <el-menu-item index="/terminals"><el-icon><Monitor /></el-icon><span>终端管理</span></el-menu-item>
        <el-menu-item index="/terminal-preview"><el-icon><View /></el-icon><span>终端预览</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="user">
          <el-avatar :size="32" class="avatar">{{ userStore.username?.charAt(0)?.toUpperCase() || 'U' }}</el-avatar>
          <span class="user-info">{{ userStore.username || '未登录' }}</span>
          <el-button type="danger" link @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main-content"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { computed } from 'vue';
import { useUserStore } from '../stores/user';
import { Promotion, Monitor, View } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const active = computed(() => route.path);
const onSelect = (path: string) => { router.push(path); };
const logout = () => { userStore.logout(); router.push('/login'); };
</script>

<style scoped>
.aside { background: linear-gradient(180deg, #e6a23c 0%, #f56c6c 100%); color: #fff; }
.logo { padding: 20px 16px; display: flex; align-items: center; gap: 10px; border-bottom: 1px solid rgba(255, 255, 255, 0.1); }
.logo-icon { font-size: 24px; }
.logo-text { font-weight: 600; font-size: 15px; color: #fff; }
.menu { border-right: none; background: transparent; }
:deep(.el-menu-item) { color: rgba(255, 255, 255, 0.8); margin: 2px 8px; border-radius: 8px; height: 40px; }
:deep(.el-menu-item:hover) { background: rgba(255, 255, 255, 0.1); color: #fff; }
:deep(.el-menu-item.is-active) { background: rgba(255, 255, 255, 0.2); color: #fff; }
:deep(.el-menu-item .el-icon) { margin-right: 8px; font-size: 18px; }
.header { display: flex; justify-content: flex-end; align-items: center; background: #fff; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08); padding: 0 24px; height: 50px; }
.user { display: flex; align-items: center; gap: 12px; }
.avatar { background: linear-gradient(135deg, #e6a23c, #f56c6c); color: #fff; font-weight: 600; }
.user-info { color: #606266; font-size: 14px; }
.main-content { background: #f5f7fa; padding: 16px 20px; min-height: calc(100vh - 50px); }
</style>
