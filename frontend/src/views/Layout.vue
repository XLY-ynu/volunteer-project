<!--
 * @Author: 陈力宏
 * @Module: 系统管理 - 管理员端布局
 * @Description: 管理员端布局框架，包含侧边栏导航和顶部用户信息
-->
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

        <!-- 系统管理 -->
        <div class="menu-group">系统管理</div>
        <el-menu-item index="/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/volunteers">
          <el-icon><Avatar /></el-icon>
          <span>志愿者审核</span>
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
        <div class="user">
          <el-button type="primary" link @click="goPortal">访问门户</el-button>
          <el-avatar :size="32" class="avatar">{{ adminUsername?.charAt(0)?.toUpperCase() || 'U' }}</el-avatar>
          <span class="user-info">{{ adminUsername }}</span>
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
import { computed, onMounted } from 'vue';
import {
  HomeFilled, UserFilled, List, Setting, Avatar
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();

// 设置页面标题
onMounted(() => {
  document.title = '管理员端 - 志愿者多媒体平台';
});

const active = computed(() => route.path);

// 从 sessionStorage 获取管理员用户名
const adminUsername = computed(() => sessionStorage.getItem('admin_username') || '未登录');

const onSelect = (path: string) => {
  router.push(path);
};

const logout = () => {
  // 清除管理员端的 token
  sessionStorage.removeItem('admin_token');
  sessionStorage.removeItem('admin_username');
  sessionStorage.removeItem('admin_role');
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
  padding: 12px 20px 6px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  text-transform: uppercase;
  letter-spacing: 1px;
}

:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.8);
  margin: 2px 8px;
  border-radius: 8px;
  height: 40px;
  line-height: 40px;
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
  justify-content: flex-end;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 0 24px;
  height: 50px;
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
  padding: 16px 20px;
  min-height: calc(100vh - 50px);
}
</style>
