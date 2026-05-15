import api from '../utils/api'

export function getCurrentUserPermissionList(): Promise<string[]> {
  return api.get('/api/user/permission/list')
}
