# 阶段 C 细则：基础架构搭建

本文件是 `ppt-to-fullstack` 阶段 C 的强制执行规范。目标：把参考工程的「框架级」资产复制到新项目，命名改造为新项目，删除全部业务样例代码，得到一个**可编译、可构建、无业务代码**的前后端骨架，供阶段 D 直接写业务代码。

## 0. 模板源（只读）与目标布局

模板源（**只读**，复制后再改）。`SKILL_DIR` = 本 skill 的安装根目录（含 `SKILL.md` 的那一层），骨架随 skill 内置、无需任何外部工程：

| 内容 | 路径 |
|---|---|
| 后端骨架 | `<SKILL_DIR>/internal/scaffold-template/backend` |
| 前端骨架（用户端） | `<SKILL_DIR>/internal/scaffold-template/webfront/User` |
| 前端骨架（管理端） | `<SKILL_DIR>/internal/scaffold-template/webfront/Manager` |
| 交互原型规范参考 | `<SKILL_DIR>/internal/scaffold-template/prototype-reference.html` |

目标布局（`<根>` = 项目根目录）：

```
<根>/
├── ppttomd.md                          # 阶段 B 产物（已存在）
├── <项目名>_需求规格说明书.docx          # 阶段 B 产物（已存在）
├── <db>.sql                            # 阶段 B 产物（已存在）
├── <项目名>_交互原型.html               # 本阶段产出（C5）
├── <snake>_backend/                    # Spring Boot 3 骨架
└── <snake>_webfront/
    ├── <角色端A>/                       # 如 User（非管理角色共用）
    └── <角色端B>/                       # 如 Manager（管理角色）
```

## 1. 参数确定（先读三件套，再动手）

| 参数 | 取值规则 |
|---|---|
| `<snake>` | 英文 snake_case。优先 SQL 文件名去 `.sql`（阶段 B 生成 SQL 时已用 snake_case），否则用户给的英文名，否则项目名拼音 |
| 数据库名 `<db>` | `<snake>` 本身（`application.yml` datasource URL 使用） |
| 包名 | `com.example.<snake>`（如 `com.example.teaching_archive`） |
| `<Pascal>` | `<snake>` 转 PascalCase（如 `teaching_archive → TeachingArchive`） |
| 角色分组 | 从 SRS 权限矩阵读取。默认沿用模板两分组：非管理角色（teacher/dean_chair/dean…）→ 用户端 User；管理角色（admin…）→ 管理端 Manager。角色分组不同时按 SRS 增删前端工程（每个分组一个独立 Vite 工程） |
| 端口 | 后端固定 `8080`（占用则 8081 起找空闲）；前端按工程数从 `5173` 起递增分配并避开占用端口；端口表写入`<根>/<项目名>_项目总结.md` |

## 2. 后端骨架

### 2.1 复制与保留

```powershell
Copy-Item '<SKILL_DIR>\internal\scaffold-template\backend' '<根>\<snake>_backend' -Recurse
```

保留项（框架级，全部保留）：

| 路径（相对后端根） | 说明 |
|---|---|
| `pom.xml` | 依赖完整（SB 3.1 / MP 3.5.5 / Security / jjwt / Redis / POI 5.2.3 / quartz / mail / validation）；仅改 artifactId/name/description |
| `mvnw`、`mvnw.cmd`、`.mvn/wrapper/maven-wrapper.properties` | Maven Wrapper |
| `.gitignore`、`.gitattributes` | |
| `src/main/java/com/example/edu_digization/EduDigizationApplication.java` | 入口类，重命名为 `<Pascal>Application`（文件名同步） |
| `.../common/Result.java` | 统一响应（code/message/data/timestamp + 静态工厂） |
| `.../config/`（SecurityConfig、MyBatisPlusConfig、MyMetaObjectHandler、RedisConfig） | 安全、分页、字段自动填充、Redis 序列化 |
| `.../exception/`（BusinessException、GlobalExceptionHandler） | 业务异常 + 全局兜底 |
| `.../filter/JwtAuthenticationFilter.java` | JWT 过滤器（白名单 `/api/public/`、`/api/forget/`，角色加 `ROLE_` 前缀） |
| `.../utils/JwtTokenProvider.java` | JWT 签发/解析（secret、2h 过期，可配） |
| `.../utils/PasswordEncoderUtil.java` | BCrypt 生成工具，`main` 直接输出哈希（阶段 D 换种子密码用） |
| `.../utils/SecurityUtils.java` | **需简化**（见 2.4） |
| `src/main/resources/application.yml`、`application.properties` | 改造（见 2.5） |

