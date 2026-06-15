# Career Planning Agent · AI大学生职业规划智能体

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=flat&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/LangChain4j-0.35-3178C6?style=flat" alt="LangChain4j">
  <img src="https://img.shields.io/badge/Vue%203-4FC08D?style=flat&logo=vuedotjs" alt="Vue 3">
  <img src="https://img.shields.io/badge/Neo4j-4581C3?style=flat&logo=neo4j" alt="Neo4j">
  <img src="https://img.shields.io/badge/RocketMQ-2.3.0-D77310?style=flat&logo=apacherocketmq" alt="RocketMQ">
  <img src="https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis" alt="Redis">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat&logo=mysql" alt="MySQL">
  <img src="https://img.shields.io/badge/Element%20Plus-409EFF?style=flat&logo=element" alt="Element Plus">
  <img src="https://img.shields.io/badge/ECharts-AA344D?style=flat&logo=apacheecharts" alt="ECharts">
</p>

## 项目简介

**Career Planning Agent** 是一个基于大语言模型的智能职业规划系统，针对大学生求职痛点，提供从 **AI 智能咨询 → 简历解析 → 能力画像 → 人岗匹配 → 路径规划 → 报告生成** 的一站式全链路服务。

后端采用 **Spring Boot 3.3** 单体架构，通过 **LangChain4j** 内置集成大语言模型（DeepSeek / Qwen），无需额外部署 AI 微服务。结合 **Neo4j** 职业知识图谱、**RocketMQ** 异步消息、**Redis** 分布式缓存等中间件，实现高可用、可扩展的企业级后端系统。前端基于 **Vue 3 + Element Plus + ECharts**，提供流畅的交互体验。

---

## 系统架构

```
┌─────────────────────────────────────────────────┐
│            Vue 3 + Element Plus + ECharts         │
│                  (前端界面)                        │
└──────────────────┬──────────────────────────────┘
                   │ HTTP / REST
┌──────────────────▼──────────────────────────────┐
│          Spring Boot 3.3 · Java 17               │
│     (业务后端 - MySQL / Neo4j / Redis / RocketMQ)  │
│                                                   │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐  │
│  │Auth  │ │学生  │ │岗位  │ │匹配  │ │报告  │  │
│  │Controller│Controller│Controller│Controller│Controller│  │
│  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘  │
│                                                   │
│        ┌──────────────────────────────┐          │
│        │    LangChain4j LLM 集成       │          │
│        │  (DeepSeek / Qwen / 国产模型)  │          │
│        └──────────────────────────────┘          │
│                                                   │
│        ┌──────────────────────────────┐          │
│        │    RocketMQ 异步消息          │          │
│        └──────────────────────────────┘          │
│                                                   │
│        ┌──────────────────────────────┐          │
│        │    Neo4j 职业知识图谱          │          │
│        │  (垂直晋升 / 水平换岗路径)     │          │
│        └──────────────────────────────┘          │
└─────────────────────────────────────────────────┘
```

---

## 核心功能

### 1. AI Agent 智能对话
- 基于 **LangChain4j** 集成多模态大模型（DeepSeek / Qwen）
- 支持多轮对话，上下文感知的职业咨询体验
- 内置 Agent 配置，可动态切换模型与推理参数

### 2. 简历智能解析
- 上传 Word / PDF 简历，调用大模型自动提取结构化信息
- 生成**学生能力画像**：教育背景、专业技能、证书资质、软技能、实习经历、项目经历

### 3. 人岗多维度匹配

| 维度 | 权重 | 算法 |
|------|------|------|
| 基础条件 | 20% | 学历等级映射 + 专业关键词匹配 |
| 硬技能 | 40% | Jaccard 相似度（交集/并集） |
| 软技能 | 25% | 大模型语义评分 / 级别映射兜底 |
| 发展潜力 | 15% | 大模型评估 / 数量统计兜底 |

- 输出总分、各维度得分、差距分析文本、匹配/缺失技能列表

### 4. 职业知识图谱
- 基于 **Neo4j** 图数据库构建岗位关系网络
- **垂直晋升路径**（PROMOTE_TO）：同系列岗位按级别晋升
- **水平换岗路径**（CAN_SWITCH_TO）：技能相似度 > 0.6 的跨系列岗位切换
- Cypher 可变长度路径查询，1 行代码查出完整职业路线

### 5. 智能报告生成
- 6 章节完整职业规划报告：自我认知 → 岗位剖析 → 匹配分析 → 路径规划 → 行动计划 → 评估调整
- **RocketMQ** 异步架构：秒级响应返回 pending，后台完成 AI 计算
- 支持单段润色 + 导出 Word 文档

---

## 技术栈

### 后端
| 技术 | 用途 |
|------|------|
| Spring Boot 3.3.5 | 应用框架 |
| MyBatis-Plus 3.5.5 | ORM / MySQL 操作 |
| LangChain4j 0.35.0 | AI 大模型集成（DeepSeek / Qwen） |
| Spring Data Neo4j | 图数据库操作 |
| Spring Data Redis | 缓存与分布式 Session |
| RocketMQ 2.3.0 | 异步消息 |
| Knife4j 4.5.0 | API 文档 |
| JWT | 用户认证 |
| Apache POI | Word 文档处理 |

