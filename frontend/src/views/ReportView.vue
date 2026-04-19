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
          :model-value="locationLabel"
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
      <van-field :model-value="reporterLabel" name="reporterId" label="报修人" readonly />
      <div class="ticket-meta">联系方式将在工单详情中自动显示。</div>
    </div>

    <van-button round block type="primary" @click="onSubmit">提交报修</van-button>

    <van-notice-bar v-if="success" color="#0f766e" background="#e6f7f4" left-icon="success">
      工单提交成功，系统已进入自动派单流程。
    </van-notice-bar>

    <van-popup v-model:show="showLocationPicker" position="bottom" round>
      <van-picker :columns="locationOptions" @confirm="onLocationConfirm" @cancel="showLocationPicker = false" />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { api } from '../services/api';
import { getAuth } from '../services/auth';
import { showFailToast, showSuccessToast } from 'vant';

const success = ref(false);
const imageInput = ref('');
const showLocationPicker = ref(false);
const auth = getAuth();

const form = reactive({
  title: '',
  description: '',
  locationId: '',
  reporterId: auth?.id ?? '',
});

const locationOptions = ref<Array<{ text: string; value: string }>>([]);

const locationLabel = computed(() => {
  return locationOptions.value.find((item) => item.value === form.locationId)?.text ?? '';
});

const reporterLabel = computed(() => {
  if (!auth) return '未登录';
  return `${auth.name} (${auth.phone})`;
});

type PickerConfirmPayload = {
  selectedOptions?: Array<{ text?: string; value?: string | number }>;
  selectedValues?: Array<string | number>;
};

const getPickedValue = (value: unknown) => {
  const payload = value as PickerConfirmPayload | Array<{ value?: string | number }> | string | undefined;
  if (payload && typeof payload === 'object' && !Array.isArray(payload)) {
    const option = payload.selectedOptions?.[0];
    if (option?.value != null) {
      return String(option.value);
    }
    const selectedValue = payload.selectedValues?.[0];
    if (selectedValue != null) {
      return String(selectedValue);
    }
  }

  const picked = Array.isArray(payload) ? payload[0] : payload;
  if (picked && typeof picked === 'object' && 'value' in picked) {
    return String((picked as { value: string | number }).value);
  }
  if (typeof picked === 'string' || typeof picked === 'number') {
    return String(picked);
  }
  return '';
};

const onLocationConfirm = (value: unknown) => {
  const pickedValue = getPickedValue(value);
  if (pickedValue) {
    form.locationId = pickedValue;
  }
  showLocationPicker.value = false;
};

const onSubmit = async () => {
  if (!form.locationId) {
    showFailToast('请选择位置');
    return;
  }
  if (!form.reporterId) {
    showFailToast('请先登录后再提交报修');
    return;
  }

  const images = imageInput.value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);

  try {
    await api.post('/tickets', { ...form, images });
    success.value = true;
    showSuccessToast('提交成功');
    form.title = '';
    form.description = '';
    form.locationId = '';
    form.reporterId = auth?.id ?? '';
    imageInput.value = '';
  } catch (error: any) {
    showFailToast(error?.response?.data?.message || '提交失败，请稍后重试');
  }
};

const loadOptions = async () => {
  const locations = await api.get('/locations');
  locationOptions.value = locations.data.map((item: { id: string; name: string }) => ({
    text: item.name,
    value: item.id,
  }));
};

onMounted(loadOptions);
</script>
