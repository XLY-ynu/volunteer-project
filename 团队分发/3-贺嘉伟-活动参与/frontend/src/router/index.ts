import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import VolunteerPortal from '../views/VolunteerPortal.vue';
import UserPublicPortal from '../views/UserPublicPortal.vue';

const routes: RouteRecordRaw[] = [
  { path: '/', component: VolunteerPortal, meta: { public: true } },
  { path: '/portal', component: VolunteerPortal, meta: { public: true } },
  { path: '/user-portal', component: UserPublicPortal, meta: { public: true } }
];

const router = createRouter({ history: createWebHistory(), routes });

export default router;
