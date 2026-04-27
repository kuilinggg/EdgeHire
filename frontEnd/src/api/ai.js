import { API_BASE_URL } from '../config'
import { useAuthStore } from '../stores/authStore'

export async function resumeOptimizeStream(conversationId, resumeContent, prompt, signal = null) {
  const authStore = useAuthStore()
  const headers = {
    'Content-Type': 'application/json'
  }

  if (authStore.token) {
    headers.Authorization = `Bearer ${authStore.token}`
  }

  const fetchOptions = {
    method: 'POST',
    headers,
    body: JSON.stringify({
      id: conversationId,
      resume: JSON.stringify(resumeContent),
      prompt
    })
  }

  if (signal) {
    fetchOptions.signal = signal
  }

  const response = await fetch(`${API_BASE_URL}/ai/resumeOptimizeStream`, fetchOptions)
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  if (!response.body) {
    throw new Error('ReadableStream not supported')
  }

  return response.body
}

export async function parseStreamResponse(stream, onChunk, onComplete, onError) {
  const reader = stream.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let sawSseLine = false

  try {
    while (true) {
      const { done, value } = await reader.read()

      if (done) {
        if (buffer) {
          sawSseLine = processBuffer(buffer, onChunk, sawSseLine)
        }
        onComplete()
        break
      }

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        sawSseLine = processLine(line, onChunk, sawSseLine)
      }
    }
  } catch (error) {
    console.error('流式响应解析错误:', error)
    onError(error)
  } finally {
    reader.releaseLock()
  }
}

function processBuffer(buffer, onChunk, sawSseLine) {
  const lines = buffer.split('\n')
  let isSse = sawSseLine
  for (const line of lines) {
    isSse = processLine(line, onChunk, isSse)
  }
  return isSse
}

function processLine(line, onChunk, sawSseLine) {
  const trimmedLine = line.trimEnd()

  if (!trimmedLine) {
    return sawSseLine
  }

  if (trimmedLine.startsWith('data:')) {
    let data = trimmedLine.slice(5)
    if (data.startsWith(' ')) {
      data = data.slice(1)
    }

    while (data.startsWith('data:')) {
      data = data.slice(5)
      if (data.startsWith(' ')) {
        data = data.slice(1)
      }
    }

    if (data === '[DONE]') {
      return true
    }

    if (data === '') {
      onChunk('\n')
      return true
    }

    if (data) {
      onChunk(data)
    }
    return true
  }

  if (
    trimmedLine.startsWith('event:') ||
    trimmedLine.startsWith('id:') ||
    trimmedLine.startsWith('retry:')
  ) {
    return true
  }

  onChunk(sawSseLine ? trimmedLine : line)
  return sawSseLine
}
