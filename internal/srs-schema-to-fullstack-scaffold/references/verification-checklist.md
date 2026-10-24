# 交付前验证清单

本文件是 `srs-schema-to-fullstack-scaffold` 的强制验证清单。逐项核对，全部通过才允许交付。

## 输入完整性

- [ ] 已遍历前后端工程目录结构（包名、分层、端口、依赖）
- [ ] 已通读 SRS（docx 已转纯文本），角色 / 功能 / 状态机 / 业务规则 / 接口无遗漏
- [ ] 已通读交互原型 HTML，每个页面 / 表格列 / 弹窗均有对应
- [ ] 已通读 SQL，表 / 字段 / 外键 / CHECK / 视图 / 初始数据记录完整
- [ ] 已标记 SQL 与 SRS 不一致处（视图笔误），后端不沿用

## 框架约束（红线）

- [ ] 未更改任何框架级文件夹 / 包名 / 工程名 / 端口
- [ ] 覆盖已存在文件前均已先 Read
- [ ] 临时文件（classpath、临时脚本）已清理

## 后端

- [ ] 每张表对应一个实体（`@TableName/@TableId`）
- [ ] 时间字段自动填充；逻辑删除字段 `@TableLogic`
- [ ] 每个实体有 Mapper（`BaseMapper`），关联查询正确
- [ ] 每个实体有 IService + ServiceImpl
- [ ] DTO / VO 分离
- [ ] 基础设施：文件存储（扩展名 + 大小 + 文件头魔数 + SHA-256 + UUID）、Excel 导出（POI）
- [ ] 业务异常 + 全局异常处理
- [ ] Controller 统一返回 Result，路径与 SRS 接口契约一致
- [ ] 角色映射正确（`ROLE_` 前缀），`@PreAuthorize` 权限到位
- [ ] **`mvnw -DskipTests compile` → EXIT=0**

## 前端（每个工程）

- [ ] 公共样式 main.css 源自原型，main.js 已引入
- [ ] user store（token / userInfo，localStorage 持久化）
- [ ] API 模块按域拆分，响应取值与拦截器实际返回一致
- [ ] 登录页：本端角色校验 + 跳转
- [ ] 主布局：角色化菜单 + 退出
- [ ] 上传弹窗（课前固定槽 / 课后动态、校验、预览、提交）
- [ ] 档案详情弹窗
- [ ] 审核弹窗（逐槽预览、勾选/一键驳回、通过、意见必填）
- [ ] 全部业务页面落地
- [ ] 路由 + 守卫（无 token→登录，角色放行）
- [ ] 附件预览走 axios blob（携带 JWT）
- [ ] **`npm run build` → ✓ built**

## 数据可运行性

- [ ] 种子密码非占位符：已用 `BCryptPasswordEncoder` 生成真实哈希
- [ ] 哈希为 60 字符、以 `$2a$` 开头（满足 CHECK 约束）
- [ ] 已记录种子账号与统一初始密码

## 环境排坑确认（Windows）

- [ ] 编译 / 运行使用 JDK 17（非 PATH 上的 Java 11）
- [ ] PowerShell `-D` 参数已加引号
- [ ] 读取含中文路径的文件用 `-Encoding UTF8`
- [ ] `package.json` 声明的依赖已实际安装

## 交付信息

- [ ] 列出新增 / 修改文件清单（后端、前端）
- [ ] 给出验证结果（后端编译、前端构建）
- [ ] 给出启动顺序（建库导入 SQL → MySQL/Redis/上传目录 → 后端 → 各前端 dev）
- [ ] 验证失败项已如实说明，不宣称完成
