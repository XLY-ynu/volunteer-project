import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import Login from '../views/Login.vue';
import Layout from '../views/Layout.vue';
import OrgDashboard from '../views/OrgDashboard.vue';
import Categories from '../views/Categories.vue';
import Content from '../views/Content.vue';
import OrgVolunteers from '../views/OrgVolunteers.vue';

const routes: RouteRecordRaw[] = [
  { path: '/login', component: Login },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: '/dashboard', component: OrgDashboard },
      { path: '/categories', component: Categories },
      { path: '/content', component: Content },
      { path: '/volunteers', component: OrgVolunteers }
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
