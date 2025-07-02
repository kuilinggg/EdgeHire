import { API_BASE_URL } from '../config'
import { useAuthStore } from '../stores/authStore'

/**
 * AI简历优化流式接口
 * @param {string} conversationId 对话ID
 * @param {object} resumeContent 简历内容
 * @param {string} prompt 用户提示
 * @param {AbortSignal} signal 中止信号
 * @returns {Promise<ReadableStream>} 流式响应
 */
export async function resumeOptimizeStream(conversationId, resumeContent, prompt, signal = null) {
    try {
        const authStore = useAuthStore()
        const headers = {
            'Content-Type': 'application/json'
        }

        // 如果有token，添加Authorization头
        if (authStore.token) {
            headers['Authorization'] = `Bearer ${authStore.token}`
        }

        const fetchOptions = {
            method: 'POST',
            headers,
            body: JSON.stringify({
                id: conversationId,
                resume: JSON.stringify(resumeContent),
                prompt: prompt
            })
        }

        // 如果提供了中止信号，添加到请求选项中
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
    } catch (error) {
        console.error('AI API调用失败:', error)
        throw error
    }
}

/**
 * 解析流式响应
 * @param {ReadableStream} stream 响应流
 * @param {Function} onChunk 接收到数据块时的回调
 * @param {Function} onComplete 完成时的回调
 * @param {Function} onError 错误时的回调
 */
export async function parseStreamResponse(stream, onChunk, onComplete, onError) {
    const reader = stream.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    try {
        while (true) {
            const { done, value } = await reader.read()

            if (done) {
                // 处理缓冲区中剩余的数据
                if (buffer.trim()) {
                    // 最后处理一次buffer中的数据
                    processBuffer(buffer, onChunk)
                }
                onComplete()
                break
            }

            // 解码数据，保持流式特性
            const chunk = decoder.decode(value, { stream: true })
            buffer += chunk

            // 按行处理数据
            const lines = buffer.split('\n')
            buffer = lines.pop() || '' // 保留最后一个可能不完整的行

            for (const line of lines) {
                processLine(line, onChunk)
            }
        }
    } catch (error) {
        console.error('流式响应解析错误:', error)
        onError(error)
    } finally {
        reader.releaseLock()
    }
}

/**
 * 处理单行数据
 * @param {string} line 单行数据
 * @param {Function} onChunk 回调函数
 */
function processLine(line, onChunk) {
    const trimmedLine = line.trim()

    if (!trimmedLine) {
        // 空行，输出换行符
        onChunk('\n')
        return
    }

    if (trimmedLine.startsWith('data:')) {
        // SSE格式处理
        let data = trimmedLine.slice(5) // 移除 'data:' 前缀

        // 如果data后面有空格，也要移除
        if (data.startsWith(' ')) {
            data = data.slice(1)
        }

        // 跳过空的data行、结束标记和只包含data:的行
        if (!data || data === '[DONE]' || data === 'data:' || data.trim() === '') {
            return
        }

        // 递归移除可能存在的多层data:前缀
        while (data.startsWith('data:')) {
            data = data.slice(5)
            if (data.startsWith(' ')) {
                data = data.slice(1)
            }
        }

        // 只有在有实际内容时才输出，并在内容后自动添加换行
        if (data.trim()) {
            onChunk(data + '\n')
        }
    } else if (!trimmedLine.startsWith('event:') && !trimmedLine.startsWith('id:') && !trimmedLine.startsWith('retry:')) {
        // 不是SSE格式的控制行，直接输出并添加换行
        onChunk(trimmedLine + '\n')
    }
}

/**
 * 处理缓冲区数据
 * @param {string} buffer 缓冲区数据
 * @param {Function} onChunk 回调函数
 */
function processBuffer(buffer, onChunk) {
    const lines = buffer.split('\n')
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i]
        processLine(line, onChunk)

        // 如果不是最后一行，添加换行符
        if (i < lines.length - 1) {
            onChunk('\n')
        }
    }
}
