import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { AUTH_TOKEN_KEY, USER_ID_KEY } from '../../constants/auth'

export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  error?: string
}

export class AppError extends Error {
  constructor(
    message: string,
    public code?: string,
    public status?: number
  ) {
    super(message)
    this.name = 'AppError'
  }
}

class ApiClient {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: '/api',
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    // 请求拦截器
    this.client.interceptors.request.use(
      (config) => {
        // 添加认证token
        const token = localStorage.getItem(AUTH_TOKEN_KEY)
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    // 响应拦截器
    this.client.interceptors.response.use(
      (response: AxiosResponse) => {
        const data = response.data as ApiResponse
        // 统一处理业务逻辑错误
        if (data && data.success === false) {
          return Promise.reject(new AppError(data.error || '操作失败', 'BUSINESS_ERROR'))
        }
        return response.data
      },
      (error) => {
        // 统一错误处理
        if (error.response?.status === 401) {
          // 未授权，跳转到登录页
          localStorage.removeItem(AUTH_TOKEN_KEY)
          localStorage.removeItem(USER_ID_KEY)
          window.location.href = '/login'
          return Promise.reject(new AppError('未授权，请重新登录', 'UNAUTHORIZED', 401))
        }
        const message = error.response?.data?.error || error.message || '网络错误'
        return Promise.reject(new AppError(message, 'NETWORK_ERROR', error.response?.status))
      }
    )
  }

  public get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.client.get(url, config)
  }

  public post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.client.post(url, data, config)
  }

  public put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.client.put(url, data, config)
  }

  public delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.client.delete(url, config)
  }

  public patch<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.client.patch(url, data, config)
  }
}

const apiClient = new ApiClient()
export default apiClient