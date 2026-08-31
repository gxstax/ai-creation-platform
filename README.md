# AI 创作平台

基于 DeepSeek 大模型的 AI 创作平台，用于构建各类 AI 相关项目。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17 · Spring Boot 3.5 · Maven |
| 架构 | DDD 六边形架构（Ports & Adapters） |
| ORM | MyBatis-Plus 3.5 |
| 前端 | React 18 · TypeScript · Ant Design 5 · Vite 6 |
| 数据库 | MySQL 8 · Redis 7 |
| 测试 | JUnit 5 · Mockito（后端）· Vitest · Testing Library（前端） |
| 大模型 | DeepSeek API |
| CI/CD | GitHub Actions（规划中） |
| 部署 | Docker · Kubernetes（规划中） |

## 项目结构

```
├── backend/     # Spring Boot 后端（DDD 六边形架构）
├── frontend/    # React 前端
├── deploy/      # K8s 部署清单
├── docs/        # 设计文档
└── .github/     # CI/CD 流水线
```

### 后端（DDD 六边形架构）

依赖方向：`interfaces → application → domain`，禁止反向；`infrastructure` 实现 `application/port/out` 端口。

```
com.aicreation
├── interfaces/       # 主动适配器：REST 控制器、定时任务、消息监听
├── application/      # 应用层：用例编排
│   └── port/         # in：入站端口（用例接口）；out：出站端口（仓库/外部服务）
├── domain/           # 领域层：纯业务，零外部框架依赖
├── infrastructure/   # 被动适配器：MyBatis-Plus、Redis、DeepSeek 调用实现
├── common/           # Result、全局异常、常量、工具
└── config/           # 配置类
```

### 前端

```
src/
├── api/         # 接口定义（按模块）
├── components/  # 通用组件
├── pages/       # 页面
├── hooks/       # 自定义 hooks
├── store/       # 全局状态（zustand）
├── types/       # TS 类型定义
└── utils/       # 工具（axios 封装等）
```

## 快速开始

### 环境要求

- JDK 17+（构建需用 JDK 17，Lombok 暂不兼容更高版本）
- Maven 3.9+
- Node 20+
- MySQL 8、Redis 7

### 1. 初始化数据库

```sql
CREATE DATABASE ai_creation_platform DEFAULT CHARACTER SET utf8mb4;
```

### 2. 启动后端

```bash
cd backend
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
```

配置项通过环境变量注入（均有本地默认值）：

| 变量 | 默认值 | 说明 |
|---|---|---|
| `DB_HOST` / `DB_PORT` | `localhost` / `3306` | MySQL 地址 |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `root` | 数据库账号 |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis 地址 |
| `REDIS_PASSWORD` | 空 | Redis 密码 |
| `DEEPSEEK_API_KEY` | 空 | DeepSeek API Key（必填才能调用 AI） |

后端默认运行在 `http://localhost:8080`，健康检查 `GET /actuator/health`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`。开发模式下 `/api` 请求自动代理到 `localhost:8080`。

### 4. 运行测试

```bash
# 后端
cd backend && JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn test

# 前端
cd frontend && npm test && npm run lint && npm run build
```

## 开发规范

完整规则见 [CLAUDE.md](./CLAUDE.md)，核心约定：

- **分支策略**：Git Flow（`main` 生产 / `develop` 集成 / `feature/*` 功能 / `release/*` 发布 / `hotfix/*` 修复）
- **提交信息**：Conventional Commits（`feat:` / `fix:` / `docs:` / `refactor:` / `test:` / `chore:` / `ci:`）
- **后端**：统一 `Result<T>` 返回、全局异常处理、DTO 校验、AI 调用统一封装在 `infrastructure/ai`
- **前端**：函数组件 + Hooks、请求统一走 `utils/request.ts` 封装
- **数据库**：表名 snake_case、必备 `id`/`create_time`/`update_time`/`deleted`（逻辑删除）
- **测试**：核心业务覆盖率 ≥ 80%，改动需带对应测试

## CI/CD 与部署

- **CI**：GitHub Actions（规划中）——PR 与 push `develop` 时执行后端 `mvn verify` 与前端 lint/build/test
- **CD**：push `main` 时构建 Docker 镜像 → 推送镜像仓库 → 更新 K8s 部署
- **部署**：`deploy/` 目录存放 K8s 清单（待完善）

## 路线图

- [x] 项目规则（CLAUDE.md）与目录骨架
- [x] 后端骨架（Spring Boot + DDD + MyBatis-Plus + Redis）
- [x] 前端骨架（Vite + React + TS + Ant Design）
- [ ] GitHub Actions CI/CD 流水线
- [ ] 第一个 DDD 完整用例（创作任务模块）
- [ ] DeepSeek AI 集成层
- [ ] K8s 部署清单
