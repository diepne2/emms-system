import axios from 'axios'
import { getAccessToken, logout } from './auth'

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'https://emms-system-production-4239.up.railway.app'

const api = axios.create({
  baseURL: API_BASE_URL,
})

api.interceptors.request.use((config) => {
  const token = getAccessToken()

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      logout()
    }

    return Promise.reject(err)
  }
)

export default api