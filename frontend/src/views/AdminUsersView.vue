<template>
  <div class="page">
    <div class="section-title">人员管理</div>

    <div class="card">
      <div class="ticket-meta">仅管理员可维护人员信息与角色。</div>
      <div style="margin-top: 12px;">
        <van-button size="small" type="primary" @click="openCreate">新增人员</van-button>
      </div>
    </div>

    <div v-for="user in users" :key="user.id" class="card ticket-card">
      <div class="ticket-header">
        <div>
          <div style="font-weight: 600;">{{ user.name }}</div>
          <div class="ticket-meta">账号 {{ user.userId }} · {{ user.phone }}</div>
        </div>
        <van-tag :type="roleTag(user.roleCode)">{{ roleLabel(user.roleCode) }}</van-tag>
      </div>
      <div class="chip-group" style="margin-top: 8px;">
        <div class="chip" :class="{ warn: user.isActive === 0 }">
          {{ user.isActive === 1 ? '启用中' : '已停用' }}
        </div>
        <div class="chip alt">创建 {{ formatDate(user.createdAt) }}</div>
      </div>
      <div style="margin-top: 12px;">
        <van-button size="small" plain type="primary" @click="openEdit(user)">编辑</van-button>
      </div>
    </div>

    <van-popup v-model:show="showCreate" position="bottom" round>
      <div style="padding: 16px;">
        <div class="section-title">新增人员</div>
        <van-field v-model="createForm.userId" label="用户ID" placeholder="如：u1001" />
        <van-field v-model="createForm.phone" label="手机号" placeholder="请输入手机号" />
        <van-field v-model="createForm.name" label="姓名" placeholder="请输入姓名" />
        <van-field v-model="createForm.password" label="初始密码" type="password" placeholder="至少 6 位" />
        <van-cell-group inset>
          <van-cell title="角色">
            <template #right-icon>
              <van-radio-group v-model="createForm.roleCode" direction="horizontal">
                <van-radio name="USER">用户</van-radio>
                <van-radio name="REPAIRER">维修员</van-radio>
                <van-radio name="ADMIN">管理员</van-radio>
              </van-radio-group>
            </template>
          </van-cell>
        </van-cell-group>
        <div style="margin-top: 12px; display: flex; gap: 8px;">
          <van-button size="small" plain type="primary" @click="generateUserId">生成ID</van-button>
          <van-button size="small" type="primary" @click="createUser">保存</van-button>
          <van-button size="small" plain @click="showCreate = false">取消</van-button>
        </div>
      </div>
    </van-popup>

    <van-popup v-model:show="showEdit" position="bottom" round>
      <div style="padding: 16px;">
        <div class="section-title">编辑人员</div>
        <van-field v-model="editForm.userId" label="用户ID" readonly />
        <van-field v-model="editForm.phone" label="手机号" placeholder="请输入手机号" />
        <van-field v-model="editForm.name" label="姓名" placeholder="请输入姓名" />
        <van-field v-model="editForm.password" label="重置密码" type="password" placeholder="留空则不修改" />
        <van-cell-group inset>
          <van-cell title="角色">
            <template #right-icon>
              <van-radio-group v-model="editForm.roleCode" direction="horizontal">
                <van-radio name="USER">用户</van-radio>
                <van-radio name="REPAIRER">维修员</van-radio>
                <van-radio name="ADMIN">管理员</van-radio>
              </van-radio-group>
            </template>
          </van-cell>
          <van-cell title="账号状态">
            <template #right-icon>
              <van-switch v-model="editForm.isActive" size="20px" />
            </template>
          </van-cell>
        </van-cell-group>
        <div style="margin-top: 12px; display: flex; gap: 8px;">
          <van-button size="small" type="primary" @click="updateUser">保存</van-button>
          <van-button size="small" plain @click="showEdit = false">取消</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { showFailToast, showSuccessToast } from 'vant';
import { api } from '../services/api';

type AdminUser = {
  id: string;
  userId: string;
  phone: string;
  name: string;
  roleCode: 'USER' | 'REPAIRER' | 'ADMIN';
  isActive: number;
  createdAt: string;
};

const users = ref<AdminUser[]>([]);
const showCreate = ref(false);
const showEdit = ref(false);

const createForm = reactive({
  userId: '',
  phone: '',
  name: '',
  password: '',
  roleCode: 'USER',
});

const editForm = reactive({
  id: '',
  userId: '',
  phone: '',
  name: '',
  password: '',
  roleCode: 'USER',
  isActive: true,
});

const loadUsers = async () => {
  const res = await api.get('/admin/users');
  users.value = res.data;
};

const roleLabel = (role: AdminUser['roleCode']) => {
  switch (role) {
    case 'ADMIN':
      return '管理员';
    case 'REPAIRER':
      return '维修员';
    default:
      return '用户';
  }
};

const roleTag = (role: AdminUser['roleCode']) => {
  switch (role) {
    case 'ADMIN':
      return 'danger';
    case 'REPAIRER':
      return 'primary';
    default:
      return 'success';
  }
};

const formatDate = (value: string) => new Date(value).toLocaleDateString();

const generateUserId = () => {
  const seed = Date.now().toString().slice(-6);
  const rand = Math.floor(Math.random() * 90 + 10);
  createForm.userId = `u${seed}${rand}`;
};

const openCreate = () => {
  createForm.userId = '';
  createForm.phone = '';
  createForm.name = '';
  createForm.password = '';
  createForm.roleCode = 'USER';
  showCreate.value = true;
};

const createUser = async () => {
  if (!createForm.userId || !createForm.phone || !createForm.name || !createForm.password) {
    showFailToast('请完整填写信息');
    return;
  }
  await api.post('/admin/users', { ...createForm });
  showCreate.value = false;
  showSuccessToast('人员已新增');
  await loadUsers();
};

const openEdit = (user: AdminUser) => {
  editForm.id = user.id;
  editForm.userId = user.userId;
  editForm.phone = user.phone;
  editForm.name = user.name;
  editForm.password = '';
  editForm.roleCode = user.roleCode;
  editForm.isActive = user.isActive === 1;
  showEdit.value = true;
};

const updateUser = async () => {
  if (!editForm.phone || !editForm.name) {
    showFailToast('请完整填写信息');
    return;
  }
  const payload: Record<string, unknown> = {
    phone: editForm.phone,
    name: editForm.name,
    roleCode: editForm.roleCode,
    isActive: editForm.isActive ? 1 : 0,
  };
  if (editForm.password) {
    payload.password = editForm.password;
  }
  await api.patch(`/admin/users/${editForm.id}`, payload);
  showEdit.value = false;
  showSuccessToast('人员已更新');
  await loadUsers();
};

onMounted(loadUsers);
</script>
