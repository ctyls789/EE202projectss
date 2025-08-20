import axios from 'axios'
import router from '@/router'

const api = axios.create({
  baseURL: 'http://localhost:8082',
  timeout: 5000
})

// 自動帶 Token
api.interceptors.request.use((config) => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (user?.token) {
    config.headers.Authorization = `Bearer ${user.token}`
  }
  return config
})

// 如果 401，跳回登入
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      // localStorage.removeItem('user')
      router.push('/login')
    }
    return Promise.reject(err)
  }
)

export default api