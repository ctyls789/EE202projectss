<script setup>
import { ref, reactive } from 'vue'

// 表單資料
const form = reactive({
  machineId: '',
  maintenanceDescription: '',
  employeeId: '',
})

// 錯誤訊息
const errors = reactive({
  machineId: '',
  maintenanceDescription: '',
  employeeId: '',
})

// 系統訊息
const message = ref('')
const messageType = ref('')
const loading = ref(false)

// 清除錯誤
function clearErrors() {
  errors.machineId = ''
  errors.maintenanceDescription = ''
  errors.employeeId = ''
}

// 驗證表單
function validateForm() {
  clearErrors()
  let isValid = true

  const machineId = (form.machineId + '').trim()
  if (!machineId) {
    errors.machineId = '請輸入機台編號'
    isValid = false
  } else {
    const parsedMachineId = parseInt(machineId)
    if (isNaN(parsedMachineId) || parsedMachineId <= 0) {
      errors.machineId = '機台編號必須是有效的正整數'
      isValid = false
    }
  }

  const maintenanceDescription = (form.maintenanceDescription + '').trim()
  if (!maintenanceDescription) {
    errors.maintenanceDescription = '請輸入保養描述'
    isValid = false
  } else if (maintenanceDescription.length > 500) {
    errors.maintenanceDescription = '保養描述不能超過500個字元'
    isValid = false
  }

  const employeeId = (form.employeeId + '').trim()
  if (!employeeId) {
    errors.employeeId = '請輸入員工編號'
    isValid = false
  } else {
    const parsedEmployeeId = parseInt(employeeId)
    if (isNaN(parsedEmployeeId) || parsedEmployeeId <= 0) {
      errors.employeeId = '員工編號必須是有效的正整數'
      isValid = false
    }
  }

  return isValid
}

// 顯示訊息
function showMessage(text, type = 'success') {
  message.value = text
  messageType.value = type

  setTimeout(() => {
    message.value = ''
  }, 3000)
}

// 清空表單
function resetForm() {
  form.machineId = ''
  form.maintenanceDescription = ''
  form.employeeId = ''
  clearErrors()
}

// 提交表單
async function handleSubmit() {
  if (!validateForm()) return

  loading.value = true

  try {
    const userJson = localStorage.getItem('user')
    const user = userJson ? JSON.parse(userJson) : null
    const token = user ? user.token : null

    if (!token) {
      throw new Error('未找到驗證權杖，請先登入。')
    }
    let response = await fetch('http://localhost:8082/api/maintenance', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        machineId: parseInt(form.machineId),
        maintenanceDescription: form.maintenanceDescription,
        employeeId: parseInt(form.employeeId),
      }),
    })
    if (response.status === 401) {
      throw new Error('驗證已過期，請重新登入。')
    }
    if (response.status === 403) {
      throw new Error('您的權限不足。')
    }

    if (response.ok) {
      showMessage('✅ 保養排程新增成功！')
      resetForm()
    } else {
      const text = await response.text()
      throw new Error(text || '新增失敗')
    }
  } catch (error) {
    console.error('提交錯誤:', error)
    showMessage('❌ 新增失敗，請重試', 'error')
  } finally {
    loading.value = false
  }
}

// 即時驗證
function validateMachineId() {
  const machineId = (form.machineId + '').trim()
  if (machineId && (isNaN(parseInt(machineId)) || parseInt(machineId) <= 0)) {
    errors.machineId = '機台編號必須是有效的正整數'
  } else {
    errors.machineId = ''
  }
}

function validateEmployeeId() {
  const employeeId = (form.employeeId + '').trim()
  if (employeeId && (isNaN(parseInt(employeeId)) || parseInt(employeeId) <= 0)) {
    errors.employeeId = '員工編號必須是有效的正整數'
  } else {
    errors.employeeId = ''
  }
}

function validateDescription() {
  const desc = (form.maintenanceDescription + '').trim()
  if (!desc) {
    errors.maintenanceDescription = '請輸入保養描述'
  } else if (desc.length > 500) {
    errors.maintenanceDescription = '保養描述不能超過500個字元'
  } else {
    errors.maintenanceDescription = ''
  }
}
</script>

