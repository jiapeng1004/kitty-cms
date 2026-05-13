import api from '../utils/api'

const PREFIX = '/api/auth'

/**
 * 获取当前登录的 Access Key 信息
 * @returns {Promise<{ accessKeyId: string, name?: string, status: string, description?: string }>}
 */
export function getCurrentAccessKey() {
  return api.get(`${PREFIX}/me`)
}

/**
 * 使用 AK/SK 登录，返回 token 与 accessKeyId
 * @param {{ accessKeyId: string, secretKey: string }} body
 * @returns {Promise<{ token: string, accessKeyId: string }>}
 */
export function login(body) {
  return api.post(`${PREFIX}/login`, body)
}

/**
 * 获取 Access Key 列表（不含 secretKey）
 * @returns {Promise<Array<{ accessKeyId: string, name?: string, status: string, description?: string, createdAt?: string }>>}
 */
export function listAccessKeys() {
  return api.get(`${PREFIX}/access-key`)
}

/**
 * 创建 Access Key
 * @param {{ name?: string }} body - 名称（如：前端控制台）
 * @returns {Promise<{ accessKeyId: string, secretKey: string, name?: string }>}
 */
export function createAccessKey(body) {
  return api.post(`${PREFIX}/access-key`, body)
}

/**
 * 删除 Access Key
 * @param {string} accessKeyId
 * @returns {Promise<boolean>}
 */
export function deleteAccessKey(accessKeyId) {
  return api.delete(`${PREFIX}/access-key/${encodeURIComponent(accessKeyId)}`)
}
