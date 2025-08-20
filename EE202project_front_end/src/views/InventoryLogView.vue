<template>
  <div class="inventory-log-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>庫存異動紀錄</span>
        </div>
      </template>
      <el-table :data="inventoryTransactions" style="width: 100%" v-loading="loadingTransactions">
        <el-table-column prop="transactionId" label="交易ID" width="80"></el-table-column>
        <el-table-column prop="material.materialName" label="物料名稱"></el-table-column>
        <el-table-column prop="transactionType" label="交易類型"></el-table-column>
        <el-table-column prop="quantity" label="異動數量"></el-table-column>
        <el-table-column prop="transactionDate" label="交易日期"></el-table-column>
        <el-table-column prop="referenceTable" label="參考表"></el-table-column>
        <el-table-column prop="referenceId" label="參考ID"></el-table-column>
        <el-table-column prop="notes" label="備註"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import http from '../http-common'
import { ElMessage } from 'element-plus'

interface Material {
  materialId: number;
  materialName: string;
}

interface InventoryTransaction {
  transactionId: number;
  material: Material;
  transactionType: string;
  quantity: number;
  transactionDate: string;
  referenceTable: string;
  referenceId: number;
  notes: string;
}

const inventoryTransactions = ref<InventoryTransaction[]>([])
const loadingTransactions = ref(false)

const API_BASE_URL = '/depot/transactions'

// Fetch all inventory transactions
const fetchInventoryTransactions = async () => {
  loadingTransactions.value = true
  try {
    const response = await http.get<InventoryTransaction[]>(API_BASE_URL)
    inventoryTransactions.value = response.data.map(transaction => ({
      ...transaction,
      transactionDate: new Date(transaction.transactionDate).toLocaleString() // Format date
    }))
  } catch (error) {
    console.error('Error fetching inventory transactions:', error)
    ElMessage.error('獲取庫存異動紀錄失敗')
  } finally {
    loadingTransactions.value = false
  }
}

onMounted(() => {
  fetchInventoryTransactions()
})
</script>

<style scoped>
.inventory-log-view {
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
</style>