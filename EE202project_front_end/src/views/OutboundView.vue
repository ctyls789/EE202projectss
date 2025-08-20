<template>
  <div class="outbound-view">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>工單列表</span>
             
            </div>
          </template>
          <el-table :data="workOrders" style="width: 100%" v-loading="loadingWorkOrders">
            <el-table-column prop="woId" label="ID" width="80"></el-table-column>
            <el-table-column prop="woNumber" label="工單號碼"></el-table-column>
            <el-table-column prop="materialName" label="生產產品"></el-table-column>
            <el-table-column prop="requiredQuantity" label="需求數量" width="120"></el-table-column>
            <el-table-column prop="status" label="狀態" width="100">
              <template #default="scope">
                <el-tag :type="statusTagType(scope.row.status)">{{ translateStatus(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button link type="primary" size="small" @click="viewWorkOrderDetails(scope.row)">詳情</el-button>
                <el-button link type="danger" size="small" @click="deleteWorkOrder(scope.row.woId)">刪除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>已完成的工單</span>
               <div>
                <el-button type="success" @click="exportToWord">匯出 Word</el-button>
                <el-button type="primary" @click="exportToMarkdown">匯出 Markdown</el-button>
              </div>
            </div>
          </template>
          <div v-if="completedWorkOrders.length > 0">
            <el-table :data="completedWorkOrders" style="width: 100%">
                <el-table-column prop="woNumber" label="工單號碼"></el-table-column>
                <el-table-column prop="materialName" label="生產產品"></el-table-column>
                <el-table-column prop="requiredQuantity" label="完成數量"></el-table-column>
                <el-table-column prop="status" label="狀態" width="100">
                <template #default="scope">
                  <el-tag :type="statusTagType(scope.row.status)">{{ translateStatus(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
                <el-table-column prop="updatedAt" label="完成時間"></el-table-column>
                 <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button link type="primary" size="small" @click="viewWorkOrderDetails(scope.row)">詳情</el-button>
              </template>
            </el-table-column>
            </el-table>
          </div>
          <div v-else>
            <p>目前沒有已完成的工單。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增工單 Dialog -->
    <el-dialog v-model="showAddWorkOrderDialog" title="新增工單" width="600">
      <el-form :model="newWorkOrder" :rules="addWorkOrderRules" ref="addWorkOrderFormRef" label-width="120px">
        <el-form-item label="工單號碼" prop="woNumber">
          <el-input v-model="newWorkOrder.woNumber"></el-input>
        </el-form-item>
        <el-form-item label="生產產品" prop="materialId">
          <el-select v-model.number="newWorkOrder.materialId" placeholder="請選擇產品" style="width: 100%;" filterable v-if="availableProducts.length > 0">
            <el-option
              v-for="mat in availableProducts"
              :key="mat.materialId"
              :label="mat.materialName"
              :value="mat.materialId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="需求數量" prop="requiredQuantity">
          <el-input-number v-model="newWorkOrder.requiredQuantity" :min="1"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddWorkOrderDialog = false">取消</el-button>
          <el-button type="primary" @click="addWorkOrder">
            派出工單
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 工單詳情 Dialog -->
    <el-dialog v-model="showWorkOrderDetailsDialog" title="工單詳情" width="800">
      <div v-if="selectedWorkOrder">
        <el-steps :active="activeStep" :process-status="stepStatus" finish-status="success" align-center style="margin-bottom: 20px;">
          <el-step title="派出工單" :icon="Edit"></el-step>
          <el-step title="檢查庫存" :icon="Upload"></el-step>
          <el-step title="生產中" :icon="VideoPlay"></el-step>
          <el-step title="已完成" :icon="CircleCheck"></el-step>
        </el-steps>

        <p><strong>工單號碼:</strong> {{ selectedWorkOrder.woNumber }}</p>
        <p><strong>生產產品:</strong> {{ selectedWorkOrder.materialName }}</p>
        <p><strong>需求數量:</strong> {{ selectedWorkOrder.requiredQuantity }}</p>
        <p><strong>狀態:</strong> {{ translateStatus(selectedWorkOrder.status) }}</p>
        <p><strong>建立時間:</strong> {{ selectedWorkOrder.createdAt }}</p>

        <h4>領料明細 (BOM):</h4>
        <el-table :data="selectedWorkOrderMaterials" style="width: 100%">
          <el-table-column prop="materialName" label="物料名稱"></el-table-column>
          <el-table-column prop="requestedQuantity" label="需求數量"></el-table-column>
          <el-table-column prop="issuedQuantity" label="已發料數量"></el-table-column>
          <el-table-column prop="status" label="狀態">
             <template #default="scope">
                <el-tag :type="statusTagType(scope.row.status)">{{ translateStatus(scope.row.status) }}</el-tag>
              </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showWorkOrderDetailsDialog = false">關閉</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import http from '../http-common'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Edit, Upload, VideoPlay, CircleCheck } from '@element-plus/icons-vue'


interface Material {
  materialId: number;
  materialName: string;
  stockCurrent: number;
  unit: string;
  materialDescription?: string;
  location?: string;
  safetyStock?: number | null;
  reorderLevel?: number | null;
  active?: boolean;
  materialType?: string;
}

onMounted(() => {
  console.log('OutboundView component mounted. Fetching initial data...');
  fetchWorkOrders();
});

interface WorkOrderDto {
  woId?: number;
  woNumber: string;
  materialId: number;
  materialName: string;
  requiredQuantity: number;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

interface WorkOrderMaterialDto {
  woMaterialId?: number;
  materialId: number;
  materialName: string;
  requestedQuantity: number;
  issuedQuantity?: number;
  status: string;
}

interface WorkOrderCreateDto {
  woNumber: string;
  materialId: number | null;
  requiredQuantity: number;
  status: string;
}

const workOrders = ref<WorkOrderDto[]>([])
const loadingWorkOrders = ref(false)
const showAddWorkOrderDialog = ref(false)
const newWorkOrder = ref<WorkOrderCreateDto>({
  woNumber: `WO-${Date.now()}`,
  materialId: null, 
  requiredQuantity: 1,
  status: 'PENDING', 
})

const addWorkOrderFormRef = ref<FormInstance>()

const addWorkOrderRules = ref<FormRules>({
  woNumber: [
    { required: true, message: '請輸入工單號碼', trigger: 'blur' },
  ],
  materialId: [
    { required: true, message: '請選擇生產產品', trigger: 'change' },
  ],
  requiredQuantity: [
    { required: true, message: '請輸入需求數量', trigger: 'blur' },
    { type: 'number', min: 1, message: '需求數量必須大於0', trigger: 'blur' },
  ],
})

const showWorkOrderDetailsDialog = ref(false)
const selectedWorkOrder = ref<WorkOrderDto | null>(null)
const selectedWorkOrderMaterials = ref<WorkOrderMaterialDto[]>([])

const availableProducts = ref<Material[]>([])

const activeStep = ref(0);
const stepStatus = ref<'process' | 'error' | 'success' | 'wait'>('process');

const API_BASE_URL = '/workorder'
const MATERIAL_API_BASE_URL = '/depot/materials'

const completedWorkOrders = computed(() => {
  return workOrders.value.filter(wo => wo.status === 'COMPLETED');
});

const statusMap: Record<string, string> = {
  PENDING: '待處理',
  IN_PROGRESS: '進行中',
  OUT_OF_STOCK: '庫存不足',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
};

const translateStatus = (status: string) => statusMap[status] || status;

const statusTagType = (status: string) => {
  switch (status) {
    case 'COMPLETED': return 'success';
    case 'IN_PROGRESS': return 'primary';
    case 'OUT_OF_STOCK': return 'danger';
    case 'PENDING': return 'warning';
    default: return 'info';
  }
};

// Fetch work orders
const fetchWorkOrders = async () => {
  loadingWorkOrders.value = true
  try {
    const response = await http.get<WorkOrderDto[]>(API_BASE_URL)
    workOrders.value = response.data.sort((a, b) => (b.woId ?? 0) - (a.woId ?? 0)); // Sort by ID descending
  } catch (error: any) {
    console.error('Error fetching work orders:', error)
    ElMessage.error(`獲取工單失敗: ${error.response?.data?.message || error.message}`)
  } finally {
    loadingWorkOrders.value = false
  }
}

// Fetch all available products (materials of type 'PRODUCT')
const fetchProducts = async () => {
  try {
    const response = await http.get<Material[]>(`${MATERIAL_API_BASE_URL}?materialType=PRODUCT`)
    availableProducts.value = response.data
  } catch (error: any) {
    console.error('Error fetching available products:', error)
    ElMessage.error(`獲取可用產品列表失敗: ${error.response?.data?.message || error.message}`)
  }
}

// Add a new work order
const addWorkOrder = async () => {
  if (!addWorkOrderFormRef.value) return
  await addWorkOrderFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const response = await http.post<WorkOrderDto>(API_BASE_URL, newWorkOrder.value)
        ElMessage.success('工單新增成功，後端正在處理庫存檢查。')
        showAddWorkOrderDialog.value = false
        newWorkOrder.value = {
          woNumber: `WO-${Date.now()}`,
          materialId: null,
          requiredQuantity: 1,
          status: 'PENDING',
        }
        fetchWorkOrders() // Refresh list
      } catch (error: any) {
        console.error('Error adding work order:', error)
        ElMessage.error(`新增工單失敗: ${error.response?.data?.message || error.message}`)
      }
    } else {
      ElMessage.warning('請檢查表單填寫是否正確')
    }
  })
}

// Delete work order
const deleteWorkOrder = async (id?: number) => {
  if (!id) return
  await ElMessageBox.confirm('確定要刪除此工單嗎？相關的領料記錄也會被刪除。', '警告', {
    confirmButtonText: '確定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await http.delete(`${API_BASE_URL}/${id}`)
        ElMessage.success('工單刪除成功')
        fetchWorkOrders() // Refresh list
      } catch (error: any) {
        console.error('Error deleting work order:', error)
        ElMessage.error(`刪除工單失敗: ${error.response?.data?.message || error.message}`)
      }
    })
    .catch(() => {
      ElMessage.info('已取消刪除')
    })
}

// View work order details and its materials
const exportToWord = () => {
  let content = '已完成的工單列表\n\n';
  completedWorkOrders.value.forEach(wo => {
    content += `工單號碼: ${wo.woNumber}\n`;
    content += `生產產品: ${wo.materialName}\n`;
    content += `完成數量: ${wo.requiredQuantity}\n`;
    content += `完成時間: ${wo.updatedAt}\n`;
    content += '--------------------\n';
  });

  const blob = new Blob([content], { type: 'application/msword' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'completed_work_orders.doc';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
  ElMessage.success('已匯出 Word 文件');
};

const exportToMarkdown = () => {
  let content = '# 已完成的工單列表\n\n';
  content += '| 工單號碼 | 生產產品 | 完成數量 | 完成時間 |\n';
  content += '|---|---|---|---|';
  completedWorkOrders.value.forEach(wo => {
    content += `| ${wo.woNumber} | ${wo.materialName} | ${wo.requiredQuantity} | ${wo.updatedAt} |\n`;
  });

  const blob = new Blob([content], { type: 'text/markdown' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'completed_work_orders.md';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
  ElMessage.success('已匯出 Markdown 文件');
};

const viewWorkOrderDetails = async (workOrder: WorkOrderDto) => {
  selectedWorkOrder.value = workOrder
  showWorkOrderDetailsDialog.value = true

  // Update steps bar based on status
  switch (workOrder.status) {
    case 'PENDING':
      activeStep.value = 0;
      stepStatus.value = 'process';
      break;
    case 'OUT_OF_STOCK':
      activeStep.value = 1;
      stepStatus.value = 'error';
      break;
    case 'IN_PROGRESS':
      activeStep.value = 2;
      stepStatus.value = 'process';
      break;
    case 'COMPLETED':
      activeStep.value = 4; // Finish status will show the 4th step as completed
      stepStatus.value = 'success';
      break;
    default:
      activeStep.value = 0;
      stepStatus.value = 'wait';
  }

  try {
    const response = await http.get<WorkOrderMaterialDto[]>(`${API_BASE_URL}/${workOrder.woId}/materials`)
    selectedWorkOrderMaterials.value = response.data
  } catch (error) {
    console.error('Error fetching work order materials:', error)
    ElMessage.error('獲取領料明細失敗')
    selectedWorkOrderMaterials.value = []
  }
}

</script>

<style scoped>
.outbound-view {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.dialog-footer button:first-child {
  margin-right: 10px;
}
</style>
