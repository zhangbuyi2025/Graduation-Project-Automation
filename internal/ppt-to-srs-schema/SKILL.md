---
name: ppt-to-srs-schema
description: Convert a PPT usage manual or system introduction into a complete software project pipeline Markdown summary, SRS Word document, and SQL schema. Use when the user provides a pptx and asks for requirements docs, SRS, database design, or schema generation. Do not use when the source is not a PPT or the user only wants a single standalone artifact from scratch.
---

# PPT → SRS → SQL 完整流水线

本 Skill 将一份 PPT 使用手册（或系统介绍）自动转化为一套可交付的软件项目产物：

```
PPT 使用手册 → ① Markdown 结构化总结 → ② SRS Word 需求规格说明书 → ③ SQL 建表脚本
```

## 触发条件

- 用户提供一个 `.pptx` 文件，并要求"总结 / 整理 / 写需求 / 做数据库设计 / 生成 SQL"等
- 用户明确提到「使用手册 / 介绍 PPT / 产品介绍」并希望产出工程化文档
- 用户说"从这个 PPT 做一套需求 + 数据库"

## 前置准备

在开始前先确认：
1. PPT 文件路径（Glob / LS 查找 .pptx）
2. 根目录路径（所有产物输出到根目录）
3. 若目标路径已有同名文件，**先读再覆盖**或询问用户

## 阶段 ①：PPT → Markdown 结构化总结

### 提取内容

优先使用 `markitdown`（由本 Skill 自带依赖提供；缺失时执行 `python "<SKILL_DIR>/internal/scripts/bootstrap.py"` 自动检测安装（官方源失败自动切国内镜像），其中 `SKILL_DIR` 是本 skill 的安装根目录）：
```bash
python -m markitdown "path/to/manual.pptx"
```
- 如果 markitdown 不可用，回退到 `python-pptx`：
  ```python
  from pptx import Presentation
  prs = Presentation("manual.pptx")
  for i, slide in enumerate(prs.slides):
      print(f"\n<!-- Slide number: {i+1} -->")
      for shape in slide.shapes:
          if shape.has_text_frame:
              for para in shape.text_frame.paragraphs:
                  t = para.text.strip()
                  if t: print(t)
  ```

### 整理原则

- 按 **角色** 分章（教师 / 主任 / 院长 / 教务处 …）
- 每章下按 **功能块** 分节（登录首页 / 提交附件 / 审核 / 归档 …）
- **保留全部规则细节**：PDF-only、5 个附件槽位、驳回重提、一键驳回、跨学期只读、Excel 导出等
- 图片引用（`![](图片X.jpg)`）改为文字描述或删除
- 末尾增加「整体流程总结」章节：审核层级流程图 + 附件类型表 + 关键规则清单

### 输出

- 文件名：`ppttomd.md`（项目根目录）
- 必须可读、无截图占位符、所有功能描述完整保留

### 自检清单

- [ ] PPT 每个 slide 的文字信息都被覆盖（无遗漏章节）
- [ ] 角色/模块划分清晰，与原 PPT 目录结构一致
- [ ] 业务规则（权限约束、格式限制、状态流转）都以独立条目保留
- [ ] 审核层级链（A → B → C → 归档）明确写出

---

## 阶段 ②：Markdown → SRS Word

### 文档结构（严格遵循）

```
封面页（大标题 + 版本 + 编制单位 + 日期）
修订记录（表格）
目录（TOC，Heading1-3）
1 引言（目的 / 背景 / 范围 / 术语）
2 总体描述（产品视角 / 用户角色 / 权限矩阵 / 运行环境）
3 功能需求（按角色分 3.1 / 3.2 / 3.3 / 3.4 … 每节含用例表 + 图片占位）
3.X 档案状态流转（状态机表格 + 状态机图占位）
4 数据需求（E-R 描述 + 实体属性表 + ER 图占位）
5 业务流程（泳道图 + 用例图占位 + 校验子流程）
6 非功能需求（性能 / 安全 / 可靠性 / 可维护性 / 兼容性 量化指标）
7 接口需求（外部接口表 + 内部 REST API 示例）
8 验收标准（功能验收项 + 非功能验收项）
```

### 用例表格式（每个核心功能必须有）

```
用例编号 | 用例名称 | 参与者 | 前置条件 | 基本流程 | 异常流程 | 业务规则
```

### 图片占位写法

```
【此处插入图片：<类型>——<描述>】
```
- 截图类：`【此处插入图片：教师登录首页截图——左侧导航栏展示 X、Y 两个菜单】`
- 图表类：`【此处插入图片：系统 E-R 图——建议绘制 9 个核心实体及其属性与关系基数】`

### 技术栈

