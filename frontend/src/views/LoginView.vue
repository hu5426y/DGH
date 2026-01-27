<template>
  <div class="page auth-page">
    <div class="auth-hero">
      <div class="auth-title">DGH 设备报修</div>
      <div class="auth-sub">请选择登录方式进入对应角色工作台</div>
    </div>

    <div class="card auth-card">
      <van-tabs v-model:active="activeTab" animated>
        <van-tab title="账号密码">
          <van-form @submit="loginPassword">
            <van-field v-model="passwordForm.userId" name="userId" label="用户ID" placeholder="如：u1001" required />
            <van-field
              v-model="passwordForm.password"
              name="password"
              type="password"
              label="密码"
              placeholder="请输入密码"
              required
            />
            <van-button round block type="primary" native-type="submit">登录</van-button>
          </van-form>
        </van-tab>
        <van-tab title="手机验证码">
          <van-form @submit="loginCode">
            <van-field v-model="codeForm.phone" name="phone" label="手机号" placeholder="请输入手机号" required />
            <van-field v-model="codeForm.code" name="code" label="验证码" placeholder="4位数字" required>
              <template #button>
                <van-button
                  size="small"
                  type="primary"
                  :loading="sending"
                  :disabled="!canSendCode"
                  @click.prevent="sendCode"
                >
                  {{ sendLabel }}
                </van-button>
              </template>
            </van-field>
            <van-button round block type="primary" native-type="submit">登录</van-button>
          </van-form>
        </van-tab>
      </van-tabs>
    </div>

    <div class="auth-footer">
      <span>没有账号？</span>
      <van-button plain type="primary" size="small" @click="goRegister">去注册</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '../services/api';
import { setAuth } from '../services/auth';

const router = useRouter();
const activeTab = ref(0);
const sending = ref(false);
const countdown = ref(0);
let timer: number | undefined;

const passwordForm = reactive({
  userId: '',
  password: '',
});

const codeForm = reactive({
  phone: '',
  code: '',
});

const canSendCode = computed(() => countdown.value === 0 && !sending.value && !!codeForm.phone);
const sendLabel = computed(() => (countdown.value > 0 ? `已发送(${countdown.value}s)` : '发送验证码'));

const goHome = (roleCode: string) => {
  if (roleCode === 'USER') {
    router.push('/user/home');
  } else if (roleCode === 'REPAIRER') {
    router.push('/repairer/home');
  } else {
    router.push('/admin/home');
  }
};

const loginPassword = async () => {
  const res = await api.post('/auth/login/password', { ...passwordForm });
  setAuth(res.data);
  goHome(res.data.roleCode);
};

const sendCode = async () => {
  if (!canSendCode.value) return;
  sending.value = true;
  try {
    await api.post('/auth/login/code/send', { phone: codeForm.phone });
    startCountdown();
  } finally {
    sending.value = false;
  }
};

const loginCode = async () => {
  const res = await api.post('/auth/login/code', { ...codeForm });
  setAuth(res.data);
  goHome(res.data.roleCode);
};

const goRegister = () => {
  router.push('/register');
};

const startCountdown = () => {
  if (timer) window.clearInterval(timer);
  countdown.value = 60;
  timer = window.setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0 && timer) {
      window.clearInterval(timer);
      timer = undefined;
      countdown.value = 0;
    }
  }, 1000);
};

onUnmounted(() => {
  if (timer) window.clearInterval(timer);
});
</script>
