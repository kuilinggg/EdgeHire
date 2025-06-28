import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/t_info';
const UPLOAD_URL = 'http://localhost:8080/api/upload';

// 获取用户信息
export function getInfoByUserId(userId) {
  return axios.get(`${API_BASE}/user/${userId}`)
}

// 新建个人信息
export function createInfo(data) {
  return axios.post(API_BASE, data)
}

// 更新个人信息
export function updateInfo(id, data) {
  return axios.put(`${API_BASE}/${id}`, data)
}

// 上传文件（通用，可用于头像或其它文件）
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return axios.post(UPLOAD_URL, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

//获取头像URL
export async function getAvatarUrl(userId) {
  const res = await axios.get(`${API_BASE}/avatar/${userId}`)
  return res.data
}
