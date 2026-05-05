export const ROLE = {
  ADMIN: 'ROLE_ADMIN',
  QUANLYKYTHUAT: 'ROLE_QUANLYKYTHUAT',
  NHANVIENKYTHUAT: 'ROLE_NHANVIENKYTHUAT',
  NHANVIENVANHANH: 'ROLE_NHANVIENVANHANH',
}

const TOKEN_KEYS = ['accessToken', 'token', 'access_token', 'jwt']
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEYS = ['currentUser', 'user']

export function getAccessToken() {
  for (const key of TOKEN_KEYS) {
    const token = localStorage.getItem(key) || sessionStorage.getItem(key)
    if (token && token !== 'undefined' && token !== 'null') return token
  }
  return ''
}

export function getRefreshToken() {
  return (
    localStorage.getItem(REFRESH_TOKEN_KEY) ||
    sessionStorage.getItem(REFRESH_TOKEN_KEY) ||
    ''
  )
}

export function getAuthHeaders() {
  const token = getAccessToken()

  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }
}

export function saveAuth(data = {}, remember = true) {
  const storage = remember ? localStorage : sessionStorage

  const token =
    data.accessToken ||
    data.token ||
    data.jwt ||
    data.access_token ||
    ''

  const refreshToken =
    data.refreshToken ||
    data.refresh_token ||
    ''

  const user = data.user || extractUserFromToken(token) || {}

  const roles =
    data.roles ||
    user.roles ||
    data.authorities ||
    user.authorities ||
    []

  if (!token) return ''

  TOKEN_KEYS.forEach((key) => storage.setItem(key, token))
  TOKEN_KEYS.forEach((key) => localStorage.setItem(key, token))

  if (refreshToken) {
    storage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  }

  const finalUser = {
    ...user,
    roles: normalizeRoles(roles),
  }

  storage.setItem('currentUser', JSON.stringify(finalUser))
  storage.setItem('user', JSON.stringify(finalUser))
  localStorage.setItem('currentUser', JSON.stringify(finalUser))
  localStorage.setItem('user', JSON.stringify(finalUser))
  localStorage.setItem('roles', JSON.stringify(finalUser.roles))

  return token
}

export function clearAuth() {
  const keys = [
    ...TOKEN_KEYS,
    REFRESH_TOKEN_KEY,
    ...USER_KEYS,
    'roles',
    'authorities',
    'permissions',
    'role',
  ]

  keys.forEach((key) => {
    localStorage.removeItem(key)
    sessionStorage.removeItem(key)
  })
}

export function isTokenExpired(token) {
  if (!token) return true

  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    if (!payload.exp) return false
    return payload.exp * 1000 < Date.now()
  } catch {
    return true
  }
}

export function logout(message = 'Phiên đăng nhập đã hết hạn') {
  clearAuth()
  if (message) alert(message)
  window.location.href = '/#/login'
}

export function normalizeRoles(roles = []) {
  let arr = roles

  if (typeof arr === 'string') {
    arr = arr.split(' ').filter(Boolean)
  }

  if (!Array.isArray(arr)) {
    arr = []
  }

  return arr.map((r) => {
    const role = String(r).trim().toUpperCase()
    return role.startsWith('ROLE_') ? role : `ROLE_${role}`
  })
}

export function extractUserFromToken(token) {
  if (!token) return null

  try {
    const payload = JSON.parse(atob(token.split('.')[1]))

    const authorities =
      payload.authorities ||
      payload.roles ||
      payload.roleNames ||
      payload.scope ||
      payload.role ||
      []

    return {
      username: payload.sub || payload.username || '',
      fullName: payload.fullName || '',
      email: payload.email || '',
      roles: normalizeRoles(authorities),
    }
  } catch {
    return null
  }
}

export function getCurrentUser() {
  for (const key of USER_KEYS) {
    const raw = localStorage.getItem(key) || sessionStorage.getItem(key)

    if (raw) {
      try {
        return JSON.parse(raw)
      } catch {
        return null
      }
    }
  }

  const token = getAccessToken()
  return extractUserFromToken(token)
}

export function hasAnyRole(user, allowedRoles = []) {
  if (!user) return false

  const userRoles = normalizeRoles(user.roles || [])
  const allowed = normalizeRoles(allowedRoles)

  return allowed.some((role) => userRoles.includes(role))
}