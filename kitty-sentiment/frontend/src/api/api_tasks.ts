import type { AxiosResponse } from 'axios'
import client from './client'
import {
  pathsV1,
  taskById,
  taskExecutions,
  taskExecutionsExport,
  taskStart,
  taskStop,
} from './v1Paths'

export function listTasks() {
  return client.get(pathsV1.tasks)
}

export function validateTaskFrequency(frequency: string) {
  return client.post(pathsV1.tasksValidateFrequency, { frequency })
}

export function createTask(body: Record<string, unknown>) {
  return client.post(pathsV1.tasks, body)
}

export function updateTask(taskId: number, body: Record<string, unknown>) {
  return client.put(taskById(taskId), body)
}

export function startTask(taskId: number) {
  return client.post(taskStart(taskId))
}

export function stopTask(taskId: number) {
  return client.post(taskStop(taskId))
}

export function deleteTask(taskId: number) {
  return client.delete(taskById(taskId))
}

export type ListTaskExecutionsParams = {
  page?: number
  page_size?: number
  status?: string
  start_at?: string
  end_at?: string
}

export function listTaskExecutions(taskId: number, params: ListTaskExecutionsParams) {
  return client.get(taskExecutions(taskId), { params })
}

export type ExportTaskExecutionsParams = {
  status?: string
  start_at?: string
  end_at?: string
}

export function exportTaskExecutionsCsv(
  taskId: number,
  params: ExportTaskExecutionsParams,
): Promise<AxiosResponse<Blob>> {
  return client.get(taskExecutionsExport(taskId), {
    params,
    responseType: 'blob',
  })
}
