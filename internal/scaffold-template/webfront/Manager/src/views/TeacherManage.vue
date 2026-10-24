<script setup>
import { onMounted, ref } from 'vue'
import {
  getTeachers,
  createTeacher,
  updateTeacher,
  deleteTeacher,
  updateTeacherStatus,
  resetPassword,
} from '@/api/teacher'
import { getColleges, getDepartments } from '@/api/dict'

const colleges = ref([])
const departments = ref([])

const filters = ref({
  keyword: '',
  collegeId: null,
  deptId: null,
  status: null,
})

const list = ref([])
const loading = ref(false)

const roleLabels = {
  teacher: '教师',
  dean_chair: '教研室主任',
  dean: '教学院长',
}

// 表单弹窗
const formVisible = ref(false)
const isEdit = ref(false)
const form = ref({})

// 重置密码弹窗
const pwdVisible = ref(false)
const pwdTarget = ref(null)
const newPassword = ref('')

async function loadList() {
  loading.value = true
  try {
    const res = await getTeachers(filters.value)
    list.value = res.data
  } finally {
    loading.value = false
  }
}

async function onCollegeChange() {
  filters.value.deptId = null
  const depRes = await getDepartments(filters.value.collegeId)
  departments.value = depRes.data
  await loadList()
}

async function onFormCollegeChange() {
  form.value.deptId = null
  const depRes = await getDepartments(form.value.collegeId)
  departments.value = depRes.data
}

function openCreate() {
  isEdit.value = false
  form.value = {
    userNo: '',
    username: '',
    realName: '',
    role: 'teacher',
    collegeId: null,
    deptId: null,
    email: '',
    phone: '',
    password: '',
    status: 1,
  }
  formVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.value = { ...row, password: '' }
  if (row.collegeId) {
    getDepartments(row.collegeId).then((res) => {
      departments.value = res.data
    })
  }
  formVisible.value = true
}

async function saveForm() {
  if (!form.value.userNo || !form.value.realName) {
    alert('请填写工号和姓名')
    return
  }
  try {
    if (isEdit.value) {
      await updateTeacher(form.value.userId, form.value)
    } else {
      if (!form.value.password) {
        alert('请填写初始密码')
        return
      }
      await createTeacher(form.value)
    }
    formVisible.value = false
    await loadList()
  } catch (e) {
    alert(e.message || '保存失败')
  }
}

