const TOKEN_KEY = 'kitty_topic_token'
const PROVIDER_KEY = 'kitty_topic_oauth_provider'

export function initAuthFromUrl() {
  const params = new URLSearchParams(window.location.search)
  const token = params.get('token')
  const provider =
    params.get('provider') ||
    params.get('oauth_provider') ||
    params.get('oauthProvider') ||
    params.get('X-OAuth-Provider')

  if (token && String(token).trim() !== '') {
    localStorage.setItem(TOKEN_KEY, String(token).trim())
  }
  if (provider && String(provider).trim() !== '') {
    localStorage.setItem(PROVIDER_KEY, String(provider).trim())
  }
}

export function getToken(): string | null {
  const v = localStorage.getItem(TOKEN_KEY)
  return v && String(v).trim() !== '' ? v : null
}

export function getProvider(): string | null {
  const v = localStorage.getItem(PROVIDER_KEY)
  return v && String(v).trim() !== '' ? v : null
}

