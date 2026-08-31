import axios, { type AxiosRequestConfig } from 'axios'
import { message } from 'antd'
import type { ApiResult } from '@/types/api'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 30_000,
})

// Attach auth token to every request
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('aicp_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Unwrap Result<T> and surface business errors as antd messages
request.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResult<unknown>
    if (res.code !== 0) {
      message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return response
  },
  (error) => {
    message.error(error.response?.data?.message || '网络异常')
    return Promise.reject(error)
  },
)

export async function get<T>(url: string, params?: object, config?: AxiosRequestConfig): Promise<T> {
  const res = await request.get<ApiResult<T>>(url, { params, ...config })
  return res.data.data
}

export async function post<T>(url: string, data?: object, config?: AxiosRequestConfig): Promise<T> {
  const res = await request.post<ApiResult<T>>(url, data, config)
  return res.data.data
}

export default request
