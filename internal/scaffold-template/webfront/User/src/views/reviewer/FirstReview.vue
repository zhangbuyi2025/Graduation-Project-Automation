<script setup>
import { onMounted, ref } from 'vue'
import { getPending } from '@/api/review'
import { getSemesters } from '@/api/dict'
import ReviewDetailModal from '@/components/ReviewDetailModal.vue'
import {
  STATUS_TAG,
  STATUS_LABEL,
  TYPE_LABEL,
  formatDateTime,
} from '@/utils/archive-labels'

const semesters = ref([])
const selectedSemesterId = ref(null)
const list = ref([])
const loading = ref(false)
const reviewArchiveId = ref(null)

async function loadList() {
  loading.value = true
  try {
    const res = await getPending(selectedSemesterId.value)
    list.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const semRes = await getSemesters()
  semesters.value = semRes.data
  const current = semesters.value.find((s) => s.isCurrent === 1)
  if (current) selectedSemesterId.value = current.semesterId
  await loadList()
})
</script>

<template>
  <div>
    <div class="crumb">教研室管理 / <b>初审审核</b></div>
    <h1 class="page">初审审核</h1>
    <div class="sub">待初审的教学档案：逐项预览、可勾选单个槽位驳回或一键驳回</div>

    <div class="toolbar">
      <select v-model="selectedSemesterId" @change="loadList">
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
            <th>课程号</th>
            <th>课程 / 班级</th>
            <th>类型</th>
            <th>提交时间</th>
            <th>版本</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && list.length === 0">
            <td colspan="8" class="empty">暂无待初审档案</td>
          </tr>
          <tr v-for="item in list" :key="item.archiveId">
            <td>{{ item.collegeName }}</td>
            <td>{{ item.courseCode }}</td>
            <td>{{ item.courseName }} · {{ item.className }}</td>
            <td>{{ TYPE_LABEL[item.archiveType] }}</td>
            <td>{{ formatDateTime(item.submitTime) }}</td>
            <td>v{{ item.versionNo }}</td>
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

    <ReviewDetailModal
      :archive-id="reviewArchiveId"
      :level="1"
      @close="reviewArchiveId = null"
      @reviewed="loadList"
    />
  </div>
</template>
