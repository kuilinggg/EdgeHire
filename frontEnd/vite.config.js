import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path' // 导入 path 模块

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: { // 添加 resolve 配置
    alias: {
      '@': resolve(__dirname, 'src') // 明确配置 @ 指向 src
    }
  }
})