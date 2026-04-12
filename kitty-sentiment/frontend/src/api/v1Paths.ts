/**
 * API v1 路径单处维护；页面组件只通过 api_*.ts 调用，不拼接路径。
 */
const V1 = '/api/v1'

export const pathsV1 = {
  sources: `${V1}/sources`,
  sourcesValidateSpider: `${V1}/sources/validate-spider`,
  sourcesTypeTagSuggestions: `${V1}/sources/type-tag-suggestions`,
  tasks: `${V1}/tasks`,
  tasksValidateFrequency: `${V1}/tasks/validate-frequency`,
  stats: `${V1}/stats`,
  sentiments: `${V1}/sentiments`,
  settings: `${V1}/settings`,
  authLogin: `${V1}/auth/login`,
} as const

export function taskById(taskId: number): string {
  return `${pathsV1.tasks}/${taskId}`
}

export function taskStart(taskId: number): string {
  return `${pathsV1.tasks}/${taskId}/start`
}

export function taskStop(taskId: number): string {
  return `${pathsV1.tasks}/${taskId}/stop`
}

export function taskExecutions(taskId: number): string {
  return `${pathsV1.tasks}/${taskId}/executions`
}

export function taskExecutionsExport(taskId: number): string {
  return `${pathsV1.tasks}/${taskId}/executions/export`
}

export function sourceById(sourceId: number): string {
  return `${pathsV1.sources}/${sourceId}`
}
