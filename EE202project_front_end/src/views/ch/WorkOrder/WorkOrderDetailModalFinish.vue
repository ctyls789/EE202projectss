<template>
  <el-card class="work-order-detail-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold">📄 工單詳情</span>
    </template>

    <div v-if="!workOrder || !workOrder.woId">
      <el-empty description="尚未選擇工單或資料載入中" />
      <div class="action-buttons">
        <el-button @click="$emit('back')">返回列表</el-button>
      </div>
    </div>

    <div v-else>
      <!-- 工單基本資料 -->
      <el-descriptions :column="2" border class="detail-descriptions">
        <el-descriptions-item label="工單ID">{{ workOrder.woId }}</el-descriptions-item>
        <el-descriptions-item label="工單編號">{{ workOrder.woNumber }}</el-descriptions-item>
        <el-descriptions-item label="需求數量">{{ workOrder.requiredQuantity }}</el-descriptions-item>
        <el-descriptions-item label="狀態">
          <el-tag :type="statusTagType(workOrder.status)">{{ workOrder.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="建立時間">{{ workOrder.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新時間">{{ workOrder.updatedAt }}</el-descriptions-item>
      </el-descriptions>

      <!-- 材料明細 -->
      <el-card class="material-list-card" style="margin-top: 20px;">
        <template #header>
          <span style="font-size: 16px; font-weight: bold">材料明細</span>
        </template>
        <el-table :data="workOrderMaterials" border stripe style="width: 100%">
          <el-table-column prop="materialId" label="材料ID" width="120" />
          <el-table-column prop="materialName" label="材料名稱" />
          <el-table-column prop="requestedQuantity" label="需求數量" />
          <el-table-column prop="issuedQuantity" label="已發料數量" />
          <el-table-column prop="status" label="狀態" />
        </el-table>
        <el-empty v-if="workOrderMaterials.length === 0" description="沒有材料明細" />
      </el-card>

      <!-- 功能按鈕 -->
      <div class="action-buttons">
        <el-button type="success" @click="showProductionDialog = true">開始生產</el-button>
        <el-button @click="$emit('back')">返回列表</el-button>
      </div>

      <!-- 生產彈窗 -->
      <el-dialog 
        v-model="showProductionDialog" 
        title="開始生產" 
        width="900px" 
        @close="resetProductionDialog"
      >
        <!-- 狀態顯示（只顯示，不可選） -->
        <div style="margin-bottom: 16px;">
          <span>機台狀態：{{ getLabelByCode(selectedStatus) }}</span>
          <el-button type="info" size="small" @click="fetchRunningMachines">刷新</el-button>
        </div>

        <el-alert
          :title="`工單：${workOrder.woNumber}`"
          :description="`產品：${workOrder.materialName || '未知產品'}，需求數量：${workOrder.requiredQuantity}`"
          type="info"
          show-icon
          :closable="false"
          style="margin-bottom: 20px;"
        />

        <el-row :gutter="20">
          <!-- 機台列表只顯示ID、名稱、加入 -->
          <el-col :span="12">
            <el-card class="machine-selection-card">
              <template #header>
                <div class="card-header">
                  <span>可用的機台</span>
                  <el-button type="info" size="small" @click="fetchRunningMachines">刷新</el-button>
                </div>
              </template>
              <el-table :data="runningMachines" style="width: 100%;" max-height="300">
                <el-table-column prop="machineId" label="機台ID" width="120" />
                <el-table-column prop="machineName" label="機台名稱" />
                <el-table-column label="操作" width="80">
                  <template #default="scope">
                    <el-button 
                      link 
                      type="primary" 
                      size="small" 
                      @click="addMachineToQueue(scope.row)" 
                      :disabled="isMachineInQueue(scope.row.machineId)"
                    >
                      加入
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="runningMachines.length === 0" description="沒有可用機台" />
            </el-card>
          </el-col>

          <!-- 生產佇列 -->
          <el-col :span="12">
            <el-card class="production-queue-card">
              <template #header>
                <div class="card-header">
                  <span>生產佇列</span>
                </div>
              </template>
              <el-table :data="productionQueue" style="width: 100%;" max-height="300">
                <el-table-column prop="machineName" label="機台名稱" />
                <el-table-column label="生產數量" width="120">
                  <template #default="scope">
                    <el-input-number
                      v-model="scope.row.quantityToProduce"
                      :min="1"
                      :max="workOrder.requiredQuantity"
                      size="small"
                    />
                  </template>
                </el-table-column>
                <!-- 生產佇列操作欄位：確認、移除 -->
                <el-table-column label="操作" width="160">
                  <template #default="scope">
                    <el-button
                      type="primary"
                      size="small"
                      @click="confirmMachineQuantity(scope.row)"
                      :disabled="scope.row.confirmed"
                    >
                      確認
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      @click="removeMachineFromQueue(scope.row.machineId)"
                    >
                      移除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-if="productionQueue.length === 0" description="請從左側加入機台" />
            </el-card>
          </el-col>
        </el-row>

        <template #footer>
          <el-button type="primary" @click="showFinishForm = true">完成工單</el-button>
          <el-button @click="showProductionDialog = false">關閉</el-button>
        </template>
      </el-dialog>

      <!-- 完成工單表單 -->
      <WorkOrderFinishForm 
        v-model="showFinishForm"
        :work-order="workOrder"
        @close="showFinishForm = false"
        @submit="handleProductionComplete"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'
import WorkOrderFinishForm from './WorkOrderFinishForm.vue'

const workOrderMaterials = ref([])
const showFinishForm = ref(false)
const showProductionDialog = ref(false)
const runningMachines = ref([])
const productionQueue = ref([])
const currentProductionMachine = ref(null)
const machineStatusOptions = ref([])
const selectedStatus = ref('RUN') // 預設運行中

const props = defineProps({
  workOrder: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['back'])

// 取得工單材料
const fetchWorkOrderMaterials = async () => {
  if (!props.workOrder.woId) {
    workOrderMaterials.value = []
    return
  }
  try {
    const res = await api.get(`/api/workorder/${props.workOrder.woId}/materials`)
    workOrderMaterials.value = Array.isArray(res.data) ? res.data : []
  } catch {
    workOrderMaterials.value = []
  }
}

// 取得所有機台狀態選項
const fetchMachineStatusOptions = async () => {
  try {
    const statusRes = await api.get('/api/status-codes/machine')
    machineStatusOptions.value = Array.isArray(statusRes.data) ? statusRes.data : []
    // 預設選 RUN
    if (!selectedStatus.value && machineStatusOptions.value.length > 0) {
      selectedStatus.value = machineStatusOptions.value[0].status_code
    }
  } catch (error) {
    machineStatusOptions.value = []
  }
}

// 取得指定狀態的機台
const fetchRunningMachines = async () => {
  try {
    const statusLabel = getLabelByCode(selectedStatus.value) || selectedStatus.value;
    const response = await api.get('/api/machines', {
      params: { statusFilter: statusLabel }
    })
    runningMachines.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    ElMessage.error('載入機台失敗')
    runningMachines.value = []
  }
}

// 依 status_code 找中文 label
const getLabelByCode = (code) => {
  const item = machineStatusOptions.value.find(o => o.status_code === code)
  return item ? item.status_label : code
}

const isMachineInQueue = (machineId) => productionQueue.value.some(m => m.machineId === machineId)

const addMachineToQueue = (machine) => {
  if (!isMachineInQueue(machine.machineId)) {
    productionQueue.value.push({
      ...machine,
      quantityToProduce: props.workOrder.requiredQuantity || 1,
      confirmed: false
    })
  } else {
    ElMessage.warning('該機台已在生產佇列中。')
  }
}

const removeMachineFromQueue = (machineId) => {
  productionQueue.value = productionQueue.value.filter(m => m.machineId !== machineId)
}

const startProduction = (machine) => {
  if (machine.quantityToProduce === 0) {
    ElMessage.warning('生產數量不能為0。')
    return
  }
  currentProductionMachine.value = machine
  showFinishForm.value = true
}

const handleProductionComplete = async (formData) => {
  showFinishForm.value = false
  try {
    await api.post(`/api/workorder/${props.workOrder.woId}/complete`, {
      quantityDone: formData.quantityDone,
      quantityFailed: formData.quantityFailed
    })
    if (currentProductionMachine.value) {
      const machineId = currentProductionMachine.value.machineId
      const machineRes = await api.get(`/api/machines/${machineId}`)
      const machine = machineRes.data
      machine.mstatus = 'STOP'
      await api.put(`/api/machines/${machineId}`, machine)
    }
    ElMessage.success(`工單完成！成功：${formData.quantityDone}，失敗：${formData.quantityFailed}`)
    emit('back')
  } catch (error) {
    ElMessage.error('工單完成處理失敗')
    emit('back')
  }
}

// 狀態標籤顏色
const statusTagType = (status) => {
  switch(status) {
    case '已完成': return 'success'
    case '進行中': return 'warning'
    case '取消': return 'danger'
    default: return 'info'
  }
}

watch(() => props.workOrder.woId, (newWoId) => {
  if (newWoId) fetchWorkOrderMaterials()
  else workOrderMaterials.value = []
}, { immediate: true })

onMounted(() => {
  fetchMachineStatusOptions()
  fetchRunningMachines()
  if (props.workOrder.woId) fetchWorkOrderMaterials()
})

// 監聽狀態選擇自動刷新機台
watch(selectedStatus, () => {
  fetchRunningMachines()
})

const confirmMachineQuantity = (machine) => {
  // 計算所有已確認機台的生產數量
  const totalConfirmed = productionQueue.value
    .filter(m => m.confirmed)
    .reduce((sum, m) => sum + (m.quantityToProduce || 0), 0)
  // 加上本次要確認的數量
  const newTotal = totalConfirmed + (machine.quantityToProduce || 0)
  if (newTotal > props.workOrder.requiredQuantity) {
    ElMessage.warning('機台總生產數量不能超過需求數量')
    return
  }
  machine.confirmed = true
}
</script>

<style scoped>
.work-order-detail-card { width: 100%; max-width: 1200px; margin: 0 auto; }
.detail-descriptions { margin-bottom: 30px; }
.material-list-card { margin-top: 20px; }
.action-buttons, .production-actions { display: flex; justify-content: center; gap: 15px; padding-top: 20px; border-top: 1px solid #ebeef5; }
.production-actions { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ebeef5; }
.machine-selection-card, .production-queue-card { height: 400px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; }
@media (max-width: 768px) { .work-order-detail-card { max-width: 100%; margin: 0 10px; } }
</style>
