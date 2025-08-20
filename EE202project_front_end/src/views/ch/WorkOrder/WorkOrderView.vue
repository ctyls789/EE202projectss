<template>
  <div class="work-order-container">
    <!-- 導航按鈕 -->
    <div class="nav-buttons" v-if="currentView !== 'detail'">
      <el-button 
        :type="currentView === 'list' ? 'primary' : 'default'"
        @click="showList"
      >
        工單列表
      </el-button>
    </div>

    <!-- 返回按鈕 (僅在詳情頁顯示) -->
    <div class="back-button" v-if="currentView === 'detail'">
      <el-button @click="backToList" icon="ArrowLeft">返回列表</el-button>
    </div>

    <!-- 動態顯示不同頁面 -->
    <div class="view-content">
      <!-- 修復組件名稱 -->
      <WorkOrderFinishList 
        v-if="currentView === 'list'"
        @view-detail="viewDetail"
        ref="listComponentRef"
      />
      
      <WorkOrderDetailModalFinish 
        v-if="currentView === 'detail' && selectedWorkOrder"
        :work-order="selectedWorkOrder"
        @back="backToList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import WorkOrderFinishList from './WorkOrderFinishList.vue' // 確保路徑正確
import WorkOrderDetailModalFinish from './WorkOrderDetailModalFinish.vue'

const currentView = ref('list')
const selectedWorkOrder = ref(null)
const listComponentRef = ref(null)

const showList = () => {
  currentView.value = 'list'
  selectedWorkOrder.value = null
}

const viewDetail = (workOrder) => {
  console.log('查看工單詳情:', workOrder) // 調試用
  selectedWorkOrder.value = workOrder
  currentView.value = 'detail'
}

const backToList = async () => {
  currentView.value = 'list'
  selectedWorkOrder.value = null
  
  // 等待 DOM 更新後重新載入列表資料
  await nextTick()
  if (listComponentRef.value && listComponentRef.value.fetchWorkOrders) {
    listComponentRef.value.fetchWorkOrders()
  }
}
</script>

<style scoped>
.work-order-container {
  padding: 20px;
  height: 100vh;
  overflow-y: auto;
}

.nav-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.back-button {
  margin-bottom: 20px;
}

.view-content {
  width: 100%;
}
</style>