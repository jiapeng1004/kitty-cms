<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  listMessages,
  markMessageRead,
  sendMessage,
  subscribeMaterialMessageSse,
  type MaterialInternalMessageVO
} from '@/api/mam_message_api'

const loading = ref(false)
const rows = ref<MaterialInternalMessageVO[]>([])
const receiverUserId = ref('')
const content = ref('')
const sseOn = ref(false)
const sseHint = ref('未连接')
let stopSse: (() => void) | null = null

async function load() {
  loading.value = true
  try {
    rows.value = await listMessages()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function onSend() {
  if (!receiverUserId.value.trim() || !content.value.trim()) {
    message.warning('请填写接收者用户 ID 与正文')
    return
  }
  try {
    await sendMessage({ receiverUserId: receiverUserId.value.trim(), content: content.value.trim() })
    message.success('已发送')
    content.value = ''
    await load()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '发送失败')
  }
}

async function onMarkRead(row: MaterialInternalMessageVO) {
  try {
    await markMessageRead(row.id)
    message.success('已标为已读')
    await load()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '操作失败')
  }
}

function startSse() {
  if (stopSse) {
    stopSse()
    stopSse = null
  }
  sseOn.value = true
  sseHint.value = '连接中…'
  stopSse = subscribeMaterialMessageSse(
    (messageId) => {
      message.info(`收到新消息推送: ${messageId}`)
      void load()
    },
    () => {
      sseHint.value = '已连接（SSE）'
    },
    () => {
      sseHint.value = '连接异常或已断开'
    }
  )
}

function stopSseConn() {
  if (stopSse) {
    stopSse()
    stopSse = null
  }
  sseOn.value = false
  sseHint.value = '未连接'
}

onMounted(() => {
  void load()
})

onUnmounted(() => {
  stopSseConn()
})
</script>

<template>
  <div style="padding: 20px;">
    <a-typography-title :level="4">站内信</a-typography-title>
    <a-alert
      type="info"
      show-icon
      style="margin-bottom: 12px;"
      message="需权限 material:message:read / material:message:send；SSE 使用与 REST 相同的 Bearer Token（fetch）。"
    />

    <a-space wrap style="margin-bottom: 16px;">
      <a-input v-model:value="receiverUserId" style="width: 220px;" placeholder="接收者用户 ID" />
      <a-input v-model:value="content" style="width: 360px;" placeholder="消息正文" />
      <a-button type="primary" @click="onSend">发送</a-button>
      <a-button @click="load" :loading="loading">刷新收件箱</a-button>
      <a-button v-if="!sseOn" type="dashed" @click="startSse">连接实时推送</a-button>
      <a-button v-else danger @click="stopSseConn">断开 SSE</a-button>
      <a-tag :color="sseOn ? 'green' : 'default'">{{ sseHint }}</a-tag>
    </a-space>

    <a-table
      :data-source="rows"
      row-key="id"
      :loading="loading"
      :pagination="false"
      size="small"
    >
      <a-table-column title="ID" data-index="id" key="id" :ellipsis="true" />
      <a-table-column title="发件人" data-index="senderUserId" key="senderUserId" />
      <a-table-column title="正文" data-index="content" key="content" />
      <a-table-column title="状态" data-index="messageStatus" key="messageStatus" />
      <a-table-column title="时间" data-index="createdAt" key="createdAt" />
      <a-table-column title="操作" key="action">
        <template #default="{ record }">
          <a-button
            v-if="record.messageStatus === 'unread'"
            type="link"
            size="small"
            @click="onMarkRead(record)"
          >
            标为已读
          </a-button>
        </template>
      </a-table-column>
    </a-table>
  </div>
</template>
