# SQL 编码规范（MySQL 8.0）

本文件是 `ppt-to-srs-schema` 阶段 ③ 的强制编码标准。

## 通用规则

- 字符集 `utf8mb4`，Collation `utf8mb4_unicode_ci`
- 引擎 `InnoDB`（事务 + 行锁 + 外键）
- 表名前缀 `t_`，全小写 snake_case
- 字段全小写 snake_case，禁止缩写（`submit_time` 而非 `sub_time`）
- **每个表、每个字段必须有 COMMENT（中文）**
- **所有时间字段用 DATETIME**，不要用 TIMESTAMP（时区坑）

## 命名规范

| 对象 | 规则 | 示例 |
|------|------|------|
| 表 | `t_` + 名词复数（或单数表名即可） | `t_archive` |
| 主键 | `<entity>_id`，BIGINT UNSIGNED AUTO_INCREMENT | `archive_id` |
| 外键 | `<target_entity>_id`，数据类型必须与目标表主键完全一致 | `dept_id BIGINT UNSIGNED` |
| 索引 | `idx_<table>_<field>[_<field>]` | `idx_archive_status_time` |
| 唯一索引 | `uk_<table>_<field>[_<field>]` | `uk_user_no` |
| 约束 | `fk_<table>_<target>` / `chk_<table>_<rule>` | `fk_archive_instance` / `chk_att_pdf_only` |

## 字段类型选择

| 场景 | 推荐类型 | 备注 |
|------|---------|------|
| 主键 / 外键 | BIGINT UNSIGNED | 自增主键；统一类型方便 JOIN |
| 短文本（代码、工号） | VARCHAR(32) | 不要 CHAR |
| 中文姓名 | VARCHAR(64) | |
| 长文本 / URL | VARCHAR(255–512) | |
| JSON 配置 | JSON | MySQL 5.7+ 原生支持 |
| 枚举状态 | ENUM(...) | 不使用 TINYINT + 注释 |
| 布尔 | TINYINT 0/1 | 或 ENUM('Y','N') |
| 金额 | DECIMAL(12,2) | 禁止 FLOAT/DOUBLE |
| 学分 | DECIMAL(4,2) | |
| 文件大小 | BIGINT UNSIGNED | 字节 |
| 日期 | DATE | |
| 精确时间 | DATETIME | DEFAULT CURRENT_TIMESTAMP |
| 审计字段 | DATETIME | ON UPDATE CURRENT_TIMESTAMP |

## 必须有的字段

每张业务表**必须**包含：
```sql
`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                 ON UPDATE CURRENT_TIMESTAMP                    COMMENT '更新时间'
```

核心业务表（涉及审计）额外添加：
```sql
`is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除'
```

## 外键规则

```sql
CONSTRAINT `fk_archive_instance` FOREIGN KEY (`instance_id`)
    REFERENCES `t_course_instance` (`instance_id`)
    ON DELETE RESTRICT    -- 有引用不准删主表
    ON UPDATE CASCADE     -- 主键变更自动同步
```

- 默认 `ON DELETE RESTRICT`（防止误删脏级联）
- 可选关联（如教研室主任）用 `ON DELETE SET NULL`
- **被引用字段必须是唯一键（通常是 PK）**

## CHECK 约束使用场景

| 场景 | 示例 |
|------|------|
| 密码 BCrypt | `CHECK (password LIKE '$2a$%' AND CHAR_LENGTH = 60)` |
| 文件格式 | `CHECK (LOWER(file_ext) = 'pdf')` |
| 文件大小 | `CHECK (file_size <= 50 * 1024 * 1024)` |
| 状态枚举合法 | `CHECK (status IN ('A','B','C'))` 或 CASE WHEN |
| 评分范围 | `CHECK (score >= 0 AND score <= 100)` |
| 槽位编号 | `CHECK (slot_index >= 1 AND slot_index <= 99)` |

## 初始数据

- 学院、教研室、学期等字典表：**必须提供 INITIAL INSERT**，方便系统首次启动
- 用户密码字段：使用 BCrypt **占位符**，格式合法但内容无效，注释说明部署时替换
- 示例：`'$2a$10$PLACEHOLDER_REPLACE_WITH_REAL_BCRYPT_HASH______________________________'`（凑满 60 字符）

## 视图命名

- `v_` 前缀 + 用途描述
- 归档 / 导出类视图命名：`v_archive_export`、`v_pending_first_review`

## 禁止事项

- ❌ 不要用 `INT` 做主键（数据量上来不够）
- ❌ 不要用 `VARCHAR` 存日期
- ❌ 不要省略 `COMMENT`
- ❌ 不要用 `utf8` 字符集（不完整 emoji 支持）
- ❌ 不要在密码字段存明文
- ❌ 不要省略外键声明（靠应用层 = 架构隐患）
- ❌ 不要用 `INSERT IGNORE` 静默吞错误
