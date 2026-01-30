<template>
  <div class="app-shell">
    <header v-if="!isAuthPage" class="top-bar">
      <div class="brand">
        <div class="brand-mark">DGH</div>
        <div>
          <div class="brand-title">校园温暖服务台</div>
          <div class="brand-sub">白天安心 · 夜晚守护</div>
        </div>
      </div>
      <div class="status-pill">
        <van-icon name="location-o" />
        在线 · 24h
      </div>
      <div class="theme-toggle">
        <van-icon name="sunny-o" />
        <van-switch v-model="isNight" size="20px" />
        <van-icon name="moon-o" />
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
import { computed, onMounted, onUnmounted, ref, watchEffect } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { clearAuth, getAuth } from './services/auth';
import {
  emitThemeUpdate,
  getManualTheme,
  getThemeMode,
  getThemeOverride,
  initThemeStorage,
  setManualTheme,
  setThemeOverride,
  type ThemeMode,
  type ThemeName,
} from './services/theme';

const route = useRoute();
const router = useRouter();
const auth = computed(() => getAuth());

initThemeStorage();

const themeMode = ref<ThemeMode>(getThemeMode());
const manualTheme = ref<ThemeName>(getManualTheme());
const themeOverride = ref<ThemeName | null>(getThemeOverride());
const now = ref(Date.now());

const isNight = computed({
  get: () => effectiveTheme.value === 'night',
  set: (value) => toggleTheme(value ? 'night' : 'day'),
});

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
        { label: '我的', to: '/me', icon: 'contact' },
      ];
    case 'REPAIRER':
      return [
        { label: '维修端', to: '/repairer/home', icon: 'manager' },
        { label: '我的', to: '/me', icon: 'contact' },
      ];
    case 'ADMIN':
      return [
        { label: '管理端', to: '/admin/home', icon: 'setting-o' },
        { label: '人员', to: '/admin/users', icon: 'friends-o' },
        { label: '我的', to: '/me', icon: 'contact' },
      ];
    default:
      return [];
  }
});

const logout = () => {
  clearAuth();
  router.push('/login');
};

const getSystemTheme = () => {
  const hour = new Date(now.value).getHours();
  return hour >= 18 || hour < 6 ? 'night' : 'day';
};

const effectiveTheme = computed<ThemeName>(() => {
  if (themeMode.value === 'auto') {
    return themeOverride.value ?? getSystemTheme();
  }
  return manualTheme.value;
});

const toggleTheme = (nextTheme: ThemeName) => {
  if (themeMode.value === 'auto') {
    themeOverride.value = nextTheme;
    setThemeOverride(nextTheme);
    manualTheme.value = nextTheme;
    setManualTheme(nextTheme);
  } else {
    manualTheme.value = nextTheme;
    setManualTheme(nextTheme);
  }
  emitThemeUpdate();
};

const syncThemeFromStorage = () => {
  themeMode.value = getThemeMode();
  manualTheme.value = getManualTheme();
  themeOverride.value = getThemeOverride();
};

let timer: number | undefined;
onMounted(() => {
  timer = window.setInterval(() => {
    now.value = Date.now();
  }, 60000);
  window.addEventListener('dgh-theme-updated', syncThemeFromStorage);
});

onUnmounted(() => {
  if (timer) {
    window.clearInterval(timer);
  }
  window.removeEventListener('dgh-theme-updated', syncThemeFromStorage);
});

watchEffect(() => {
  document.body.classList.toggle('theme-night', effectiveTheme.value === 'night');
});
</script>
