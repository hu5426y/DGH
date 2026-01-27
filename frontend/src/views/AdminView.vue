<template>
  <div class="page">
    <div class="section-title">管理端</div>

    <div class="card">
      <div style="font-weight: 600;">数据统计</div>
      <div class="metric-grid" style="margin-top: 12px;">
        <div class="metric-card">
          <div class="metric-value">{{ stats.total }}</div>
          <div class="metric-label">总工单</div>
        </div>
        <div class="metric-card">
          <div class="metric-value">{{ stats.completed }}</div>
          <div class="metric-label">已完成</div>
        </div>
        <div class="metric-card">
          <div class="metric-value">{{ stats.avgRating }}</div>
          <div class="metric-label">平均评分</div>
        </div>
        <div class="metric-card">
          <div class="metric-value">{{ Object.keys(stats.frequentLocations).length }}</div>
          <div class="metric-label">高频故障点</div>
        </div>
      </div>
      <div v-if="Object.keys(stats.frequentLocations).length" style="margin-top: 12px;">
        <div class="ticket-meta">高频故障点</div>
        <div style="margin-top: 6px; display: flex; flex-wrap: wrap; gap: 6px;">
          <div v-for="(count, location) in stats.frequentLocations" :key="location" class="tag">
            {{ location }} · {{ count }}
          </div>
        </div>
      </div>
    </div>

    <div class="section-title">工单分配</div>
    <div v-for="ticket in tickets" :key="ticket.id" class="card ticket-card">
      <div class="ticket-header">
        <div>
          <div style="font-weight: 600;">{{ ticket.title }}</div>
          <div class="ticket-meta">{{ ticket.locationName || ticket.locationId }} · {{ formatDate(ticket.createdAt) }}</div>
        </div>
        <van-tag :type="statusTag(ticket.status)">{{ statusText(ticket.status) }}</van-tag>
      </div>
      <div class="ticket-meta">{{ ticket.description }}</div>
      <van-field v-model="assign.name" label="维修员" placeholder="输入维修员姓名" />
      <van-field v-model="assign.contact" label="联系方式" placeholder="输入联系方式" />
      <van-button size="small" type="primary" @click="assignTicket(ticket.id)">派单</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { api, type Ticket } from '../services/api';

const tickets = ref<Ticket[]>([]);
const stats = reactive({
  total: 0,
  completed: 0,
  avgRating: 0,
  frequentLocations: {} as Record<string, number>,
});
const assign = reactive({
  name: '',
  contact: '',
});

const loadData = async () => {
  const [ticketRes, statsRes] = await Promise.all([api.get('/tickets'), api.get('/stats')]);
  tickets.value = ticketRes.data;
  Object.assign(stats, statsRes.data);
};

const assignTicket = async (id: string) => {
  await api.patch(`/tickets/${id}/assign`, {
    assignedToName: assign.name,
    assignedToContact: assign.contact,
  });
  assign.name = '';
  assign.contact = '';
  await loadData();
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

onMounted(loadData);
</script>
