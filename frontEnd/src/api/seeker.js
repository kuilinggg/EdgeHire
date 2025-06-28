import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/seeker-info';

// 获取所有求职者信息
export function getAllSeekers() {
  return axios.get(API_BASE);
}

// 根据ID获取求职者信息
export function getSeekerById(id) {
  return axios.get(`${API_BASE}/${id}`);
}

// 根据userId获取求职者信息
export function getSeekerByUserId(userId) {
  return axios.get(`${API_BASE}/user/${userId}`);
}

// 创建求职者信息
export function createSeeker(data) {
  return axios.post(API_BASE, data);
}

// 更新求职者信息
export function updateSeeker(id, data) {
  return axios.put(`${API_BASE}/${id}`, data);
}

// 删除求职者信息
export function deleteSeeker(id) {
  return axios.delete(`${API_BASE}/${id}`);
}
