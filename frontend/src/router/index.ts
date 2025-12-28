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
import PublicActivities from '../views/PublicActivities.vue';
import Checkin from '../views/Checkin.vue';
import UserPortal from '../views/UserPortal.vue';
import ScreenPlayer from '../views/ScreenPlayer.vue';

const routes: RouteRecordRaw[] = [
  { path: '/login', component: Login },
  { path: '/checkin', component: Checkin, meta: { public: true } },
  { path: '/portal', component: UserPortal, meta: { public: true } },
  { path: '/screen', component: ScreenPlayer, meta: { public: true } },
  {
    path: '/',
    component: Layout,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: '/dashboard', component: Dashboard },
      { path: '/media', component: Media },
      { path: '/categories', component: Categories },
      { path: '/content', component: Content },
      { path: '/layouts', component: Layouts },
      { path: '/playlists', component: Playlists },
      { path: '/terminals', component: Terminals },
      { path: '/broadcasts', component: Broadcasts },
      { path: '/ops', component: OperationLogs },
      { path: '/users', component: Users },
      { path: '/system', component: System },
      { path: '/public-content', component: PublicContent },
      { path: '/terminal-preview', component: TerminalPreview },
      { path: '/activities', component: Activities },
      { path: '/volunteer-public', component: VolunteerPublic }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token');
  if (to.path !== '/login' && !to.meta.public && !token) {
    next('/login');
  } else {
    next();
  }
});

export default router;
