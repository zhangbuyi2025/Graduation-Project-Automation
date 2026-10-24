<script setup>
import { computed, onMounted, ref } from 'vue'
import { getTasks } from '@/api/archive'
import { getSemesters } from '@/api/dict'
import ArchiveDetailModal from '@/components/ArchiveDetailModal.vue'
import UploadModal from '@/components/UploadModal.vue'
import { STATUS_TAG, STATUS_LABEL } from '@/utils/archive-labels'

const semesters = ref([])
const tasks = ref([])
const currentSemesterId = ref(null)
const selectedSemesterId = ref(null)
const loading = ref(false)

const isCurrent = computed(() => selectedSemesterId.value === currentSemesterId.value)

const detailArchiveId = ref(null)

// ============ 常驻提交面板（课前 / 课后，始终显示在表格下方） ============
const preTaskId = ref(null)
const postTaskId = ref(null)

const preTask = computed(
  () => tasks.value.find((t) => t.instanceId === preTaskId.value) || null,
)
const postTask = computed(
  () => tasks.value.find((t) => t.instanceId === postTaskId.value) || null,
)

// 已提交（审核中 / 已归档）不可重复提交，仅 REJECTED 可重新提交
function isSubmitted(task, type) {
  const s = type === 'PRE' ? task.preStatus : task.postStatus
  return !!s && s !== 'REJECTED'
}
const preSubmitted = computed(() => (preTask.value ? isSubmitted(preTask.value, 'PRE') : false))
const postSubmitted = computed(() => (postTask.value ? isSubmitted(postTask.value, 'POST') : false))

function scrollToPanel(id) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
// 表格中点击"重新提交"：选中该任务并滚动到对应提交区
function selectPre(task) {
  preTaskId.value = task.instanceId
  scrollToPanel('pre-panel')
}
function selectPost(task) {
  postTaskId.value = task.instanceId
  scrollToPanel('post-panel')
}

async function loadTasks() {
  loading.value = true
  try {
    const res = await getTasks(selectedSemesterId.value)
    tasks.value = res.data
    // 默认选中第一个任务；当前选中已不在列表中时落到第一个
    if (tasks.value.length && !tasks.value.some((t) => t.instanceId === preTaskId.value)) {
      preTaskId.value = tasks.value[0].instanceId
    }
    if (tasks.value.length && !tasks.value.some((t) => t.instanceId === postTaskId.value)) {
      postTaskId.value = tasks.value[0].instanceId
    }
  } finally {
    loading.value = false
  }
}

// ============ 表格展示辅助 ============
function statusTag(status) {
  return STATUS_TAG[status] || 'tag-gray'
}
function statusText(status) {
  return status ? STATUS_LABEL[status] : ''
}
function preTagClass(task) {
  return task.preStatus ? statusTag(task.preStatus) : 'tag-amber'
}
function preTagText(task) {
  if (!task.preStatus) return '待提交'
  if (task.preStatus === 'PENDING') return '已提交·待初审'
  return statusText(task.preStatus)
}
function postTagClass(task) {
  return task.postStatus ? statusTag(task.postStatus) : 'tag-gray'
}
function postTagText(task) {
  if (!task.postStatus) return '待提交'
  if (task.postStatus === 'PENDING') return '已提交·待初审'
  return statusText(task.postStatus)
}
function overallTag(task) {
  if (task.preStatus === 'ARCHIVED' && task.postStatus === 'ARCHIVED') {
    return { cls: 'tag-green', text: '已归档' }
  }
  if (task.preStatus === 'REJECTED' || task.postStatus === 'REJECTED') {
    return { cls: 'tag-red', text: '已驳回' }
  }
  if (task.preStatus || task.postStatus) {
    return { cls: 'tag-blue', text: '审核中' }
  }
  return { cls: 'tag-blue', text: '待提交' }
}

onMounted(async () => {
  const semRes = await getSemesters()
  semesters.value = semRes.data
  const current = semesters.value.find((s) => s.isCurrent === 1)
  if (current) {
    currentSemesterId.value = current.semesterId
    selectedSemesterId.value = current.semesterId
  } else if (semesters.value.length) {
    selectedSemesterId.value = semesters.value[0].semesterId
  }
  await loadTasks()
})
</script>

