# lang-to-ppt-to-fullstack-ppt

> 一份一段需求描述（或需求 PPT）进去，一套完整的全栈实训/课设项目出来：
> 需求规格说明书 → 数据库 SQL → Spring Boot 3 + Vue3 骨架 → 全栈基础代码 → 项目总结 → 答辩 PPT。

这是一个面向 AI 编程助手（Claude Code / Trae / 豆包等支持 Agent Skills 的工具）的**自包含 Skill**：流水线手册、前后端骨架模板、PPT 生成引擎、Word 文档生成器与 Node 依赖全部随包提供，不依赖任何外部 Skill 目录。

## 功能特性

- **两种输入均可**：

  1.直接给 `.pptx` 需求文档；

  2.或只给文字需求描述（先自动物化为需求 PPT，再进入流水线）。

- **六阶段全自动流水线**，每个阶段都有明确的产物与自检门禁：
  - **A** 需求描述 → 需求 PPT（内置 ppt-master 引擎）
  - **B** PPT → 三件套：`ppttomd.md`、`*_需求规格说明书.docx`、`*.sql`（MySQL 8.0）
  - **C** 基础架构搭建：复制内置骨架 → 包名/工程改名 → 删除业务样例 → 生成交互原型 HTML → 骨架自检
  - **D** 全栈基础代码：按 SRS + SQL + 原型补全后端分层代码与用户端/管理端双前端页面
  - **E** 自动生成《项目名_项目总结.md》
  - **F** 自动生成《项目名_答辩PPT.pptx》

- **双前端**：用户端（User）+ 管理端（Manager）两个 Vue3 + Vite 工程；后端 Spring Boot 3。

- **产物落到你指定的项目根目录**，Skill 安装目录始终保持只读。

## 目录结构

```
lang-to-ppt-to-fullstack-ppt/
├── SKILL.md                  # 总编排手册（给 AI 执行，含六阶段流程与纪律）
├── README.md / LICENSE / .gitignore
├── requirements.txt          # Python 最小依赖清单
├── references/
│   ├── architecture-template.md   # 阶段 C 细则（复制/改名/删除/自检/排坑）
│   └── delivery-templates.md      # 阶段 E 总结模板 + 阶段 F 答辩大纲
└── internal/
    ├── ppt-master/                       # 内置 PPT 生成引擎（MIT，第三方）
    ├── ppt-to-srs-schema/                # 阶段 B 手册
    ├── srs-schema-to-fullstack-scaffold/ # 阶段 D 手册（前后端规范 + 验证清单）
    ├── scaffold-template/                # 只读骨架模板（后端 + 双前端 + 原型规范）
    ├── scripts/
    │   ├── bootstrap.py                  # 环境自举：检测并自动安装缺失依赖
    │   └── gen-srs.js                    # SRS Word 文档生成器（docx-js）
    └── deps/                             # Node 依赖（docx），随包内置
```

## 环境要求（仅 Windows）

| 组件 | 版本要求 | 说明 |
|---|---|---|
| 操作系统 | **Windows 10 / 11** | **本 Skill 仅支持 Windows（PowerShell），不支持也不计划适配 macOS / Linux** |
| Python | ≥ 3.10（推荐 3.10–3.13） | 需加入 PATH，命令为 `python` |
| Node.js | ≥ 22（含 npm） | 需加入 PATH |
| JDK | 17 | 需设置 `JAVA_HOME`；Maven 由骨架自带的 Maven Wrapper 提供，无需单独安装 |
| MySQL | 8.0 | 运行最终项目时使用；生成代码阶段不依赖 |
| 网络 | 首次运行需要 | 安装 Python 依赖、Maven Wrapper 首次下载 Maven 发行包 |

## 安装

将整个 `lang-to-ppt-to-fullstack-ppt` 文件夹放到你的 AI 工具的 Skills 目录下，保持目录结构不变即可。无需手动安装依赖——首次执行流水线时，AI 会自动运行环境自举脚本：

