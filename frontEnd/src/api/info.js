import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/t_info';

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

//获取头像URL
export async function getAvatarUrl(userId) {
  const res = await axios.get(`${API_BASE}/avatar/${userId}`)
  return res.data
}
