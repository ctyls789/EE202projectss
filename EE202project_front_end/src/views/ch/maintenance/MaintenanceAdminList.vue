<script setup>
import { ref } from 'vue'
import MaintenanceList from './admin/AdminMaintenanceList.vue'
import MaintenanceSearch from './admin/AdminMaintenanceSearch.vue'

const showOriginalList = ref(true)

// 搜尋完成時隱藏原始列表
function handleSearchComplete() {
  showOriginalList.value = false
}

// 清除搜尋時顯示原始列表
function handleSearchClear() {
  showOriginalList.value = true
}
</script>

<template>
  <div class="maintenance-admin">
    <h1>保養管理系統</h1>

    <!-- 搜尋組件永遠顯示 -->
    <MaintenanceSearch @search-complete="handleSearchComplete" @search-clear="handleSearchClear" />

    <!-- 原本列表，搜尋時隱藏 -->
    <MaintenanceList v-if="showOriginalList" />

    <!-- 搜尋結果區塊，搜尋時顯示 -->
    <div v-else class="search-results-container">
      <!-- 搜尋結果已經在 MaintenanceSearch 組件內部顯示 -->
    </div>
  </div>
</template>

<style scoped>
.maintenance-admin {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 30px;
  font-size: 28px;
}
</style>
