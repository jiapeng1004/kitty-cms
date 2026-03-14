<template>
  <div class="page-dashboard">
    <div class="page-header">
      <h1 class="page-title">工作台</h1>
      <p class="page-desc">欢迎使用 Kitty CMS，以下是关键数据概览</p>
    </div>

    <a-row :gutter="[20, 20]" class="stats-row">
      <a-col :xs="24" :sm="24" :md="8">
        <a-card class="stat-card" :bordered="false">
          <a-statistic
            title="用户数"
            :value="stats.userCount ?? 0"
          >
            <template #prefix>
              <UserOutlined class="stat-icon stat-icon-user" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="24" :md="8">
        <a-card class="stat-card" :bordered="false">
          <a-statistic
            title="配置项"
            :value="stats.configCount ?? 0"
          >
            <template #prefix>
              <SettingOutlined class="stat-icon stat-icon-config" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="24" :md="8">
        <a-card class="stat-card" :bordered="false">
          <a-statistic
            title="分类数"
            :value="stats.configClassCount ?? 0"
          >
            <template #prefix>
              <AppstoreOutlined class="stat-icon stat-icon-category" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { UserOutlined, SettingOutlined, AppstoreOutlined } from '@ant-design/icons-vue'
import { getStats } from '../api/dashboard_api'

const stats = ref({
  userCount: 0,
  configCount: 0,
  configClassCount: 0
})

async function loadStats() {
  try {
    const data = await getStats()
    stats.value = {
      userCount: data?.userCount ?? 0,
      configCount: data?.configCount ?? 0,
      configClassCount: data?.configClassCount ?? 0
    }
  } catch (_) {
    stats.value = { userCount: 0, configCount: 0, configClassCount: 0 }
  }
}

onMounted(loadStats)
</script>

<style scoped>
.page-dashboard {
  padding: 0;
}
.page-header {
  margin-bottom: 24px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.88);
  margin: 0 0 4px 0;
}
.page-desc {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.45);
  margin: 0;
}
.stats-row {
  margin-top: 8px;
}
.stat-card {
  border-radius: var(--admin-radius);
  box-shadow: var(--admin-card-shadow);
  transition: box-shadow 0.2s;
}
.stat-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.stat-card :deep(.ant-statistic-title) {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.45);
}
.stat-card :deep(.ant-statistic-content) {
  display: flex;
  align-items: center;
  gap: 8px;
}
.stat-icon {
  font-size: 20px;
  opacity: 0.85;
}
.stat-icon-user { color: #1677ff; }
.stat-icon-config { color: #52c41a; }
.stat-icon-category { color: #722ed1; }
</style>
