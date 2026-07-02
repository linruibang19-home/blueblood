# 从零接手项目手册 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `docs/04-从零接手项目手册/` 下创建一套面向“几天没看/刚接手项目”的系统学习手册，帮助用户从 0 重新理解、跑通、阅读、掌握蓝血菁英平台。

**Architecture:** 新增一个独立文档目录，不改动现有代码与现有文档结构。文档按“先恢复全貌 → 再跑通项目 → 再理解业务 → 再读代码 → 再吃透核心链路 → 最后自测”的顺序组织。

**Tech Stack:** Markdown 文档、现有 Spring Boot/Vue3/uni-app/MySQL/Redis/Docker 项目资料。

---

## File Structure

Create:
- `docs/04-从零接手项目手册/README.md`：入口索引、阅读路线。
- `docs/04-从零接手项目手册/00-先看这篇-如何使用本手册.md`：学习方法和 1/3/7 天恢复路线。
- `docs/04-从零接手项目手册/01-项目一句话讲清楚.md`：项目定位、业务对象、四端、核心闭环。
- `docs/04-从零接手项目手册/02-目录结构导览.md`：仓库目录解释和优先阅读文件。
- `docs/04-从零接手项目手册/03-本地启动与验证.md`：Docker/本地启动与验证清单。
- `docs/04-从零接手项目手册/04-账号角色与演示路径.md`：演示账号、角色权限、推荐演示路径。
- `docs/04-从零接手项目手册/05-业务全流程从用户视角走一遍.md`：普通用户、企业用户、管理员、学习成长路线。
- `docs/04-从零接手项目手册/06-后端代码阅读路线.md`：Spring Boot 后端阅读顺序。
- `docs/04-从零接手项目手册/07-前台H5代码阅读路线.md`：user-web 阅读顺序。
- `docs/04-从零接手项目手册/08-后台管理端代码阅读路线.md`：admin-web 阅读顺序。
- `docs/04-从零接手项目手册/09-小程序代码阅读路线.md`：miniapp 阅读顺序。
- `docs/04-从零接手项目手册/10-数据库从零理解.md`：按业务域理解数据表与核心关系。
- `docs/04-从零接手项目手册/11-核心链路一-登录认证JWT.md`：登录认证链路。
- `docs/04-从零接手项目手册/12-核心链路二-任务发布与接单.md`：任务发布、接单、秒杀库存链路。
- `docs/04-从零接手项目手册/13-核心链路三-里程碑提交审核结算.md`：里程碑与钱包入账链路。
- `docs/04-从零接手项目手册/14-核心链路四-钱包提现.md`：提现与后台审核链路。
- `docs/04-从零接手项目手册/15-Redis在项目里到底做了什么.md`：Redis key 与业务用途。
- `docs/04-从零接手项目手册/16-如果我要继续开发应该从哪里下手.md`：新增功能开发路线。
- `docs/04-从零接手项目手册/17-常见问题与排坑清单.md`：启动、权限、数据库、前端联调常见坑。
- `docs/04-从零接手项目手册/18-重新掌握项目自测题.md`：L1-L5 自测题。

---

## Tasks

### Task 1: Create handbook entry and usage guide

**Files:**
- Create: `docs/04-从零接手项目手册/README.md`
- Create: `docs/04-从零接手项目手册/00-先看这篇-如何使用本手册.md`
- Create: `docs/04-从零接手项目手册/01-项目一句话讲清楚.md`
- Create: `docs/04-从零接手项目手册/02-目录结构导览.md`

- [ ] Step 1: Create README with handbook purpose and reading order.
- [ ] Step 2: Create usage guide with 1-day/3-day/7-day routes.
- [ ] Step 3: Create project positioning document.
- [ ] Step 4: Create directory guide.

### Task 2: Create running and role walkthrough docs

**Files:**
- Create: `docs/04-从零接手项目手册/03-本地启动与验证.md`
- Create: `docs/04-从零接手项目手册/04-账号角色与演示路径.md`
- Create: `docs/04-从零接手项目手册/05-业务全流程从用户视角走一遍.md`

- [ ] Step 1: Create Docker and native startup document.
- [ ] Step 2: Create account and role document.
- [ ] Step 3: Create user-perspective business walkthrough.

### Task 3: Create code reading route docs

**Files:**
- Create: `docs/04-从零接手项目手册/06-后端代码阅读路线.md`
- Create: `docs/04-从零接手项目手册/07-前台H5代码阅读路线.md`
- Create: `docs/04-从零接手项目手册/08-后台管理端代码阅读路线.md`
- Create: `docs/04-从零接手项目手册/09-小程序代码阅读路线.md`

- [ ] Step 1: Create backend reading route.
- [ ] Step 2: Create user-web reading route.
- [ ] Step 3: Create admin-web reading route.
- [ ] Step 4: Create miniapp reading route.

### Task 4: Create database and core chain docs

**Files:**
- Create: `docs/04-从零接手项目手册/10-数据库从零理解.md`
- Create: `docs/04-从零接手项目手册/11-核心链路一-登录认证JWT.md`
- Create: `docs/04-从零接手项目手册/12-核心链路二-任务发布与接单.md`
- Create: `docs/04-从零接手项目手册/13-核心链路三-里程碑提交审核结算.md`
- Create: `docs/04-从零接手项目手册/14-核心链路四-钱包提现.md`

- [ ] Step 1: Create database domain model document.
- [ ] Step 2: Create JWT authentication chain document.
- [ ] Step 3: Create task publish/accept chain document.
- [ ] Step 4: Create milestone settlement chain document.
- [ ] Step 5: Create wallet withdraw chain document.

### Task 5: Create Redis, continuation, troubleshooting, and self-test docs

**Files:**
- Create: `docs/04-从零接手项目手册/15-Redis在项目里到底做了什么.md`
- Create: `docs/04-从零接手项目手册/16-如果我要继续开发应该从哪里下手.md`
- Create: `docs/04-从零接手项目手册/17-常见问题与排坑清单.md`
- Create: `docs/04-从零接手项目手册/18-重新掌握项目自测题.md`

- [ ] Step 1: Create Redis usage document.
- [ ] Step 2: Create continuation development guide.
- [ ] Step 3: Create troubleshooting checklist.
- [ ] Step 4: Create self-test document.

### Task 6: Verify documentation structure

**Files:**
- Verify: `docs/04-从零接手项目手册/**/*.md`

- [ ] Step 1: List all created markdown files.
- [ ] Step 2: Confirm README links point to existing files.
- [ ] Step 3: Confirm no placeholders such as TBD/TODO remain.

---

## Self-Review

Spec coverage: The plan covers all requested content in方案A: README, usage guide, project positioning, directory guide, startup, accounts, business walkthrough, code reading routes, database, core chains, Redis, continuation guide, troubleshooting, self-test.

Placeholder scan: No TBD/TODO placeholders are used.

Scope check: This is documentation-only and focused on a single new docs folder. No code changes or tests are required.
