import api from '../utils/api'
import { USER_SERVICE_PATH } from './constants'

const PREFIX = `${USER_SERVICE_PATH}/api/user/permission`

export function getCurrentUserPermissionList(): Promise<string[]> {
  return api.get(`${PREFIX}/list`)
}

