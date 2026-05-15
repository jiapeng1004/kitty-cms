import api from '@/utils/api'

export interface MaterialResourceTaskVO {
  id: string
  resourceId: string
  resourceTitle: string
  taskType: string
  thirdTaskId?: string
  progress?: number
  status?: string
  inputType?: string
  inputPath?: string
  materialStrategyId?: string
  strategyName?: string
}

export interface MaterialTranscodeEnqueueDTO {
  resourceId: string
  inputType?: string
  inputPath?: string
  strategyId?: string
  priority?: number
}

export function listTasksByResource(resourceId: string): Promise<MaterialResourceTaskVO[]> {
  return api.get(`/api/material/task/list`, { params: { resourceId } })
}

export function enqueueTranscode(data: MaterialTranscodeEnqueueDTO): Promise<MaterialResourceTaskVO> {
  return api.post(`/api/material/task/transcode/enqueue`, data)
}

export function retryTranscode(taskId: string): Promise<MaterialResourceTaskVO> {
  return api.post(`/api/material/task/transcode/retry`, {}, { params: { taskId } })
}
