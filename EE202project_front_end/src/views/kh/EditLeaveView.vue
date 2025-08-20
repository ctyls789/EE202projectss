<template>
  <el-card class="edit-leave-card" v-loading="loading">
    <template #header>
      <div class="card-header">
        <span style="font-size: 20px; font-weight: bold;">
          {{ isEditing ? '✏️ 編輯請假單' : '📄 請假單詳情' }}
        </span>
        <div>
          <el-button v-if="!isEditing" type="primary" icon="Edit" @click="isEditing = true">編輯</el-button>
          <el-button v-if="isEditing" @click="cancelEdit">取消</el-button>
        </div>
      </div>
    </template>

    <el-form v-if="form" :model="form" :rules="rules" ref="formRef" label-width="120px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="員工編號">{{ form.employeeId }}</el-descriptions-item>
        <el-descriptions-item label="員工姓名">{{ form.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="代理人編號">{{ form.agentId || '無' }}</el-descriptions-item>
        <el-descriptions-item label="代理人姓名">{{ form.agentName || '無' }}</el-descriptions-item>
        <el-descriptions-item label="假別">{{ form.leaveTypeName }}</el-descriptions-item>
        <el-descriptions-item label="狀態">{{ form.status }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <el-form-item label="請假事由" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="3"
          :readonly="!isEditing"
          placeholder="概略說明請假事由，上限200字"
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="開始時間" prop="startDatetime">
            <el-date-picker
              v-model="form.startDatetime"
              type="datetime"
              placeholder="選擇開始日期與時間"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DDTHH:mm:ss"
              :readonly="!isEditing"
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="結束時間" prop="endDatetime">
            <el-date-picker
              v-model="form.endDatetime"
              type="datetime"
              placeholder="選擇結束日期與時間"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DDTHH:mm:ss"
              :readonly="!isEditing"
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="請假時數" prop="hours">
        <el-input-number v-model="form.hours" :min="0" :step="0.5" :disabled="!isEditing" />
      </el-form-item>

      <el-form-item v-if="isEditing">
        <el-button type="primary" icon="Check" @click="submitForm">儲存更新</el-button>
        <el-button @click="cancelEdit">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)

const loading = ref(true)
const isEditing = ref(false)
const form = ref(null)
const originalForm = ref(null) // 用於儲存原始資料以便取消編輯

const rules = {
  reason: [{ required: true, message: '請說明請假事由', trigger: 'blur' }],
  startDatetime: [{ required: true, message: '開始日期不為空', trigger: 'change' }],
  endDatetime: [{ required: true, message: '結束日期不為空', trigger: 'change' }],
}

const fetchRecord = async () => {
  loading.value = true
  try {
    const uuid = route.params.uuid
    const res = await api.get(`/api/leave/records/${uuid}`)
    form.value = res.data
    // 深拷貝一份原始資料
    originalForm.value = JSON.parse(JSON.stringify(res.data))
  } catch (error) {
    console.error('獲取請假單詳情失敗:', error)
    ElMessage.error('無法載入請假單資料。')
    router.push('/kh/leave/list')
  } finally {
    loading.value = false
  }
}

const cancelEdit = () => {
  isEditing.value = false
  // 恢復原始資料
  form.value = JSON.parse(JSON.stringify(originalForm.value))
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const uuid = route.params.uuid
        // 後端 Update DTO 需要的欄位
        const updateData = {
          reason: form.value.reason,
          startDatetime: form.value.startDatetime,
          endDatetime: form.value.endDatetime,
          hours: form.value.hours,
        }
        await api.put(`/api/leave/records/${uuid}`, updateData)
        ElMessage.success('請假單已成功更新！')
        router.push('/kh/leave/list')
      } catch (error) {
        console.error('更新失敗', error)
        ElMessage.error('更新失敗，請檢查輸入資料或聯繫管理員。')
      }
    } else {
      ElMessage.warning('請檢查表單必填欄位。')
      return false
    }
  })
}

onMounted(fetchRecord)
</script>

<style scoped>
.edit-leave-card {
  margin: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
