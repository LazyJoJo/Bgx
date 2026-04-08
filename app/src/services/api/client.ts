import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'

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
        const token = localStorage.getItem('auth_token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    //响应拦截器
    this.client.interceptors.response.use(
      (response: AxiosResponse) => {
        return response.data
      },
      (error) => {
        //统一错误处理
        if (error.response?.status === 401) {
          // 未授权，跳转到登录页
          localStorage.removeItem('auth_token')
          localStorage.removeItem('userId')
          window.location.href = '/login'
        }
        return Promise.reject(error)
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