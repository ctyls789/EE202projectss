<template>
  <el-card class="workorder-list-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold">📋 工單列表</span>
    </template>

    <el-table v-loading="loading" :data="workOrderList" stripe border style="width: 100%">
      <el-table-column prop="woId" label="工單ID" width="100" />
      <el-table-column prop="woNumber" label="工單編號" />
      <el-table-column prop="status" label="狀態" width="120" >
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="viewDetail(row)">查看詳情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && workOrderList.length === 0" description="📂 沒有工單資料" />
    <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon style="margin-top: 16px;" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'

const workOrderList = ref([])
const loading = ref(false)
const errorMsg = ref('')

const emit = defineEmits(['view-detail'])

const fetchWorkOrders = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await api.get('/api/workorder')
    workOrderList.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    errorMsg.value = '載入工單失敗，請稍後再試'
    workOrderList.value = []
  } finally {
    loading.value = false
  }
}

const viewDetail = (workOrder) => {
  emit('view-detail', workOrder)
}

const statusTagType = (status) => {
  switch(status) {
    case '已完成': return 'success'
    case '進行中': return 'warning'
    case '取消': return 'danger'
    default: return 'info'
  }
}

onMounted(fetchWorkOrders)

defineExpose({
  fetchWorkOrders
})
</script>

<style scoped>
.workorder-list-card {
  width: 100%;
}
</style>