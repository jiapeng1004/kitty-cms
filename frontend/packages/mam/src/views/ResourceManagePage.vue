<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { message, UploadProps } from 'ant-design-vue'
import {
  listResources,
  type MaterialResourceVO,
  createFolder
} from '@/api/mam_resource_api'
import { queryCatalogTree, type MaterialCatalogNode } from '@/api/mam_catalog_api'

// 状态定义
const loading = ref(false)
const resources = ref<MaterialResourceVO[]>([])
const selectedKeys = ref<string[]>([])
const expandedKeys = ref<string[]>(['0'])
const searchKeyword = ref('')
const fileType = ref<string>('all')
const currentPath = ref('素材库 / 全部文件')
const form = reactive({
  catalogId: '',
  parentId: '0'
})

// 目录树数据
const treeData = ref<MaterialCatalogNode[]>([])

// 加载栏目树
async function loadCatalogTree() {
  try {
    treeData.value = await queryCatalogTree()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '加载栏目树失败')
  }
}

// 筛选后的资源
const filteredResources = computed(() => {
  let list = resources.value
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(item => 
      item.title.toLowerCase().includes(keyword) || 
      item.path?.toLowerCase().includes(keyword)
    )
  }
  if (fileType.value !== 'all') {
    list = list.filter(item => item.type === parseInt(fileType.value))
  }
  return list
})

// 获取文件图标，暂时用文字代替
const getFileIcon = (type: number) => {
  return null
}

// 获取文件类型标签
const getFileTypeLabel = (type: number) => {
  switch(type) {
    case 1: return '视频'
    case 2: return '音频'
    case 3: return '图片'
    case 4: return '文档'
    default: return '其他'
  }
}

// 获取文件类型颜色
const getFileTypeColor = (type: number) => {
  switch(type) {
    case 1: return '#1890ff'
    case 2: return '#52c41a'
    case 3: return '#faad14'
    case 4: return '#f5222d'
    default: return '#8c8c8c'
  }
}

// 格式化文件大小
const formatFileSize = (size?: number) => {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(1) + ' MB'
  return (size / (1024 * 1024 * 1024)).toFixed(1) + ' GB'
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return '-'
  return time.substring(0, 10)
}

