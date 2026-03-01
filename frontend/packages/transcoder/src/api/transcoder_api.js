import api from '../utils/api'

const PREFIX = '/api/transcode'

/**
 * 获取单个任务详情
 * @param {string} taskId
 * @returns {Promise<object>}
 */
export function getTask(taskId) {
  return api.get(`${PREFIX}/task/${taskId}`)
}

/**
 * 分页获取任务列表
 * @param {{ page?: number, size?: number }} params
 * @returns {Promise<Array<object>>}
 */
export function listTasks(params = {}) {
  const { page = 1, size = 50 } = params
  return api.get(`${PREFIX}/tasks`, { params: { page, size } })
}

/**
 * 创建转码任务
 * @param {object} body - inputType, inputPath, strategyId, priority 等
 * @returns {Promise<string>} 任务 ID
 */
export function createTask(body) {
  return api.post(`${PREFIX}/task`, body)
}

/**
 * 取消任务
 * @param {string} taskId
 * @returns {Promise<void>}
 */
export function cancelTask(taskId) {
  return api.delete(`${PREFIX}/task/${taskId}`)
}
