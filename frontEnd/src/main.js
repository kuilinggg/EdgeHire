import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { API_BASE_URL } from './config'
import { createPinia } from "pinia";

const app = createApp(App)
const pinia = createPinia();

// 配置axios默认URL
axios.defaults.baseURL = API_BASE_URL

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router);
app.use(pinia);
app.use(ElementPlus);
app.mount('#app')