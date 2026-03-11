import axios from 'axios'

const request = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = 'Bearer ' + token
    }
    return config
})

export const register = (data) => {
    return request.post('/user/register', data)
}

export const login = (data) => {
    return request.post('/user/login', data)
}

export const getInfo = () => {
    return request.get('/user/info')
}

export const updateUser = (data) => {
    return request.put('/user/update', data)
}
