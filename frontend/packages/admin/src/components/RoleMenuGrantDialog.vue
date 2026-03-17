<template>
  <a-modal
    v-model:open="innerOpen"
    :title="`为角色开通菜单权限 - ${menuName}`"
    :width="520"
    :confirm-loading="loading"
    ok-text="确定开通"
    cancel-text="取消"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <div v-if="!menu">
      <a-alert type="warning" message="未选择菜单，请先在列表中选择要开通的菜单" show-icon />
    </div>
    <div v-else>
      <p class="desc">
        该菜单声明了以下权限 code（来自后端菜单配置 <code>p_codes</code>），默认全选，管理员可根据需要取消部分或全部勾选。
      </p>
      <a-alert
        v-if="allCodes.length === 0"
        type="info"
        message="当前菜单未配置任何权限 code（p_codes 为空），开通菜单时不会同步开通权限。"
        show-icon
      />
      <div v-else class="checkbox-group-wrapper">
        <a-checkbox-group v-model:value="checkedCodes">
          <a-space direction="vertical">
            <a-checkbox v-for="code in allCodes" :key="code" :value="code">
              {{ code }}
            </a-checkbox>
          </a-space>
        </a-checkbox-group>
      </div>
      <a-alert
        v-if="allCodes.length > 0 && checkedCodes.length === 0"
        type="warning"
        message="已取消全部勾选，仅会为角色开通菜单，不会同步任何权限。"
        show-icon
        class="mt-12"
      />
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { MenuTreeItem } from '../api/menu_api'
import { message } from 'ant-design-vue'
import { getResponseMessage } from '../utils/api'
import { grantMenuWithPermissions } from '../api/role_menu_permission_api'

interface Props {
  /** 是否打开弹窗 */
  open: boolean
  /** 角色ID */
  roleId: string
  /** 当前选中的菜单（需要包含 pCodes） */
  menu?: MenuTreeItem | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

const innerOpen = computed({
  get: () => props.open,
  set: (val: boolean) => {
    emit('update:open', val)
  }
})

const allCodes = ref<string[]>([])
const checkedCodes = ref<string[]>([])
const loading = ref(false)

const menuName = computed(() => props.menu?.menuName ?? '未命名菜单')

watch(
  () => props.menu,
  (menu) => {
    if (!menu) {
      allCodes.value = []
      checkedCodes.value = []
      return
    }
    const raw = menu.pCodes ?? ''
    const codes = raw
      .split(',')
      .map((s) => s.trim())
      .filter((s) => s.length > 0)
    allCodes.value = codes
    checkedCodes.value = [...codes]
  },
  { immediate: true }
)

watch(
  () => props.open,
  (opened) => {
    if (opened && props.menu) {
      const raw = props.menu.pCodes ?? ''
      const codes = raw
        .split(',')
        .map((s) => s.trim())
        .filter((s) => s.length > 0)
      allCodes.value = codes
      checkedCodes.value = [...codes]
    }
  }
)

async function handleOk() {
  if (!props.roleId || !props.menu) {
    message.error('缺少角色或菜单信息')
    return
  }
  loading.value = true
  try {
    await grantMenuWithPermissions({
      roleId: props.roleId,
      menuId: props.menu.id,
      pCodes: checkedCodes.value
    })
    message.success('开通成功')
    emit('success')
    innerOpen.value = false
  } catch (error) {
    message.error(getResponseMessage(error))
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  innerOpen.value = false
}
</script>

<style scoped>
.desc {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.65);
  margin-bottom: 12px;
}
.checkbox-group-wrapper {
  max-height: 260px;
  overflow-y: auto;
  padding: 8px 4px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}
.mt-12 {
  margin-top: 12px;
}
</style>

