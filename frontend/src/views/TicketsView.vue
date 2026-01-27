<template>
  <div class="page">
    <div class="section-title">我的工单</div>
    <div class="summary-grid">
      <div class="summary-card">
        <div class="summary-label">全部工单</div>
        <div class="metric-value">{{ tickets.length }}</div>
      </div>
      <div class="summary-card">
        <div class="summary-label">处理中</div>
        <div class="metric-value">{{ statusCounts.inProgress }}</div>
      </div>
      <div class="summary-card">
        <div class="summary-label">已完成</div>
        <div class="metric-value">{{ statusCounts.completed }}</div>
      </div>
    </div>

    <div class="chip-group">
      <button class="chip" :class="{ alt: activeFilter === 'all' }" @click="activeFilter = 'all'">全部</button>
      <button
        class="chip"
        :class="{ alt: activeFilter === 'pending' }"
        @click="activeFilter = 'pending'"
      >
        待处理
      </button>
      <button
        class="chip"
        :class="{ alt: activeFilter === 'in_progress' }"
        @click="activeFilter = 'in_progress'"
      >
        处理中
      </button>
      <button
        class="chip"
        :class="{ alt: activeFilter === 'completed' }"
        @click="activeFilter = 'completed'"
      >
        已完成
      </button>
      <button class="chip warn" @click="activeFilter = 'rating'">待评价</button>
    </div>

    <van-empty v-if="loading" description="加载中..." />
    <div v-else class="page">
      <div v-for="ticket in filteredTickets" :key="ticket.id" class="card ticket-card">
        <div class="ticket-header">
          <div>
            <div style="font-weight: 600;">{{ ticket.title }}</div>
            <div class="ticket-meta">{{ ticket.locationName || ticket.locationId }} · {{ formatDate(ticket.createdAt) }}</div>
          </div>
          <van-tag :type="statusTag(ticket.status)">{{ statusText(ticket.status) }}</van-tag>
        </div>

        <div>{{ ticket.description }}</div>
        <div v-if="ticket.images.length" style="display: flex; gap: 8px; overflow-x: auto;">
          <van-image
            v-for="(img, index) in ticket.images"
            :key="ticket.id + index"
            :src="img"
            width="84"
            height="84"
            radius="12"
            fit="cover"
          />
        </div>

        <div class="ticket-meta">
          报修人：{{ ticket.reporterName }}（{{ ticket.reporterPhone || '未留存' }}）
        </div>
        <div v-if="ticket.assignedToName" class="ticket-meta">
          维修员：{{ ticket.assignedToName }} {{ ticket.assignedToContact || '' }}
        </div>

        <div v-if="ticket.checkIns.length" class="timeline">
          <div v-for="item in ticket.checkIns" :key="item.id" class="timeline-item">
            {{ item.technicianName }} · {{ item.location }} · {{ formatDate(item.createdAt) }}
          </div>
        </div>

        <div v-if="ticket.status === 'completed'" class="card" style="padding: 12px;">
          <div v-if="ticket.rating">评分：{{ ticket.rating }} 星</div>
          <div v-else>
            <div class="ticket-meta">请为本次维修服务打分</div>
            <van-rate v-model="feedback.rating" :count="5" />
            <van-field v-model="feedback.feedback" placeholder="填写评价" />
            <van-button size="small" type="primary" @click="submitFeedback(ticket.id)">提交评价</van-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { api, type Ticket } from '../services/api';
import { getAuth } from '../services/auth';

const tickets = ref<Ticket[]>([]);
const loading = ref(true);
const activeFilter = ref<'all' | 'pending' | 'in_progress' | 'completed' | 'rating'>('all');
const feedback = reactive({
  rating: 5,
  feedback: '',
});

const statusCounts = computed(() => {
  return tickets.value.reduce(
    (acc, ticket) => {
      if (ticket.status === 'in_progress') acc.inProgress += 1;
      if (ticket.status === 'completed') acc.completed += 1;
      return acc;
    },
    { inProgress: 0, completed: 0 }
  );
});

const filteredTickets = computed(() => {
  if (activeFilter.value === 'all') return tickets.value;
  if (activeFilter.value === 'rating') {
    return tickets.value.filter((ticket) => ticket.status === 'completed' && !ticket.rating);
  }
  return tickets.value.filter((ticket) => ticket.status === activeFilter.value);
});

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

const loadTickets = async () => {
  const auth = getAuth();
  const response = await api.get('/tickets', {
    params: auth?.id ? { reporterId: auth.id } : undefined,
  });
  tickets.value = response.data;
  loading.value = false;
};

const submitFeedback = async (id: string) => {
  await api.post(`/tickets/${id}/feedback`, { ...feedback });
  feedback.rating = 5;
  feedback.feedback = '';
  await loadTickets();
};

const formatDate = (value: string) => new Date(value).toLocaleString();

onMounted(loadTickets);
</script>
