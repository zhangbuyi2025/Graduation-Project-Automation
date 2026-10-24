<script setup>
import { ref, watch } from 'vue'
import { getArchiveDetail, middleReview } from '@/api/archive'
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
const emit = defineEmits(['close', 'reviewed'])

const detail = ref(null)
const loading = ref(false)
const submitting = ref(false)
const comment = ref('')

watch(
  () => props.archiveId,
  async (id) => {
    detail.value = null
    comment.value = ''
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

async function doReview(result) {
  if (result === 'REJECTED' && !comment.value.trim()) {
    alert('驳回必须填写审核意见')
    return
  }
  submitting.value = true
  try {
    await middleReview(props.archiveId, {
      result,
      comment: comment.value.trim() || '无',
    })
    emit('reviewed')
    emit('close')
  } catch (e) {
    alert(e.message || '审核失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div v-if="props.archiveId" class="modal-mask" @click.self="emit('close')">
    <div class="modal wide">
      <div class="modal-header">
        <h3>
          中间审核详情
          — {{ detail ? detail.archive.courseName + '（' + detail.archive.className + '）' : '' }}
        </h3>
        <button class="close" @click="emit('close')">×</button>
      </div>

      <div v-if="loading" class="empty">加载中…</div>

      <template v-else-if="detail">
        <div class="card" style="margin-bottom:14px">
          <div class="form-grid">
            <div class="form-item">
              <span class="lbl">课程：</span>
              <span>{{ detail.archive.courseCode }} {{ detail.archive.courseName }}</span>
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
            <div class="form-item">
              <span class="lbl">提交人：</span>
              <span>{{ detail.archive.teacherName }}</span>
            </div>
          </div>
        </div>

        <!-- 初审信息 -->
        <div v-if="detail.reviewLogs.length" style="margin-bottom:14px">
          <h3 style="font-size:14px;margin-bottom:8px">初审记录（已通过初审）</h3>
          <div class="tablewrap">
            <table class="tb">
              <thead>
                <tr>
                  <th style="width:14%">层级</th>
                  <th style="width:14%">结果</th>
                  <th>意见</th>
                  <th style="width:18%">时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="log in detail.reviewLogs" :key="log.reviewId">
                  <td>{{ LEVEL_LABEL[log.reviewLevel] }}</td>
                  <td>
                    <span class="tag" :class="log.result === 'APPROVED' ? 'tag-green' : 'tag-red'">
                      {{ log.result === 'APPROVED' ? '通过' : '驳回' }}
                    </span>
                  </td>
                  <td>{{ log.comment }}</td>
                  <td>{{ formatDateTime(log.reviewTime) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <h3 style="font-size:14px;margin-bottom:8px">附件列表（逐项预览）</h3>
        <div class="tablewrap" style="margin-bottom:14px">
          <table class="tb">
            <thead>
              <tr>
                <th style="width:10%">槽位</th>
                <th>文件名</th>
                <th style="width:12%">大小</th>
                <th style="width:12%">预览</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="att in detail.attachments" :key="att.attId">
                <td>{{ att.slotIndex }}<span v-if="att.slotName"> · {{ att.slotName }}</span></td>
                <td>{{ att.originalName }}</td>
                <td>{{ formatSize(att.fileSize) }}</td>
                <td><a @click="previewAttachment(att.attId)">预览</a></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="form-item" style="align-items:flex-start;margin-bottom:14px">
          <span class="lbl" style="margin-top:8px">审核意见：</span>
          <textarea
            v-model="comment"
            rows="3"
            style="flex:1;min-width:300px;padding:8px 10px;border:1px solid var(--border);border-radius:6px;font-size:14px"
            placeholder="驳回时必填，通过时可留空"
          ></textarea>
        </div>

        <div class="toolbar">
          <button class="btn btn-green" :disabled="submitting" @click="doReview('APPROVED')">
            审核通过
          </button>
          <button class="btn btn-danger" :disabled="submitting" @click="doReview('REJECTED')">
            驳回
          </button>
          <button class="btn btn-ghost" @click="emit('close')">取消</button>
        </div>
      </template>
    </div>
  </div>
</template>
