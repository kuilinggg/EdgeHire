import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/resume'

export function getAllRusumes(){
  return axios.get(API_BASE)
}

export function getResumesByUserId(userId) {
  return axios.get(`${API_BASE}/user/${userId}`)
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
