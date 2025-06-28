import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/resume'

export function getAllResumes(){
  return axios.get(API_BASE)
}

// 获取当前用户的所有简历（数组）
export function getResumesByUserId(userId) {
  return axios.get(`${API_BASE}/user/${userId}`)
}

// 获取当前用户的第一份简历（单份，常用于编辑/展示）
export async function getResumeByUserId(userId) {
  const res = await getResumesByUserId(userId)
  if (Array.isArray(res.data) && res.data.length > 0) {
    return { data: res.data[0] }
  } else {
    return { data: null }
  }
}

// 语义更清晰的单份简历获取（可选用）
export async function getSingleResumeByUserId(userId) {
  return getResumeByUserId(userId)
}

export function getResumeById(id) {
  return axios.get(`${API_BASE}/${id}`)
}

export function createResume(data) {
  return axios.post(API_BASE, data)
}

export function updateResume(id, data) {
  return axios.put(`${API_BASE}/${id}`, data)
}

export function deleteResume(id) {
  return axios.delete(`${API_BASE}/${id}`)
}
