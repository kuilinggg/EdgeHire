import { API_BASE_URL } from '../config'
import { useAuthStore } from '../stores/authStore'

function authHeaders(json = false) {
  const authStore = useAuthStore()
  const headers = json ? { 'Content-Type': 'application/json' } : {}
  if (authStore.token) {
    headers.Authorization = `Bearer ${authStore.token}`
  }
  return headers
}

export async function offerAgentChatStream(userId, conversationId, message, signal = null) {
  const options = {
    method: 'POST',
    headers: authHeaders(true),
    body: JSON.stringify({ userId, conversationId, message })
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
  const response = await fetch(`${API_BASE_URL}/offer-agent/context/${userId}`, {
    headers: authHeaders()
  })
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  return response.json()
}

export async function executeOfferAgentTools(userId, conversationId, message) {
  const response = await fetch(`${API_BASE_URL}/offer-agent/tools/execute`, {
    method: 'POST',
    headers: authHeaders(true),
    body: JSON.stringify({ userId, conversationId, message })
  })
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  return response.json()
}

export async function getOfferAgentToolLogs(conversationId) {
  const params = new URLSearchParams({ conversationId })
  const response = await fetch(`${API_BASE_URL}/offer-agent/tools/logs?${params.toString()}`, {
    headers: authHeaders()
  })
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  return response.json()
}

export async function evaluateOfferAgentAnswer({
  userId,
  conversationId,
  message,
  targetPosition = '',
  finalAnswer,
  toolReport
}) {
  const response = await fetch(`${API_BASE_URL}/offer-agent/evaluation/evaluate`, {
    method: 'POST',
    headers: authHeaders(true),
    body: JSON.stringify({
      userId,
      conversationId,
      message,
      targetPosition,
      finalAnswer,
      toolReport
    })
  })
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  return response.json()
}

async function postWorkflow(endpoint, userId, conversationId, targetPosition) {
  const response = await fetch(`${API_BASE_URL}/offer-agent/workflows/${endpoint}`, {
    method: 'POST',
    headers: authHeaders(true),
    body: JSON.stringify({ userId, conversationId, targetPosition })
  })
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  return response.json()
}

export async function runOfferAgentWorkflowStream(
  workflowType,
  userId,
  conversationId,
  signal = null
) {
  const endpointMap = {
    sprint: 'ai-agent-sprint',
    resume: 'resume-optimization',
    interview: 'mock-interview'
  }
  const endpoint = endpointMap[workflowType] || endpointMap.sprint
  const options = {
    method: 'POST',
    headers: authHeaders(true),
    body: JSON.stringify({ userId, conversationId })
  }
  if (signal) {
    options.signal = signal
  }

  const response = await fetch(`${API_BASE_URL}/offer-agent/workflows/${endpoint}/stream`, options)
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  if (!response.body) {
    throw new Error('ReadableStream not supported')
  }
  return response.body
}

export async function runAiAgentSprintWorkflow(userId, conversationId, targetPosition = 'AI Agent 实习生') {
  return postWorkflow('ai-agent-sprint', userId, conversationId, targetPosition)
}

export async function runResumeOptimizationWorkflow(userId, conversationId, targetPosition = 'AI Agent 实习生') {
  return postWorkflow('resume-optimization', userId, conversationId, targetPosition)
}

export async function runMockInterviewWorkflow(userId, conversationId, targetPosition = 'AI Agent 实习生') {
  return postWorkflow('mock-interview', userId, conversationId, targetPosition)
}
