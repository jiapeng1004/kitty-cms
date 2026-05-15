<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  approveReview,
  BIZ_TYPE_MATERIAL_RESOURCE,
  queryReviews,
  rejectReview,
  submitReview,
  type MaterialReviewTaskVO
} from '@/mam/api/mam_review_api'

const bizType = ref(BIZ_TYPE_MATERIAL_RESOURCE)
const bizId = ref('')
const loading = ref(false)
const rows = ref<MaterialReviewTaskVO[]>([])
const rejectComment = ref<Record<string, string>>({})
const approveComment = ref<Record<string, string>>({})

async function doQuery() {
  if (!bizId.value.trim()) {
    message.warning('请填写业务主键（媒资为 resourceId）')
    return
  }
  loading.value = true
  try {
    rows.value = await queryReviews({ bizType: bizType.value, bizId: bizId.value.trim() })
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

async function doSubmit() {
  if (!bizId.value.trim()) {
    message.warning('请填写资源 ID（bizId）')
    return
  }
  try {
    await submitReview({ bizType: bizType.value, bizId: bizId.value.trim() })
    message.success('已提交审核')
    await doQuery()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '提交失败')
  }
}

async function doApprove(row: MaterialReviewTaskVO) {
  try {
    const c = approveComment.value[row.id]?.trim()
    await approveReview({ taskId: row.id, reviewComment: c || undefined })
    message.success('已通过')
    await doQuery()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '操作失败')
  }
}

async function doReject(row: MaterialReviewTaskVO) {
  try {
    const c = rejectComment.value[row.id]?.trim()
    await rejectReview({ taskId: row.id, reviewComment: c || undefined })
    message.success('已拒绝')
    await doQuery()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '操作失败')
  }
}
</script>

<template>
  <div style="padding: 20px;">
    <a-typography-title :level="4">通用审核</a-typography-title>
    <a-alert
      type="info"
      show-icon
      style="margin-bottom: 12px;"
      message="当前后端仅对 bizType=material_resource 做栏目权限校验；提交需 material:review:submit，通过/拒绝需 material:review:approve。"
    />

    <a-space wrap style="margin-bottom: 16px;">
      <a-select v-model:value="bizType" style="width: 200px;" :options="[{ value: BIZ_TYPE_MATERIAL_RESOURCE, label: '媒资资源 material_resource' }]" />
      <a-input v-model:value="bizId" style="width: 280px;" placeholder="业务 ID（媒资填 resourceId）" />
      <a-button type="primary" :loading="loading" @click="doQuery">查询审核记录</a-button>
      <a-button @click="doSubmit">提交待审</a-button>
    </a-space>

    <a-table
      :data-source="rows"
      row-key="id"
      :loading="loading"
      :pagination="false"
      size="small"
    >
      <a-table-column title="任务 ID" data-index="id" key="id" :ellipsis="true" />
      <a-table-column title="状态" data-index="status" key="status" />
      <a-table-column title="提交人" data-index="submitUserId" key="submitUserId" />
      <a-table-column title="审核人" data-index="reviewUserId" key="reviewUserId" />
      <a-table-column title="意见" data-index="reviewComment" key="reviewComment" />
      <a-table-column title="创建时间" data-index="createdAt" key="createdAt" />
      <a-table-column title="操作" key="op" :width="320">
        <template #default="{ record }">
          <template v-if="record.status === 'pending'">
            <a-input
              v-model:value="approveComment[record.id]"
              size="small"
              style="width: 100px; margin-right: 4px;"
              placeholder="通过备注"
            />
            <a-button type="link" size="small" @click="doApprove(record)">通过</a-button>
            <a-input
              v-model:value="rejectComment[record.id]"
              size="small"
              style="width: 100px; margin-right: 4px;"
              placeholder="拒绝原因"
            />
            <a-button type="link" danger size="small" @click="doReject(record)">拒绝</a-button>
          </template>
          <span v-else style="color: #999;">—</span>
        </template>
      </a-table-column>
    </a-table>
  </div>
</template>
