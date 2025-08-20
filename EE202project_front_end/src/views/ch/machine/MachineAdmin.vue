<script setup>
import { ref } from 'vue'
import AdminMachineList from './admin/AdminMachineList.vue'
import AdminMachineSearch from './admin/AdminMachineSearch.vue'

const showOriginalList = ref(true)

// 搜尋完成時改為 false，隱藏原始列表，顯示搜尋結果
function handleSearchComplete() {
  showOriginalList.value = false
}

// 清除搜尋時改為 true，顯示原始列表，隱藏搜尋結果
function handleSearchClear() {
  showOriginalList.value = true
}
</script>

<template>
  <div class="machine-admin">
    <h1>機台管理系統</h1>

    <!-- 搜尋組件永遠顯示 -->
    <AdminMachineSearch @search-complete="handleSearchComplete" @search-clear="handleSearchClear" />

    <!-- 原本列表，搜尋時隱藏 -->
    <AdminMachineList v-if="showOriginalList" />

    <!-- 搜尋結果區塊，搜尋時顯示 -->
    <div v-else class="search-results-container">
      <!-- 這裡你可以放一些提示文字或樣式 -->
    </div>
  </div>
</template>

<style scoped>
.machine-admin {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 30px;
  font-size: 28px;
}
</style>
