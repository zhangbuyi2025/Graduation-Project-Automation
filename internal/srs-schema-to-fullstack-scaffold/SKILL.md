---
name: srs-schema-to-fullstack-scaffold
description: Given a software requirement specification (SRS), an interactive HTML prototype, an existing database *.sql schema, and empty Spring Boot + Vue3 project skeletons, fill in the backend (entities, Mappers, Services, Controllers) and the frontend (Vue3 pages, stores, API modules, routes), then verify by Maven compile and Vite build. Use when the user provides SRS/prototype/SQL plus an already-created full-stack framework and asks to develop the basic/foundational code. Do not use when there is no existing framework (build from scratch) or when only documentation is wanted.
---

# SRS + 原型 + SQL → 全栈代码骨架

本 Skill 在**已存在的前后端工程框架**上，依据需求规格说明书（SRS）、交互原型 HTML 和数据库 SQL，补齐可编译、可运行的后端基础代码（实体 / Mapper / Service / Controller）与前端 Vue3 界面，并完成编译构建验证。

```
SRS + 交互原型HTML + 数据库SQL + 前后端骨架
        → 后端实体/Mapper/Service/Controller
        → 前端 Vue3 页面/Store/API/路由
        → Maven 编译 + Vite 构建验证
```

## 触发条件

- 用户已提供 SRS（docx）、交互原型（html）、数据库 `*.sql`，且目录下有已创建的 Spring Boot 与 Vue3 工程骨架
- 用户要求"按需求进行项目基础开发 / 补全实体类、Controller、Mapper、Service / 照着原型做前端界面"
- 硬性约束：**不得更改现有框架级文件夹名称**

## 铁律（先读再动手）

1. **保留一切框架级命名**：包名（可能含历史拼写，如 `edu_digization`）、分层目录、前端工程名、端口一律不改
2. **先遍历后开发**：不读全 SRS / 原型 / SQL 前不写业务代码
3. **以 SRS 为权威**：当 SQL 视图、字段注释与 SRS 状态机 / 枚举冲突时，以 SRS 为准，不沿用 SQL 笔误
4. **覆盖已存在文件前必须先 Read**；Windows 环境命令用 PowerShell（`Get-ChildItem`/`Get-Content`），不使用 Bash

---

## 阶段 ⓪：侦察（遍历框架与输入）

### 0.1 遍历目录结构

- 根目录：定位 SRS `.docx`、交互原型 `.html`、`*.sql`，以及包含前后端的工程目录（如 `edu_digitization/`）
- 后端：记录包名、分层目录（`common/config/controller/dto/entity/exception/filter/mapper/service/impl/infrastructure/task/utils/vo`）、`pom.xml` 已有依赖、`application.yml` 配置
- 前端：记录每个独立工程（如 Manager / User）、端口、`package.json` 依赖、`src` 现有占位文件

### 0.2 读 SRS（docx → 纯文本）

优先 `markitdown`，不可用则解压 docx 读 `word/document.xml`：
```bash
python -m markitdown "需求规格说明书.docx" > req.txt
```
通读并提取：
- **角色清单**与权限矩阵
- **按角色的功能模块**（登录 / 提交 / 各级审核 / 归档 / 基础数据 CRUD）
- **状态机流转表**与枚举取值
- **业务规则**：PDF-only、课前固定槽位数、课后 1..N、文件大小上限、驳回重提、一键驳回、跨学期只读、Excel 导出
- **接口清单**：REST 路径、方法、参数、返回 VO

### 0.3 读交互原型 HTML

- 逐 section 对应到页面：表格列、按钮、标签、弹窗、槽位
- 提取 `<style>` 中的主题变量与公共 class，作为前端公共样式的来源

### 0.4 读 SQL

- 逐表记录表名、字段、类型、注释、外键、CHECK 约束、视图、初始数据
- **标记视图 / 注释与 SRS 不一致处**（如视图 WHERE 用了错误的状态枚举），后端不沿用

### 0.5 锁定权威状态机（示例）

