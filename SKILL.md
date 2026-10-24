---
name: lang-to-ppt-to-fullstack-ppt
description: One-stop pipeline from a requirement PPT or a plain requirement description to a complete full-stack project, covering PPT to SRS docs and SQL, scaffolding the Spring Boot and Vue3 architecture, filling in all foundational backend and frontend code, writing a project summary Markdown, and generating a defense PPT. Use when the user provides a pptx or a requirement description and asks for the whole project flow, including requirements docs, database design, project scaffolding, full-stack development, summary, or defense presentation. Do not use when only a single standalone artifact is wanted, or the full-stack code already exists.
---

# PPT / 需求描述 → 全栈项目全流程（自包含 · 仅 Windows）

> **平台支持：仅 Windows（PowerShell + JDK 17 + Node 22）。不支持、也不计划适配 macOS / Linux。**

本 Skill **基本自包含**：三段子流程手册、前后端骨架模板、PPT 生成脚本、SRS 生成器均在本 skill 目录内，**Node 依赖（docx-js）随包内置**于 `internal/deps/node_modules`；仅 6 个 Python 依赖未随包，首次运行时由 bootstrap 自动联网安装（见第 2 节）。运行时**不读取、不调用任何外部 skill 目录**（不再依赖 `E:\代码\实训\.trae\skills` 之类的路径）。

```
输入（PPT 或 需求描述）
  ├─ 有 PPT ──────────────► 阶段 B
  └─ 只有需求描述 ─► 阶段 A（内置 ppt-master 生成需求 PPT）─► 阶段 B
阶段 B 产物：ppttomd.md + <项目名>_需求规格说明书.docx + <db>.sql
  ▼
阶段 C  基础架构搭建（复制内置骨架 + 改名 + 删样例 + 交互原型 + 自检）
  ▼
阶段 D  全栈基础代码补全（内置 srs-schema-to-fullstack-scaffold 手册）
  ▼
阶段 E  项目根目录生成 <项目名>_项目总结.md
  ▼
阶段 F  答辩 PPT（内置 ppt-master 生成）
  ▼
交付说明
```

## 0. 目录结构

`<SKILL_DIR>` = 本 skill 的安装根目录（即本 `SKILL.md` 所在目录）。

```
<SKILL_DIR>/
├── SKILL.md                              # 本文件：总编排
├── requirements.txt                      # Python 依赖总入口
├── references/
│   ├── architecture-template.md          # 阶段 C 细则（复制/改名/删除/自检/排坑）
│   └── delivery-templates.md             # 阶段 E 总结模板 + 阶段 F 答辩大纲
└── internal/
    ├── ppt-master/                       # PPT 生成引擎（SKILL.md + workflows/ + scripts/ + templates/ + references/）
    ├── ppt-to-srs-schema/                # 阶段 B 手册（PPT→ppttomd.md→SRS docx→SQL）
    ├── srs-schema-to-fullstack-scaffold/ # 阶段 D 手册（SRS+SQL+原型→全栈代码）
    ├── scaffold-template/                # 阶段 C 内置骨架模板（只读）
    │   ├── backend/                      #   Spring Boot 3 框架级骨架
    │   ├── webfront/User/                #   非管理端 Vue3 工程骨架
    │   ├── webfront/Manager/             #   管理端 Vue3 工程骨架
    │   └── prototype-reference.html      #   交互原型样式规范参考
    ├── scripts/gen-srs.js                # SRS DOCX 生成器（内容 JSON → docx）
    └── deps/                             # Node 依赖（docx-js），安装在此
```

## 1. 路径纪律（先看这一节）

1. 所有内部手册、脚本、模板一律用 `<SKILL_DIR>/...` 绝对路径引用；**禁止** `cd` 到 skill 目录后使用相对路径。
2. `internal/ppt-master/SKILL.md` 中出现的 `${SKILL_DIR}` 指 **`<SKILL_DIR>/internal/ppt-master`**（它自己的 skill 根）；手册里凡是 `skills/ppt-master/xxx` 的前缀，执行时替换为 `<SKILL_DIR>/internal/ppt-master/xxx`。
3. 用户项目路径与 skill 路径严格分离：产物只写用户项目根目录，skill 目录只读（唯一例外是 `internal/deps` 的 `npm install` 与修复缺陷）。
4. PowerShell 路径含中文时用双引号包裹，命令里不要拼接 `&&`/`||`。
5. 内部手册里的 `python3` 在 Windows 上要改写为 `python`（本机没有 `python3` 命令）；`bash` 代码块一律按 PowerShell 等价写法执行。手册中 `python3 "<SKILL_DIR>/scripts/attribution_guard.py"` 这类门禁标记只要求**命令语义**一致，不要求字面保留 `python3`。
6. **平台仅支持 Windows**：所有命令、路径、脚本均按 Windows / PowerShell 环境编写并验证；不支持 macOS / Linux，也不为其他平台做兼容处理。

