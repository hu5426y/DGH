<template>
  <div class="page">
    <div class="section-title">我的</div>

    <section class="card profile-header">
      <div class="profile-avatar">
        <van-image v-if="avatarUrl" :src="avatarUrl" width="72" height="72" fit="cover" radius="18" />
        <div v-else class="avatar-fallback">{{ avatarInitial }}</div>
      </div>
      <div class="profile-info">
        <div class="profile-name">{{ auth?.name || '未命名用户' }}</div>
        <div class="profile-sub">账号：{{ auth?.userId || '--' }}</div>
        <div class="profile-sub">手机号：{{ auth?.phone || '--' }}</div>
      </div>
      <div class="profile-badges">
        <div class="pill">在线</div>
        <div class="pill pill-soft">角色：{{ roleLabel }}</div>
      </div>
    </section>

    <section class="card profile-section">
      <div class="section-title">个人签名</div>
      <van-field
        v-model="intro"
        type="textarea"
        rows="2"
        placeholder="写一句话介绍自己或维修风格"
      />
      <van-button size="small" type="primary" @click="saveIntro">保存签名</van-button>
    </section>

    <section class="card profile-section">
      <div class="section-title">头像设置</div>
      <div class="ticket-meta">支持上传图片或直接填写头像链接。</div>
      <van-uploader :after-read="onAvatarRead" accept="image/*" :max-count="1" />
      <van-field v-model="avatarInput" label="头像链接" placeholder="https://example.com/avatar.png" />
      <div class="profile-actions">
        <van-button size="small" plain type="primary" @click="applyAvatarUrl">应用链接</van-button>
        <van-button size="small" plain type="warning" @click="resetAvatar">恢复默认</van-button>
      </div>
    </section>

    <section class="card profile-section">
      <div class="section-title">账号安全</div>
      <div class="profile-grid">
        <div class="profile-panel">
          <div class="panel-title">换手机号</div>
          <van-field v-model="phoneForm.phone" label="新手机号" placeholder="请输入新手机号" />
          <van-button size="small" type="primary" @click="updatePhone">保存手机号</van-button>
        </div>
        <div class="profile-panel">
          <div class="panel-title">修改密码</div>
          <van-field v-model="passwordForm.oldPassword" label="旧密码" type="password" placeholder="请输入旧密码" />
          <van-field v-model="passwordForm.newPassword" label="新密码" type="password" placeholder="请输入新密码" />
          <van-field
            v-model="passwordForm.confirmPassword"
            label="确认新密码"
            type="password"
            placeholder="再次输入新密码"
          />
          <van-button size="small" type="primary" @click="updatePassword">保存密码</van-button>
        </div>
      </div>
    </section>

    <section class="card profile-section">
      <div class="section-title">通知偏好</div>
      <van-cell-group inset>
        <van-cell title="短信提醒">
          <template #right-icon>
            <van-switch v-model="preferences.sms" size="20px" />
          </template>
        </van-cell>
        <van-cell title="微信消息">
          <template #right-icon>
            <van-switch v-model="preferences.wechat" size="20px" />
          </template>
        </van-cell>
        <van-cell title="工单超时预警">
          <template #right-icon>
            <van-switch v-model="preferences.overdue" size="20px" />
          </template>
        </van-cell>
      </van-cell-group>
      <van-button size="small" type="primary" @click="savePreferences">保存偏好</van-button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { showFailToast, showSuccessToast } from 'vant';
import { getAuth, setAuth, type AuthState } from '../services/auth';

const auth = ref<AuthState | null>(getAuth());

const avatarStorageKey = 'dgh_profile_avatar';
const introStorageKey = 'dgh_profile_intro';
const preferenceStorageKey = 'dgh_profile_preferences';

const avatarUrl = ref(localStorage.getItem(avatarStorageKey) || '');
const avatarInput = ref(avatarUrl.value);
const intro = ref(localStorage.getItem(introStorageKey) || '');
const preferences = reactive(
  JSON.parse(localStorage.getItem(preferenceStorageKey) || '{"sms":true,"wechat":true,"overdue":true}')
);

const roleLabel = computed(() => {
  switch (auth.value?.roleCode) {
    case 'ADMIN':
      return '管理员';
    case 'REPAIRER':
      return '维修员';
    default:
      return '用户';
  }
});

const avatarInitial = computed(() => (auth.value?.name || 'D').slice(0, 1));

const saveIntro = () => {
  localStorage.setItem(introStorageKey, intro.value);
  showSuccessToast('签名已保存');
};

const onAvatarRead = (file: { content?: string } | Array<{ content?: string }>) => {
  const item = Array.isArray(file) ? file[0] : file;
  if (!item?.content) {
    showFailToast('无法读取图片');
    return;
  }
  avatarUrl.value = item.content;
  avatarInput.value = item.content;
  localStorage.setItem(avatarStorageKey, item.content);
  showSuccessToast('头像已更新');
};

const applyAvatarUrl = () => {
  if (!avatarInput.value.trim()) {
    showFailToast('请输入头像链接');
    return;
  }
  avatarUrl.value = avatarInput.value.trim();
  localStorage.setItem(avatarStorageKey, avatarUrl.value);
  showSuccessToast('头像链接已应用');
};

const resetAvatar = () => {
  avatarUrl.value = '';
  avatarInput.value = '';
  localStorage.removeItem(avatarStorageKey);
  showSuccessToast('已恢复默认头像');
};

const phoneForm = reactive({
  phone: auth.value?.phone || '',
});

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const updatePhone = () => {
  const trimmed = phoneForm.phone.trim();
  if (!/^\d{11}$/.test(trimmed)) {
    showFailToast('手机号格式不正确');
    return;
  }
  if (!auth.value) {
    showFailToast('未登录，无法保存');
    return;
  }
  const next = { ...auth.value, phone: trimmed };
  setAuth(next);
  auth.value = next;
  showSuccessToast('手机号已更新');
};

const updatePassword = () => {
  if (!passwordForm.newPassword || passwordForm.newPassword.length < 6) {
    showFailToast('密码至少 6 位');
    return;
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    showFailToast('两次输入的新密码不一致');
    return;
  }
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  showSuccessToast('密码已更新');
};

const persistPreferences = () => {
  localStorage.setItem(preferenceStorageKey, JSON.stringify(preferences));
};

const savePreferences = () => {
  persistPreferences();
  showSuccessToast('偏好已保存');
};

watch(
  () => preferences,
  () => persistPreferences(),
  { deep: true }
);
</script>
