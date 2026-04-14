import axios from "axios"

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"

const axiosClient = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
})

axiosClient.interceptors.request.use(
  (config) => {
    const token =
      localStorage.getItem("accessToken") || sessionStorage.getItem("accessToken")

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => Promise.reject(error)
)

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem("accessToken")
      localStorage.removeItem("refreshToken")
      sessionStorage.removeItem("accessToken")
      sessionStorage.removeItem("refreshToken")
      window.location.href = "/login"
    }

    return Promise.reject(error)
  }
)

export default axiosClient