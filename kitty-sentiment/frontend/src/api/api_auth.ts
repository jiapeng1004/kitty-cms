import client from './client'
import { pathsV1 } from './v1Paths'

export type LoginBody = {
  username: string
  password: string
}

export function login(body: LoginBody) {
  return client.post<{ token: string; expires_in?: number }>(pathsV1.authLogin, body)
}
