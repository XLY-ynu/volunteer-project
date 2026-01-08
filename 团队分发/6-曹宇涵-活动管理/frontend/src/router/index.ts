import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import Login from '../views/Login.vue';
import Layout from '../views/Layout.vue';
import OrgActivities from '../views/OrgActivities.vue';
import OrgHelpRequests from '../views/OrgHelpRequests.vue';

const routes: RouteRecordRaw[] = [
  { path: '/login', component: Login },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/activities' },
      { path: '/activities', component: OrgActivities },
      { path: '/help-requests', component: OrgHelpRequests }
    ]
  }
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('org_token');
  if (to.path === '/login') { next(); return; }
  if (!token) { next('/login'); return; }
  next();
});

export default router;
