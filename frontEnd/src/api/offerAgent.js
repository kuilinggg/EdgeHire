import { API_BASE_URL } from '../config'
import { useAuthStore } from '../stores/authStore'

export async function offerAgentChatStream(userId, conversationId, message, signal = null) {
  const authStore = useAuthStore()
  const headers = {
    'Content-Type': 'application/json'
  }

  if (authStore.token) {
    headers.Authorization = `Bearer ${authStore.token}`
  }

  const options = {
    method: 'POST',
    headers,
    body: JSON.stringify({
      userId,
      conversationId,
      message
    })
  }

  if (signal) {
    options.signal = signal
  }

  const response = await fetch(`${API_BASE_URL}/offer-agent/chat/stream`, options)
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  if (!response.body) {
    throw new Error('ReadableStream not supported')
  }

  return response.body
}

export async function getOfferAgentContext(userId) {
  const authStore = useAuthStore()
  const headers = {}
  if (authStore.token) {
    headers.Authorization = `Bearer ${authStore.token}`
  }

  const response = await fetch(`${API_BASE_URL}/offer-agent/context/${userId}`, { headers })
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  return response.json()
}
