import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(config => {
    let token = localStorage.getItem('token')

    if (!token) {
        const userStr = localStorage.getItem('user')
        const user = userStr ? JSON.parse(userStr) : null
        token = user?.token
    }

    if (token) {
        // 自动补 Bearer 前缀，避免后端判定未登录
        config.headers.Authorization = token.startsWith('Bearer ')
            ? token
            : `Bearer ${token}`
    }

    return config
})

// 响应拦截器
request.interceptors.response.use(
    response => response.data,
    error => {
        if (error.response?.status === 401) {
            ElMessage.error('登录已过期，请重新登录')
            localStorage.removeItem('user')
            localStorage.removeItem('token')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export default request