以 SRS 状态流转表 + 枚举 + 接口注释为准：
```
PENDING(待初审) --初审通过--> FIRST_REVIEW(待中间审核)
       --中间审核通过--> MIDDLE_REVIEW(待终审)
       --终审通过--> ARCHIVED(已归档)
任意环节驳回 --> REJECTED；教师重提 --> PENDING
```
各待审列表：主任查 PENDING、教务处查 FIRST_REVIEW、院长查 MIDDLE_REVIEW。

---

## 阶段 ①：后端基础开发

遵循后端分层规范，详见 `references/backend-standards.md`。推荐顺序：

1. **常量枚举**（`common/`）：状态、类型等字符串常量类（如 `ArchiveStatus`、`ArchiveType`）
2. **实体类**（`entity/`）：每张表一个
   - `@TableName` / `@TableId(type=AUTO)` / `@TableField`
   - `created_at/updated_at` 用 `@TableField(fill=...)` + 自定义 `MetaObjectHandler` 自动填充
   - 逻辑删除字段用 `@TableLogic`
3. **Mapper**（`mapper/`）：`extends BaseMapper<Entity>`；关联查询用 `@Select` + `<script>` 动态条件，开启下划线转驼峰
4. **DTO / VO**（`dto/`、`vo/`）：请求参数与响应视图分离
5. **Service**（`service/` + `service/impl/`）：`IXxxService extends IService<Entity>`，`XxxServiceImpl extends ServiceImpl<XxxMapper, Entity>`
6. **基础设施服务**（`service/infrastructure/`）：
   - 文件存储：PDF 三重校验（扩展名、大小上限、文件头魔数 `%PDF-`）+ SHA-256 + UUID 落盘
   - Excel 导出：Apache POI 生成 `.xlsx`（表头样式、自适应列宽、文件名 UTF-8 编码）
7. **异常与工具**（`exception/`、`utils/`）：`BusinessException` + `@RestControllerAdvice` 全局处理；`SecurityUtils` 取当前用户
8. **Controller**（`controller/`）：`@RestController`，统一返回 `Result`；路径与 SRS 接口契约一致
9. **安全权限**：
   - JWT 过滤器中角色 `toUpperCase()` 后加 `ROLE_` 前缀（`dean_chair → ROLE_DEAN_CHAIR`）
   - 登录即可访问的路径放行，方法级权限用 `@PreAuthorize`（初审 `hasRole('DEAN_CHAIR')`、终审 `hasRole('DEAN')`、教务处 `hasRole('ADMIN')`）

### 依赖补齐（编译报错对照）

