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

export async function executeOfferAgentTools(userId, conversationId, message) {
  const authStore = useAuthStore()
  const headers = {
    'Content-Type': 'application/json'
  }
  if (authStore.token) {
    headers.Authorization = `Bearer ${authStore.token}`
  }

  const response = await fetch(`${API_BASE_URL}/offer-agent/tools/execute`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ userId, conversationId, message })
  })
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  return response.json()
}

export async function getOfferAgentToolLogs(conversationId) {
  const authStore = useAuthStore()
  const headers = {}
  if (authStore.token) {
    headers.Authorization = `Bearer ${authStore.token}`
  }

  const params = new URLSearchParams({ conversationId })
  const response = await fetch(`${API_BASE_URL}/offer-agent/tools/logs?${params.toString()}`, { headers })
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  return response.json()
}

export async function runAiAgentSprintWorkflow(userId, conversationId, targetPosition = 'AI Agent intern') {
  const authStore = useAuthStore()
  const headers = {
    'Content-Type': 'application/json'
  }
  if (authStore.token) {
    headers.Authorization = `Bearer ${authStore.token}`
  }

  const response = await fetch(`${API_BASE_URL}/offer-agent/workflows/ai-agent-sprint`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ userId, conversationId, targetPosition })
  })
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  return response.json()
}

export async function runResumeOptimizationWorkflow(userId, conversationId, targetPosition = 'AI Agent intern') {
  const authStore = useAuthStore()
  const headers = {
    'Content-Type': 'application/json'
  }
  if (authStore.token) {
    headers.Authorization = `Bearer ${authStore.token}`
  }

  const response = await fetch(`${API_BASE_URL}/offer-agent/workflows/resume-optimization`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ userId, conversationId, targetPosition })
  })
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  return response.json()
}

export async function runMockInterviewWorkflow(userId, conversationId, targetPosition = 'AI Agent intern') {
  const authStore = useAuthStore()
  const headers = {
    'Content-Type': 'application/json'
  }
  if (authStore.token) {
    headers.Authorization = `Bearer ${authStore.token}`
  }

  const response = await fetch(`${API_BASE_URL}/offer-agent/workflows/mock-interview`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ userId, conversationId, targetPosition })
  })
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  return response.json()
}
