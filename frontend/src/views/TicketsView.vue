<template>
  <div class="page">
    <div class="section-title">我的工单</div>
    <van-empty v-if="loading" description="加载中..." />
    <van-cell-group v-else>
      <van-cell
        v-for="ticket in tickets"
        :key="ticket.id"
        :title="ticket.title"
        :label="ticket.description"
      >
        <template #right-icon>
          <van-tag :type="statusTag(ticket.status)">{{ statusText(ticket.status) }}</van-tag>
        </template>
      </van-cell>
      <div v-for="ticket in tickets" :key="ticket.id + '-detail'" class="card" style="margin: 12px 0;">
        <p>位置：{{ ticket.location }}</p>
        <p>报修人：{{ ticket.reporterName }}（{{ ticket.reporterContact }}）</p>
        <p v-if="ticket.assignedToName">维修员：{{ ticket.assignedToName }} {{ ticket.assignedToContact }}</p>
        <div v-if="ticket.status === 'completed'" style="margin-top: 12px;">
          <div v-if="ticket.rating">评分：{{ ticket.rating }} 星</div>
          <div v-else>
            <van-rate v-model="feedback.rating" :count="5" />
            <van-field v-model="feedback.feedback" placeholder="填写评价" />
            <van-button size="small" type="primary" @click="submitFeedback(ticket.id)">提交评价</van-button>
          </div>
        </div>
      </div>
    </van-cell-group>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { api, type Ticket } from '../services/api';

const tickets = ref<Ticket[]>([]);
const loading = ref(true);
const feedback = reactive({
  rating: 5,
  feedback: '',
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
  const response = await api.get('/tickets');
  tickets.value = response.data;
  loading.value = false;
};

const submitFeedback = async (id: string) => {
  await api.post(`/tickets/${id}/feedback`, { ...feedback });
  feedback.rating = 5;
  feedback.feedback = '';
  await loadTickets();
};

onMounted(loadTickets);
</script>
