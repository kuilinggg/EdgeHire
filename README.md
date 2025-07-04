

# EdgeHire

EdgeHire 是一个专注于招聘与求职的服务平台，结合AI技术，为用户提供简历优化、职位匹配、在线沟通等功能。本项目由前端与后端模块组成，后端进一步分为AI模块与核心业务模块。

---

## 后端配置

后端使用 Java 编写，基于 Spring Boot 框架，包含以下核心组件：

- **用户管理**：支持用户注册、登录、密码修改等功能。
- **职位管理**：HR可以发布职位，求职者可以查看职位信息。
- **简历管理**：求职者可以上传、修改、删除简历，HR可以查看并优化简历。
- **AI 优化**：提供简历AI优化建议，支持流式与非流式交互。
- **指导请求**：求职者可以申请职业指导，HR可以接收与处理指导请求。
- **消息系统**：支持用户间在线聊天，实时统计未读消息、最新消息等。
- **WebSocket 支持**：实时在线用户统计、消息推送等。

后端通过 RESTful API 提供服务，支持 CORS，使用 JWT 进行身份验证。

---

## AI 配置

AI 模块基于 Spring Boot，整合了 OpenAI SDK，提供以下功能：

- **简历优化**：通过 `/api/ai/resumeOptimize` 提供简历优化服务。
- **流式简历优化**：通过 `/api/ai/resumeOptimizeStream` 提供流式优化建议。
- **自定义 Prompt**：初始 Prompt 存储在 `resources/prompts/initial.txt` 中，可根据需求修改。
- **ChatMemory 支持**：支持会话记忆，为用户提供连贯的 AI 交互体验。

---

## 项目基础功能

- 用户注册与登录
- 职位发布与搜索
- 简历上传与管理
- AI 简历优化建议
- 求职者与 HR 在线沟通
- 指导请求提交与处理
- 实时在线统计与消息推送

---

## 项目拓展功能

- 简历 A4 打印样式支持（提供多种简历模板）
- 用户资料管理（头像、个人信息、HR认证等）
- 职位匹配推荐
- 会员系统（Vip 功能预留）
- 数据统计与分析（KPI、用户行为等）
- 文件上传与下载支持（简历、图片等）

---

## 如何启动项目

### 后端模块

1. 确保已安装 JDK 17+ 与 Maven
2. 进入 `backEnd` 目录
3. 执行 `mvnw spring-boot:run` 启动后端服务

### AI 模块

1. 确保后端服务已启动
2. 进入 `ai-module` 目录
3. 执行 `mvnw spring-boot:run` 启动 AI 微服务

### 前端模块

1. 确保已安装 Node.js 与 npm
2. 进入 `frontEnd` 目录
3. 执行 `npm install` 安装依赖
4. 执行 `npm run dev` 启动开发服务器

---

## 使用说明

- 访问 `http://localhost:5173` 进入前端页面
- 用户可登录、注册、修改个人信息
- 求职者可上传简历、申请职位、发起职业指导请求
- HR 可发布职位、接收指导请求、查看简历并提供优化建议
- 用户间可通过 `/api/chat` 接口进行消息交互

---

## 技术栈

- 后端：Spring Boot, Spring WebFlux, Spring Data JPA, JWT
- 前端：Vue 3, Vite, Pinia, WebSocket
- 数据库：MySQL / H2（开发环境）
- AI 模块：OpenAI SDK, ChatClient
- 实时通信：WebSocket + ServerEndpointExporter

---

## 开源协议

本项目采用 MIT License，请参考 LICENSE 文件获取详细信息。