<template>
  <el-card class="leave-list-card">
    <template #header>
      <div class="card-header">
        <span style="font-size: 20px; font-weight: bold;">📋 請假紀錄列表</span>
        <el-button
          type="primary"
          icon="Plus"
          @click="$router.push('/kh/leave-application')"
        >
          新增請假申請
        </el-button>
      </div>
    </template>

    <el-table :data="leaveRecords" stripe border v-loading="loading" style="width: 100%">
      <el-table-column prop="employeeName" label="員工姓名" width="120" />
      <el-table-column prop="leaveTypeName" label="假別" width="100" />
      <el-table-column prop="reason" label="事由" min-width="200" show-overflow-tooltip />
      <el-table-column prop="startDatetime" label="開始時間" width="180" />
      <el-table-column prop="endDatetime" label="結束時間" width="180" />
      <el-table-column prop="hours" label="時數" width="80" />
      <el-table-column prop="status" label="狀態" width="100" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            icon="Edit"
            circle
            @click="editRecord(row.uuid)"
            title="編輯"
          />
          <el-button
            type="danger"
            icon="Delete"
            circle
            @click="deleteRecord(row.uuid)"
            title="刪除"
          />
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const leaveRecords = ref([])
const loading = ref(true)

const fetchLeaveRecords = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/leave/records')
    leaveRecords.value = res.data
  } catch (error) {
    console.error('獲取請假列表失敗:', error)
    ElMessage.error('無法載入請假列表。')
  } finally {
    loading.value = false
  }
}

const editRecord = (uuid) => {
  // 導航到編輯頁面
  router.push(`/kh/leave/edit/${uuid}`)
}

const deleteRecord = async (uuid) => {
  await ElMessageBox.confirm('確定要刪除這筆請假紀錄嗎？', '警告', { type: 'warning' })
  await api.delete(`/api/leave/records/${uuid}`)
  ElMessage.success('刪除成功！')
  await fetchLeaveRecords() // 重新獲取列表
}

onMounted(fetchLeaveRecords)
</script>

<style scoped>
.leave-list-card {
  margin: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
