<template>
  <el-card class="leave-application-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold;">📝 請假申請表單</span>
    </template>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="員工編號" prop="employeeId">
            <el-input v-model="form.employeeId" placeholder="請輸入您的員工編號" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="職務代理人編號" prop="agentId">
            <el-input v-model="form.agentId" placeholder="請輸入代理人編號 (可選填)" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="假別" prop="leaveTypeId">
        <el-select v-model="form.leaveTypeId" placeholder="請選擇假別" style="width: 100%;">
          <el-option
            v-for="type in leaveTypes"
            :key="type.id"
            :label="type.name"
            :value="type.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="請假事由" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="3"
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
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="請假時數" prop="hours">
        <el-input-number v-model="form.hours" :min="0" :step="0.5" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="Check" @click="submitForm">提交申請</el-button>
        <el-button @click="goBack">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null) // 用於表單驗證

// 表單資料
const form = ref({
  employeeId: '',
  agentId: null, // agentId 是可選的，可以設為 null
  leaveTypeId: null,
  reason: '',
  startDatetime: '',
  endDatetime: '',
  hours: 0.0,
})

// 假別選項
const leaveTypes = ref([])

// 表單驗證規則
const rules = {
  employeeId: [{ required: true, message: '員工編號不能為空', trigger: 'blur' }],
  leaveTypeId: [{ required: true, message: '請選擇假別', trigger: 'change' }],
  reason: [{ required: true, message: '請說明請假事由', trigger: 'blur' }],
  startDatetime: [{ required: true, message: '開始日期不為空', trigger: 'change' }],
  endDatetime: [{ required: true, message: '結束日期不為空', trigger: 'change' }],
}

// 元件掛載後，從後端獲取假別資料
onMounted(async () => {
  try {
    const res = await api.get('/api/leave/form-data') // *** 對應新的 API 端點 ***
    leaveTypes.value = res.data.leaveTypes
  } catch (error) {
    console.error('獲取假別資料失敗', error)
    ElMessage.error('無法載入頁面所需資料，請稍後再試。')
  }
})

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await api.post('/api/leave/records', form.value) // *** 對應新的 API 端點 ***
        ElMessage.success('請假申請已成功送出！')
        // 導航到請假列表頁
        router.push('/kh/leave/list')
      } catch (error) {
        console.error('新增失敗', error)
        ElMessage.error('申請失敗，請檢查輸入資料或聯繫管理員。')
      }
    } else {
      ElMessage.warning('請檢查表單必填欄位。')
      return false
    }
  })
}

const goBack = () => {
  // 根據你的專案規劃，可以回到上一頁或某個固定頁面
  router.back()
}
</script>

<style scoped>
.leave-application-card {
  margin: 20px;
}
</style>
