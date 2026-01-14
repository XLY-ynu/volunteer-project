import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import Login from '../views/Login.vue';
import Dashboard from '../views/Dashboard.vue';
import Layout from '../views/Layout.vue';
import Media from '../views/Media.vue';
import Categories from '../views/Categories.vue';
import Playlists from '../views/Playlists.vue';
import Terminals from '../views/Terminals.vue';
import Content from '../views/Content.vue';
import Layouts from '../views/Layouts.vue';
import Broadcasts from '../views/Broadcasts.vue';
import OperationLogs from '../views/OperationLogs.vue';
import Users from '../views/Users.vue';
import System from '../views/System.vue';
import PublicContent from '../views/PublicContent.vue';
import TerminalPreview from '../views/TerminalPreview.vue';
import Activities from '../views/Activities.vue';
import VolunteerPublic from '../views/VolunteerPublic.vue';
import VolunteerAudit from '../views/VolunteerAudit.vue';
import PublicActivities from '../views/PublicActivities.vue';
import Checkin from '../views/Checkin.vue';
import UserPortal from '../views/UserPortal.vue';
import VolunteerPortal from '../views/VolunteerPortal.vue';
import ScreenPlayer from '../views/ScreenPlayer.vue';
// 组织端
import OrgLayout from '../views/OrgLayout.vue';
import OrgLogin from '../views/OrgLogin.vue';
import OrgDashboard from '../views/OrgDashboard.vue';
import OrgVolunteers from '../views/OrgVolunteers.vue';
import OrgHelpRequests from '../views/OrgHelpRequests.vue';
import OrgActivities from '../views/OrgActivities.vue';
// 用户门户
import UserPublicPortal from '../views/UserPublicPortal.vue';

const routes: RouteRecordRaw[] = [
  // 管理员端登录
  { path: '/login', component: Login },
  // 组织端登录
  { path: '/org/login', component: OrgLogin },
  // 公开页面
  { path: '/checkin', component: Checkin, meta: { public: true } },
  { path: '/portal', component: VolunteerPortal, meta: { public: true } },
  { path: '/user-portal', component: UserPublicPortal, meta: { public: true } },
  { path: '/portal-old', component: UserPortal, meta: { public: true } },
  { path: '/screen', component: ScreenPlayer, meta: { public: true } },
  
  // 管理员端（系统管理）
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: '/dashboard', component: Dashboard },
      { path: '/users', component: Users },
      { path: '/volunteers', component: VolunteerAudit },
      { path: '/ops', component: OperationLogs },
      { path: '/system', component: System }
    ]
  },
  
  // 组织端
  {
    path: '/org',
    component: OrgLayout,
    meta: { requiresAuth: true, role: 'ORG' },
    children: [
      { path: '', redirect: '/org/dashboard' },
      { path: 'dashboard', component: OrgDashboard },
      // 内容展示管理
      { path: 'categories', component: Categories },
      { path: 'content', component: Content },
      // 视频展示管理
      { path: 'media', component: Media },
      { path: 'playlists', component: Playlists },
      // 多媒体信息发布
      { path: 'layouts', component: Layouts },
      { path: 'broadcasts', component: Broadcasts },
      { path: 'terminals', component: Terminals },
      { path: 'terminal-preview', component: TerminalPreview },
      // 志愿者管理
      { path: 'volunteers', component: OrgVolunteers },
      // 活动管理
      { path: 'activities', component: OrgActivities },
      // 求助管理
      { path: 'help-requests', component: OrgHelpRequests }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, _from, next) => {
  const isPublic = to.meta.public;
  const isOrgLogin = to.path === '/org/login';
  const isAdminLogin = to.path === '/login';
  
  // 公开页面直接放行
  if (isPublic || isOrgLogin || isAdminLogin) {
    next();
    return;
  }
  
  // 根据路由检查对应的token (使用sessionStorage)
  if (to.path.startsWith('/org')) {
    // 组织端路由检查 org_token
    const orgToken = sessionStorage.getItem('org_token');
    if (!orgToken) {
      next('/org/login');
      return;
    }
  } else {
    // 管理员端路由检查 admin_token
    const adminToken = sessionStorage.getItem('admin_token');
    if (!adminToken) {
      next('/login');
      return;
    }
  }
  
  next();
});

export default router;
