<template>
  <el-dialog
    :model-value="modelValue"
    title="完成工單"
    width="500px"
    @close="$emit('close')"
  >
    <el-form 
      ref="formRef" 
      :model="form" 
      :rules="rules" 
      label-width="120px"
    >
      <el-form-item label="工單編號">
        <el-input v-model="workOrder.woNumber" disabled />
      </el-form-item>
      
      <el-form-item label="材料名稱">
        <el-input v-model="workOrder.materialName" disabled />
      </el-form-item>
      
      <el-form-item label="成功數量" prop="quantityDone" required>
        <el-input-number 
          v-model="form.quantityDone" 
          :min="0" 
          :max="workOrder.requiredQuantity"
          style="width: 100%"
        />
        <div class="form-tip">需求數量: {{ workOrder.requiredQuantity }}</div>
      </el-form-item>

      <el-form-item label="失敗數量" prop="quantityFailed" required>
        <el-input-number 
          v-model="form.quantityFailed" 
          :min="0" 
          :max="workOrder.requiredQuantity"
          style="width: 100%"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('close')">取消</el-button>
      <el-button type="primary" @click="submitForm" :loading="loading">確認完成</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  workOrder: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['close', 'submit'])

const formRef = ref()
const loading = ref(false)

const form = reactive({
  quantityDone: 0,
  quantityFailed: 0
})

const rules = {
  quantityDone: [
    { required: true, message: '請輸入成功數量', trigger: 'blur' },
    { type: 'number', min: 0, message: '數量不可小於0', trigger: 'blur' }
  ],
  quantityFailed: [
    { required: true, message: '請輸入失敗數量', trigger: 'blur' },
    { type: 'number', min: 0, message: '數量不可小於0', trigger: 'blur' }
  ]
}

// 監聽彈窗開啟，重置表單
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    resetForm()
  }
})

const resetForm = () => {
  form.quantityDone = props.workOrder.requiredQuantity || 0
  form.quantityFailed = 0
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    const submitData = {
      workOrder: { woId: props.workOrder.woId },
      quantityDone: form.quantityDone,
      quantityFailed: form.quantityFailed
    }
    // 模擬API調用
    setTimeout(() => {
      emit('submit', submitData)
      loading.value = false
    }, 1000)
  } catch (error) {
    console.error('表單驗證失敗:', error)
  }
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>