使用本 Skill 内置的 `docx` npm 包（docx-js）与内置生成器，**不要在用户项目里安装依赖，也不要在项目里手写临时脚本**：

1. 首次使用时（或 `internal/deps/node_modules/docx` 不存在时）跑一次自举（缺才装，失败自动切 npmmirror）：
   ```bash
   python "<SKILL_DIR>/internal/scripts/bootstrap.py"
   ```
2. 按下方「SRS 内容 JSON 规格」写一份内容 JSON（放在项目根的临时目录或系统临时目录），然后：
   ```bash
   node "<SKILL_DIR>/internal/scripts/gen-srs.js" "<内容.json>" "<项目根>/<项目名>_需求规格说明书.docx"
   ```
   `gen-srs.js` 已封装封面、修订记录、目录域、Heading1/2、表格、项目符号与编号、页眉页脚页码，只需提供内容。

SRS 内容 JSON 规格：

```json
{
  "title": "<项目名>",
  "subtitle": "软件需求规格说明书",
  "version": "V1.0",
  "organization": "<单位名称>",
  "date": "YYYY年M月",
  "header": "<系统名> — 软件需求规格说明书",
  "revisions": [["V1.0", "YYYY-MM-DD", "初稿", "编写人"]],
  "chapters": [
    {
      "heading": "1 引言",
      "sections": [
        {
          "heading": "1.1 编写目的",
          "blocks": [
            { "type": "p", "text": "正文段落" },
            { "type": "bullets", "items": ["条目一", "条目二"] },
            { "type": "numbers", "items": ["步骤一", "步骤二"] },
            { "type": "table", "header": ["列1", "列2"], "rows": [["a", "b"]] },
            { "type": "image", "text": "【此处插入图片：<类型>——<描述>】" }
          ]
        }
      ]
    }
  ]
}
```

关键配置（脚本已内置，仅说明）：
- 页面：A4（11906 × 16838 DXA），上下左右边距各 1440（1 inch）
- 字体：宋体正文 24pt；黑体 Heading1/2/3
- 页眉：`系统名 — 软件需求规格说明书`
- 页脚：`第 X 页 / 共 Y 页`
- 表格：**必须同时设置 table width（DXA）和 cell width**，两者相等，否则渲染错误
- 表格边框：`BorderStyle.SINGLE, 1px, #CCCCCC`
- 表头背景：深蓝 `#1F4E79`，白字
- 列表：**必须用 LevelFormat.BULLET / LevelFormat.DECIMAL**，不要手写 • 或 1.

### 输出文件名

- `项目根目录/教学档案数字化系统_需求规格说明书.docx`
- 通用命名：`<项目名>_需求规格说明书.docx`

### 文档验证

生成后用 `python-docx` 回读验证：
```python
import docx
d = docx.Document("xxx.docx")
for p in d.paragraphs:
    if p.text.strip():
        print(p.text.strip())
```
- 确认封面/修订/目录/8 章内容完整
- 确认图片占位符都存在
- 确认表格无空白

### ⚠️ 重要提示

Word 打开后**目录不会立即渲染**——需要按 `F9` 或右键目录 → 更新域 → 更新整个目录。生成完成后必须在交付说明里告诉用户。

---

## 阶段 ③：SRS → SQL 建表脚本

### 数据库选择

默认 **MySQL 8.0+**，字符集 `utf8mb4`，Collation `utf8mb4_unicode_ci`。
若 SRS 中有明确指定（PostgreSQL、SQL Server），以 SRS 为准。

### 目录表（必须有）

先写字典 / 组织架构表，再写核心业务表：

| 序号 | 表名（前缀 t_） | 说明 |
|------|----------------|------|
| 1 | `t_college` | 学院 |
| 2 | `t_department` | 教研室（外键 → t_college，自关联 chair → t_user） |
| 3 | `t_user` | 用户（角色 ENUM + BCrypt 密码 CHECK） |
| 4 | `t_semester` | 学期 |
| 5 | `t_course` | 课程主数据 |
| 6 | `t_course_instance` | 课程实例（某学期某教师的某门课 = 教学任务） |
| 7 | `t_archive` | 档案（课前 PRE / 课后 POST，状态机 ENUM） |
| 8 | `t_attachment` | 附件（槽位编号 + PDF 强制校验 + 文件哈希） |
| 9 | `t_review_log` | 审核记录（留痕审计） |
| 10 | `t_audit_log` | 通用操作审计日志 |

### 硬性规则（不满足不准出）

