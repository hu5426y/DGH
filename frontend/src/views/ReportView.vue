<template>
  <div class="page">
    <div class="section-title">故障上报</div>
    <van-form @submit="onSubmit">
      <van-field v-model="form.title" name="title" label="故障标题" placeholder="请输入故障标题" required />
      <van-field
        v-model="form.description"
        name="description"
        label="详细描述"
        type="textarea"
        rows="3"
        placeholder="描述问题、出现时间、影响范围"
        required
      />
      <van-field v-model="form.location" name="location" label="位置" placeholder="如：A栋 3楼" required />
      <van-field v-model="form.reporterName" name="reporterName" label="报修人" required />
      <van-field v-model="form.reporterContact" name="reporterContact" label="联系方式" required />
      <van-field
        v-model="imageInput"
        label="图片链接"
        placeholder="可填写图片 URL，多张用逗号分隔"
      />
      <div style="margin: 16px 0;">
        <van-button round block type="primary" native-type="submit">提交报修</van-button>
      </div>
    </van-form>
    <van-notice-bar v-if="success" color="#07c160" background="#e8f9f3" left-icon="success">
      工单提交成功，维修人员将尽快处理。
    </van-notice-bar>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { api } from '../services/api';

const success = ref(false);
const imageInput = ref('');
const form = reactive({
  title: '',
  description: '',
  location: '',
  reporterName: '',
  reporterContact: '',
});

const onSubmit = async () => {
  const images = imageInput.value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
  await api.post('/tickets', { ...form, images });
  success.value = true;
  form.title = '';
  form.description = '';
  form.location = '';
  form.reporterName = '';
  form.reporterContact = '';
  imageInput.value = '';
};
</script>
