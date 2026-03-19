import api from '@/utils/api'

const PREFIX = '/api/topics'

export interface TopicItem {
  id: string
  title: string
  source: string
  content: string
  tags?: string
  status?: number
  publishedAt?: string | null
  createdBy?: string
  updatedBy?: string
  createdAt?: string
  updatedAt?: string
}

export function getTopicPage(params?: Record<string, unknown>): Promise<{ records: TopicItem[]; total: number }> {
  return api.get(`${PREFIX}`, { params })
}

export function getTopicById(id: string): Promise<TopicItem> {
  return api.get(`${PREFIX}/${id}`)
}

export function createTopic(data: Record<string, unknown>): Promise<{ id: string }> {
  return api.post(`${PREFIX}`, data)
}

export function updateTopic(id: string, data: Record<string, unknown>): Promise<{ success: boolean }> {
  return api.put(`${PREFIX}/${id}`, data)
}

export function deleteTopic(id: string): Promise<{ success: boolean }> {
  return api.delete(`${PREFIX}/${id}`)
}

export function submitTopic(id: string): Promise<{ success: boolean }> {
  return api.post(`${PREFIX}/${id}/submit`)
}

export function rejectTopic(id: string): Promise<{ success: boolean }> {
  return api.post(`${PREFIX}/${id}/reject`)
}

export function approveTopic(id: string): Promise<{ success: boolean }> {
  return api.post(`${PREFIX}/${id}/approve`)
}

export function publishTopic(id: string): Promise<{ success: boolean }> {
  return api.post(`${PREFIX}/${id}/publish`)
}

