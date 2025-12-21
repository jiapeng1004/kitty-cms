<template>
  <div class="dashboard">
    <h1>欢迎使用 Kitty CMS</h1>
    <p>这是一个现代化的后台管理系统</p>
    
    <a-row :gutter="[16, 16]" class="stats">
      <a-col :span="8">
        <a-card>
          <a-statistic title="用户数" :value="112893" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card>
          <a-statistic title="配置项" :value="8848" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card>
          <a-statistic title="分类数" :value="56" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
</script>

<style scoped>
.dashboard {
  padding: 24px;
}

.stats {
  margin-top: 24px;
}
</style>