import { createApp } from 'vue';
import { createRouter, createWebHistory } from 'vue-router';
import Vant from 'vant';
import 'vant/lib/index.css';
import App from './App.vue';
import HomeView from './views/HomeView.vue';
import ReportView from './views/ReportView.vue';
import TicketsView from './views/TicketsView.vue';
import TechnicianView from './views/TechnicianView.vue';
import AdminView from './views/AdminView.vue';
import LoginView from './views/LoginView.vue';
import RegisterView from './views/RegisterView.vue';
import './styles.css';
import { getAuth } from './services/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginView, meta: { public: true } },
    { path: '/register', component: RegisterView, meta: { public: true } },
    { path: '/user/home', component: HomeView, meta: { role: 'USER' } },
    { path: '/user/report', component: ReportView, meta: { role: 'USER' } },
    { path: '/user/tickets', component: TicketsView, meta: { role: 'USER' } },
    { path: '/repairer/home', component: TechnicianView, meta: { role: 'REPAIRER' } },
    { path: '/admin/home', component: AdminView, meta: { role: 'ADMIN' } },
  ],
});

router.beforeEach((to) => {
  const auth = getAuth();
  if (to.meta.public) {
    if (auth && (to.path === '/login' || to.path === '/register')) {
      return auth.roleCode === 'USER'
        ? '/user/home'
        : auth.roleCode === 'REPAIRER'
          ? '/repairer/home'
          : '/admin/home';
    }
    return true;
  }
  if (!auth) {
    return '/login';
  }
  const role = to.meta.role as string | undefined;
  if (role && auth.roleCode !== role) {
    return auth.roleCode === 'USER'
      ? '/user/home'
      : auth.roleCode === 'REPAIRER'
        ? '/repairer/home'
        : '/admin/home';
  }
  return true;
});

createApp(App).use(router).use(Vant).mount('#app');
