# 项目路线图与任务拆解

目标：在腾讯云服务器上搭建单节点 K3s 集群，实现自动化 CI/CD（GitHub Actions + ArgoCD）、可观测性（Prometheus + Grafana）、运维控制台（Rancher）、业务后台管理系统，并具备持续运维能力。

## 阶段总览

| 阶段 | 内容 | 优先级 | 依赖 | 预计工作量 |
|---|---|---|---|---|
| 0. 基础设施与集群 | 腾讯云 CVM + K3s | P0 | - | 0.5 天 |
| 1. CI/CD GitOps | Docker + GitHub Actions + ArgoCD | P0 | 阶段 0 | 1.5 天 |
| 2. 可观测性 | Prometheus + Grafana + 告警 | P1 | 阶段 0、1 | 1 天 |
| 3. 运维控制台 | Rancher | P1 | 阶段 0 | 0.5 天 |
| 4. 业务后台系统 | 认证 + 用户/任务/AI 配置管理 | P1 | 骨架 | 3-5 天 |
| 5. 运维能力 | 备份、回滚、手册 | P2 | 阶段 0、1 | 1 天 |
| 6. 上线 | 冒烟、文档、v0.2.0 发布 | P2 | 全部 | 0.5 天 |

## 阶段 0：基础设施与集群（P0）

**目标**：一台腾讯云服务器跑起 K3s 单节点集群，作为一切部署的基础。

| # | 任务 | 交付物 | 验收标准 |
|---|---|---|---|
| 0.1 | 购买腾讯云 CVM：**4C8G** 起步（K3s + Prometheus/Grafana + ArgoCD + Rancher + MySQL/Redis + 应用，2C4G 会吃紧），系统 Ubuntu 22.04/24.04，公网 IP，数据盘挂载 | 服务器 | SSH 可登录 |
| 0.2 | 安全组最小化：仅开放 22（SSH）、80/443（Ingress 入口），其余内部访问走集群内网 | 安全组配置 | 非必需端口不可达 |
| 0.3 | 服务器基础配置：创建非 root 用户、设置时区（Asia/Shanghai）、hostname | 环境 | 新用户可 sudo 登录 |
| 0.4 | 安装 K3s 单节点：`curl -sfL https://get.k3s.io | sh -`，kubeconfig 权限 644 | K3s 集群 | `kubectl get nodes` 显示 Ready |
| 0.5 | 安装 Helm（k3s 自带 kubectl，helm 需单独装） | helm | `helm version` 可用 |
| 0.6 | 验证 K3s 自带 Traefik Ingress Controller | Ingress | 部署测试应用可通过 80/443 访问 |
| 0.7 | （可选，建议）域名购买与解析：主域名 A 记录指向服务器 IP，用于 ingress 主机头与 HTTPS | DNS | 域名能解析到服务器 |

**选型说明**：
- 单节点 K3s 的 `deleted` 数据存本地，后续可加 worker 节点平滑扩展
- MySQL/Redis 放集群内（StatefulSet + 本地存储），备份在阶段 5 解决

## 阶段 1：CI/CD GitOps（P0）

**目标**：push `main` 后自动完成"构建 → 测试 → 镜像 → 部署"，集群状态声明式管理、可回滚。

