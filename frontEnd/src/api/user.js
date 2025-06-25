import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/users';

export function getUsers() {
  return axios.get(API_BASE);
}

export function getUser(id) {
  return axios.get(`${API_BASE}/${id}`);
}

export function createUser(user) {
  return axios.post(API_BASE, user);
}

export function updateUser(id, user) {
  return axios.put(`${API_BASE}/${id}`, user);
}

export function deleteUser(id) {
  return axios.delete(`${API_BASE}/${id}`);
}