### 2.2 删除（业务样例，阶段 D 会按新 SRS 重建）

```powershell
$dst = '<根>\<snake>_backend'
Remove-Item "$dst\src\main\java\com\example\edu_digization\controller" -Recurse -Force
Remove-Item "$dst\src\main\java\com\example\edu_digization\dto" -Recurse -Force
Remove-Item "$dst\src\main\java\com\example\edu_digization\vo" -Recurse -Force
Remove-Item "$dst\src\main\java\com\example\edu_digization\entity" -Recurse -Force
Remove-Item "$dst\src\main\java\com\example\edu_digization\mapper" -Recurse -Force
Remove-Item "$dst\src\main\java\com\example\edu_digization\service" -Recurse -Force
Remove-Item "$dst\src\main\java\com\example\edu_digization\common\ArchiveStatus.java" -Force
Remove-Item "$dst\src\main\java\com\example\edu_digization\common\ArchiveType.java" -Force
Remove-Item "$dst\src\test" -Recurse -Force
```

删除后按项目重建**空分层目录**（阶段 D 侦察时能直接对上分层规范，放 `.gitkeep`）：

```powershell
New-Item "$dst\src\main\java\com\example\edu_digization\controller" -ItemType Directory
New-Item "$dst\src\main\java\com\example\edu_digization\dto" -ItemType Directory
New-Item "$dst\src\main\java\com\example\edu_digization\vo" -ItemType Directory
New-Item "$dst\src\main\java\com\example\edu_digization\entity" -ItemType Directory
New-Item "$dst\src\main\java\com\example\edu_digization\mapper" -ItemType Directory
New-Item "$dst\src\main\java\com\example\edu_digization\service\impl" -ItemType Directory
New-Item "$dst\src\main\java\com\example\edu_digization\service\infrastructure" -ItemType Directory
```

### 2.3 包名与应用重命名

1. 目录改名：

```powershell
Rename-Item '<根>\<snake>_backend\src\main\java\com\example\edu_digization' '<snake>'
```

2. 批量替换文件内的包名引用（注意避免写入 BOM：PowerShell 5 的 `Set-Content -Encoding UTF8` 会加 BOM，必须用下面的写法）：

```powershell
$old = 'com.example.edu_digization'; $new = 'com.example.<snake>'
Get-ChildItem '<根>\<snake>_backend\src' -Recurse -File -Include *.java,*.yml,*.properties | ForEach-Object {
  $text = [IO.File]::ReadAllText($_.FullName, [Text.Encoding]::UTF8)
  if ($text.Contains($old)) {
    $text = $text.Replace($old, $new)
    [IO.File]::WriteAllText($_.FullName, $text, [Text.UTF8Encoding]::new($false))
  }
}
```

3. 入口类改名（类名 + 文件名同步）：

powershell 命令提示下执行：
```powershell
Rename-Item '<根>\<snake>_backend\src\main\java\com\example\<snake>\EduDigizationApplication.java' '<Pascal>Application.java'
```
然后编辑该文件：类名 `EduDigizationApplication` → `<Pascal>Application`。

4. `pom.xml`：`artifactId`、`<name>`、`<description>` 改为新项目（例如 artifactId=`<snake>`，description=中文项目名）。

### 2.4 SecurityUtils 简化

模板版 `SecurityUtils` 依赖业务实体 `User` 与 `UserMapper`（已被删除），必须简化为纯框架工具：

