<template>
  <TopicForm :formState="formState" :disabled="true" />
</template>

<script setup lang="ts">
import TopicForm from './TopicForm.vue'

defineProps<{
  formState: Record<string, any>
}>()
</script>

