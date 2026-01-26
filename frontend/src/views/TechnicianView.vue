<template>
  <div class="page">
    <div class="section-title">维修端工作台</div>
    <van-empty v-if="loading" description="加载中..." />
    <div v-else>
      <div v-for="ticket in tickets" :key="ticket.id" class="card" style="margin-bottom: 12px;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <div style="font-weight: 600;">{{ ticket.title }}</div>
            <div style="font-size: 12px; color: #969799;">{{ ticket.location }}</div>
          </div>
          <van-tag :type="statusTag(ticket.status)">{{ statusText(ticket.status) }}</van-tag>
        </div>
        <p style="margin-top: 8px;">{{ ticket.description }}</p>
        <van-field v-model="technicianName" label="维修员" placeholder="输入姓名" />
        <van-field v-model="checkinLocation" label="签到位置" placeholder="输入签到位置" />
        <div style="display: flex; gap: 8px; margin-top: 8px;">
          <van-button size="small" type="primary" @click="updateStatus(ticket.id, 'in_progress')">开始处理</van-button>
          <van-button size="small" type="success" @click="updateStatus(ticket.id, 'completed')">完成</van-button>
          <van-button size="small" type="warning" @click="signIn(ticket.id)">打卡签到</van-button>
        </div>
        <div v-if="ticket.checkIns.length" style="margin-top: 8px;">
          <div v-for="item in ticket.checkIns" :key="item.id" style="font-size: 12px; color: #969799;">
            {{ item.technicianName }} 于 {{ new Date(item.createdAt).toLocaleString() }} 在 {{ item.location }} 签到
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { api, type Ticket } from '../services/api';

const tickets = ref<Ticket[]>([]);
const loading = ref(true);
const technicianName = ref('');
const checkinLocation = ref('');

const loadTickets = async () => {
  const response = await api.get('/tickets');
  tickets.value = response.data;
  loading.value = false;
};

const updateStatus = async (id: string, status: Ticket['status']) => {
  await api.patch(`/tickets/${id}/status`, { status });
  await loadTickets();
};

const signIn = async (id: string) => {
  await api.post(`/tickets/${id}/checkins`, {
    technicianName: technicianName.value || '未命名维修员',
    location: checkinLocation.value || '默认位置',
  });
  await loadTickets();
};

const statusText = (status: Ticket['status']) => {
  switch (status) {
    case 'pending':
      return '待处理';
    case 'in_progress':
      return '处理中';
    case 'completed':
      return '已完成';
    default:
      return status;
  }
};

const statusTag = (status: Ticket['status']) => {
  switch (status) {
    case 'pending':
      return 'warning';
    case 'in_progress':
      return 'primary';
    case 'completed':
      return 'success';
    default:
      return 'default';
  }
};

onMounted(loadTickets);
</script>
