<script setup>
import { onMounted, ref } from 'vue'
import { getMyArchives, deleteArchive } from '@/api/archive'
import ArchiveDetailModal from '@/components/ArchiveDetailModal.vue'
import UploadModal from '@/components/UploadModal.vue'
import {
  STATUS_TAG,
  STATUS_LABEL,
  TYPE_LABEL,
  formatDateTime,
} from '@/utils/archive-labels'

const archives = ref([])
const loading = ref(false)
const detailArchiveId = ref(null)

const uploadModal = ref({
  visible: false,
  task: {},
  type: 'PRE',
  archiveId: null,
})

async function loadList() {
  loading.value = true
  try {
    const res = await getMyArchives()
    archives.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleDelete(archive) {
  if (!confirm(`确定删除 ${archive.courseName}（${archive.className}）的${
    TYPE_LABEL[archive.archiveType]
  }档案？删除后不可恢复。`)) {
    return
  }
  try {
    await deleteArchive(archive.archiveId)
    await loadList()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

function handleResubmit(archive) {
  uploadModal.value = {
    visible: true,
    task: {
      instanceId: archive.instanceId,
      deptId: archive.deptId,
      deptName: archive.deptName,
      courseName: archive.courseName,
      className: archive.className,
    },
    type: archive.archiveType,
    archiveId: archive.archiveId,
  }
}

onMounted(loadList)
</script>

<template>
  <div>
    <div class="crumb">工作台 / <b>查看审核情况</b></div>
    <h1 class="page">查看审核情况</h1>
    <div class="sub">查看本人所有已提交档案及审核状态；已归档不可删除，已驳回可重新提交</div>

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
            <th>审核状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && archives.length === 0">
            <td colspan="8" class="empty">暂无已提交档案</td>
          </tr>
          <tr v-for="archive in archives" :key="archive.archiveId">
            <td>{{ archive.collegeName }}</td>
            <td>{{ archive.courseCode }}</td>
            <td>{{ archive.courseName }} · {{ archive.className }}</td>
            <td>{{ TYPE_LABEL[archive.archiveType] }}</td>
            <td>{{ formatDateTime(archive.submitTime) }}</td>
            <td>v{{ archive.versionNo }}</td>
            <td>
              <span class="tag" :class="STATUS_TAG[archive.status]">
                {{ STATUS_LABEL[archive.status] }}
              </span>
            </td>
            <td>
              <a @click="detailArchiveId = archive.archiveId">查看</a>
              <a v-if="archive.status === 'REJECTED'" @click="handleResubmit(archive)">重新提交</a>
              <a v-if="archive.status !== 'ARCHIVED'" @click="handleDelete(archive)">删除</a>
              <span v-if="archive.status === 'ARCHIVED'">—</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <UploadModal
      :visible="uploadModal.visible"
      :task="uploadModal.task"
      :type="uploadModal.type"
      :archive-id="uploadModal.archiveId"
      @close="uploadModal.visible = false"
      @submitted="loadList"
    />

    <ArchiveDetailModal :archive-id="detailArchiveId" @close="detailArchiveId = null" />
  </div>
</template>
