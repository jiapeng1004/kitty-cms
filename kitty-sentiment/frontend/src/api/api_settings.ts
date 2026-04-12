import client from './client'
import { pathsV1 } from './v1Paths'

export function getSettings() {
  return client.get(pathsV1.settings)
}

export function saveSettings(values: Record<string, unknown>) {
  return client.post(pathsV1.settings, values)
}
