<template>
  <div class="page">
    <div class="section-title">维修端工作台</div>

    <div class="card">
      <div style="font-weight: 600;">今日作业提示</div>
      <div class="ticket-meta">请及时签到并更新工单状态，系统将自动记录维修时效。</div>
      <div style="display: flex; gap: 8px; margin-top: 10px;">
        <van-field v-model="technicianName" label="维修员" placeholder="输入姓名" />
        <van-field v-model="checkinLocation" label="签到位置" placeholder="输入签到位置" />
      </div>
    </div>

    <van-empty v-if="loading" description="加载中..." />
    <div v-else class="page">
      <div v-for="ticket in tickets" :key="ticket.id" class="card ticket-card">
        <div class="ticket-header">
          <div>
            <div style="font-weight: 600;">{{ ticket.title }}</div>
            <div class="ticket-meta">{{ ticket.locationName || ticket.locationId }} · {{ formatDate(ticket.createdAt) }}</div>
          </div>
          <van-tag :type="statusTag(ticket.status)">{{ statusText(ticket.status) }}</van-tag>
        </div>
        <div>{{ ticket.description }}</div>
        <div class="ticket-meta">报修人：{{ ticket.reporterName }}</div>
        <div class="ticket-meta" v-if="ticket.assignedToName">
          当前负责人：{{ ticket.assignedToName }}
        </div>

        <div style="display: flex; gap: 8px; margin-top: 8px; flex-wrap: wrap;">
          <van-button size="small" type="primary" @click="updateStatus(ticket.id, 'in_progress')">开始处理</van-button>
          <van-button size="small" type="success" @click="updateStatus(ticket.id, 'completed')">完成</van-button>
          <van-button size="small" type="warning" @click="signIn(ticket.id)">打卡签到</van-button>
        </div>

        <div v-if="ticket.checkIns.length" class="timeline">
          <div v-for="item in ticket.checkIns" :key="item.id" class="timeline-item">
            {{ item.technicianName }} · {{ item.location }} · {{ formatDate(item.createdAt) }}
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

const formatDate = (value: string) => new Date(value).toLocaleString();

onMounted(loadTickets);
</script>
