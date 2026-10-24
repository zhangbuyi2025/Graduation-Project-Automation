<script setup>
import { onMounted, ref } from 'vue'
import { getArchivedList, exportArchived } from '@/api/archive'
import { getSemesters, getDepartments } from '@/api/dict'
import { useUserStore } from '@/stores/user'
import {
  STATUS_TAG,
  STATUS_LABEL,
  TYPE_LABEL,
  formatDateTime,
} from '@/utils/archive-labels'

const userStore = useUserStore()
const isDean = userStore.role === 'dean'

const semesters = ref([])
const departments = ref([])

const filters = ref({
  deptId: null,
  semesterId: null,
  status: null,
  archiveType: null,
})

const list = ref([])
const loading = ref(false)

async function loadList() {
  loading.value = true
  try {
    const params = { ...filters.value }
    // 主任范围固定到本教研室，不传 deptId
    if (!isDean) delete params.deptId
    const res = await getArchivedList(params)
    list.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleExport() {
  try {
    const params = { ...filters.value }
    if (!isDean) delete params.deptId
    await exportArchived(params)
  } catch (e) {
    alert(e.message || '导出失败')
  }
}

onMounted(async () => {
  const semRes = await getSemesters()
  semesters.value = semRes.data
  const current = semesters.value.find((s) => s.isCurrent === 1)
  if (current) filters.value.semesterId = current.semesterId

  if (isDean) {
    const depRes = await getDepartments(userStore.userInfo.collegeId)
    departments.value = depRes.data
  }
  await loadList()
})
</script>

<template>
  <div>
    <div class="crumb">{{ '管理' }} / <b>归档</b></div>
    <h1 class="page">归档管理</h1>
    <div class="sub">查询已归档与审核中的档案，支持导出 Excel 归档清单</div>

    <div class="toolbar">
      <select v-if="isDean" v-model="filters.deptId" @change="loadList">
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

      <button class="btn btn-primary" @click="handleExport">导出 Excel</button>
    </div>

    <div class="tablewrap">
      <table class="tb">
        <thead>
          <tr>
            <th>开课学院</th>
            <th>教研室</th>
            <th>课程号</th>
            <th>课程 / 班级</th>
            <th>类型</th>
            <th>版本</th>
            <th>提交时间</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && list.length === 0">
            <td colspan="8" class="empty">暂无档案</td>
          </tr>
          <tr v-for="item in list" :key="item.archiveId">
            <td>{{ item.collegeName }}</td>
            <td>{{ item.deptName }}</td>
            <td>{{ item.courseCode }}</td>
            <td>{{ item.courseName }} · {{ item.className }}</td>
            <td>{{ TYPE_LABEL[item.archiveType] }}</td>
            <td>v{{ item.versionNo }}</td>
            <td>{{ formatDateTime(item.submitTime) }}</td>
            <td>
              <span class="tag" :class="STATUS_TAG[item.status]">
                {{ STATUS_LABEL[item.status] }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