## 2. 环境自举（每个会话第一次执行前做一次，已就绪则跳过）

一条命令搞定：**缺什么装什么**，只装流水线真正用到的依赖（`requirements.txt` = 最小集），官方源失败自动切清华 / 阿里云镜像，npm 失败自动切 npmmirror。

```powershell
python "<SKILL_DIR>\internal\scripts\bootstrap.py"          # 检测 + 缺则自动安装
python "<SKILL_DIR>\internal\scripts\bootstrap.py" --check   # 只检测，不动环境
python "<SKILL_DIR>\internal\scripts\bootstrap.py" --mirror  # 直接走国内镜像（源站不通时用）
```

判定门槛：**末行输出 `BOOTSTRAP: OK` 且 exit 0** 才能进入后续阶段。
`BOOTSTRAP: BLOCKED`（exit 78）时，脚本会打印手动修复命令；把缺的项和受影响阶段如实告知用户，不要带病往下跑。

安装内容（最小集，见 `<SKILL_DIR>\requirements.txt`）：
`python-pptx`、`Pillow`、`flask`、`PyYAML`、`markitdown`、`python-docx`；Node 侧只装 `internal/deps` 里的 `docx`（SRS DOCX 生成器用）。

按需手动安装（脚本不预装，出现具体需求时再装，同样可加 `-i https://pypi.tuna.tsinghua.edu.cn/simple`）：
`pymupdf`（用户给的是 PDF）、`skia-pathops` / `uharfbuzz`（SVG 布尔/文字轮廓）、`edge-tts`（旁白音频）、`google-genai`（AI 生图）、`openpyxl` / `XlsxWriter`（Excel 型 deck）。

其余自检项（与依赖自动安装无关，仍需确认）：

| 检查 | 命令 | 期望 |
|---|---|---|
| ppt-master 完整性 | `python "<SKILL_DIR>\internal\ppt-master\scripts\attribution_guard.py"` | exit 0 |
| JDK / Maven | `$env:JAVA_HOME`、`<骨架>\mvnw.cmd -v` | JDK 17 |
| Node | `node -v` | ≥ 22（骨架 engines 要求） |

依赖缺失且无法联网时：明确告知用户缺哪一项、影响哪个阶段，并只跳过受影响的产物，不得谎报完成。

## 3. 阶段执行规则（读内部手册，不调外部 Skill）

| 阶段 | 执行依据 | 输入 | 产出 |
|---|---|---|---|
| A 需求描述 → 需求 PPT | `<SKILL_DIR>/internal/ppt-master/SKILL.md` | 用户需求描述 | `<项目名>需求说明.pptx` |
| B PPT → 三件套 | `<SKILL_DIR>/internal/ppt-to-srs-schema/SKILL.md` | `.pptx` | `ppttomd.md`、`*_需求规格说明书.docx`、`*.sql` |
| C 基础架构搭建 | 本文件 + [architecture-template.md](references/architecture-template.md) | 三件套 + 内置骨架 | 后端骨架 + 前端骨架 + 交互原型 html |
| D 骨架 → 全栈代码 | `<SKILL_DIR>/internal/srs-schema-to-fullstack-scaffold/SKILL.md` | SRS + 原型 + SQL + 骨架 | 可编译全栈代码 |
| E 项目总结 | 本文件 + [delivery-templates.md](references/delivery-templates.md) | 全部 | `<项目名>_项目总结.md` |
| F 答辩 PPT | `<SKILL_DIR>/internal/ppt-master/SKILL.md` | 总结 + SRS + SQL + 代码 | `<项目名>_答辩PPT.pptx` |

执行规则：