```powershell
# 自动检测，缺什么装什么（官方源失败会自动切清华/阿里云镜像、npmmirror）
python "<Skills目录>\lang-to-ppt-to-fullstack-ppt\internal\scripts\bootstrap.py"

# 只想提前体检环境（不安装）
python "<Skills目录>\lang-to-ppt-to-fullstack-ppt\internal\scripts\bootstrap.py" --check
```

末行输出 `BOOTSTRAP: OK` 即代表环境就绪。

**依赖说明（请注意）**：

- **Node 依赖（docx）随包内置**在 `internal/deps/node_modules`，无需联网安装；
- **Python 依赖（python-pptx、Pillow、flask、PyYAML、markitdown、python-docx）未随包内置**，首次运行时由 bootstrap 自动联网安装到你的 Python 环境；完全离线的机器需提前从别的机器拷贝 site-packages。

## 使用方法

1. 准备好需求 PPT（.pptx），或直接用文字描述需求（例如："做一个校园图书借阅管理系统，有读者和管理员两类角色，读者可以查书、借书、续借、归还，管理员管理图书和借阅记录……"）。
2. 指定一个**空的项目根目录**（全部产物输出到这里，建议路径不含空格）。
3. 对 AI 说："用 `lang-to-ppt-to-fullstack-ppt` 这个 skill，根据这份需求生成完整项目。"
4. 流水线在关键节点（角色确认、库名/包名确认等）会暂停等你确认，属于正常设计。

最终产物：

| 产物 | 说明 |
|---|---|
| `ppttomd.md` | 由 PPT 转换的结构化 Markdown 需求 |
| `*_需求规格说明书.docx` | 8 章 + 用例表 + 图片占位的正式 SRS |
| `*.sql` | MySQL 8.0 建库建表脚本（约 10 类表 + 视图 + 初始化数据 + 校验 SQL） |
| `*_交互原型.html` | 单文件、无 JS 的角色 × 页面交互原型 |
| `*_backend/` | Spring Boot 3 后端工程（可编译） |
| `*_webfront/User/`、`*/Manager/` | 用户端、管理端 Vue3 工程（可构建） |
| `*_项目总结.md` | 含产物清单、架构说明、启动部署步骤的总结文档 |
| `*_需求说明.pptx` / `*_答辩PPT.pptx` | 需求 PPT（无 PPT 输入时）与答辩 PPT |

## 启动生成的项目（流程结束后）

1. MySQL 8.0 建库并导入生成的 `.sql`；
2. 按需修改后端 `application.yml`（数据库密码、Redis、上传目录）；
3. 启动后端：`<项目>_backend` 目录执行 `.\mvnw.cmd spring-boot:run`；
4. 启动前端：各端目录执行 `npm install` 后 `npm run dev`；
5. 种子账号与统一初始密码见《项目总结.md》。

## 常见问题

- **Maven Wrapper 首次运行很慢**：它需要下载 Maven 3.9.x 发行包到用户目录 `~/.m2`，只下载一次，后续离线可用。
- **`mvnw.cmd` 报 `'powershell' is not recognized`**：Maven Wrapper 依赖 Windows PowerShell（`C:\Windows\System32\WindowsPowerShell\v1.0`），请确认该路径在 PATH 中。
- **Word 目录打开后页码不对**：全选后按 `F9` 刷新域。
- **pip 安装慢或失败**：手动执行 `python -m pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple`。

## 开源许可

本仓库作者原创部分采用 [MIT License](LICENSE)。

内置的第三方组件版权归各自作者所有：

- **ppt-master**（`internal/ppt-master/`）：MIT License，Copyright (c) 2025-2026 Hugo He，许可文件随附；
- **docx** 等 npm 依赖（`internal/deps/node_modules/`）：各包内置 LICENSE（以 MIT 为主）；
- ppt-master 模板中的图标、音效资源：见 `internal/ppt-master/templates/icons/THIRD_PARTY_NOTICES.md` 与 `templates/sounds/THIRD_PARTY_NOTICES.md`。
