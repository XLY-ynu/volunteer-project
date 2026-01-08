import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import Login from '../views/Login.vue';
import Layout from '../views/Layout.vue';
import Broadcasts from '../views/Broadcasts.vue';
import Terminals from '../views/Terminals.vue';
import TerminalPreview from '../views/TerminalPreview.vue';
import UserPublicPortal from '../views/UserPublicPortal.vue';

const routes: RouteRecordRaw[] = [
  { path: '/login', component: Login },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/broadcasts' },
      { path: '/broadcasts', component: Broadcasts },
      { path: '/terminals', component: Terminals },
      { path: '/terminal-preview', component: TerminalPreview }
    ]
  },
  // 普通用户端 - 发布求助
  { path: '/user-portal', component: UserPublicPortal, meta: { public: true } }
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('org_token');
  if (to.meta.public) { next(); return; }
  if (to.path === '/login') { next(); return; }
  if (!token) { next('/login'); return; }
  next();
});

export default router;