1. **进入某阶段前，必须先用 Read 读完该阶段对应手册全文**，再按其流程逐步执行；不得凭记忆跳步。
2. 遵守手册纪律，尤其 ppt-master 的：Mandatory Load Order（`SKILL.md` → `attribution_guard.py` → `workflows/routing.md` → 选且只选一个 route）、不跨 phase 合并、`⛔ BLOCKING` 处停下等用户确认。
3. 本文件只负责阶段间数据交接，不替代各手册内部流程。
4. 可重入：若项目根目录已有完整三件套（`ppttomd.md` + `*.docx` + `*.sql`），从阶段 C 开始；若已有骨架，从阶段 D 开始。

## 前置确认（每个会话开始）

1. 项目根目录：产物全部输出到根目录。优先用用户指定路径；用户既没有 PPT 也没有指定目录时，询问一次项目根目录。
2. 项目名：优先用户给定；否则从 PPT 标题 / SQL 文件名（snake_case）/ 需求描述提取，确定「中文显示名」与「英文 snake 名」两个值，供包名、库名、工程名使用。确定后全流程保持一致，并写入阶段 E 总结。

---

## 阶段 A：需求描述 → 需求 PPT（仅当用户没有 PPT）

输入不是规范需求文档（几句话、一段话、要点清单）时，先物化为 PPT 再回归正常流水线：

1. 读取 `<SKILL_DIR>/internal/ppt-master/SKILL.md`，按其 routing 选择 `Generate PPTX — ordinary Default`，完整执行其流程（含 attribution_guard 与各 gate）。
2. 生成《`<项目名>需求说明.pptx`》到项目根目录。这份 PPT 是阶段 B 的唯一信息源，**内容必须完整**，覆盖：
   - 项目概述页（背景、目标、使用者）
   - 角色清单页（每个角色及其职责）
   - 按角色/模块组织的功能页（每个功能：入口、操作步骤、输入输出、约束）
   - 业务规则与状态流转页（状态机、流转条件、驳回/重提规则、只读/导出等硬性规则）
   - 非功能要求页（性能、安全、兼容性）
   - 整体流程页（端到端业务流程）
3. 完成后进入阶段 B，把这份 PPT 当作普通输入。

**PPT 产物落盘规则（阶段 A/F 通用）**：

- ppt-master 的工作区（`svg_output/`、`svg_final/`、`exports/` 等）建在**用户项目根目录下**，如 `<根>/需求PPT_workspace/`；**不要**建到 `<SKILL_DIR>/projects`（那会污染 skill 安装目录）。
- ppt-master 自身只把成品写到 `<工作区>/exports/<name>_<timestamp>.pptx`。交付前必须显式落到根目录：导出时用 `-o "<根>/<项目名>需求说明.pptx"`，或导出后把 `exports/` 里的最终 pptx 复制/重命名为该路径。
- 交付前用 `Test-Path` 确认根目录那份 pp tx 存在且大小 > 0，再进入下一阶段。

## 阶段 B：PPT → SRS 三件套

1. 读取 `<SKILL_DIR>/internal/ppt-to-srs-schema/SKILL.md`，按其三个阶段执行：
   - ① PPT → `ppttomd.md`（按角色分章、按功能分节、规则全保留）
   - ② Markdown → `<项目名>_需求规格说明书.docx`（8 章 + 用例表 + 图片占位）
   - ③ SRS → `<database_name>.sql`（MySQL 8.0，10 类表 + 视图 + 初始化 + 校验 SQL）
2. DOCX 一律用内置生成器产出，不在用户项目里安装 npm 依赖：

   ```powershell
   python "<SKILL_DIR>\internal\scripts\bootstrap.py"                          # 首次：缺什么装什么
   node "<SKILL_DIR>\internal\scripts\gen-srs.js" "<内容.json>" "<根>\<项目名>_需求规格说明书.docx"
   ```

   内容 JSON 规格见 `internal/ppt-to-srs-schema/SKILL.md` 的「技术栈」节。
3. 三件套输出到项目根目录；执行其自检与验证脚本直到全部通过（`python-docx` 回读、SQL 校验全 ✓）。

## 阶段 C：基础架构搭建（本 Skill 核心）

从**内置模板** `<SKILL_DIR>/internal/scaffold-template/` 复制 Spring Boot + Vue3 骨架到项目根目录，做命名改造、删除业务样例、生成交互原型 HTML，并自检可编译可构建。

