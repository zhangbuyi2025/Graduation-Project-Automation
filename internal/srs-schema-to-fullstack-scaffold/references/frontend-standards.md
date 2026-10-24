# 前端编码规范（Vue 3 + Vite + Pinia）

本文件是 `srs-schema-to-fullstack-scaffold` 阶段 ② 的强制编码标准。
技术栈基线：Vue 3.5（`<script setup>`）、Vite、Pinia、Vue Router、Axios。

## 工程结构（保留现有框架）

多个独立工程（如 `User` 教师端 / `Manager` 管理端）各自独立，不互相引用，工程名、端口不改：

```
src/
├── api/              # 按业务域拆分的 API 模块
│   ├── auth.js
│   ├── dict.js
│   ├── archive.js
│   └── review.js     # 管理端另含 teacher.js / course.js
├── assets/
│   └── main.css      # 公共样式（源自交互原型）
├── components/       # 可复用组件（弹窗）
├── layout/
│   └── MainLayout.vue
├── router/
│   └── index.js
├── stores/
│   └── user.js
├── utils/
│   ├── preview.js
│   └── archive-labels.js
└── views/
    └── <模块>/<页面>.vue
```

## 公共样式

- 从交互原型 `<style>` 提取主题变量（`:root` CSS 变量）、布局 class（`.topbar/.side/.main`）、`.btn/.tag/.tb/.slot/.modal/.login-*`
- 集中放在 `src/assets/main.css`，`main.js` 中 `import './assets/main.css'`
- 页面只组合 class，不重复定义样式，保证与原型视觉一致

## 用户 Store

```js
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
    role: (s) => s.userInfo?.role || '',
    realName: (s) => s.userInfo?.realName || '',
  },
  actions: {
    setLogin(token, userInfo) {
      this.token = token
      this.userInfo = userInfo
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
  },
})
```
多工程共存时可用不同 localStorage key（如 `manager_token`）避免串端。

## Axios 实例与 API 模块

- 复用脚手架已有的 axios 实例（`src/api.js`），通常已配置 `baseURL='/api'`、请求拦截加 token、响应拦截解包
- 业务模块按域拆分，从实例引入：
  ```js
  // src/api/archive.js
  import request from '../api.js'

  export const getTasks = (semesterId) =>
    request.get('/work/task', { params: { semesterId } })

  export const submitArchive = (formData) =>
    request.post('/work/archive/submit', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000,
    })
  ```

### 响应取值约定（务必先确认拦截器）

- 若拦截器返回**完整 Result**（`{code,message,data}`），页面用 `res.data` 取业务载荷
- 若拦截器已解包返回 `data`，则直接使用
- **不要凭记忆猜**，以 `src/api.js` 实际拦截器返回为准

## 登录页

- 工号/用户名 + 密码；空值校验；loading 状态；错误提示
- 登录成功后按角色跳转，并对非本端角色拦截：
  - 教师端：`admin` 提示去管理端并登出
  - 管理端：非 `admin` 提示去教师端并登出

## 主布局（角色化菜单）

- 顶部栏：系统名 + 角色·姓名 + 退出
- 侧边栏：按角色 `computed` 生成菜单分组；`route.path` 高亮 active
- 内容区：`<router-view />`
- 示例分组：
  - 教师（我的工作台）：提交教学附件、查看审核情况
  - 主任（教研室管理）：初审、审批记录、归档
  - 院长（学院管理）：终审、审批记录、归档
  - 教务处（全校档案 / 基础数据）：归档总览、中间审核、教师管理、课程管理

## 可复用组件

- **上传弹窗 UploadModal**：
  - 课前固定 5 槽（固定槽位名）；课后 1..N（可增删、可改名）
  - 每个槽位：文件选择（`accept=".pdf"`）→ 前端校验扩展名/大小 → 本地预览（`URL.createObjectURL(file)`）
  - 提交前校验所有槽位就绪；组装 `FormData`：`files`、`instanceId`、`deptId`、`archiveType`、`slotIndexes`、`slotNames`（JSON），重提加 `archiveId`
- **档案详情弹窗 ArchiveDetailModal**：基础信息 + 附件（预览）+ 审核记录
- **审核弹窗 ReviewDetailModal**：
  - 逐槽位预览附件；每槽位复选框"驳回"
  - "一键驳回"勾选全部槽位
  - "通过"提交 APPROVED；"驳回"提交 REJECTED（至少勾一个槽位 + 意见必填）
  - 终审弹窗复用同一组件，仅传入不同 level 与调用不同接口
- 弹窗统一 `.modal-mask`（点击遮罩关闭）+ `.modal`

## 认证附件预览（blob）

```js
import request from '../api.js'

export async function previewAttachment(id) {
  const blob = await request.get(`/work/attachment/${id}/download`, {
    responseType: 'blob',
    params: { inline: true },
  })
  window.open(URL.createObjectURL(blob), '_blank')
}
```
- 响应拦截器对 blob 响应应原样返回（无 `.code` 时直接返回 blob）
- 不能用 `window.open('/api/...')`（缺少 Authorization 头，会 401）

## 标签 / 文案工具

集中维护状态/类型/层级的映射，避免页面写判断：
```js
export const STATUS_TAG = {
  PENDING: 'tag-amber', FIRST_REVIEW: 'tag-orange',
  MIDDLE_REVIEW: 'tag-blue', ARCHIVED: 'tag-green', REJECTED: 'tag-red',
}
export const STATUS_LABEL = {
  PENDING: '待初审', FIRST_REVIEW: '待中间审核',
  MIDDLE_REVIEW: '待终审', ARCHIVED: '已归档', REJECTED: '已驳回',
}
export const TYPE_LABEL = { PRE: '课前', POST: '课后' }
export function formatSize(bytes) { /* B/KB/MB */ }
export function formatDateTime(value) { /* yyyy-MM-dd HH:mm */ }
```

## 路由与守卫

```js
const routes = [
  { path: '/login', component: () => import('@/views/Login.vue') },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/submit',
    children: [
      { path: 'submit', component: () => import('@/views/teacher/Submit.vue'),
        meta: { roles: ['teacher', 'dean_chair', 'dean'] } },
      // ...
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/submit' },
]

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.path === '/login') return true
  if (!userStore.isLoggedIn) return { path: '/login' }
  const roles = to.meta?.roles
  if (roles && !roles.includes(userStore.role)) {
    // 回到该角色的默认页
  }
  return true
})
```

## 依赖

- 构建前确认 `package.json` 声明的依赖已**实际安装**（脚手架可能漏装 axios 等）；执行 `npm install`
- 构建命令：`npm run build`（vite build）

## 禁止事项

- 不更改工程名、端口、框架目录
- 不在每个页面重复写样式（用公共 class）
- 不直接 `window.open` 打开需认证的附件
- 不凭记忆猜测拦截器返回结构（先读 `api.js`）
- 不遗漏角色守卫与菜单
