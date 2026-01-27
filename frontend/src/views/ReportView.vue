<template>
  <div class="page">
    <div class="section-title">故障上报</div>

    <div class="notice-pill">
      建议填写完整位置与故障描述，系统将自动匹配维修员并生成处理时限。
    </div>

    <div class="card">
      <div style="font-weight: 600;">报修指南</div>
      <div class="ticket-meta">上传现场照片有助于提升派单准确度。</div>
      <div class="chip-group" style="margin-top: 10px;">
        <div class="chip">自动识别故障等级</div>
        <div class="chip alt">位置关联楼宇</div>
        <div class="chip warn">紧急事项优先</div>
      </div>
    </div>

    <div class="card form-card">
      <div style="font-weight: 600;">故障信息</div>
      <van-form @submit="onSubmit">
        <van-field v-model="form.title" name="title" label="故障标题" placeholder="如：水管漏水" required />
        <van-field
          v-model="form.description"
          name="description"
          label="详细描述"
          type="textarea"
          rows="3"
          placeholder="描述问题、出现时间、影响范围"
          required
        />
        <van-field
          v-model="locationLabel"
          name="locationId"
          label="位置"
          placeholder="请选择位置"
          readonly
          is-link
          required
          @click="showLocationPicker = true"
        />
        <div style="display: flex; gap: 8px; flex-wrap: wrap;">
          <van-tag plain type="primary">教学楼</van-tag>
          <van-tag plain type="success">宿舍区</van-tag>
          <van-tag plain type="warning">公共设施</van-tag>
          <van-tag plain type="danger">紧急报修</van-tag>
        </div>
        <van-field
          v-model="imageInput"
          label="图片链接"
          placeholder="可填写图片 URL，多张用逗号分隔"
        />
      </van-form>
    </div>

    <div class="card form-card">
      <div style="font-weight: 600;">报修人信息</div>
      <van-field
        v-model="reporterLabel"
        name="reporterId"
        label="报修人"
        placeholder="请选择报修人"
        readonly
        is-link
        required
        @click="showReporterPicker = true"
      />
      <div class="ticket-meta">联系方式将在工单详情中自动显示。</div>
    </div>

    <van-button round block type="primary" @click="onSubmit">提交报修</van-button>

    <van-notice-bar v-if="success" color="#0f766e" background="#e6f7f4" left-icon="success">
      工单提交成功，系统已进入自动派单流程。
    </van-notice-bar>

    <van-popup v-model:show="showLocationPicker" position="bottom" round>
      <van-picker :columns="locationOptions" @confirm="onLocationConfirm" @cancel="showLocationPicker = false" />
    </van-popup>

    <van-popup v-model:show="showReporterPicker" position="bottom" round>
      <van-picker :columns="reporterOptions" @confirm="onReporterConfirm" @cancel="showReporterPicker = false" />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { api } from '../services/api';

const success = ref(false);
const imageInput = ref('');
const showLocationPicker = ref(false);
const showReporterPicker = ref(false);

const form = reactive({
  title: '',
  description: '',
  locationId: '',
  reporterId: '',
});

const locationOptions = ref<Array<{ text: string; value: string }>>([]);
const reporterOptions = ref<Array<{ text: string; value: string }>>([]);

const locationLabel = computed(() => {
  return locationOptions.value.find((item) => item.value === form.locationId)?.text ?? '';
});

const reporterLabel = computed(() => {
  return reporterOptions.value.find((item) => item.value === form.reporterId)?.text ?? '';
});

const onLocationConfirm = (value: unknown) => {
  const picked = Array.isArray(value) ? value[0] : value;
  if (picked && typeof picked === 'object' && 'value' in picked) {
    form.locationId = String((picked as { value: string }).value);
  } else if (typeof picked === 'string') {
    form.locationId = picked;
  }
  showLocationPicker.value = false;
};

const onReporterConfirm = (value: unknown) => {
  const picked = Array.isArray(value) ? value[0] : value;
  if (picked && typeof picked === 'object' && 'value' in picked) {
    form.reporterId = String((picked as { value: string }).value);
  } else if (typeof picked === 'string') {
    form.reporterId = picked;
  }
  showReporterPicker.value = false;
};

const onSubmit = async () => {
  const images = imageInput.value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
  await api.post('/tickets', { ...form, images });
  success.value = true;
  form.title = '';
  form.description = '';
  form.locationId = '';
  form.reporterId = '';
  imageInput.value = '';
};

const loadOptions = async () => {
  const [locations, users] = await Promise.all([
    api.get('/locations'),
    api.get('/users', { params: { role_code: 'USER' } }),
  ]);
  locationOptions.value = locations.data.map((item: { id: string; name: string }) => ({
    text: item.name,
    value: item.id,
  }));
  reporterOptions.value = users.data.map((item: { id: string; name: string; phone: string }) => ({
    text: `${item.name} (${item.phone})`,
    value: item.id,
  }));
};

onMounted(loadOptions);
</script>