| # | 任务 | 交付物 | 验收标准 |
|---|---|---|---|
| 1.1 | 后端 Dockerfile：多阶段构建（`maven:3.9-eclipse-temurin-17` 构建 → `eclipse-temurin:17-jre` 运行） | `backend/Dockerfile` | 本地 `docker build` 成功，镜像 < 300MB |
| 1.2 | 前端 Dockerfile：多阶段构建（node 构建 → nginx 静态服务，含 `/api` 反代配置） | `frontend/Dockerfile` + nginx.conf | 构建成功，浏览器可访问 |
| 1.3 | 准备 GHCR（GitHub Container Registry）：仓库可见性与推送权限 | GHCR 可用 | 手动 `docker push` 成功 |
| 1.4 | GitHub Actions CI 工作流：PR 与 push `develop` 触发——后端 `mvn verify`、前端 `lint + build + test` | `.github/workflows/ci.yml` | PR 上显示全绿检查 |
| 1.5 | GitHub Actions CD 工作流：push `main` 触发——构建双镜像 → 推 GHCR（tag=git sha） | `.github/workflows/cd.yml` | 镜像出现在 GHCR |
| 1.6 | 安装 ArgoCD（helm，k3s 内，ingress 暴露） | ArgoCD | Web UI 可登录 |
| 1.7 | 编写 `deploy/` K8s 清单：backend/frontend 的 Deployment、Service、Ingress、ConfigMap（配置）、Secret（DB 密码/API Key 占位） | `deploy/` 清单 | `kubectl apply --dry-run` 通过 |
| 1.8 | ArgoCD 创建 Application：关联 GitHub 仓库 `deploy/` 目录，自动同步 + 自愈 | ArgoCD Application | 状态 `Synced / Healthy`；手动改集群配置会被自动纠正 |

**说明**：镜像 tag 更新方式用"CD 工作流更新 deploy/ 清单中镜像 tag 并提交"（简单直接），后续可升级 ArgoCD ImageUpdater。

## 阶段 2：可观测性（P1）

**目标**：集群与应用指标可视化 + 告警，出问题能第一时间发现。

| # | 任务 | 交付物 | 验收标准 |
|---|---|---|---|
| 2.1 | helm 安装 `kube-prometheus-stack`（Prometheus + Grafana + Alertmanager + node-exporter 一体） | 监控栈 | `kubectl get pods -n monitoring` 全 Running |
| 2.2 | 后端接入 Micrometer：pom 加 `micrometer-registry-prometheus`，暴露 `/actuator/prometheus` | 应用指标端点 | curl 返回指标数据 |
| 2.3 | 编写 ServiceMonitor：Prometheus 自动抓取后端指标（scrape 路径 /actuator/prometheus） | `deploy/monitoring/service-monitor.yaml` | Prometheus target 中后端 UP |
| 2.4 | Grafana 看板：集群总览（节点/容器资源）、Spring Boot 看板（JVM、HTTP 请求量、错误率、延迟） | 2-3 张看板 | 看板数据正常刷新 |
| 2.5 | Grafana 访问：ingress 暴露 + 修改初始 admin 密码 | 访问入口 | 浏览器可登录 |
| 2.6 | 告警规则（Pod 重启、CPU 高、接口 5xx 增多）+ Alertmanager 通知（邮箱或飞书/钉钉 webhook） | 告警 + 通知 | 触发规则能收到通知 |

## 阶段 3：运维控制台（P1）

**目标**：集群可视化运维，降低日常维护门槛。

| # | 任务 | 交付物 | 验收标准 |
|---|---|---|---|
| 3.1 | 安装 Rancher（helm 单容器部署，资源占用小；备选 KubeSphere——功能全但重，单节点不推荐） | Rancher | Web UI 可访问 |
| 3.2 | 接入本地 K3s 集群（import 模式） | 集群纳管 | 控制台可见节点/工作负载/Pod 状态 |
| 3.3 | 基础配置：修改 admin 密码、接入告警入口 | 配置 | 可日常使用 |

**说明**：Rancher 与 ArgoCD 职责互补——Rancher 管"看与操作"，ArgoCD 管"声明式部署"。

## 阶段 4：业务后台管理系统（P1）

**目标**：平台自己的管理后台，用现有 React + antd 扩展，管理用户、创作任务、AI 配置。

