import axios from 'axios'
import { API_BASE_URL } from '../config'

const uploadClient = axios.create({
    baseURL: API_BASE_URL,
    timeout: 30000, // 上传文件需要更长超时时间
    headers: {
        'Content-Type': 'multipart/form-data'
    }
})

export function uploadFile(file) {
    return uploadClient.post('files/upload', file)
}