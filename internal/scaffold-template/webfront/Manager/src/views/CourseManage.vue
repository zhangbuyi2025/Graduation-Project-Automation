<script setup>
import { onMounted, ref } from 'vue'
import {
  getCourses,
  createCourse,
  updateCourse,
  deleteCourse,
} from '@/api/course'
import { getColleges, getDepartments } from '@/api/dict'

const colleges = ref([])
const departments = ref([])

const filters = ref({
  keyword: '',
  collegeId: null,
  deptId: null,
  courseType: null,
})

const list = ref([])
const loading = ref(false)

const formVisible = ref(false)
const isEdit = ref(false)
const form = ref({})

const typeLabels = { required: '必修', elective: '选修' }

async function loadList() {
  loading.value = true
  try {
    const res = await getCourses(filters.value)
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
    courseCode: '',
    courseName: '',
    credit: 2,
    hours: 32,
    courseType: 'required',
    deptId: null,
    status: 1,
  }
  formVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  if (row.collegeId) {
    getDepartments(row.collegeId).then((res) => {
      departments.value = res.data
    })
  }
  formVisible.value = true
}

async function saveForm() {
  if (!form.value.courseCode || !form.value.courseName) {
    alert('请填写课程号和课程名称')
    return
  }
  try {
    if (isEdit.value) {
      await updateCourse(form.value.courseId, form.value)
    } else {
      await createCourse(form.value)
    }
    formVisible.value = false
    await loadList()
  } catch (e) {
    alert(e.message || '保存失败')
  }
}

async function handleDelete(row) {
  if (!confirm(`确定停用 / 删除课程 ${row.courseName}（${row.courseCode}）？`)) return
  try {
    await deleteCourse(row.courseId)
    await loadList()
  } catch (e) {
    alert(e.message || '删除失败')
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
    <div class="crumb">基础数据管理 / <b>课程管理</b></div>
    <h1 class="page">课程管理</h1>
    <div class="sub">维护课程主数据，支持新增、编辑、停用</div>

    <div class="toolbar">
      <input v-model="filters.keyword" type="text" placeholder="课程号 / 课程名"
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
      <select v-model="filters.courseType" @change="loadList">
        <option :value="null">全部类型</option>
        <option value="required">必修</option>
        <option value="elective">选修</option>
      </select>
      <button class="btn btn-primary" @click="loadList">查询</button>
      <button class="btn btn-primary" @click="openCreate">新增课程</button>
    </div>

    <div class="tablewrap">
      <table class="tb">
        <thead>
          <tr>
            <th>课程号</th>
            <th>课程名称</th>
            <th>学分</th>
            <th>学时</th>
            <th>类型</th>
            <th>教研室</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && list.length === 0">
            <td colspan="8" class="empty">暂无课程信息</td>
          </tr>
          <tr v-for="row in list" :key="row.courseId">
            <td>{{ row.courseCode }}</td>
            <td>{{ row.courseName }}</td>
            <td>{{ row.credit }}</td>
            <td>{{ row.hours }}</td>
            <td>{{ typeLabels[row.courseType] || row.courseType }}</td>
            <td>{{ departments.find((d) => d.deptId === row.deptId)?.deptName || '—' }}</td>
            <td>
              <span class="tag" :class="row.status === 1 ? 'tag-green' : 'tag-gray'">
                {{ row.status === 1 ? '启用' : '停用' }}
              </span>
            </td>
            <td>
              <a @click="openEdit(row)">编辑</a>
              <a @click="handleDelete(row)">停用</a>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="formVisible" class="modal-mask" @click.self="formVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑课程' : '新增课程' }}</h3>
          <button class="close" @click="formVisible = false">×</button>
        </div>

        <div class="form-grid">
          <div class="form-item">
            <span class="lbl">课程号</span>
            <input v-model="form.courseCode" type="text">
          </div>
          <div class="form-item">
            <span class="lbl">课程名称</span>
            <input v-model="form.courseName" type="text">
          </div>
          <div class="form-item">
            <span class="lbl">学分</span>
            <input v-model="form.credit" type="number" min="0" step="0.5">
          </div>
          <div class="form-item">
            <span class="lbl">学时</span>
            <input v-model="form.hours" type="number" min="0">
          </div>
          <div class="form-item">
            <span class="lbl">类型</span>
            <select v-model="form.courseType">
              <option value="required">必修</option>
              <option value="elective">选修</option>
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
        </div>

        <div class="toolbar" style="margin-top:16px">
          <button class="btn btn-primary" @click="saveForm">保存</button>
          <button class="btn btn-ghost" @click="formVisible = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>