**模板源只读铁律：`internal/scaffold-template` 只能读取，绝不修改。**

详细清单、重命名规则、验证命令与排坑见 [references/architecture-template.md](references/architecture-template.md)，此处只列执行顺序：

1. 读取三件套，确定：数据库名 `db`、snake 项目名、角色分组、后端包名 `com.example.<snake>`、前端端口分配。
2. `backend` → `<根>/<snake>_backend/`，删除业务样例（controller/dto/vo/entity/mapper/service、示例枚举、test），保留框架级文件。
3. 包名与应用重命名：`com/example/edu_digization` → 新包名；`EduDigizationApplication` → `<Pascal>Application`；`pom.xml` / `application.yml` / `application.properties` 同步改名；`SecurityUtils` 简化。
4. `webfront/User`、`webfront/Manager` → `<根>/<snake>_webfront/<角色端>/`；删除业务 `api/components/layout/views/utils` 与 `dist`，保留最小可构建文件；`package.json` name、`vite.config.js` 端口、`index.html` title 按工程改名。
5. 生成 `<根>/<项目名>_交互原型.html`（单文件、无 JS、`:target` 路由，角色 × 页面全覆盖，规范见 [architecture-template.md](references/architecture-template.md) 第 4 节，样式参考 `internal/scaffold-template/prototype-reference.html`）。
6. 骨架自检：后端 `mvnw.cmd compile`、前端 `npm install && npm run build`；环境缺失（无 JDK 17 / Node 22 / 网络）则记录缺口交由阶段 D 统一验证。自检不通过必须修到通过再进阶段 D。

## 阶段 D：骨架 → 全栈基础代码

1. 读取 `<SKILL_DIR>/internal/srs-schema-to-fullstack-scaffold/SKILL.md`，按其阶段⓪ 侦察 → 阶段① 后端 → 阶段② 前端 → 阶段③ 编译构建验证完整执行。
2. 交接清单（全部已就绪）：
   - `<项目名>_需求规格说明书.docx`（手册会以 `markitdown` 转纯文本读取）
   - `<项目名>_交互原型.html`（阶段 C 产物，作为页面规格与主题样式来源）
   - `<database_name>.sql`（与 SRS 同源）
   - 前后端骨架（框架级命名已在阶段 C 定死，本阶段不得再改）
3. 种子密码：骨架 `utils/PasswordEncoderUtil.java` 的 `main` 可直接生成 BCrypt 哈希，替换 SQL 占位符。
4. 阶段结束必须给出真实构建输出（后端 `compile` EXIT=0、各前端 `npm run build` 成功），不得只写结论。

## 阶段 E：项目根目录生成总结 Markdown

按 [references/delivery-templates.md](references/delivery-templates.md) 的模板，在**项目根目录**生成 `<项目名>_项目总结.md`，必须包含：

- 需求来源（PPT 路径或需求描述原文 + 生成的 PPT 路径）
- 全部交付产物清单（文件 → 路径 → 说明）
- 需求规格摘要（角色、核心功能、状态机、关键业务规则）
- 数据库设计摘要（表清单、核心表字段与约束、视图）
- 系统架构（技术栈、目录结构、包名、端口）
- 开发内容统计（后端类清单、前端页面清单）
- 验证结果（`mvnw compile` EXIT、`npm run build` 结果、种子账号与统一初始密码）
- 启动与部署步骤（建库导入 SQL → MySQL/Redis/上传目录 → 后端 → 各前端 dev）
- 答辩 PPT 位置

## 阶段 F：生成答辩 PPT

1. 素材组织：阶段 E 的总结 md 为骨架，辅以 SRS（状态机、用例）、SQL（表设计）、交互原型（页面规格）、代码实现（关键类、验证输出）。可截图内容（原型 html 浏览器打开、dev server 页面）一并放进素材。
2. 读取 `<SKILL_DIR>/internal/ppt-master/SKILL.md`，按 routing 选择 `Generate PPTX — ordinary Default`，完整执行其流程。答辩大纲模板见 [references/delivery-templates.md](references/delivery-templates.md) 第 2 节，可按实际内容增删。
3. 生成 `<项目名>_答辩PPT.pptx` 到项目根目录（落盘方式同上：工作区放在项目根下，导出时用 `-o "<根>/<项目名>_答辩PPT.pptx"`，或从 `exports/` 复制改名）。

