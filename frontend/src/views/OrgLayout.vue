<!--
 * @Author: 梁玉杰
 * @Module: 组织端布局框架
 * @Description: 组织端布局框架，包含侧边栏导航和顶部用户信息
-->
<template>
  <el-container style="min-height: 100vh">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span class="logo-icon">🏢</span>
        <span class="logo-text">组织管理平台</span>
      </div>
      <el-menu :default-active="active" class="menu" @select="onSelect">
        <el-menu-item index="/org/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
        </el-menu-item>

        <div class="menu-group">内容管理</div>
        <el-menu-item index="/org/categories">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/org/content">
          <el-icon><Document /></el-icon>
          <span>内容管理</span>
        </el-menu-item>

        <div class="menu-group">媒体管理</div>
        <el-menu-item index="/org/media">
          <el-icon><Picture /></el-icon>
          <span>媒体资源</span>
        </el-menu-item>
        <el-menu-item index="/org/playlists">
          <el-icon><VideoPlay /></el-icon>
          <span>播放列表</span>
        </el-menu-item>

        <div class="menu-group">终端发布</div>
        <el-menu-item index="/org/layouts">
          <el-icon><Grid /></el-icon>
          <span>布局模板</span>
        </el-menu-item>
        <el-menu-item index="/org/broadcasts">
          <el-icon><Bell /></el-icon>
          <span>广播推送</span>
        </el-menu-item>
        <el-menu-item index="/org/terminals">
          <el-icon><Monitor /></el-icon>
          <span>终端管理</span>
        </el-menu-item>

        <div class="menu-group">志愿者管理</div>
        <el-menu-item index="/org/volunteers">
          <el-icon><User /></el-icon>
          <span>志愿者审核</span>
        </el-menu-item>

        <div class="menu-group">活动管理</div>
        <el-menu-item index="/org/activities">
          <el-icon><Calendar /></el-icon>
          <span>活动列表</span>
        </el-menu-item>

        <div class="menu-group">求助管理</div>
        <el-menu-item index="/org/help-requests">
          <el-icon><ChatDotRound /></el-icon>
          <span>求助处理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="org-name">{{ orgName }}</div>
        <div class="user">
          <el-avatar :size="32" class="avatar">{{ username?.charAt(0)?.toUpperCase() || 'O' }}</el-avatar>
          <span class="user-info">{{ username }}</span>
          <el-button type="danger" link @click="logout">退出</el-button>
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
import { computed, ref, onMounted } from 'vue';
import { HomeFilled, Folder, Document, Picture, VideoPlay, Grid, Bell, Monitor, User, Calendar, ChatDotRound } from '@element-plus/icons-vue';
import axios from 'axios';

const router = useRouter();
const route = useRoute();
const username = ref(localStorage.getItem('org_username') || '');
const orgName = ref('');

const active = computed(() => route.path);

const onSelect = (path: string) => {
  router.push(path);
};

const logout = () => {
  localStorage.removeItem('org_token');
  localStorage.removeItem('org_username');
  localStorage.removeItem('org_role');
  router.push('/org/login');
};

onMounted(async () => {
  // 设置页面标题
  document.title = '组织端 - 志愿者多媒体平台';
  
  try {
    const token = localStorage.getItem('org_token');
    const resp = await axios.get('/api/org/info', {
      headers: { Authorization: `Bearer ${token}` }
    });
    if (resp.data.data) {
      orgName.value = resp.data.data.name;
    }
  } catch (e) {
    // ignore
  }
});
</script>

<style scoped>
.aside {
  background: linear-gradient(180deg, #2c5282 0%, #1a365d 100%);
  color: #fff;
}

.logo {
  padding: 20px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon { font-size: 24px; }
.logo-text { font-weight: 600; font-size: 15px; color: #fff; }

.menu { border-right: none; background: transparent; }

.menu-group {
  padding: 12px 20px 6px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  text-transform: uppercase;
}

:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.8);
  margin: 2px 8px;
  border-radius: 8px;
  height: 40px;
}

:deep(.el-menu-item:hover) { background: rgba(255, 255, 255, 0.1); color: #fff; }
:deep(.el-menu-item.is-active) { background: #3182ce; color: #fff; }

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 0 24px;
  height: 50px;
}

.org-name { font-weight: 600; color: #2c5282; }
.user { display: flex; align-items: center; gap: 12px; }
.avatar { background: #3182ce; color: #fff; }
.user-info { color: #606266; font-size: 14px; }
.main-content { background: #f5f7fa; padding: 16px 20px; }
</style>
