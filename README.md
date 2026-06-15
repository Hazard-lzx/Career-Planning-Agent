# ?? Career Planning Agent

AI 赋能的职业规划助手，基于 Spring Boot + Vue 3 全栈架构，提供智能岗位匹配、职业路径分析、简历诊断与报告生成等一站式职业规划服务。

## 技术栈

### 后端
| 技术 | 说明 |
|------|------|
| Spring Boot 3.x | 应用框架 |
| MyBatis-Plus | ORM 框架 |
| LangChain4j | AI 大模型集成 |
| Neo4j | 知识图谱（职业技能关系图） |
| Redis | 缓存 |
| RocketMQ | 消息队列 |
| MySQL | 关系型数据库 |
| Knife4j | API 文档 |
| SpringDoc OpenAPI | 接口规范 |

### 前端
| 技术 | 说明 |
|------|------|
| Vue 3 | 前端框架 |
| Vite | 构建工具 |
| Pinia | 状态管理 |
| Vue Router | 路由管理 |

## 项目结构

```
career-planning-agent/
├── career-agent-backend/          # 后端服务
│   ├── src/main/java/com/career/agent/
│   │   ├── agent/                 # AI Agent 核心
│   │   ├── ai/                    # AI 服务接口
│   │   ├── common/                # 通用组件（异常、结果封装）
│   │   ├── config/                # 配置（安全、Redis、RocketMQ 等）
│   │   ├── controller/            # 接口层
│   │   ├── dto/                   # 数据传输对象
│   │   ├── entity/                # 数据实体
│   │   ├── listener/              # RocketMQ 监听器
│   │   ├── mapper/                # MyBatis 映射
│   │   ├── neo4j/                 # 图数据库实体与仓库
│   │   ├── service/               # 业务逻辑层
│   │   └── util/                  # 工具类
│   └── src/main/resources/
│       ├── db/                    # 数据库初始化脚本
│       └── prompts/               # AI 提示词模板
│
└── career-agent-frontend/         # 前端应用
    └── src/
        ├── components/            # 通用组件
        ├── router/                # 路由配置
        ├── stores/                # 状态管理
        ├── utils/                 # 工具函数
        └── views/                 # 页面视图
```

## 快速开始

### 前置条件

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis
- RocketMQ
- Neo4j

### 本地运行

**1. 数据库初始化**

创建 MySQL 数据库 `career_agent`，执行初始化脚本：

```sql
source career-agent-backend/src/main/resources/db/init.sql
```

**2. 后端配置**

复制配置模板并填入实际参数：

```bash
cp career-agent-backend/src/main/resources/application-example.yml career-agent-backend/src/main/resources/application.yml
```

编辑 `application.yml`，设置数据库密码、Redis 密码、Neo4j 连接信息等。

**3. 启动后端**

```bash
cd career-agent-backend
mvn spring-boot:run
```

**4. 启动前端**

```bash
cd career-agent-frontend
npm install
npm run dev
```

访问 http://localhost:5173 即可使用。

## 核心功能

- ?? **AI Agent 对话** — 基于 LangChain4j + DeepSeek/Qwen 多轮对话
- ?? **人岗匹配分析** — 简历解析 + 岗位要求智能匹配
- ??? **职业路径规划** — Neo4j 知识图谱驱动的技能发展路线
- ?? **报告生成优化** — 自动生成职业分析报告并支持润色

## License

MIT