## 排坑：浏览器幻灯片预览「画布为空 / 缺少根 <svg>」

**现象**：`svg_editor` 预览页报「幻灯片已加载但画布为空，SVG 可能损坏或缺少根 `<svg>` 元素」，而磁盘上的 SVG 文件本身完好。

**根因**：`scripts/svg_editor/server.py` 的 `/api/slide/<name>` 用 `ElementTree` 重序列化 SVG。`ET.register_namespace` 是**进程全局**的，`svg_to_pptx/pptx_ooxml/ooxml.py`、`pptx_package/builder.py` 等模块在导入时用 `register_namespace("", <其它命名空间>)` 覆盖掉 SVG 的默认命名空间映射，导致输出根元素变成 `<ns0:svg xmlns:ns0="http://www.w3.org/2000/svg">`。前端 `schema` 解析仍通过（namespaceURI 未变），但 `innerHTML` 走 HTML 解析器，`ns0:svg` 不被识别为 SVG → `querySelector("svg")` 为 null → 触发空画布守卫。

**修复（已内置到本 skill 副本）**：`server.py` 中新增 `_serialize_svg(root)`，在**每次** `ET.tostring` 之前重新注册 `''→http://www.w3.org/2000/svg`、`xlink→http://www.w3.org/1999/xlink`，`/api/slide` 一律走该函数。**不要把注册放到模块顶部**（会被后续 import 覆盖）。

**自检/排查顺序**：

1. 起预览：`python "<SKILL_DIR>\internal\ppt-master\scripts\svg_editor\server.py" "<项目根>" --live --no-browser`
2. 断言响应：`Invoke-WebRequest "<url>/api/slide/01_cover.svg"` 的首行必须是 `<svg xmlns="http://www.w3.org/2000/svg"`，出现 `ns0:` 即为回归。
3. 检查每页根元素下除 `defs/style/title/desc` 外是否有可见子元素（空页也会触发同一提示）。
4. 端口占用导致连到旧进程时，先按 `<项目根>/live_preview/lock.json` 的 pid 结束旧进程再重启。
5. 预览不可用时兜底：直接打开 `svg_final/`（或 `svg_output/`）下的 SVG 静态查看，并说明预览服务未启动，不阻塞导出。

**另一个高频误报**：在 `svg_output/` 目录内运行质检脚本会全部报 `Project-local icon not found`。必须在**项目根**运行：

```powershell
python "<SKILL_DIR>\internal\ppt-master\scripts\svg_quality_checker.py" svg_output --stage final --json
```

## 交付说明

流水线结束时，按顺序向用户给出：

1. 产物清单：三件套 + 交互原型 + 骨架 + 全栈代码 + `<项目名>_项目总结.md` + `<项目名>_答辩PPT.pptx`（逐项标注所在路径）。
2. 验证结果：后端编译、前端各工程构建、种子账号与初始密码。
3. 启动顺序：建库导入 SQL → MySQL/Redis/上传目录 → 后端 → 各前端 `npm run dev`。
4. 提示：Word 目录打开后需 F9 刷新；SQL 密码若仍为占位符需用 `PasswordEncoderUtil` 生成真实哈希；答辩 PPT 与需求 PPT 的区别说明（如需）。

## 禁止事项

- 不得调用、读取或依赖任何外部 skill 目录（`E:\代码\实训\.trae\skills` 等）；一切走 `<SKILL_DIR>/internal/`。
- 不得修改 `internal/scaffold-template` 模板源，也不得修改 `internal/ppt-master` 中受 `attribution_guard.py` 校验的文件（SKILL.md 元数据、门禁标记、LICENSE、9 个 gate 脚本）。
- 不得跳过任一阶段（跳过阶段 B 会丢需求细节；跳过阶段 C 阶段 D 无骨架可用）。
- 不得在未读三件套之前搭骨架（项目名/库名/角色分组全由三件套决定）。
- 不得在阶段 C 前进入阶段 D。
- 不得改变已定框架级命名（包名、工程名、端口）——阶段 C 定名后全流程保持一致。
- 不得跨过内部手册的 `⛔ BLOCKING` 门槛替用户做决定。
- 不得在验证失败时宣称完成；会话临时文件（内容 JSON、验证脚本、classpath 文件）用完清理，但不要清理 `internal/deps`。
