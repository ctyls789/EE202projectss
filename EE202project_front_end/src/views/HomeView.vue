<template>
  
</template>

<script setup lang="ts">
import { Box, User, ShoppingCart, Document } from '@element-plus/icons-vue'
</script>

<style scoped>
h1 {
  font-weight: 600;
  color: #303133;
  font-size: 24px;
  margin-bottom: 20px;
}

.el-card {
  border-radius: 10px;
  text-align: center;
}

.card-content {
  position: relative;
}

.card-icon {
  position: absolute;
  top: -10px;
  right: -10px;
  font-size: 28px;
  color: #c0c4cc;
}

.el-statistic :deep(.el-statistic__title) {
  font-size: 16px;
  font-weight: 500;
  color: #606266;
}

.el-statistic :deep(.el-statistic__content) {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
</style>