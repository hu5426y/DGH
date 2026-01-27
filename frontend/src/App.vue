<template>
  <div class="app-shell">
    <header v-if="!isAuthPage" class="top-bar">
      <div class="brand">
        <div class="brand-mark">DGH</div>
        <div>
          <div class="brand-title">设备报修指挥台</div>
          <div class="brand-sub">校园·社区维保全流程</div>
        </div>
      </div>
      <div class="status-pill">
        <van-icon name="location-o" />
        在线 · 24h
      </div>
      <van-button v-if="auth" size="small" plain type="primary" @click="logout">退出</van-button>
    </header>

    <main class="app-main">
      <router-view />
    </main>

    <van-tabbar v-if="showTabbar" route class="glass-tabbar">
      <van-tabbar-item
        v-for="item in tabItems"
        :key="item.to"
        :to="item.to"
        :icon="item.icon"
      >
        {{ item.label }}
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { clearAuth, getAuth } from './services/auth';

const route = useRoute();
const router = useRouter();
const auth = computed(() => getAuth());

const isAuthPage = computed(() => route.path === '/login' || route.path === '/register');
const showTabbar = computed(() => !!auth.value && !isAuthPage.value);

const tabItems = computed(() => {
  if (!auth.value) return [];
  switch (auth.value.roleCode) {
    case 'USER':
      return [
        { label: '首页', to: '/user/home', icon: 'home-o' },
        { label: '报修', to: '/user/report', icon: 'edit' },
        { label: '工单', to: '/user/tickets', icon: 'records' },
      ];
    case 'REPAIRER':
      return [{ label: '维修端', to: '/repairer/home', icon: 'manager' }];
    case 'ADMIN':
      return [{ label: '管理端', to: '/admin/home', icon: 'setting-o' }];
    default:
      return [];
  }
});

const logout = () => {
  clearAuth();
  router.push('/login');
};
</script>