<template>
  <div class="maintenance-form">
    <h2>
      <i class="fas fa-calendar-plus"></i>
      新增保養排程
    </h2>

    <p class="form-description">請填寫以下資訊以建立新的保養排程。</p>

    <!-- 訊息顯示 -->
    <div v-if="message" class="message" :class="messageType">
      <i
        :class="messageType === 'success' ? 'fas fa-check-circle' : 'fas fa-exclamation-triangle'"
      ></i>
      {{ message }}
    </div>

    <!-- 表單 -->
    <form @submit.prevent="handleSubmit">
      <!-- 機台編號 -->
      <div class="form-group">
        <label for="machineId"> <i class="fas fa-microchip"></i> 機台編號： </label>
        <input
          id="machineId"
          v-model="form.machineId"
          type="text"
          placeholder="請輸入機台的唯一編號，例如：101"
          :class="{ error: errors.machineId }"
          @input="validateMachineId"
        />
        <div v-if="errors.machineId" class="error-text">
          {{ errors.machineId }}
        </div>
        <small class="helper-text">請確認輸入的機台編號為一個存在的正整數。</small>
      </div>

      <!-- 保養描述 -->
      <div class="form-group">
        <label for="maintenanceDescription"> <i class="fas fa-align-left"></i> 保養描述： </label>
        <textarea
          id="maintenanceDescription"
          v-model="form.maintenanceDescription"
          placeholder="請簡要描述此次保養的內容或目的，例如：每月例行檢查及潤滑。"
          :class="{ error: errors.maintenanceDescription }"
          @input="validateDescription"
        ></textarea>
        <div v-if="errors.maintenanceDescription" class="error-text">
          {{ errors.maintenanceDescription }}
        </div>
        <small class="helper-text">建議清楚說明保養範圍和細節。</small>
      </div>

      <!-- 員工編號 -->
      <div class="form-group">
        <label for="employeeId"> <i class="fas fa-user-tie"></i> 員工編號： </label>
        <input
          id="employeeId"
          v-model="form.employeeId"
          type="text"
          placeholder="請輸入負責保養的員工編號，例如：2001"
          :class="{ error: errors.employeeId }"
          @input="validateEmployeeId"
        />
        <div v-if="errors.employeeId" class="error-text">
          {{ errors.employeeId }}
        </div>
        <small class="helper-text">請確認輸入的員工編號為一個存在的正整數。</small>
      </div>

      <!-- 按鈕區域 -->
      <div class="form-actions">
        <button type="submit" :disabled="loading" class="btn-submit">
          <i class="fas fa-check-circle"></i>
          {{ loading ? '提交中...' : '送出排程' }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css');
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@300;400;500;700&display=swap');

.maintenance-form {
  max-width: 650px;
  margin: 3rem auto;
  padding: 2.5rem 3rem;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  font-family: 'Noto Sans TC', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

h2 {
  font-size: 2.2rem;
  color: #2c3e50;
  margin-bottom: 1.5rem;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.form-description {
  text-align: center;
  color: #555;
  margin-top: -1rem;
  margin-bottom: 2rem;
  line-height: 1.6;
}

/* 訊息樣式 */
.message {
  padding: 1rem;
  margin-bottom: 1.5rem;
  border-radius: 8px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
}

.message.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

/* 表單樣式 */
form {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

.form-group {
  margin-bottom: 0.5rem;
}

.form-group label {
  font-weight: 600;
  color: #495057;
  margin-bottom: 0.5rem;
  display: block;
  font-size: 1.05rem;
}

input[type='text'],
textarea {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ced4da;
  border-radius: 8px;
  font-size: 1rem;
  color: #333;
  transition: all 0.3s ease-in-out;
  background-color: #fcfcfc;
  box-sizing: border-box;
  font-family: inherit;
}

input[type='text']:focus,
textarea:focus {
  border-color: #007bff;
  box-shadow: 0 0 0 4px rgba(0, 123, 255, 0.15);
  outline: none;
  background-color: #ffffff;
}

input.error,
textarea.error {
  border-color: #dc3545;
}

textarea {
  resize: vertical;
  min-height: 100px;
}

.error-text {
  color: #dc3545;
  font-size: 0.875em;
  margin-top: 0.25rem;
  min-height: 1.25em;
  display: block;
}

.helper-text {
  color: #666;
  display: block;
  margin-top: 5px;
  font-size: 0.9rem;
}

/* 按鈕區域 */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin-top: 2.5rem;
}

.btn-submit,
.btn-cancel {
  padding: 12px 28px;
  border-radius: 8px;
  font-size: 1.05rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  white-space: nowrap;
  border: none;
}

.btn-submit {
  background-color: #28a745;
  color: white;
  box-shadow: 0 4px 10px rgba(40, 167, 69, 0.2);
}

.btn-submit:hover:not(:disabled) {
  background-color: #218838;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(40, 167, 69, 0.3);
}

.btn-submit:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
  transform: none;
}

.btn-cancel {
  background-color: #6c757d;
  color: white;
  box-shadow: 0 4px 10px rgba(108, 117, 125, 0.2);
}

.btn-cancel:hover {
  background-color: #5a6268;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(108, 117, 125, 0.3);
}

/* 響應式調整 */
@media (max-width: 768px) {
  .maintenance-form {
    margin: 1.5rem auto;
    padding: 1.5rem;
    border-radius: 8px;
  }

  h2 {
    font-size: 1.8rem;
    margin-bottom: 1rem;
  }

  input[type='text'],
  textarea {
    padding: 10px 12px;
    font-size: 0.95rem;
  }

  .form-actions {
    flex-direction: column;
    gap: 1rem;
    margin-top: 1.5rem;
  }

  .btn-submit,
  .btn-cancel {
    width: 100%;
    padding: 10px 20px;
  }
}
</style>
