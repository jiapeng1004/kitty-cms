import api from '../utils/api'

export interface MenuTreeItem {
  id: string
  parentId?: string
  menuName: string
  menuType?: 'DIR' | 'MENU' | 'LINK'
  menuKey: string
  path?: string
  component?: string
  linkType?: 'INTERNAL' | 'EXTERNAL'
  linkUrl?: string
  icon?: string
  sort?: number
  /** 后端返回的绑定权限code列表（逗号分隔），用于角色开通菜单时提示 */
  pCodes?: string
  children?: MenuTreeItem[]
}

export function getCurrentMenuTree(): Promise<MenuTreeItem[]> {
  return api.get('/api/menu/tree/current')
}

export function getAllMenuTree(): Promise<MenuTreeItem[]> {
  return api.get('/api/menu/tree/all')
}
