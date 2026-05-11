import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_BASE_URL || ''

export const inventoryApi = axios.create({
  baseURL: API_BASE,
})

inventoryApi.interceptors.request.use((config) => {
  const token =
    localStorage.getItem('token') ||
    localStorage.getItem('accessToken') ||
    localStorage.getItem('access_token') ||
    localStorage.getItem('jwt')

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

export const getErrorMessage = (err) => {
  return (
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    err?.message ||
    'Có lỗi xảy ra'
  )
}