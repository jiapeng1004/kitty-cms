import { defineStore } from 'pinia'

/**
 * 用于 Fork 任务时传递参数到新建页，避免 history.state / sessionStorage 的时序问题。
 * take() 只读取不清空，由 TaskCreate 在 onBeforeUnmount 时 clear，避免 remount 导致数据丢失。
 */
export const useForkTaskStore = defineStore('forkTask', {
  state: () => ({ task: null }),
  actions: {
    set(task) {
      this.task = task
    },
    take() {
      return this.task
    },
    clear() {
      this.task = null
    }
  }
})