1. **密码 BCrypt 加密**：`t_user.password VARCHAR(64)` + `CHECK (password LIKE '$2a$%' AND CHAR_LENGTH = 60)`；初始化数据使用占位符 `$2a$10$PLACEHOLDER_REPLACE_WITH_REAL_BCRYPT_HASH...`
2. **PDF-only**：`t_attachment` 必须有 `CHECK (LOWER(file_ext) = 'pdf' AND mime_type = 'application/pdf')`
3. **双时间戳**：每张业务表必须有 `created_at` 和 `updated_at ON UPDATE`
4. **主键 BIGINT UNSIGNED AUTO_INCREMENT**：统一，不要用 UUID 做主键（可作为辅助字段）
5. **外键级联**：`ON DELETE RESTRICT`（防止误删引用数据）或 `ON DELETE SET NULL`（可选关联）
6. **逻辑删除**：核心业务表（archive）加 `is_deleted TINYINT DEFAULT 0`
7. **业务状态 ENUM**：不使用字符串/VARCHAR 存状态，用 `ENUM` 并加 CHECK 注释说明
8. **JSON 字段**：审计日志 detail 用 JSON 类型（MySQL 5.7+ 支持）
9. **COMMENT**：每个表、每个字段都必须有 `COMMENT`（中文）
10. **索引**：高频查询路径单独加索引，不要全表全字段

### 辅助产物（必须有）

- **外键约束**：所有关联必须显式声明 FOREIGN KEY
- **CHECK 约束**：业务规则（状态合法、密码格式、附件大小、槽位编号范围）
- **视图**：至少 4 个——教师视角列表 / 待初审清单 / 待终审清单 / 归档导出视图
- **初始化数据**：学院、教研室、用户（BCrypt 占位）、学期、课程、课程实例
- **校验 SQL 注释块**：在文件末尾提供 6 条可直接运行的检查 SQL（主键重复、外键孤儿、密码格式、状态流转异常、附件格式、重复哈希）

### 输出文件名

- `项目根目录/teaching_archive.sql`
- 通用命名：`<database_name>.sql`（database_name 使用 snake_case）

### SQL 验证（必须通过）

生成后跑以下检查：
```python
sql = open("xxx.sql", encoding="utf-8").read()
checks = {
    "CREATE TABLE == 10": sql.count("CREATE TABLE") == 10,
    "FOREIGN KEY 数量": sql.count("FOREIGN KEY") >= 10,
    "CHECK 约束": "CONSTRAINT `chk_" in sql,
    "BCrypt 占位": "PLACEHOLDER_REPLACE_WITH_REAL_BCRYPT_HASH" in sql,
    "PDF CHECK": "chk_att_pdf_only" in sql,
    "括号平衡": sql.count("(") == sql.count(")"),
}
for k, v in checks.items():
    print(f"  {k}: {'✓' if v else '✗'}")
```
全部 ✓ 才允许交付。

---

## 完整流水线执行顺序

```
① Glob 找到 pptx
② markitdown 提取 → 读全文
③ 按角色分章 → 写 ppttomd.md
④ 读回 ppttomd.md 确认无遗漏
⑤ 确保内置依赖就绪（`python "<SKILL_DIR>/internal/scripts/bootstrap.py"`，已就绪则秒过）
⑥ 写 SRS 内容 JSON → `node "<SKILL_DIR>/internal/scripts/gen-srs.js" <json> <docx>` → 产出 .docx
⑦ python-docx 读回 .docx 验证 8 章完整
⑧ 写 .sql（10 表 + 视图 + 初始化 + 校验 SQL）
⑨ 运行 SQL 验证脚本 → 全部通过
⑩ 清理本次会话的临时文件（内容 JSON、`_validate_sql.py` 等）；不要清理 `internal/deps`
⑪ 向用户交付：三件套文件路径 + 使用提示（目录 F9、BCrypt 占位替换、图片占位说明）
```

## 交付说明模板

```
已生成三份产物，位于项目根目录：

1. ppttomd.md          —— PPT 结构化总结（181 行）
2. xxx_需求规格说明书.docx —— SRS Word（8 章 + 封面/修订/目录）
3. xxx.sql             —— MySQL 8.0 建表脚本（10 表 + 4 视图 + 初始化数据）

⚠️ 使用提示：
- Word 目录：打开后按 F9 刷新域
- SQL 密码：初始化数据中的密码字段是 BCrypt 占位符，部署前需用 BCryptPasswordEncoder 替换
- 图片占位：Word 中有 N 处【此处插入图片：xxx】占位描述，按描述截图/绘图后替换
```

## 禁止事项

- 不要跳过任一阶段（PPT 直接写 SRS 会遗漏细节）
- 不要用 Unicode 手写的 • 或 1. 代替 docx-js 的 numbering 配置
- 不要在 password 字段存明文
- 不要省略 COMMENT
- 不要省略 CHECK 约束（业务规则漂移的第一根源）
- 临时脚本生成后必须清理（除非用户要求保留）
