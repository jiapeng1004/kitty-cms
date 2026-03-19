<template>
  <a-form :model="formState" :label-col="{ span: 7 }" :wrapper-col="{ span: 17 }">
    <a-form-item label="标题">
      <a-input v-model:value="formState.title" :disabled="disabled" />
    </a-form-item>
    <a-form-item label="来源">
      <a-input v-model:value="formState.source" :disabled="disabled" />
    </a-form-item>
    <a-form-item label="标签">
      <a-input v-model:value="formState.tags" :disabled="disabled" placeholder="逗号分隔，如：体育,娱乐" />
    </a-form-item>
    <a-form-item label="内容">
      <a-textarea v-model:value="formState.content" :rows="5" :disabled="disabled" />
    </a-form-item>
    <a-form-item label="状态">
      <a-select v-model:value="formState.status" :disabled="true">
        <a-select-option :value="0">草稿</a-select-option>
        <a-select-option :value="1">待审核</a-select-option>
        <a-select-option :value="2">已通过</a-select-option>
        <a-select-option :value="3">已发布</a-select-option>
      </a-select>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
defineProps<{
  formState: Record<string, any>
  disabled?: boolean
}>()
</script>

