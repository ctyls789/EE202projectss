<script setup>
import { ref } from 'vue'
import UserMachineList from './user/UserMachineList.vue'
import UserMachineSearch from './user/UserMachineSearch.vue'

// 控制顯示狀態：true=顯示原本列表，false=顯示搜尋結果
const showOriginalList = ref(true)

// 當搜尋完成時
function handleSearchComplete() {
  showOriginalList.value = false // 隱藏原本列表
}

// 當清除搜尋時
function handleSearchClear() {
  showOriginalList.value = true // 顯示原本列表
}
</script>

<template>
  <div class="machine-admin">
    <h1>機台管理系統</h1>

    <!-- 搜尋組件：永遠顯示 -->
    <UserMachineSearch @search-complete="handleSearchComplete" @search-clear="handleSearchClear" />

    <!-- 原本的機台列表：只有在沒有搜尋時才顯示 -->
    <UserMachineList v-if="showOriginalList" />
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