### 前端
| 技术 | 用途 |
|------|------|
| Vue 3 + Vite 5 | 前端框架 |
| Element Plus 2.5 | UI 组件库 |
| ECharts 5.5 + vue-echarts | 雷达图数据可视化 |
| Axios | HTTP 请求 |
| Pinia | 状态管理 |
| Vue Router 4 | 路由管理 |

### 数据库
| 数据库 | 用途 |
|--------|------|
| MySQL 8.0 | 业务数据（用户 / 学生 / 岗位 / 报告） |
| Neo4j 5.x | 职业知识图谱 |
| Redis 7.x | 学生画像缓存 / 会话管理 |

---

## 项目结构

```
Career Planning Agent/
├── career-agent-backend/          # Spring Boot 3.3 后端
│   ├── src/main/java/com/career/agent/
│   │   ├── agent/                 # AI Agent 核心配置与聊天服务
│   │   ├── ai/                    # AI 服务接口
│   │   ├── common/                # 统一响应、业务异常、全局异常处理
│   │   ├── config/                # Jackson / Redis / RocketMQ / Knife4j / 安全配置
│   │   ├── controller/            # 7 个 RESTful Controller
│   │   ├── dto/                   # 数据传输对象
│   │   ├── entity/                # MySQL 实体
│   │   ├── listener/              # RocketMQ 消息消费者
│   │   ├── mapper/                # MyBatis-Plus Mapper
│   │   ├── neo4j/                 # Neo4j 实体 + Repository
│   │   ├── service/               # 业务接口 + 实现
│   │   └── util/                  # JWT 工具类
│   └── src/main/resources/
│       ├── application.yml        # 主配置文件（已 gitignore）
│       ├── application-example.yml # 配置模板
│       ├── db/                    # MySQL + Neo4j 初始化脚本
│       └── prompts/               # 8 个 LLM 提示词模板
│
├── career-agent-frontend/         # Vue 3 前端
│   ├── src/
│   │   ├── views/                 # 8 个页面组件
│   │   │   ├── Login.vue          # 登录注册
│   │   │   ├── Home.vue           # 首页
│   │   │   ├── AgentChat.vue      # AI 对话
│   │   │   ├── JobList.vue        # 岗位列表
│   │   │   ├── JobDetail.vue      # 岗位详情
│   │   │   ├── ReportList.vue     # 报告列表
│   │   │   ├── ReportDetail.vue   # 报告详情
│   │   │   ├── Profile.vue        # 个人画像
│   │   │   └── My.vue             # 个人中心
│   │   ├── components/            # 雷达图、路径时间线等通用组件
│   │   ├── router/                # 路由配置
│   │   ├── stores/                # Pinia 状态管理
│   │   └── utils/                 # Axios 请求封装
│   └── vite.config.js
│
└── README.md
```

---

## 快速开始

### 前置条件

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Neo4j 5.x
- Redis 7.x
- RocketMQ 5.x
- 大模型 API Key（DeepSeek / 通义千问）

### 1. 启动 MySQL + Neo4j + Redis + RocketMQ

确保本地已启动以上中间件服务，端口配置见 `application-example.yml`。

### 2. 初始化数据库

```sql
-- 执行 SQL 初始化脚本
source career-agent-backend/src/main/resources/db/init.sql;

-- 执行 Neo4j 初始化
-- 在 Neo4j Browser 中运行 neo4j_init.cypher 内容
```

### 3. 后端配置

```bash
cd career-agent-backend

# 复制配置模板并填入实际参数
cp src/main/resources/application-example.yml src/main/resources/application.yml
```

编辑 `application.yml`，配置数据库密码、Redis 密码、Neo4j 连接信息、DEEPSEEK_API_KEY 等。

### 4. 启动后端

```bash
cd career-agent-backend
mvn clean install -DskipTests
mvn spring-boot:run
# 服务运行在 http://localhost:8080
# API 文档：http://localhost:8080/doc.html
```

### 5. 启动前端

```bash
cd career-agent-frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

---

## API 概览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/register | 用户注册 |
| POST | /api/student/resume/upload | 上传简历并解析 |
| GET | /api/student/profile/{id} | 获取学生能力画像 |
| PUT | /api/student/profile/{id} | 更新学生能力画像 |
| GET | /api/job/list | 分页查询岗位列表 |
| GET | /api/job/{id} | 获取岗位详情 |
| POST | /api/job/batch-import | 批量导入岗位（CSV） |
| POST | /api/job/{id}/generate-profile | 生成岗位画像 |
| POST | /api/match/calculate | 计算人岗匹配 |
| POST | /api/report/generate | 生成职业规划报告（异步） |
| GET | /api/report/{id} | 获取报告详情 |
| GET | /api/report/list/{studentId} | 查询学生报告列表 |
| POST | /api/report/{id}/polish | 润色报告中某章节 |
| GET | /api/report/{id}/export | 导出 Word 报告 |
| DELETE | /api/report/{id} | 删除报告 |
| GET | /api/path/vertical/{jobId} | 查询垂直晋升路径 |
| GET | /api/path/horizontal/{jobId} | 查询水平换岗路径 |
| POST | /api/agent/chat | AI Agent 对话 |

---

## 开源协议

本项目仅供学习和个人使用。

---

## 作者

- **Hazard-lzx** - [GitHub](https://github.com/Hazard-lzx)
