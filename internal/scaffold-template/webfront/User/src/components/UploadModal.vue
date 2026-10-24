<script setup>
import { ref, watch } from 'vue'
import { submitArchive } from '@/api/archive'

const PRE_SLOT_NAMES = ['教学大纲', '教案/课件', '课程目标分析', '实验/实践指导', '考核方案']
const POST_SLOT_NAMES = ['课后反思总结', '期末试卷分析', '课程考核材料']
const MAX_SIZE = 50 * 1024 * 1024

const props = defineProps({
  visible: { type: Boolean, default: false },
  task: { type: Object, default: () => ({}) },
  type: { type: String, default: 'PRE' },
  archiveId: { type: Number, default: null },
  embedded: { type: Boolean, default: false },
})
const emit = defineEmits(['close', 'submitted'])

const slots = ref([])
const submitting = ref(false)

watch(
  () => [props.visible, props.task, props.type],
  ([open]) => {
    if (open) {
      const names = props.type === 'PRE' ? PRE_SLOT_NAMES : POST_SLOT_NAMES
      slots.value = names.map((name, i) => ({ slotIndex: i + 1, slotName: name, file: null }))
      submitting.value = false
    }
  },
  { immediate: true },
)

function addPostSlot() {
  slots.value.push({ slotIndex: slots.value.length + 1, slotName: '', file: null })
}
function removePostSlot(index) {
  slots.value.splice(index, 1)
  slots.value.forEach((s, i) => (s.slotIndex = i + 1))
}

function onChooseFile(event, slot) {
  const file = event.target.files[0]
  event.target.value = ''
  if (!file) return
  if (!/\.pdf$/i.test(file.name)) {
    alert('仅支持 PDF 格式文件')
    return
  }
  if (file.size > MAX_SIZE) {
    alert('单个附件不能超过 50MB')
    return
  }
  slot.file = file
}

function previewLocal(slot) {
  window.open(URL.createObjectURL(slot.file), '_blank')
}

async function doSubmit() {
  for (const slot of slots.value) {
    if (!slot.file) {
      alert(`请完成槽位 ${slot.slotIndex}（${slot.slotName || '未命名'}）的文件上传`)
      return
    }
  }

  const formData = new FormData()
  slots.value.forEach((slot) => formData.append('files', slot.file))
  formData.append('instanceId', props.task.instanceId)
  formData.append('deptId', props.task.deptId)
  formData.append('archiveType', props.type)
  formData.append('slotIndexes', slots.value.map((s) => s.slotIndex).join(','))
  formData.append('slotNames', JSON.stringify(slots.value.map((s) => s.slotName)))
  if (props.archiveId) {
    formData.append('archiveId', props.archiveId)
  }

  submitting.value = true
  try {
    await submitArchive(formData)
    emit('submitted')
    emit('close')
  } catch (e) {
    alert(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div
    v-if="visible"
    class="modal-mask"
    :class="{ 'is-inline': embedded }"
    @click.self="!embedded && emit('close')"
  >
    <div class="modal">
      <div class="modal-header">
        <h3>
          {{ type === 'PRE' ? '提交课前附件' : '提交课后附件' }}
          — {{ task.courseName }}（{{ task.className }}）
        </h3>
        <button class="close" @click="emit('close')">×</button>
      </div>

      <div class="toolbar" style="margin-bottom:6px">
        <span class="lbl">所属教研室：</span>
        <select v-model="task.deptId">
          <option :value="task.deptId">{{ task.deptName }}</option>
        </select>
      </div>

      <div class="pt-note">
        <b>说明：</b>
        <template v-if="type === 'PRE'">
          课前附件固定 5 个槽位，仅支持 PDF，可预览已选文件；全部就绪后提交。
        </template>
        <template v-else>
          课后附件 1–N 个（可按需增加），仅支持 PDF，流程与课前一致。
        </template>
      </div>

      <div class="slots">
        <div v-for="(slot, i) in slots" :key="i" class="slot" :class="{ done: slot.file }">
          <div class="sn">
            槽位 {{ slot.slotIndex }}
            <a v-if="type === 'POST'" @click="removePostSlot(i)" style="margin-left:6px">×</a>
          </div>
          <input
            v-if="type === 'POST'"
            v-model="slot.slotName"
            type="text"
            placeholder="槽位名称"
            style="min-width:0;width:100%;margin:6px 0"
          />
          <div class="fn">{{ slot.file ? slot.file.name : slot.slotName || '未命名' }}</div>
          <label class="btn btn-ghost btn-sm" style="margin-bottom:6px">
            选择文件
            <input
              type="file"
              accept="application/pdf,.pdf"
              style="display:none"
              @change="onChooseFile($event, slot)"
            >
          </label>
          <div>
            <span v-if="slot.file" class="tag tag-green">已选择</span>
            <span v-else class="tag tag-gray">未上传</span>
          </div>
          <div v-if="slot.file" style="margin-top:6px">
            <a @click="previewLocal(slot)">预览</a>
          </div>
        </div>
      </div>

      <div v-if="type === 'POST'" style="margin:4px 0 12px">
        <button class="btn btn-ghost btn-sm" @click="addPostSlot">+ 增加附件</button>
      </div>

      <div class="toolbar" style="margin-top:10px">
        <button class="btn btn-primary" :disabled="submitting" @click="doSubmit">
          {{ submitting ? '提交中…' : '提交' }}
        </button>
        <button class="btn btn-ghost" @click="emit('close')">取消</button>
      </div>
    </div>
  </div>
</template>
