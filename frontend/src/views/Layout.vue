<template>
  <el-container style="min-height: 100vh">
    <el-aside width="240px" class="aside">
      <div class="logo">
        <span class="logo-icon">📺</span>
        <span class="logo-text">志愿者多媒体平台</span>
      </div>
      <el-menu :default-active="active" class="menu" @select="onSelect" :collapse="false">
        <!-- 概览 -->
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>

        <!-- 资源管理 -->
        <div class="menu-group">资源管理</div>
        <el-menu-item index="/media">
          <el-icon><Picture /></el-icon>
          <span>媒体资源</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/content">
          <el-icon><Document /></el-icon>
          <span>内容管理</span>
        </el-menu-item>

        <!-- 播放管理 -->
        <div class="menu-group">播放管理</div>
        <el-menu-item index="/layouts">
          <el-icon><Grid /></el-icon>
          <span>布局管理</span>
        </el-menu-item>
        <el-menu-item index="/playlists">
          <el-icon><VideoPlay /></el-icon>
          <span>播放列表</span>
        </el-menu-item>
        <el-menu-item index="/broadcasts">
          <el-icon><Bell /></el-icon>
          <span>插播管理</span>
        </el-menu-item>

        <!-- 终端管理 -->
        <div class="menu-group">终端管理</div>
        <el-menu-item index="/terminals">
          <el-icon><Monitor /></el-icon>
          <span>终端列表</span>
        </el-menu-item>
        <el-menu-item index="/terminal-preview">
          <el-icon><View /></el-icon>
          <span>终端预览</span>
        </el-menu-item>

        <!-- 活动管理 -->
        <div class="menu-group">活动管理</div>
        <el-menu-item index="/activities">
          <el-icon><Calendar /></el-icon>
          <span>活动列表</span>
        </el-menu-item>
        <el-menu-item index="/volunteer-public">
          <el-icon><User /></el-icon>
          <span>志愿者管理</span>
        </el-menu-item>

        <!-- 系统管理 -->
        <div class="menu-group">系统管理</div>
        <el-menu-item index="/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/ops">
          <el-icon><List /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
        <el-menu-item index="/system">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-title">{{ pageTitle }}</div>
        <div class="user">
          <el-button type="primary" link @click="goPortal">访问门户</el-button>
          <el-avatar :size="32" class="avatar">{{ userStore.username?.charAt(0)?.toUpperCase() || 'U' }}</el-avatar>
          <span class="user-info">{{ userStore.username || '未登录' }}</span>
          <el-button type="danger" link @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { computed } from 'vue';
import { useUserStore } from '../stores/user';
import {
  HomeFilled, Picture, Folder, Document, Grid, VideoPlay, Bell,
  Monitor, View, Calendar, User, UserFilled, List, Setting
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const active = computed(() => route.path);

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/dashboard': '仪表盘',
    '/media': '媒体资源',
    '/categories': '分类管理',
    '/content': '内容管理',
    '/layouts': '布局管理',
    '/playlists': '播放列表',
    '/broadcasts': '插播管理',
    '/terminals': '终端管理',
    '/terminal-preview': '终端预览',
    '/activities': '活动管理',
    '/volunteer-public': '志愿者管理',
    '/users': '用户管理',
    '/ops': '操作日志',
    '/system': '系统设置'
  };
  return titles[route.path] || '志愿者多媒体平台';
});

const onSelect = (path: string) => {
  router.push(path);
};

const logout = () => {
  userStore.logout();
  router.push('/login');
};

const goPortal = () => {
  window.open('/portal', '_blank');
};
</script>

<style scoped>
.aside {
  background: linear-gradient(180deg, #1e3a5f 0%, #0f172a 100%);
  color: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.logo {
  padding: 20px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 8px;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-weight: 600;
  font-size: 16px;
  color: #fff;
}

.menu {
  border-right: none;
  background: transparent;
}

.menu-group {
  padding: 16px 20px 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  text-transform: uppercase;
  letter-spacing: 1px;
}

:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.8);
  margin: 4px 8px;
  border-radius: 8px;
  height: 44px;
  line-height: 44px;
}

:deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
}

:deep(.el-menu-item .el-icon) {
  margin-right: 8px;
  font-size: 18px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 0 24px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  font-weight: 600;
}

.user-info {
  color: #606266;
  font-size: 14px;
}

.main-content {
  background: #f5f7fa;
  padding: 24px;
  min-height: calc(100vh - 60px);
}
</style>
