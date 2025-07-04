import axios from "axios";

const API_BASE = "http://localhost:8080/api/chat";

export function getUnreadCount(userId) {
    return axios.get(`${API_BASE}/unReadCount/${userId}`);
}