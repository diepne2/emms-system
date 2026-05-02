import axios from 'axios'
import { clearAuth, getAccessToken } from './auth'

const BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const http = axios.create({
  baseURL: BASE_URL,
  timeout: 20000,
})

http.interceptors.request.use(
  (config) => {
    const token = getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    if (!(config.data instanceof FormData)) {
      config.headers['Content-Type'] = 'application/json'
    }

    return config
  },
  (error) => Promise.reject(error),
)

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      clearAuth()
      window.location.href = '#/login'
    }
    return Promise.reject(error)
  },
)

function unwrapData(response) {
  return response?.data?.data ?? response?.data
}

function buildParams(filter = {}) {
  const params = {}
  Object.entries(filter).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) {
      params[key] = value
    }
  })
  return params
}

export const deviceApi = {
  async filter(filter = {}) {
    const res = await http.get('/api/devices/filter', {
      params: buildParams(filter),
    })
    return unwrapData(res)
  },

  async getById(id) {
    const res = await http.get(`/api/devices/${id}`)
    return unwrapData(res)
  },

  async create(payload) {
    const res = await http.post('/api/devices', payload)
    return unwrapData(res)
  },

  async update(id, payload) {
    const res = await http.put(`/api/devices/${id}`, payload)
    return unwrapData(res)
  },

  async retire(id) {
    const res = await http.delete(`/api/devices/${id}`)
    return res?.status === 204 ? true : unwrapData(res)
  },

  async getDocuments(deviceId) {
    const res = await http.get(`/api/device-documents/device/${deviceId}`)
    return unwrapData(res)
  },

  async uploadDocument(deviceId, file) {
    const formData = new FormData()
    formData.append('deviceId', deviceId)
    formData.append('file', file)

    const res = await http.post('/api/device-documents/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return unwrapData(res)
  },

  async deleteDocument(documentId) {
    const res = await http.delete(`/api/device-documents/${documentId}`)
    return unwrapData(res)
  },

  async downloadDocument(documentId, fileName = 'document') {
    const token = getAccessToken()

    const res = await axios.get(
      `${BASE_URL}/api/device-documents/download/${documentId}`,
      {
        responseType: 'blob',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    )

    const blob = new Blob([res.data], {
      type: res.headers['content-type'] || 'application/octet-stream',
    })

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  },
}