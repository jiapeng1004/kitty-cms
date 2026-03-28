import api from '@/utils/api'
import { MATERIAL_SERVICE_PATH } from './constants'

const PREFIX = `${MATERIAL_SERVICE_PATH}/api/material/review`

/** 与后端 {@code MaterialReviewBizTypes.MATERIAL_RESOURCE} 一致 */
export const BIZ_TYPE_MATERIAL_RESOURCE = 'material_resource'

export interface MaterialReviewTaskVO {
  id: string
  bizType: string
  bizId: string
  submitUserId: string
  reviewUserId?: string
  status: string
  reviewComment?: string
  createdAt?: string
  updatedAt?: string
}

export function submitReview(data: { bizType: string; bizId: string }): Promise<MaterialReviewTaskVO> {
  return api.post(`${PREFIX}/submit`, data)
}

export function approveReview(data: { taskId: string; reviewComment?: string }): Promise<MaterialReviewTaskVO> {
  return api.post(`${PREFIX}/approve`, data)
}

export function rejectReview(data: { taskId: string; reviewComment?: string }): Promise<MaterialReviewTaskVO> {
  return api.post(`${PREFIX}/reject`, data)
}

export function queryReviews(params: { bizType: string; bizId: string }): Promise<MaterialReviewTaskVO[]> {
  return api.get(`${PREFIX}/query`, { params })
}