- Spring Boot 3 用参数校验必须显式引入：
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  ```
- Excel 导出引入 `poi` 与 `poi-ooxml`（5.2.x）

---

## 阶段 ②：前端 Vue3 开发

遵循前端规范，详见 `references/frontend-standards.md`。每个工程的搭建顺序：

1. **公共样式**（`src/assets/main.css`）：从原型 `<style>` 提取主题变量、布局（topbar/side/main）、按钮、表格、标签、槽位、弹窗、登录页；`main.js` 中 `import './assets/main.css'`
2. **用户 Store**（`src/stores/user.js`）：管理 `token`、`userInfo`，`localStorage` 持久化
3. **API 模块**（`src/api/`）：按域拆分（`auth/dict/archive/review`，管理端另加 `teacher/course`），从已有 axios 实例（`../api.js`）引入
4. **登录页**：成功后存登录态，按角色跳转；非本端角色给出提示并登出（如 User 端拒绝 admin、Manager 端拒绝非 admin）
5. **主布局**（`src/layout/MainLayout.vue`）：顶部栏 + **角色化侧边菜单** + `router-view`
6. **可复用组件**：
   - 上传弹窗（课前固定槽位 / 课后动态增删，选文件→本地预览→提交）
   - 档案详情弹窗（信息 + 附件 + 审核记录）
   - 审核弹窗（逐槽位预览、勾选驳回、一键驳回、通过，驳回必填意见）
7. **业务页面**：按 SRS 功能模块逐一落地
8. **路由 + 守卫**（`src/router/index.js`）：登录页独立，其余套用主布局；`beforeEach` 校验无 token→登录页，按 `meta.roles` 放行

### 附件预览（下载端点需认证）

不能直接 `window.open`（不带 JWT），用 axios 取 blob 再打开：
```js
const blob = await request.get(`/work/attachment/${id}/download`, {
  responseType: 'blob',
  params: { inline: true },
})
window.open(URL.createObjectURL(blob), '_blank')
```

### 接口返回约定

- 响应拦截器通常返回**完整 Result 对象**，页面用 `res.data` 取业务载荷（不要误取成 `res.data.data`，以拦截器实际返回为准）

---

## 阶段 ③：编译与构建验证

### 后端

```powershell
cd <backend>
.\mvnw.cmd -q -DskipTests compile
```
以 `EXIT=0` 为准；有报错按错误信息定位（缺包、拼写、类型不匹配）。

### 前端

```powershell
cd <frontend-project>
npm install        # 补齐 package.json 已声明但未实际安装的依赖
npm run build
```
以 `✓ built` 为准。

### 种子密码修复（若密码为占位符）

- SQL 若用 BCrypt 占位符，会导致无法登录，且占位符长度若不足 60 还会违反 CHECK 约束
- 用后端工具生成真实哈希（统一初始密码，如 `123456`）：
  - 直接用项目里的 `BCryptPasswordEncoder` 工具类 `main` 方法运行
  - 或临时跑：`java -cp "target/classes;<classpath>" <PasswordEncoderUtil>`
- 替换全部占位符，哈希必须为 60 字符、以 `$2a$` 开头

### 环境排坑（Windows / PowerShell）

- **JDK 版本不一致**：PATH 上的 `java` 可能是 11，而项目用 17 编译；用 `$env:JAVA_HOME\bin\java.exe`（class file 61）
- **PowerShell 参数解析**：`-Dmdep.outputFile=cp.txt` 需加引号 `"-Dmdep.outputFile=cp.txt"`
- **中文路径乱码**：`Get-Content cp.txt -Encoding UTF8`，否则 Maven classpath 中 `~/.m2` 路径（含中文用户名）会变乱码导致 `NoClassDefFoundError`
- **Maven classpath 临时文件**（如 `cp.txt`）验证后清理

---

## 推荐执行顺序（完整清单）

```
⓪ 遍历根目录 → 读 SRS/原型/SQL → 锁定权威状态机
① 后端：枚举 → 实体 → Mapper → DTO/VO → Service → 基础设施 → 异常/工具 → Controller → 安全权限
② 前端（每个工程）：公共样式 → main.js → Store → API 模块 → 登录页 → 主布局 → 可复用组件 → 业务页面 → 路由守卫
③ 后端 mvnw compile；前端 npm install → build
④ 修复种子密码占位符（生成真实 BCrypt 哈希）
⑤ 清理临时文件，回读验证，向用户交付：新增/修改文件清单 + 验证结果 + 启动顺序
```

## 交付说明模板

```
项目基础开发已完成，后端编译、前端各工程构建均通过。

后端（包名/目录未改）：
- 实体 N 个、Mapper N 个、Service N 套、Controller N 个
- 状态机严格按 SRS：...
- 基础设施：文件存储（三重校验）、Excel 导出（POI）

前端（工程/端口未改）：
- <工程A>：登录 + 角色化菜单 + 页面清单
- <工程B>：...

验证：后端 mvnw compile EXIT=0；前端 vite build ✓
种子账号：初始密码统一为 123456（账号 ...）
启动顺序：建库导入 SQL → MySQL/Redis/上传目录 → 后端 → 各前端 npm run dev
```

## 禁止事项

- 不得更改任何框架级文件夹 / 包名 / 工程名 / 端口（即使包名有拼写）
- 不得在未读全 SRS 前写业务代码
- 不得沿用 SQL 视图 / 注释中与 SRS 状态机冲突的笔误
- 不得在密码字段存明文；不得用长度不足 60 的哈希
- 不得在覆盖已存在文件前不 Read
- 不得用 `window.open` 直接打开需认证的附件
- 不得在验证失败时宣称完成；临时 classpath 等文件验证后清理
