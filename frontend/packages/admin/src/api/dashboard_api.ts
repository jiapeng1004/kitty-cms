import api from '../utils/api'
import { USER_SERVICE_PATH } from './constants'

const PREFIX = `${USER_SERVICE_PATH}/api/dashboard`

export interface DashboardStats {
  userCount?: number
  configCount?: number
  configClassCount?: number
}

export function getStats(): Promise<DashboardStats> {
  return api.get(`${PREFIX}/stats`)
}
