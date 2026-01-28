<template>
  <div class="page">
    <section class="hero">
      <div class="hero-title">让报修从“盲点”变成“透明”</div>
      <div class="hero-desc">
        为校园与社区打造一套高效、可追踪的设备报修系统。图文+定位上报、自动派单、
        超时提醒、数据统计，让每一条工单都有归属。
      </div>
      <div class="hero-actions">
        <van-button type="primary" block round @click="go('/user/report')">立即报修</van-button>
        <van-button plain block round @click="go('/user/tickets')">查看工单</van-button>
      </div>
    </section>

    <section class="command-center card">
      <div class="command-header">
        <div>
          <div class="section-title">指挥中枢</div>
          <div class="command-sub">把校园报修“看得见”，让每一次响应都有时效。</div>
        </div>
        <div class="pulse-indicator">
          <span class="pulse-dot"></span>
          实时态势
        </div>
      </div>
      <div class="command-grid">
        <div class="signal-card">
          <div class="signal-label">今日新增</div>
          <div class="signal-value">28</div>
          <div class="signal-meta">高危 3 · 待指派 5</div>
        </div>
        <div class="signal-card accent">
          <div class="signal-label">SLA 达成</div>
          <div class="signal-value">92%</div>
          <div class="signal-meta">超时风险 2 起</div>
        </div>
        <div class="signal-card warn">
          <div class="signal-label">满意度</div>
          <div class="signal-value">4.8</div>
          <div class="signal-meta">回访覆盖 76%</div>
        </div>
      </div>
      <div class="command-map">
        <div>
          <div class="map-title">热点区域</div>
          <div class="map-legend">北区/宿舍楼 · 教学楼</div>
        </div>
        <div class="map-grid">
          <div class="map-cell active">A1</div>
          <div class="map-cell">A2</div>
          <div class="map-cell hot">B1</div>
          <div class="map-cell">B2</div>
          <div class="map-cell">C1</div>
          <div class="map-cell active">C2</div>
        </div>
      </div>
    </section>

    <div class="section-title">运行态势</div>
    <div class="metric-grid">
      <div class="metric-card">
        <div class="metric-value">{{ stats.total }}</div>
        <div class="metric-label">累计工单</div>
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
        <div class="metric-value">24h</div>
        <div class="metric-label">超时预警阈值</div>
      </div>
    </div>

    <div class="section-title">全流程可视化</div>
    <div class="flow-list">
      <div class="flow-item">
        <div class="flow-index">1</div>
        <div>
          <div style="font-weight: 600;">故障上报</div>
          <div class="ticket-meta">文字+图片+定位，快速提交故障信息。</div>
        </div>
      </div>
      <div class="flow-item">
        <div class="flow-index">2</div>
        <div>
          <div style="font-weight: 600;">自动派单</div>
          <div class="ticket-meta">结合工单等级与区域，智能分配维修员。</div>
        </div>
      </div>
      <div class="flow-item">
        <div class="flow-index">3</div>
        <div>
          <div style="font-weight: 600;">处理跟踪</div>
          <div class="ticket-meta">状态实时更新，维修签到与处理进度可追踪。</div>
        </div>
      </div>
      <div class="flow-item">
        <div class="flow-index">4</div>
        <div>
          <div style="font-weight: 600;">评价与复盘</div>
          <div class="ticket-meta">用户评分反馈，管理端自动汇总维修时效。</div>
        </div>
      </div>
    </div>

    <div class="section-title">多角色协同</div>
    <div class="role-grid">
      <div class="role-card">
        <div class="role-title">用户端</div>
        <div class="ticket-meta">一键报修、查看进度、评价反馈。</div>
        <div class="badge">移动优先</div>
      </div>
      <div class="role-card">
        <div class="role-title">维修端</div>
        <div class="ticket-meta">工单接收、状态更新、签到打卡。</div>
        <div class="badge">现场协作</div>
      </div>
      <div class="role-card">
        <div class="role-title">管理端</div>
        <div class="ticket-meta">派单调度、数据统计、绩效复盘。</div>
        <div class="badge">数据驱动</div>
      </div>
      <div class="role-card">
        <div class="role-title">自动机制</div>
        <div class="ticket-meta">超时提醒、工单优先级分级。</div>
        <div class="badge">智能调度</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '../services/api';

const router = useRouter();
const stats = reactive({
  total: 0,
  completed: 0,
  avgRating: 0,
});

const go = (path: string) => {
  router.push(path);
};

onMounted(async () => {
  const response = await api.get('/stats');
  Object.assign(stats, response.data);
});
</script>
