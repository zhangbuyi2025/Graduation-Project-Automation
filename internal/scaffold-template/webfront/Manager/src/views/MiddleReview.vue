<script setup>
import { onMounted, ref } from 'vue'
import { getMiddlePending } from '@/api/archive'
import { getColleges, getSemesters } from '@/api/dict'
import MiddleReviewModal from '@/components/MiddleReviewModal.vue'
import {
  STATUS_TAG,
  STATUS_LABEL,
  TYPE_LABEL,
  formatDateTime,
} from '@/utils/archive-labels'

const colleges = ref([])
const semesters = ref([])

const filters = ref({
  collegeId: null,
  deptId: null,
  semesterId: null,
})

const list = ref([])
const loading = ref(false)
const reviewArchiveId = ref(null)

async function loadList() {
  loading.value = true
  try {
    const res = await getMiddlePending(filters.value)
    list.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const [colRes, semRes] = await Promise.all([getColleges(), getSemesters()])
  colleges.value = colRes.data
  semesters.value = semRes.data
  const current = semesters.value.find((s) => s.isCurrent === 1)
  if (current) filters.value.semesterId = current.semesterId
  await loadList()
})
</script>

<template>
  <div>
    <div class="crumb">全校档案管理 / <b>中间审核</b></div>
    <h1 class="page">中间审核</h1>
    <div class="sub">已通过教研室初审、待教务处中间审核的档案；通过后进入教学院长终审</div>

    <div class="toolbar">
      <select v-model="filters.collegeId" @change="loadList">
        <option :value="null">全部学院</option>
        <option v-for="c in colleges" :key="c.collegeId" :value="c.collegeId">
          {{ c.collegeName }}
        </option>
      </select>

      <select v-model="filters.semesterId" @change="loadList">
        <option :value="null">全部学期</option>
        <option v-for="s in semesters" :key="s.semesterId" :value="s.semesterId">
          {{ s.semesterName }}
        </option>
      </select>
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
            <td colspan="10" class="empty">暂无待中间审核档案信息</td>
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
            <td><a @click="reviewArchiveId = item.archiveId">审核</a></td>
          </tr>
        </tbody>
      </table>
    </div>

    <MiddleReviewModal
      :archive-id="reviewArchiveId"
      @close="reviewArchiveId = null"
      @reviewed="loadList"
    />
  </div>
</template>
