import api from '@/utils/api'
import { MATERIAL_SERVICE_PATH } from './constants'

const PREFIX = `${MATERIAL_SERVICE_PATH}/api/material/task`

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
  return api.get(`${PREFIX}/list`, { params: { resourceId } })
}

export function enqueueTranscode(data: MaterialTranscodeEnqueueDTO): Promise<MaterialResourceTaskVO> {
  return api.post(`${PREFIX}/transcode/enqueue`, data)
}

export function retryTranscode(taskId: string): Promise<MaterialResourceTaskVO> {
  return api.post(`${PREFIX}/transcode/retry`, {}, { params: { taskId } })
}
