import client from './client'
import { pathsV1 } from './v1Paths'

export type ListSentimentsParams = Record<string, string | number | undefined>

export function getStats() {
  return client.get(pathsV1.stats)
}

export function listSentiments(params: ListSentimentsParams) {
  return client.get(pathsV1.sentiments, { params })
}