async function handleDelete(row) {
  if (!confirm(`确定停用 / 删除账号 ${row.realName}（${row.userNo}）？`)) return
  try {
    await deleteTeacher(row.userId)
    await loadList()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

async function toggleStatus(row) {
  const target = row.status === 1 ? 0 : 1
  try {
    await updateTeacherStatus(row.userId, target)
    row.status = target
  } catch (e) {
    alert(e.message || '操作失败')
  }
}

function openResetPwd(row) {
  pwdTarget.value = row
  newPassword.value = ''
  pwdVisible.value = true
}

async function saveResetPwd() {
  if (!newPassword.value || newPassword.value.length < 6) {
    alert('新密码至少 6 位')
    return
  }
  try {
    await resetPassword(pwdTarget.value.userId, newPassword.value)
    pwdVisible.value = false
    alert('密码已重置')
  } catch (e) {
    alert(e.message || '重置失败')
  }
}

onMounted(async () => {
  const colRes = await getColleges()
  colleges.value = colRes.data
  const depRes = await getDepartments()
  departments.value = depRes.data
  await loadList()
})
</script>

<template>
  <div>
    <div class="crumb">基础数据管理 / <b>教师管理</b></div>
    <h1 class="page">教师管理</h1>
    <div class="sub">维护教师 / 主任 / 院长账号，支持新增、编辑、重置密码、启用停用</div>

    <div class="toolbar">
      <input v-model="filters.keyword" type="text" placeholder="工号 / 姓名"
             @keyup.enter="loadList">
      <select v-model="filters.collegeId" @change="onCollegeChange">
        <option :value="null">全部学院</option>
        <option v-for="c in colleges" :key="c.collegeId" :value="c.collegeId">
          {{ c.collegeName }}
        </option>
      </select>
      <select v-model="filters.deptId" @change="loadList">
        <option :value="null">全部教研室</option>
        <option v-for="d in departments" :key="d.deptId" :value="d.deptId">
          {{ d.deptName }}
        </option>
      </select>
      <select v-model="filters.status" @change="loadList">
        <option :value="null">全部状态</option>
        <option :value="1">启用</option>
        <option :value="0">停用</option>
      </select>
      <button class="btn btn-primary" @click="loadList">查询</button>
      <button class="btn btn-primary" @click="openCreate">新增教师</button>
    </div>

    <div class="tablewrap">
      <table class="tb">
        <thead>
          <tr>
            <th>工号</th>
            <th>姓名</th>
            <th>角色</th>
            <th>学院</th>
            <th>教研室</th>
            <th>邮箱</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && list.length === 0">
            <td colspan="8" class="empty">暂无教师信息</td>
          </tr>
          <tr v-for="row in list" :key="row.userId">
            <td>{{ row.userNo }}</td>
            <td>{{ row.realName }}</td>
            <td>{{ roleLabels[row.role] }}</td>
            <td>{{ colleges.find((c) => c.collegeId === row.collegeId)?.collegeName || '—' }}</td>
            <td>{{ departments.find((d) => d.deptId === row.deptId)?.deptName || '—' }}</td>
            <td>{{ row.email || '—' }}</td>
            <td>
              <span class="tag" :class="row.status === 1 ? 'tag-green' : 'tag-gray'">
                {{ row.status === 1 ? '启用' : '停用' }}
              </span>
            </td>
            <td>
              <a @click="openEdit(row)">编辑</a>
              <a @click="openResetPwd(row)">重置密码</a>
              <a @click="toggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</a>
              <a @click="handleDelete(row)">删除</a>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <div v-if="formVisible" class="modal-mask" @click.self="formVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑教师' : '新增教师' }}</h3>
          <button class="close" @click="formVisible = false">×</button>
        </div>

        <div class="form-grid">
          <div class="form-item">
            <span class="lbl">工号</span>
            <input v-model="form.userNo" type="text">
          </div>
          <div class="form-item">
            <span class="lbl">姓名</span>
            <input v-model="form.realName" type="text">
          </div>
          <div class="form-item">
            <span class="lbl">角色</span>
            <select v-model="form.role">
              <option value="teacher">教师</option>
              <option value="dean_chair">教研室主任</option>
              <option value="dean">教学院长</option>
            </select>
          </div>
          <div class="form-item">
            <span class="lbl">学院</span>
            <select v-model="form.collegeId" @change="onFormCollegeChange">
              <option :value="null">请选择</option>
              <option v-for="c in colleges" :key="c.collegeId" :value="c.collegeId">
                {{ c.collegeName }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <span class="lbl">教研室</span>
            <select v-model="form.deptId">
              <option :value="null">请选择</option>
              <option v-for="d in departments" :key="d.deptId" :value="d.deptId">
                {{ d.deptName }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <span class="lbl">邮箱</span>
            <input v-model="form.email" type="email">
          </div>
          <div class="form-item">
            <span class="lbl">电话</span>
            <input v-model="form.phone" type="text">
          </div>
          <div class="form-item" v-if="!isEdit">
            <span class="lbl">初始密码</span>
            <input v-model="form.password" type="password">
          </div>
        </div>

        <div class="toolbar" style="margin-top:16px">
          <button class="btn btn-primary" @click="saveForm">保存</button>
          <button class="btn btn-ghost" @click="formVisible = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 重置密码弹窗 -->
    <div v-if="pwdVisible" class="modal-mask" @click.self="pwdVisible = false">
      <div class="modal" style="width:420px">
        <div class="modal-header">
          <h3>重置密码 — {{ pwdTarget?.realName }}</h3>
          <button class="close" @click="pwdVisible = false">×</button>
        </div>
        <div class="form-item" style="margin-bottom:16px">
          <span class="lbl">新密码</span>
          <input v-model="newPassword" type="password" placeholder="至少 6 位">
        </div>
        <div class="toolbar">
          <button class="btn btn-primary" @click="saveResetPwd">确认重置</button>
          <button class="btn btn-ghost" @click="pwdVisible = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>
