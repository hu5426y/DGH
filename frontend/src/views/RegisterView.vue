<template>
  <div class="page auth-page">
    <div class="auth-hero">
      <div class="auth-title">注册账号</div>
      <div class="auth-sub">注册后可按角色进入对应工作台</div>
    </div>

    <div class="card auth-card">
      <van-form @submit="register">
        <van-field v-model="form.userId" name="userId" label="用户ID" readonly />
        <van-field v-model="form.phone" name="phone" label="手机号" placeholder="请输入手机号" required />
        <van-field v-model="form.name" name="name" label="姓名" placeholder="请输入姓名" required />
        <van-field
          v-model="form.password"
          name="password"
          type="password"
          label="密码"
          placeholder="设置登录密码"
          required
        />
        <van-field
          v-model="roleLabel"
          label="角色"
          placeholder="选择角色"
          readonly
          is-link
          @click="showRolePicker = true"
        />
        <van-button round block type="primary" native-type="submit">注册并登录</van-button>
      </van-form>
    </div>

    <div class="auth-footer">
      <span>已有账号？</span>
      <van-button plain type="primary" size="small" @click="goLogin">去登录</van-button>
    </div>

    <van-popup v-model:show="showRolePicker" position="bottom" round>
      <van-picker :columns="roleOptions" @confirm="onRoleConfirm" @cancel="showRolePicker = false" />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '../services/api';
import { setAuth } from '../services/auth';

const router = useRouter();
const showRolePicker = ref(false);

const roleOptions = [
  { text: '普通用户', value: 'USER' },
  { text: '维修人员', value: 'REPAIRER' },
  { text: '管理员', value: 'ADMIN' },
];

const form = reactive({
  userId: '',
  phone: '',
  name: '',
  password: '',
  roleCode: 'USER',
});

const roleLabel = computed(() => {
  return roleOptions.find((item) => item.value === form.roleCode)?.text ?? '';
});

const onRoleConfirm = (value: unknown) => {
  const picked = Array.isArray(value) ? value[0] : value;
  if (picked && typeof picked === 'object' && 'value' in picked) {
    form.roleCode = String((picked as { value: string }).value);
  } else if (typeof picked === 'string') {
    form.roleCode = picked;
  }
  showRolePicker.value = false;
};

const register = async () => {
  const res = await api.post('/auth/register', { ...form });
  setAuth(res.data);
  goHome(res.data.roleCode);
};

const goHome = (roleCode: string) => {
  if (roleCode === 'USER') {
    router.push('/user/home');
  } else if (roleCode === 'REPAIRER') {
    router.push('/repairer/home');
  } else {
    router.push('/admin/home');
  }
};

const goLogin = () => {
  router.push('/login');
};

const generateUserId = () => {
  const seed = Date.now().toString().slice(-6);
  const rand = Math.floor(Math.random() * 90 + 10);
  form.userId = `u${seed}${rand}`;
};

onMounted(generateUserId);
</script>
