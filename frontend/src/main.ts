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
import './styles.css';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/report', component: ReportView },
    { path: '/tickets', component: TicketsView },
    { path: '/technician', component: TechnicianView },
    { path: '/admin', component: AdminView },
  ],
});

createApp(App).use(router).use(Vant).mount('#app');
