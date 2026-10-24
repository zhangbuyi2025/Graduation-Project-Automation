<script setup>
import { onMounted, ref } from 'vue'
import { getArchiveList, exportArchive } from '@/api/archive'
import { getColleges, getDepartments, getSemesters } from '@/api/dict'
import ArchiveDetailModal from '@/components/ArchiveDetailModal.vue'
import {
  STATUS_TAG,
  STATUS_LABEL,
  TYPE_LABEL,
  formatDateTime,
} from '@/utils/archive-labels'

const colleges = ref([])
const departments = ref([])
const semesters = ref([])

const filters = ref({
  collegeId: null,
  deptId: null,
  semesterId: null,
  status: null,
  archiveType: null,
  keyword: '',
})

const list = ref([])
const loading = ref(false)
const detailArchiveId = ref(null)

async function loadList() {
  loading.value = true
  try {
    const res = await getArchiveList(filters.value)
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

async function handleExport() {
  try {
    await exportArchive(filters.value)
  } catch (e) {
    alert(e.message || '导出失败')
  }
}

onMounted(async () => {
  const [colRes, semRes] = await Promise.all([getColleges(), getSemesters()])
  colleges.value = colRes.data
  semesters.value = semRes.data
  const current = semesters.value.find((s) => s.isCurrent === 1)
  if (current) filters.value.semesterId = current.semesterId
  const depRes = await getDepartments()
  departments.value = depRes.data
  await loadList()
})
</script>

<template>
  <div>
    <div class="crumb">全校档案管理 / <b>归档总览</b></div>
    <h1 class="page">归档总览</h1>
    <div class="sub">跨学院查询所有教学档案，支持按条件筛选、搜索与导出 Excel</div>

    <div class="toolbar">
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

      <select v-model="filters.semesterId" @change="loadList">
        <option :value="null">全部学期</option>
        <option v-for="s in semesters" :key="s.semesterId" :value="s.semesterId">
          {{ s.semesterName }}
        </option>
      </select>

      <select v-model="filters.archiveType" @change="loadList">
        <option :value="null">全部类型</option>
        <option value="PRE">课前</option>
        <option value="POST">课后</option>
      </select>

      <select v-model="filters.status" @change="loadList">
        <option :value="null">全部状态</option>
        <option value="ARCHIVED">已归档</option>
        <option value="PENDING">待初审</option>
        <option value="FIRST_REVIEW">待中间审核</option>
        <option value="MIDDLE_REVIEW">待终审</option>
        <option value="REJECTED">已驳回</option>
      </select>

      <input v-model="filters.keyword" type="text" placeholder="课程号 / 教师 / 课程名"
             @keyup.enter="loadList">
      <button class="btn btn-primary" @click="loadList">查询</button>
      <button class="btn btn-ghost" @click="handleExport">导出 Excel</button>
    </div>

    <div class="tablewrap">
      <table class="tb">
        <thead>
          <tr>
            <th>开课学院</th>
            <th>教研室</th>
            <th>课程号</th>
            <th>课程 / 班级</th>
            <th>教师</th>
            <th>类型</th>
            <th>版本</th>
            <th>提交时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && list.length === 0">
            <td colspan="10" class="empty">暂无档案信息</td>
          </tr>
          <tr v-for="item in list" :key="item.archiveId">
            <td>{{ item.collegeName }}</td>
            <td>{{ item.deptName }}</td>
            <td>{{ item.courseCode }}</td>
            <td>{{ item.courseName }} · {{ item.className }}</td>
            <td>{{ item.teacherName }}</td>
            <td>{{ TYPE_LABEL[item.archiveType] }}</td>
            <td>v{{ item.versionNo }}</td>
            <td>{{ formatDateTime(item.submitTime) }}</td>
            <td>
              <span class="tag" :class="STATUS_TAG[item.status]">
                {{ STATUS_LABEL[item.status] }}
              </span>
            </td>
            <td><a @click="detailArchiveId = item.archiveId">查看</a></td>
          </tr>
        </tbody>
      </table>
    </div>

    <ArchiveDetailModal :archive-id="detailArchiveId" @close="detailArchiveId = null" />
  </div>
</template>
