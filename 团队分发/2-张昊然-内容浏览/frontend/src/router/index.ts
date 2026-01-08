import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import UserPublicPortal from '../views/UserPublicPortal.vue';
import PublicContent from '../views/PublicContent.vue';
import PublicActivities from '../views/PublicActivities.vue';
import VolunteerPortal from '../views/VolunteerPortal.vue';

const routes: RouteRecordRaw[] = [
  { path: '/', component: UserPublicPortal, meta: { public: true } },
  { path: '/user-portal', component: UserPublicPortal, meta: { public: true } },
  { path: '/public-content', component: PublicContent, meta: { public: true } },
  { path: '/public-activities', component: PublicActivities, meta: { public: true } },
  // 志愿者端 - 内容浏览和媒体观看
  { path: '/portal', component: VolunteerPortal, meta: { public: true } }
];

const router = createRouter({ history: createWebHistory(), routes });

export default router;
