<script setup>
import { ref, watch } from 'vue'
import { getArchiveDetail } from '@/api/archive'
import { previewAttachment } from '@/utils/preview'
import {
  STATUS_TAG,
  STATUS_LABEL,
  TYPE_LABEL,
  LEVEL_LABEL,
  formatSize,
  formatDateTime,
} from '@/utils/archive-labels'

const props = defineProps({
  archiveId: { type: Number, default: null },
})
const emit = defineEmits(['close'])

const detail = ref(null)
const loading = ref(false)

watch(
  () => props.archiveId,
  async (id) => {
    detail.value = null
    if (id) {
      loading.value = true
      try {
        const res = await getArchiveDetail(id)
        detail.value = res.data
      } finally {
        loading.value = false
      }
    }
  },
  { immediate: true },
)
</script>

<template>
  <div v-if="props.archiveId" class="modal-mask" @click.self="emit('close')">
    <div class="modal wide">
      <div class="modal-header">
        <h3>档案详情</h3>
        <button class="close" @click="emit('close')">×</button>
      </div>

      <div v-if="loading" class="empty">加载中…</div>

      <template v-else-if="detail">
        <div class="card" style="margin-bottom:14px">
          <div class="form-grid">
            <div class="form-item">
              <span class="lbl">课程：</span>
              <span>{{ detail.archive.courseCode }} {{ detail.archive.courseName }}
                · {{ detail.archive.className }}</span>
            </div>
            <div class="form-item">
              <span class="lbl">类型：</span>
              <span>{{ TYPE_LABEL[detail.archive.archiveType] }}</span>
            </div>
            <div class="form-item">
              <span class="lbl">版本：</span>
              <span>v{{ detail.archive.versionNo }}</span>
            </div>
            <div class="form-item">
              <span class="lbl">状态：</span>
              <span class="tag" :class="STATUS_TAG[detail.archive.status]">
                {{ STATUS_LABEL[detail.archive.status] }}
              </span>
            </div>
            <div class="form-item">
              <span class="lbl">提交时间：</span>
              <span>{{ formatDateTime(detail.archive.submitTime) }}</span>
            </div>
          </div>
        </div>

        <h3 style="font-size:14px;margin-bottom:8px">附件（当前版本 v{{ detail.archive.versionNo }}）</h3>
        <div class="tablewrap" style="margin-bottom:14px">
          <table class="tb">
            <thead>
              <tr>
                <th style="width:22%">槽位</th>
                <th>文件名</th>
                <th style="width:12%">大小</th>
                <th style="width:16%">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="att in detail.attachments" :key="att.attId">
                <td>
                  槽位{{ att.slotIndex }}
                  <span v-if="att.slotName"> · {{ att.slotName }}</span>
                </td>
                <td>{{ att.originalName }}</td>
                <td>{{ formatSize(att.fileSize) }}</td>
                <td><a @click="previewAttachment(att.attId)">预览</a></td>
              </tr>
            </tbody>
          </table>
        </div>

        <h3 style="font-size:14px;margin-bottom:8px">审核记录</h3>
        <div class="tablewrap">
          <table class="tb">
            <thead>
              <tr>
                <th style="width:12%">层级</th>
                <th style="width:14%">结果</th>
                <th>审核意见</th>
                <th style="width:18%">时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="detail.reviewLogs.length === 0">
                <td colspan="4" class="empty">暂无审核记录</td>
              </tr>
              <tr v-for="log in detail.reviewLogs" :key="log.reviewId">
                <td>{{ LEVEL_LABEL[log.reviewLevel] }}</td>
                <td>
                  <span class="tag" :class="log.result === 'APPROVED' ? 'tag-green' : 'tag-red'">
                    {{ log.result === 'APPROVED' ? '通过' : '驳回' }}
                  </span>
                </td>
                <td>{{ log.comment || '—' }}</td>
                <td>{{ formatDateTime(log.reviewTime) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </div>
  </div>
</template>
