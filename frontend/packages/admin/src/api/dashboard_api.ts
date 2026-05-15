import api from '../utils/api'

export interface DashboardStats {
  userCount?: number
  configCount?: number
  configClassCount?: number
}

export function getStats(): Promise<DashboardStats> {
  return api.get('/api/user/dashboard/stats')
}
