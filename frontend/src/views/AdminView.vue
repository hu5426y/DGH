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
      <div class="chip-group" style="margin-top: 12px;">
        <div class="chip">自动派单已启用</div>
        <div class="chip alt">SLA 24h</div>
        <div class="chip warn">关注超时工单</div>
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

    <div class="section-title">运营闭环看板</div>
    <div class="card">
      <div style="font-weight: 600;">SLA 与超时预警</div>
      <div class="metric-grid" style="margin-top: 12px;">
        <div class="metric-card">
          <div class="metric-value">{{ ops.slaHours }}h</div>
          <div class="metric-label">SLA 目标</div>
        </div>
        <div class="metric-card">
          <div class="metric-value">{{ ops.slaComplianceRate }}%</div>
          <div class="metric-label">SLA 达成率</div>
        </div>
        <div class="metric-card">
          <div class="metric-value">{{ ops.overdueCount }}</div>
          <div class="metric-label">超时工单</div>
        </div>
        <div class="metric-card">
          <div class="metric-value">{{ ops.escalatedCount }}</div>
          <div class="metric-label">跨部门升级</div>
        </div>
      </div>
    </div>

    <div class="section-title">区域自治与备品策略</div>
    <div class="card">
      <div class="ticket-meta">推荐“区域负责 + 备品库”配置</div>
      <div class="ops-list" style="margin-top: 10px;">
        <div v-for="area in ops.areaLeads" :key="area.area" class="ops-row">
          <div>
            <div style="font-weight: 600;">{{ area.area }}</div>
            <div class="ticket-meta">工单 {{ area.ticketCount }} · 推荐负责人 {{ area.suggestedLead }}</div>
          </div>
          <div class="pill">优先级调度</div>
        </div>
      </div>
    </div>

    <div class="section-title">维修画像与绩效</div>
    <div class="card">
      <div class="ops-list">
        <div v-for="tech in ops.technicianPerformance" :key="tech.technicianName" class="ops-row">
          <div>
            <div style="font-weight: 600;">{{ tech.technicianName }}</div>
            <div class="ticket-meta">
              累计 {{ tech.totalTickets }} 单 · 完成 {{ tech.completedTickets }} 单 · 平均用时
              {{ tech.averageCompletionMinutes }} 分钟
            </div>
          </div>
          <div class="badge">优秀</div>
        </div>
      </div>
    </div>

    <div class="section-title">预测性维护与口碑</div>
    <div class="card">
      <div class="ticket-meta">高频故障类型</div>
      <div style="margin-top: 8px; display: flex; flex-wrap: wrap; gap: 6px;">
        <div v-for="issue in ops.issueStats" :key="issue.issue" class="tag">
          {{ issue.issue }} · {{ issue.count }}
        </div>
      </div>
      <div class="notice-pill" style="margin-top: 12px;">
        待回访评价 {{ ops.pendingFeedbackCount }} 单，建议自动触达提升口碑。
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
import { fetchOpsOverview, type OpsOverview } from '../services/ops';

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
const ops = reactive<OpsOverview>({
  slaHours: 24,
  overdueCount: 0,
  slaComplianceRate: 0,
  areaLeads: [],
  technicianPerformance: [],
  issueStats: [],
  pendingFeedbackCount: 0,
  escalatedCount: 0,
});

const loadData = async () => {
  const [ticketRes, statsRes, opsRes] = await Promise.all([
    api.get('/tickets'),
    api.get('/stats'),
    fetchOpsOverview(),
  ]);
  tickets.value = ticketRes.data;
  Object.assign(stats, statsRes.data);
  Object.assign(ops, opsRes);
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
