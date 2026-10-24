<script setup>
import { onMounted, ref } from 'vue'
import { getReviewLogs } from '@/api/review'
import { getSemesters } from '@/api/dict'
import { LEVEL_LABEL, formatDateTime } from '@/utils/archive-labels'

const semesters = ref([])
const filters = ref({
  semesterId: null,
  reviewLevel: null,
  result: null,
  keyword: '',
})
const list = ref([])
const loading = ref(false)

async function loadList() {
  loading.value = true
  try {
    const res = await getReviewLogs(filters.value)
    list.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const semRes = await getSemesters()
  semesters.value = semRes.data
  const current = semesters.value.find((s) => s.isCurrent === 1)
  if (current) filters.value.semesterId = current.semesterId
  await loadList()
})
</script>

<template>
  <div>
    <div class="crumb">
      {{ '管理' }} / <b>审批记录</b>
    </div>
    <h1 class="page">审批记录</h1>
    <div class="sub">查询本人权限范围内的所有审核操作记录</div>

    <div class="toolbar">
      <select v-model="filters.semesterId" @change="loadList">
        <option :value="null">全部学期</option>
        <option v-for="s in semesters" :key="s.semesterId" :value="s.semesterId">
          {{ s.semesterName }}
        </option>
      </select>

      <select v-model="filters.reviewLevel" @change="loadList">
        <option :value="null">全部层级</option>
        <option :value="1">初审</option>
        <option :value="2">中间审核</option>
        <option :value="3">终审</option>
      </select>

      <select v-model="filters.result" @change="loadList">
        <option :value="null">全部结果</option>
        <option value="APPROVED">通过</option>
        <option value="REJECTED">驳回</option>
      </select>

      <input
        v-model="filters.keyword"
        type="text"
        placeholder="提交人 / 审核人 / 意见"
        @keyup.enter="loadList"
      >
      <button class="btn btn-primary" @click="loadList">查询</button>
    </div>

    <div class="tablewrap">
      <table class="tb">
        <thead>
          <tr>
            <th>提交人</th>
            <th>审核人</th>
            <th>审核时间</th>
            <th>审核层级</th>
            <th>结果</th>
            <th>审核意见</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && list.length === 0">
            <td colspan="6" class="empty">暂无审批记录</td>
          </tr>
          <tr v-for="log in list" :key="log.reviewId">
            <td>{{ log.teacherName }}</td>
            <td>{{ log.reviewerName }}</td>
            <td>{{ formatDateTime(log.reviewTime) }}</td>
            <td>{{ LEVEL_LABEL[log.reviewLevel] }}</td>
            <td>
              <span class="tag" :class="log.result === 'APPROVED' ? 'tag-green' : 'tag-red'">
                {{ log.result === 'APPROVED' ? '通过' : '驳回' }}
              </span>
            </td>
            <td>{{ log.comment }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