- 删掉 `getCurrentUser()` 方法与 `User`、`UserMapper`、`@RequiredArgsConstructor` 等全部依赖；
- 只保留静态方法 `getCurrentUsername()`（从 SecurityContext 取 JWT sub，未登录抛 `BusinessException(401, ...)`）；
- 删除 `@Component` 注解（无注入需要）。后续阶段 D 需要"查库取当前用户"时按新 SRS 自行扩展。

### 2.5 配置改造（application.yml / application.properties）

| 键 | 模板值 → 新值 |
|---|---|
| `spring.application.name` | `edu_digization` → `<snake>`（application.properties 与 application.yml 同步改） |
| `spring.datasource.url` | 库名 `teaching_archive` → `<snake>`（其余 `useSSL/serverTimezone/characterEncoding` 参数保留） |
| `spring.datasource.password` | 保留 `root`（本地开发默认，注释注明部署时修改） |
| `file.upload-dir` / `file.image-dir` | 改为 `<根>\uploads\` 与 `<根>\uploads\images\`（需创建目录） |
| `jwt.secret` / `jwt.expiration-ms` | 保留模板值（注释提示生产环境更换 secret 与密钥长度 ≥ 32 字节） |
| `mybatis-plus.type-aliases-package` | 已随 2.3 批量替换，核对为新包名 |
| `spring.mail`、`spring.data.redis`、`spring.cache`、`spring.servlet.multipart` | 整体保留不动（框架级能力） |

### 2.6 后端自检

```powershell
cd '<根>\<snake>_backend'
.\mvnw.cmd -q -DskipTests compile        # 首次运行会下载 Maven；无网络时回退本机 mvn
echo $LASTEXITCODE                        # 必须为 0
```

失败按错误修复（最常见：包名替换遗漏、SecurityUtils 残留引用）。骨架必须编译通过才允许进入阶段 D。

## 3. 前端骨架（每个角色端工程）

以模板 `User`（或 `Manager`）为源复制：

```powershell
Copy-Item '<SKILL_DIR>\internal\scaffold-template\webfront\User' '<根>\<snake>_webfront\<角色端A>' -Recurse
```

### 3.1 保留

| 路径（相对工程根） | 说明 |
|---|---|
| `package.json`、`package-lock.json` | 改 `name` 字段为 `<snake>-<端>`；依赖（vue3/pinia/router/axios + vite）齐备 |
| `vite.config.js` | 改 `server.port` 为分配的端口；`/api` 代理目标 `http://localhost:8080` 保留 |
| `index.html` | 改 `<title>` 与根元素文本 |
| `jsconfig.json`、`.gitignore`、`.vscode/extensions.json`、`public/favicon.ico`、`README.md` | 保留不动 |
| `src/main.js` | 保留（createApp + pinia + router + 引入 main.css） |
| `src/App.vue` | 保留（仅 `<router-view/>` + 基础样式） |
| `src/api.js` | 保留（统一 axios：baseURL=`/api`、请求带 JWT、响应解包 Result） |
| `src/stores/user.js` | 保留（token/userInfo + localStorage 持久化） |
| `src/router/index.js` | **替换为最小占位**（见 3.3） |
| `src/assets/main.css` | **替换为最小占位**（见 3.3） |

### 3.2 删除（业务样例）

```powershell
$fe = '<根>\<snake>_webfront\<角色端A>'
Remove-Item "$fe\src\api" -Recurse -Force          # auth.js / dict.js / archive.js / review.js / teacher.js / course.js
Remove-Item "$fe\src\components" -Recurse -Force   # 各业务弹窗
Remove-Item "$fe\src\layout" -Recurse -Force       # MainLayout.vue
Remove-Item "$fe\src\views" -Recurse -Force        # 全部页面
Remove-Item "$fe\src\utils" -Recurse -Force        # preview.js / archive-labels.js
Remove-Item "$fe\src\stores\counter.js" -Force     # create-vite 演示 store
Remove-Item "$fe\dist" -Recurse -Force -ErrorAction SilentlyContinue   # 构建产物，绝不复制
```

