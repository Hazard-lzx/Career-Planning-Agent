import axios from 'axios'
import { ElMessage } from 'element-plus'

const instance = axios.create({
  baseURL: '',
  timeout: 120000,
  headers: {}
})

// 请求拦截器：自动附加 JWT token
instance.interceptors.request.use(config => {
  const token = localStorage.getItem('career_agent_token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
}, error => Promise.reject(error))

// 响应拦截器：处理错误
instance.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('career_agent_token')
      localStorage.removeItem('career_agent_user')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    ElMessage.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  }
)

export default instance
