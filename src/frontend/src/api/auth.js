export const ROLE = {
  ADMIN: 'ROLE_ADMIN',
  QUANLYKYTHUAT: 'ROLE_QUANLYKYTHUAT',
  NHANVIENKYTHUAT: 'ROLE_NHANVIENKYTHUAT',
  NHANVIENVANHANH: 'ROLE_NHANVIENVANHANH',
}

const TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'currentUser'

export function getAccessToken() {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY) || ''
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY) || sessionStorage.getItem(REFRESH_TOKEN_KEY) || ''
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(USER_KEY)

  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(REFRESH_TOKEN_KEY)
  sessionStorage.removeItem(USER_KEY)
}

export function isTokenExpired(token) {
  if (!token) return true

  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 < Date.now()
  } catch {
    return true
  }
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

  alert(message)

  window.location.href = '/#/login'
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
      []

    let roles = []

    if (Array.isArray(authorities)) {
      roles = authorities
    } else if (typeof authorities === 'string') {
      roles = authorities.split(' ').filter(Boolean)
    }

    return {
      username: payload.sub || payload.username || '',
      fullName: payload.fullName || '',
      email: payload.email || '',
      roles,
    }
  } catch {
    return null
  }
}

export function getCurrentUser() {
  const raw = localStorage.getItem(USER_KEY) || sessionStorage.getItem(USER_KEY)
  if (raw) {
    try {
      return JSON.parse(raw)
    } catch {
      return null
    }
  }

  const token = getAccessToken()
  return extractUserFromToken(token)
}

export function hasAnyRole(user, allowedRoles = []) {
  if (!user || !Array.isArray(user.roles)) return false
  return allowedRoles.some((role) => user.roles.includes(role))
}