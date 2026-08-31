# AI 创作平台

个人自研的 AI 创作平台，用于构建 AI 相关项目。大模型能力基于 DeepSeek API。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17 · Spring Boot 3.x · Maven |
| ORM | MyBatis-Plus 3.5.x（复杂 SQL 用 XML） |
| 前端 | React 18 · TypeScript 5 · Ant Design 5 · Vite |
| 数据库 | MySQL 8 · Redis 7 |
| 测试 | JUnit 5 · Mockito · AssertJ |
| CI/CD | GitHub Actions |
| 部署 | Docker · Kubernetes |
| 大模型 | DeepSeek API |

## 目录结构

```
ai-creation-platform/
├── backend/               # Spring Boot 后端
├── frontend/              # React 前端
├── deploy/                # K8s 部署清单（yaml）
├── docs/                  # 设计文档、API 文档
└── .github/workflows/     # CI/CD 流水线
```

### 后端包结构（com.aicreation，DDD 六边形架构）

```
interfaces/          # 主动适配器（Driving Adapters）：请求入口
  web/               # REST 控制器：只做参数校验与协议转换
  job/               # 定时任务
  listener/          # 消息监听
application/         # 应用层：用例编排，不含业务规则
  service/           # 应用服务（用例实现）
  port/
    in/              # 入站端口：用例接口（主动适配器依赖它）
    out/             # 出站端口：仓库/外部服务接口（被动适配器实现它）
domain/              # 领域层：核心业务，零外部框架依赖
  model/             # 聚合、实体、值对象
  service/           # 领域服务
  event/             # 领域事件
infrastructure/      # 被动适配器（Driven Adapters）：外部依赖实现
  persistence/       # MyBatis-Plus 实体与 Mapper（实现出站端口）
  cache/             # Redis 实现
  ai/                # DeepSeek 调用实现（见 AI 集成规范）
common/              # Result、全局异常、常量、工具类
config/              # 配置类
```

依赖方向（自上而下，禁止反向）：`interfaces → application → domain`；
`infrastructure` 实现 `application/port/out` 定义的接口；
domain 不依赖任何层，不 import Spring / MyBatis 等框架（数据库实体放 infrastructure/persistence，与领域模型分离）。

### 前端目录（src/）

```
api/        # 接口定义（按模块拆分）
components/ # 通用组件
pages/      # 页面
hooks/      # 自定义 hooks
store/      # 全局状态
types/      # TS 类型定义
utils/      # 工具函数
```

## 代码规范

### 通用
- 注释一律使用英文；提交信息、文档、UI 文案使用中文
- 变量/方法/函数：camelCase；常量：UPPER_SNAKE_CASE；类名/组件名：PascalCase
- Java 代码 4 空格缩进（遵循 Java 官方风格），前端 TS/CSS 2 空格
- 前端代码通过 ESLint + Prettier 检查（提交前必须通过）

### 后端
- 遵循 DDD 依赖倒置：interfaces（主动适配器）只调用 application 的入站端口；infrastructure（被动适配器）实现出站端口；domain 纯业务，不依赖任何框架
- 所有接口统一返回 `Result<T>`（code / message / data），异常由全局 `@RestControllerAdvice` 统一处理
- 入参使用 DTO + 注解校验（@Validated），禁止直接用 Map 接收参数
- 业务返回用 VO，不直接暴露 entity 给前端
- 使用 Lombok 减少样板代码；字段尽量用包装类型
- Maven 依赖集中在父 pom 的 `<dependencyManagement>` 统一版本管理

### 前端
- 使用 React Hooks + 函数组件，禁止类组件
- 请求统一走封装好的 axios 实例（拦截器处理 token、Result 解包、错误提示）
- 页面组件不做业务逻辑，逻辑放 hooks 或 store

## 数据库规范

- 库名：`ai_creation_platform`；表名单数 snake_case（如 `user`、`generation_task`）
- 所有表必备字段：
  - `id` BIGINT UNSIGNED 自增主键
  - `create_time` / `update_time` DATETIME
  - `deleted` TINYINT 逻辑删除（配合 MyBatis-Plus 逻辑删除配置）
- 字段命名 snake_case，注释说明用途
- 索引：主键之外，为常用查询字段和唯一性字段建立索引
- Redis key 统一前缀：`aicp:<业务域>:<key>`，TTL 必须显式设置

## AI 集成规范（DeepSeek）

- DeepSeek 端口定义在 application/port/out，实现放在 infrastructure/ai（被动适配器），业务代码禁止直接调 HTTP
- API Key 通过环境变量或 K8s Secret 注入，**禁止硬编码、禁止提交到 git**
- 对话类接口支持 SSE 流式输出；模型、temperature 等参数集中管理在配置中
- 外部 API 调用需做超时、重试与降级处理

## Git 工作流（Git Flow）

- `main`：生产分支，只接受 `release/*` 合并，合并后打 tag（SemVer，如 `v0.1.0`）
- `develop`：日常集成分支，功能开发基于此切出
- `feature/<name>`：新功能，完成后合入 `develop`
- `release/<version>`：发布准备（版本号、bugfix），完成后合入 `main` 和 `develop`
- `hotfix/<name>`：生产紧急修复，完成后合入 `main` 和 `develop`

提交信息遵循 Conventional Commits：

```
feat: 新功能      fix: 修复      docs: 文档
style: 格式       refactor: 重构  test: 测试
chore: 构建/工具  ci: CI 配置
```

## 测试规范

- 单元测试：JUnit 5 + Mockito，覆盖应用层与领域层核心逻辑
- 接口测试：@WebMvcTest + MockMvc 验证 Controller 参数校验与返回结构
- 测试命名：`方法名_场景_预期`（如 `getUserById_NotFound_ThrowsException`）
- 每个 feature 分支的代码必须带对应测试，核心业务（AI 调用、任务流转）覆盖率不低于 80%
- 修改代码后必须运行受影响模块的测试，全绿才能提交

## CI/CD（GitHub Actions）

- **CI**：PR 与 push 到 `develop` 时触发 —— 后端 `mvn verify`（单测 + 打包）；前端 `npm run lint && npm run build && npm test`
- **CD**：push 到 `main` 触发 —— 构建后端/前端 Docker 镜像 → 推送镜像仓库 → 更新 `deploy/` 中的镜像版本并应用到 K8s
- 镜像 tag 使用 git sha 或版本号；配置（ConfigMap）与密钥（Secret）分离

## K8s 部署规范

- 每个服务必须有 readinessProbe / livenessProbe 健康检查
- 必须声明 resources 的 requests / limits
- 配置用 ConfigMap，敏感信息（数据库密码、API Key）用 Secret
- 环境隔离：dev 与 prod 使用不同 namespace

## 与 Claude Code 协作

- 每次改动后运行相关测试，测试通过前不提交
- 大改动先看现有代码结构，遵循已有模式，不另起炉灶
- 提交前用 `/code-review` 审查改动
- 不直接提交 `main` / `develop`，走 feature 分支
