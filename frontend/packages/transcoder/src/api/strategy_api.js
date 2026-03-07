import api from '../utils/api'

const PREFIX = '/api/transcode/strategy'

/**
 * 获取策略列表
 * @returns {Promise<Array<{ id: string, name: string, targetFormat?: string, [key: string]: any }>>}
 */
export function listStrategies() {
  return api.get(PREFIX)
}

/**
 * 获取单个策略详情
 * @param {string} id 策略ID
 * @returns {Promise<object>}
 */
export function getStrategy(id) {
  return api.get(`${PREFIX}/${encodeURIComponent(id)}`)
}

/**
 * 创建策略
 * @param {object} body - name, targetFormat, resolution, bitrate, frameRate, encoder 等
 * @returns {Promise<string>} 策略ID
 */
export function createStrategy(body) {
  return api.post(PREFIX, body)
}

/**
 * 更新策略
 * @param {string} id 策略ID
 * @param {object} body - 同创建
 * @returns {Promise<boolean>}
 */
export function updateStrategy(id, body) {
  return api.put(`${PREFIX}/${encodeURIComponent(id)}`, body)
}

/**
 * 删除策略
 * @param {string} id 策略ID
 * @returns {Promise<boolean>}
 */
export function deleteStrategy(id) {
  return api.delete(`${PREFIX}/${encodeURIComponent(id)}`)
}

/**
 * 导出策略为 YAML
 * @param {string} id 策略ID
 * @returns {Promise<string>} 文件内容
 */
export function exportStrategy(id) {
  return api.get(`${PREFIX}/${encodeURIComponent(id)}/export`, { responseType: 'text' })
}

/**
 * 修改策略ID
 * @param {string} id 当前策略ID
 * @param {string} newId 新策略ID
 */
export function updateStrategyId(id, newId) {
  return api.put(`${PREFIX}/${encodeURIComponent(id)}/id`, { newId })
}

/**
 * 从 YAML 内容导入策略
 * @param {string} content 策略文件内容
 * @returns {Promise<string>} 新策略ID
 */
export function importStrategy(content) {
  return api.post('/api/transcode/strategy/import', { content })
}
