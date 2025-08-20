<script setup>
import { ref } from 'vue'
import RepairList from './admin/AdminRepairList.vue'
import RepairSearch from './admin/AdminRepairSearch.vue'

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
  <div class="repair-admin">
    <h1>維修管理系統</h1>

    <!-- 搜尋組件永遠顯示 -->
    <RepairSearch @search-complete="handleSearchComplete" @search-clear="handleSearchClear" />

    <!-- 原本列表，搜尋時隱藏 -->
    <RepairList v-if="showOriginalList" />

    <!-- 搜尋結果區塊，搜尋時顯示 -->
    <div v-else class="search-results-container">
      <!-- 這裡你可以放一些提示文字或樣式 -->
    </div>
  </div>
</template>
<script>

</script>
<style scoped>
.repair-admin {
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