| # | 任务 | 交付物 | 验收标准 |
|---|---|---|---|
| 4.1 | 需求梳理：用户管理（增删改查/角色）、创作任务管理（列表/详情/取消/重试）、AI 配置管理（模型参数/Key 加密存储）、内容管理、操作审计、系统设置 | 需求清单 | 确认范围 |
| 4.2 | 后端认证：Spring Security + JWT 登录接口 + `admin`/`user` 两级角色，接口权限注解 | 登录/鉴权 | 未登录 401，无权限 403 |
| 4.3 | 后端用户管理模块（DDD：application 用例 + 出站端口实现，含逻辑删除/分页） | 用户 CRUD API | 接口测试通过 |
| 4.4 | 后端创作任务管理模块（状态机：创建→执行→成功/失败，支持取消/重试） | 任务管理 API | 状态流转测试通过 |
| 4.5 | 后端 AI 配置管理（DeepSeek 模型/参数管理，API Key 加密存储不落明文） | AI 配置 API | 敏感字段加密入库 |
| 4.6 | 后端操作审计（登录、敏感操作写审计日志） | 审计 API | 操作可追溯 |
| 4.7 | 前端后台布局：侧边栏菜单 + 登录页 + 路由守卫（未登录跳登录页） | 后台壳 | 路由拦截生效 |
| 4.8 | 前端管理页面：用户/任务/AI 配置/审计列表页（Table + 表单 + 权限按钮） | 管理页面 | 与后端联调通过 |
| 4.9 | 测试与联调：后端单测（核心用例 ≥80% 覆盖）、前端组件测试、全链路走通 | 测试报告 | CI 全绿 |

## 阶段 5：运维能力（P2）

**目标**：数据安全、可回滚、有手册——"能上线也能救回来"。

| # | 任务 | 交付物 | 验收标准 |
|---|---|---|---|
| 5.1 | MySQL/Redis 定时备份：cron 脚本 + 保留策略（7 天），备份异地存放（腾讯云 COS 或第二块云硬盘） | 备份脚本 + 计划任务 | 恢复演练通过（能还原数据） |
| 5.2 | 日志方案：先 `kubectl logs` + 腾讯云 CLS 采集（可选 Loki） | 日志可查 | 故障时能找到日志 |
| 5.3 | 回滚演练：ArgoCD 回滚到上一版本 | 回滚流程 | 演练通过并记录步骤 |
| 5.4 | （可选）HTTPS：cert-manager + Let's Encrypt 或腾讯云免费证书 | 证书 | 浏览器无告警 |
| 5.5 | 运维手册 `docs/ops.md`：常用命令、故障排查清单（服务挂/数据库满/磁盘满）、备份恢复步骤、扩容方法 | 手册 | 按手册可操作 |

## 阶段 6：上线（P2）

| # | 任务 | 交付物 | 验收标准 |
|---|---|---|---|
| 6.1 | 密钥收口：Secret 统一管理（K3s Secret / Rancher 加密 / SOPS 加密入库） | Secret 清单 | 无明文密钥在代码仓库 |
| 6.2 | 冒烟测试：前后端 + AI 调用（DeepSeek 真实 key）全链路 | 测试记录 | 全流程可用 |
| 6.3 | 更新 README（部署章节）与 docs | 文档 | 按文档可复现部署 |
| 6.4 | release/v0.2.0 合入 main，打 tag | 发布 | 线上可访问 |

## 关键依赖关系

```
阶段 0 (服务器+集群) ──► 阶段 1 (CI/CD) ──► 阶段 2 (可观测性)
        │                        │
        └──► 阶段 3 (Rancher)    └──► 阶段 5 (运维能力)
阶段 4 (业务后台) ──► 阶段 6 (上线)
```

- **阶段 0 是唯一硬前置**，服务器到位后 0.4/0.5 半天内集群可跑起来
- 阶段 2/3 依赖集群已部署应用（阶段 1），阶段 4 可并行开发（不依赖服务器）
- 建议顺序：**0 → 1 → 2 → 3 → 5 → 4 → 6**（先把部署链路和监控打通，再做业务后台）