<template>
  <div>
    <div class="crumb">工作台 / <b>提交教学附件</b></div>
    <h1 class="page">提交教学附件</h1>
    <div class="sub">仅可提交当前学期；历史学期仅可查看，提交操作已置灰</div>

    <div class="toolbar">
      <select v-model="selectedSemesterId" @change="loadTasks">
        <option v-for="s in semesters" :key="s.semesterId" :value="s.semesterId">
          {{ s.semesterName }}{{ s.isCurrent === 1 ? '（当前）' : '（历史·只读）' }}
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
            <th>课前附件</th>
            <th>课后附件</th>
            <th>教研室</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && tasks.length === 0">
            <td colspan="8" class="empty">暂无待提交任务</td>
          </tr>
          <tr v-for="task in tasks" :key="task.instanceId">
            <td>{{ task.collegeName }}</td>
            <td>{{ task.courseCode }}</td>
            <td>{{ task.courseName }} · {{ task.className }}</td>
            <td><span class="tag" :class="preTagClass(task)">{{ preTagText(task) }}</span></td>
            <td><span class="tag" :class="postTagClass(task)">{{ postTagText(task) }}</span></td>
            <td>{{ task.deptName }}</td>
            <td><span class="tag" :class="overallTag(task).cls">{{ overallTag(task).text }}</span></td>
            <td>
              <template v-if="isCurrent">
                <a v-if="task.preStatus === 'REJECTED'" @click="selectPre(task)">重新提交课前</a>
                <a v-else-if="task.preStatus" @click="detailArchiveId = task.preArchiveId">查看课前</a>
                <span v-else class="oper-muted">待提交</span>

                <a v-if="task.postStatus === 'REJECTED'" @click="selectPost(task)">重新提交课后</a>
                <a v-else-if="task.postStatus" @click="detailArchiveId = task.postArchiveId">查看课后</a>
                <span v-else class="oper-muted">待提交</span>
              </template>
              <template v-else>
                <a v-if="task.preArchiveId" @click="detailArchiveId = task.preArchiveId">查看课前</a>
                <a v-if="task.postArchiveId" @click="detailArchiveId = task.postArchiveId">查看课后</a>
                <span v-if="!task.preArchiveId && !task.postArchiveId">—</span>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ============ 课前附件提交区（常驻显示） ============ -->
    <div id="pre-panel" class="form-panel">
      <div class="panel-head">
        <h3>提交课前附件</h3>
        <label class="lbl">课程任务：
          <select v-model="preTaskId" :disabled="!tasks.length">
            <option v-for="t in tasks" :key="t.instanceId" :value="t.instanceId">
              {{ t.courseName }}（{{ t.className }}）
            </option>
          </select>
        </label>
      </div>
      <div v-if="!tasks.length" class="empty-panel">暂无待提交任务</div>
      <div v-else-if="preSubmitted" class="empty-panel">
        该任务课前附件已{{ STATUS_LABEL[preTask.preStatus] }}，无需重复提交；如需查看请点击表格中的「查看课前」。
      </div>
      <UploadModal
        v-else
        embedded
        :visible="true"
        :task="preTask"
        type="PRE"
        :archive-id="preTask ? preTask.preArchiveId : null"
        @submitted="loadTasks"
      />
    </div>

    <!-- ============ 课后附件提交区（常驻显示） ============ -->
    <div id="post-panel" class="form-panel">
      <div class="panel-head">
        <h3>提交课后附件</h3>
        <label class="lbl">课程任务：
          <select v-model="postTaskId" :disabled="!tasks.length">
            <option v-for="t in tasks" :key="t.instanceId" :value="t.instanceId">
              {{ t.courseName }}（{{ t.className }}）
            </option>
          </select>
        </label>
      </div>
      <div v-if="!tasks.length" class="empty-panel">暂无待提交任务</div>
      <div v-else-if="postSubmitted" class="empty-panel">
        该任务课后附件已{{ STATUS_LABEL[postTask.postStatus] }}，无需重复提交；如需查看请点击表格中的「查看课后」。
      </div>
      <UploadModal
        v-else
        embedded
        :visible="true"
        :task="postTask"
        type="POST"
        :archive-id="postTask ? postTask.postArchiveId : null"
        @submitted="loadTasks"
      />
    </div>

    <ArchiveDetailModal :archive-id="detailArchiveId" @close="detailArchiveId = null" />
  </div>
</template>

<style scoped>
.form-panel {
  margin-top: 22px;
  background: #fff;
  border: 1px solid var(--border, #e6eaf0);
  border-radius: 16px;
  padding: 20px 22px;
  box-shadow: 0 1px 3px rgba(20, 45, 80, 0.05), 0 12px 28px rgba(20, 45, 80, 0.06);
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--line, #eef1f6);
}
.panel-head h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--ink, #1c2733);
  display: flex;
  align-items: center;
  gap: 8px;
}
.panel-head h3::before {
  content: "";
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: linear-gradient(180deg, var(--primary, #143a68), var(--accent, #12a5a0));
}
.panel-head select {
  padding: 7px 12px;
  border: 1px solid var(--border, #e6eaf0);
  border-radius: 10px;
  background: #fff;
  color: var(--ink, #1c2733);
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  font-family: inherit;
}
.panel-head select:focus {
  border-color: var(--accent, #12a5a0);
  box-shadow: 0 0 0 3px rgba(18, 165, 160, 0.14);
}
.empty-panel {
  border: 1.5px dashed #c9d4e2;
  border-radius: 12px;
  padding: 30px 26px;
  text-align: center;
  color: var(--muted, #7a8699);
  background: #f8fafc;
  font-size: 13px;
  letter-spacing: 0.3px;
}
.oper-muted {
  color: var(--muted, #7a8699);
}
</style>