// 查询资源列表
async function queryList() {
  loading.value = true
  try {
    resources.value = await listResources({
      catalogId: form.catalogId || undefined,
      parentId: form.parentId || undefined,
      keyword: searchKeyword.value || undefined
    })
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

// 目录树点击事件
function onTreeSelect(keys: string[]) {
  selectedKeys.value = keys
  if (keys.length > 0) {
    form.catalogId = keys[0]
    const node = findNodeByKey(treeData.value, keys[0])
    if (node) {
      currentPath.value = `素材库 / ${node.name}`
    }
  }
  queryList()
}

// 递归查找树节点
function findNodeByKey(nodes: any[], key: string): any {
  for (const node of nodes) {
    if (node.key === key) return node
    if (node.children) {
      const found = findNodeByKey(node.children, key)
      if (found) return found
    }
  }
  return null
}

// 上传配置
const uploadProps: UploadProps = {
  action: '/api/mam/resource/upload',
  headers: {
    Authorization: 'Bearer ' + localStorage.getItem('token')
  },
  beforeUpload(file) {
    const isLt5G = file.size / 1024 / 1024 / 1024 < 5
    if (!isLt5G) {
      message.error('文件大小不能超过 5GB!')
    }
    return isLt5G
  },
  success() {
    message.success('上传成功')
    queryList()
  },
  error() {
    message.error('上传失败')
  }
}

// 创建文件夹
const folderName = ref('')
const folderModalVisible = ref(false)
async function handleCreateFolder() {
  if (!folderName.value) {
    message.warning('请输入文件夹名称')
    return
  }
  try {
    await createFolder({
      title: folderName.value,
      catalogId: selectedKeys.value[0] || '0',
      parentId: '0'
    })
    message.success('文件夹创建成功')
    folderModalVisible.value = false
    folderName.value = ''
    queryList()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '创建失败')
  }
}

onMounted(async () => {
  await loadCatalogTree()
  await queryList()
})
</script>

<template>
  <div class="resource-manage-page">
    <!-- 顶部导航栏 -->
    <div class="top-bar">
      <div class="top-bar-left">
        <a-input
          v-model:value="searchKeyword"
          placeholder="搜索素材..."
          style="width: 300px; margin-right: 16px;"
          @keyup.enter="queryList"
        />
        <a-select v-model:value="fileType" style="width: 120px; margin-right: 16px;">
          <a-select-option value="all">全部</a-select-option>
          <a-select-option value="3">图片</a-select-option>
          <a-select-option value="1">视频</a-select-option>
          <a-select-option value="2">音频</a-select-option>
          <a-select-option value="4">文档</a-select-option>
          <a-select-option value="7">其他</a-select-option>
        </a-select>
        <a-button @click="queryList">搜索</a-button>
      </div>
      <div class="top-bar-right">
        <a-button type="primary" @click="folderModalVisible = true">
          新建文件夹
        </a-button>
        <a-upload v-bind="uploadProps" :show-upload-list="false">
          <a-button type="primary" style="margin-left: 8px;">
            上传
          </a-button>
        </a-upload>
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="content-wrapper">
      <!-- 左侧目录树 -->
      <div class="sidebar">
        <div class="sidebar-title">
          素材分类
        </div>
        <a-tree
          v-model:selectedKeys="selectedKeys"
          v-model:expandedKeys="expandedKeys"
          :tree-data="treeData"
          :show-icon="true"
          @select="onTreeSelect"
        />
      </div>

      <!-- 右侧内容区 -->
      <div class="main-content">
        <!-- 路径导航 -->
        <div class="path-nav">
          {{ currentPath }}
          <span style="margin-left: 16px; color: #999; font-size: 13px;">共 {{ filteredResources.length }} 项</span>
        </div>

        <!-- 素材网格 -->
        <div class="resource-grid" v-loading="loading">
          <a-card
            v-for="item in filteredResources"
            :key="item.id"
            hoverable
            class="resource-card"
          >
            <div class="card-thumbnail">
              <span style="font-size: 24px; color: #666;">{{ getFileTypeLabel(item.type) }}</span>
              <div class="type-tag" :style="{ background: getFileTypeColor(item.type) }">
                {{ getFileTypeLabel(item.type) }}
              </div>
            </div>
            <div class="card-info">
              <div class="file-name" :title="item.title">{{ item.title }}</div>
              <div class="file-meta">
                <span class="file-size">{{ formatFileSize(item.fileSize) }}</span>
                <span class="file-time">{{ formatTime(item.createTime) }}</span>
              </div>
            </div>
          </a-card>
        </div>
      </div>
    </div>

    <!-- 新建文件夹弹窗 -->
    <a-modal
      v-model:open="folderModalVisible"
      title="新建文件夹"
      @ok="handleCreateFolder"
      @cancel="folderModalVisible = false"
    >
      <a-form layout="vertical">
        <a-form-item label="文件夹名称">
          <a-input v-model:value="folderName" placeholder="请输入文件夹名称" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.resource-manage-page {
  height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.top-bar {
  height: 60px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.top-bar-left,
.top-bar-right {
  display: flex;
  align-items: center;
}

.content-wrapper {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  overflow-y: auto;
}

.sidebar-title {
  height: 48px;
  padding: 0 16px;
  line-height: 48px;
  font-weight: 500;
  border-bottom: 1px solid #e8e8e8;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.path-nav {
  height: 40px;
  line-height: 40px;
  padding: 0 12px;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.resource-card {
  height: 220px;
  display: flex;
  flex-direction: column;
}

.card-thumbnail {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  border-radius: 4px 4px 0 0;
  position: relative;
}

.type-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 6px;
  border-radius: 2px;
  color: #fff;
  font-size: 12px;
}

.card-info {
  flex: 1;
  padding: 12px;
}

.file-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8px;
}

.file-meta {
  font-size: 12px;
  color: #999;
  display: flex;
  justify-content: space-between;
}
</style>
