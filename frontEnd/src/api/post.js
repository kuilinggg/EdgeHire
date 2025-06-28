import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/posts';

// 获取所有投递记录
export function getAllPosts() {
  return axios.get(API_BASE);
}

// 根据ID获取投递记录
export function getPostById(id) {
  return axios.get(`${API_BASE}/${id}`);
}

// 根据userId获取投递记录
export function getPostsByUserId(userId) {
  return axios.get(`${API_BASE}/user/${userId}`);
}

// 创建投递记录
export function createPost(data) {
  return axios.post(API_BASE, data);
}

// 删除投递记录
export function deletePost(id) {
  return axios.delete(`${API_BASE}/${id}`);
}
