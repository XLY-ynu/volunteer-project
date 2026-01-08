import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import Login from '../views/Login.vue';
import Layout from '../views/Layout.vue';
import Media from '../views/Media.vue';
import Playlists from '../views/Playlists.vue';
import VolunteerPortal from '../views/VolunteerPortal.vue';
import Checkin from '../views/Checkin.vue';
import VolunteerPublic from '../views/VolunteerPublic.vue';

const routes: RouteRecordRaw[] = [
  { path: '/login', component: Login },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/media' },
      { path: '/media', component: Media },
      { path: '/playlists', component: Playlists }
    ]
  },
  // 志愿者端 - 活动参与
  { path: '/portal', component: VolunteerPortal, meta: { public: true } },
  { path: '/checkin', component: Checkin, meta: { public: true } },
  { path: '/volunteer-public', component: VolunteerPublic, meta: { public: true } }
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
