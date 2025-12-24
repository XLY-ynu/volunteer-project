import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import Login from '../views/Login.vue';
import Dashboard from '../views/Dashboard.vue';
import Layout from '../views/Layout.vue';
import Media from '../views/Media.vue';
import Categories from '../views/Categories.vue';
import Playlists from '../views/Playlists.vue';
import Terminals from '../views/Terminals.vue';
import Content from '../views/Content.vue';

const routes: RouteRecordRaw[] = [
  { path: '/login', component: Login },
  {
    path: '/',
    component: Layout,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: '/dashboard', component: Dashboard },
      { path: '/media', component: Media },
      { path: '/categories', component: Categories },
      { path: '/content', component: Content },
      { path: '/playlists', component: Playlists },
      { path: '/terminals', component: Terminals }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token');
  if (to.path !== '/login' && !token) {
    next('/login');
  } else {
    next();
  }
});

export default router;
