<template>
  <el-card class="add-supplier-card">
    <template #header>
      <span style="font-size: 20px; font-weight: bold;">➕ 新增供應商</span>
    </template>

    <el-form :model="form" label-width="120px">
      <el-form-item label="供應商名稱" required>
        <el-input v-model="form.supplierName" />
      </el-form-item>

      <el-form-item label="聯絡人">
        <el-input v-model="form.pm" />
      </el-form-item>

      <el-form-item label="電話">
        <el-input v-model="form.supplierPhone" />
      </el-form-item>

      <el-form-item label="Email">
        <el-input v-model="form.supplierEmail" type="email" />
      </el-form-item>

      <el-form-item label="地址">
        <el-input v-model="form.supplierAddress" />
      </el-form-item>

      <el-form-item label="備註">
        <el-input v-model="form.supplierNote" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="Check" @click="submitForm">送出</el-button>
        <el-button @click="$router.push('/zt/supplier/list')">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>


<script setup>
import { ref } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = ref({
  supplierName: '',
  pm: '',
  supplierPhone: '',
  supplierEmail: '',
  supplierAddress: '',
  supplierNote: ''
})

const submitForm = async () => {
  try {
    await api.post('/api/supplier/add', form.value)
    alert('新增成功！')
    router.push('/zt/supplier/list') // 新增完跳回列表
  } catch (error) {
    console.error('新增失敗', error)
    alert('新增失敗，請檢查輸入資料')
  }
}
</script>

<style scoped>
.add-supplier-card {
  margin: 20px;
}
</style>
