<template>
  <div class="page">
    <van-card class="card" title="报修系统概览" desc="高效、透明的校园/社区设备报修平台" />

    <div class="section-title">核心功能</div>
    <div class="card">
      <van-tag type="primary" plain>故障上报</van-tag>
      <van-tag type="success" plain>自动派单</van-tag>
      <van-tag type="warning" plain>超时提醒</van-tag>
      <van-tag type="danger" plain>多角色权限</van-tag>
      <p style="margin-top: 12px; color: #646566;">
        覆盖用户端、维修端与管理端，支持图文+定位上报、工单追踪、评价反馈与人员调度。
      </p>
    </div>

    <div class="section-title">实时数据</div>
    <div class="card">
      <van-cell title="总工单" :value="stats.total" />
      <van-cell title="已完成" :value="stats.completed" />
      <van-cell title="平均评分" :value="stats.avgRating" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue';
import { api } from '../services/api';

const stats = reactive({
  total: 0,
  completed: 0,
  avgRating: 0,
});

onMounted(async () => {
  const response = await api.get('/stats');
  Object.assign(stats, response.data);
});
</script>
