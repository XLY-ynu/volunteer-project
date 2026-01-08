import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import Login from '../views/Login.vue';
import Dashboard from '../views/Dashboard.vue';
import Layout from '../views/Layout.vue';
import Users from '../views/Users.vue';
import System from '../views/System.vue';
import OperationLogs from '../views/OperationLogs.vue';

const routes: RouteRecordRaw[] = [
  { path: '/login', component: Login },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: '/dashboard', component: Dashboard },
      { path: '/users', component: Users },
      { path: '/ops', component: OperationLogs },
      { path: '/system', component: System }
    ]
  }
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token');
  if (to.path === '/login') { next(); return; }
  if (!token) { next('/login'); return; }
  next();
});

export default router;