### 3.3 占位文件（保证骨架能 `npm run build`）

`src/router/index.js` 最小占位（不含任何组件引用，避免引用已删除页面；阶段 D 会按 SRS/原型整体重写）：

```js
import { createRouter, createWebHistory } from 'vue-router'

// 占位路由：实际页面由全栈开发阶段按 SRS 与交互原型生成后重写本文件
const routes = []

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
```

`src/assets/main.css` 最小占位（阶段 D 会从 `<项目名>_交互原型.html` 提取主题变量与布局 class 覆盖本文件）：

```css
html, body, #app { height: 100%; margin: 0; }
:root { /* 主题变量由全栈开发阶段从交互原型提取 */ }
```

### 3.4 前端自检

```powershell
cd '<根>\<snake>_webfront\<角色端A>'
npm install
npm run build        # 输出 ✓ built 为通过
```

多个角色端工程逐一执行。本机无 Node ≥ 22 或断网时，记录缺口，由阶段 D 统一构建验证（不阻塞阶段切换）。

## 4. 交互原型 HTML 生成规范（C5）

产出 `<根>/<项目名>_交互原型.html`，单文件、无 JS、可双击直接浏览，规范参考 `<SKILL_DIR>/internal/scaffold-template/prototype-reference.html`：

- 布局骨架直接沿用其 CSS 体系：`:root` 主题变量（`--brand` 等）、`.topbar/.side/.main`、`.btn-*`、`.tag-*`、`table.tb`、`.card`、`.modal-mask/.modal`、`.slot`、登录 `.login-wrap/.login-card`；
- 用 `:target` + `.view` 做无 JS 页面切换；`#login` 默认显示，登录卡片放各角色直达入口；右上/侧边导航模拟角色化菜单；
- **页面覆盖 = SRS 功能需求清单**：每角色 × 每功能一页；每个页面列出真实表格列、筛选项、按钮、弹窗字段、状态标签（与 SRS 用例表、SQL 字段一一对应）；
- 关键业务交互（上传槽位、审核驳回、状态流转）用原型注 `pt-note` 标注规则；
- 无真实数据：表格用 2~3 行示例数据占位；
- 用途（下游两项）：① 阶段 D 前端公共样式取值来源；② 答辩 PPT 的页面截图素材（浏览器打开即可截图）。

## 5. 环境与排坑（Windows / PowerShell）

- JDK：项目要求 Java 17（pom 内 `<java.version>17</java.version>`）；PATH 上的 `java` 可能是 11，编译用 `$env:JAVA_HOME\bin\java.exe` 或让 mvnw 使用正确 JAVA_HOME。
- `mvnw.cmd` 首次运行需联网下载 Maven；断网时回退本机 `mvn`（先 `mvn -v` 确认版本）。
- PowerShell 5 写文件默认 `UTF8` 带 BOM：批量改写 `.java` 文件必须用 2.3 中 `[IO.File]::WriteAllText(... UTF8Encoding($false))` 写法，避免 BOM 引起编译问题。
- 中文路径下读取文件用 `Get-Content -Encoding UTF8`；Maven classpath 临时文件含中文路径时读取必须指定 UTF8（否则 `NoClassDefFoundError`）。
- Vite 8 / Node 需求较新（`package.json engines` node ≥ 22）：`npm install` 失败先看 Node 版本，提示用户或改用项目要求版本。
- 端口：`vite.config.js` 指定端口被占用时启动会自增；但为了总结文档的确定性，自检时先 `Get-NetTCPConnection -LocalPort <port>` 确认空闲。

## 禁止事项

- 修改模板源 `<SKILL_DIR>/internal/scaffold-template` 的任何文件。
- 保留任何业务样例代码（遗留 demo 实体/页面/路由会在阶段 D 造成死代码或编译冲突）。
- 复制 `node_modules`、`dist`、`.idea`、`.vscode`（模板自身除外）。
- 用会写 BOM 的 `Set-Content -Encoding UTF8` 改写 `.java`。
- 在骨架未通过自检前进入阶段 D。