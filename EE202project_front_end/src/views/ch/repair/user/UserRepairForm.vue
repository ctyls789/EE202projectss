<script setup>
import { ref, reactive } from 'vue'

// 表單資料
const form = reactive({
  employeeId: '',
  machineId: '',
  repairDescription: '',
})

// 錯誤訊息
const errors = reactive({
  employeeId: '',
  machineId: '',
  repairDescription: '',
})

// 系統訊息
const message = ref('')
const messageType = ref('')
const loading = ref(false)

// 清除錯誤
function clearErrors() {
  errors.employeeId = ''
  errors.machineId = ''
  errors.repairDescription = ''
}

// 驗證表單
function validateForm() {
  clearErrors()
  let isValid = true

  if (!form.employeeId.trim()) {
    errors.employeeId = '請輸入員工編號'
    isValid = false
  } else if (!/^\d+$/.test(form.employeeId)) {
    errors.employeeId = '員工編號只能是數字'
    isValid = false
  }

  if (!form.machineId.trim()) {
    errors.machineId = '請輸入機台編號'
    isValid = false
  } else if (!/^\d+$/.test(form.machineId)) {
    errors.machineId = '機台編號只能是數字'
    isValid = false
  }

  if (!form.repairDescription.trim()) {
    errors.repairDescription = '請輸入維修描述'
    isValid = false
  } else if (form.repairDescription.length > 500) {
    errors.repairDescription = '描述不能超過500字'
    isValid = false
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
  form.employeeId = ''
  form.machineId = ''
  form.repairDescription = ''
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
    let response = await fetch('http://localhost:8082/api/repair', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(form),
    })
    if (response.status === 401) {
      throw new Error('驗證已過期，請重新登入。')
    }
    if (response.status === 403) {
      throw new Error('您的權限不足。')
    }

    if (response.ok) {
      showMessage('✅ 申請提交成功！')
      resetForm()
    } else {
      throw new Error('提交失敗')
    }
  } catch (error) {
    console.error('提交錯誤:', error)
    showMessage('❌ 提交失敗，請重試', 'error')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="repair-form">
    <h2>🔧 機台報修申請</h2>

    <!-- 訊息顯示 -->
    <div v-if="message" class="message" :class="messageType">
      {{ message }}
    </div>

    <!-- 表單 -->
    <form @submit.prevent="handleSubmit">
      <!-- 員工編號 -->
      <div class="field">
        <label>👤 員工編號</label>
        <input
          v-model="form.employeeId"
          type="text"
          placeholder="請輸入員工編號"
          :class="{ error: errors.employeeId }"
        />
        <span v-if="errors.employeeId" class="error-text">
          {{ errors.employeeId }}
        </span>
      </div>

      <!-- 機台編號 -->
      <div class="field">
        <label>🖥️ 機台編號</label>
        <input
          v-model="form.machineId"
          type="text"
          placeholder="請輸入機台編號"
          :class="{ error: errors.machineId }"
        />
        <span v-if="errors.machineId" class="error-text">
          {{ errors.machineId }}
        </span>
      </div>

      <!-- 維修描述 -->
      <div class="field">
        <label>📝 維修描述</label>
        <textarea
          v-model="form.repairDescription"
          placeholder="請描述故障狀況..."
          :class="{ error: errors.repairDescription }"
        ></textarea>
        <span v-if="errors.repairDescription" class="error-text">
          {{ errors.repairDescription }}
        </span>
      </div>

      <!-- 提交按鈕 -->
      <button type="submit" :disabled="loading">
        {{ loading ? '📤 提交中...' : '📋 送出申請' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.repair-form {
  max-width: 500px;
  margin: 2rem auto;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 2rem;
  font-size: 1.8rem;
  font-weight: bold;
}

/* 訊息樣式 */
.message {
  padding: 12px 15px;
  margin-bottom: 1.5rem;
  border-radius: 6px;
  font-weight: bold;
  text-align: center;
}

.message.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

/* 表單欄位 */
.field {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #34495e;
  font-size: 14px;
}

input,
textarea {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
  box-sizing: border-box;
  background: #fcfcfc;
}

input:focus,
textarea:focus {
  outline: none;
  border-color: #3498db;
  background: white;
}

input.error,
textarea.error {
  border-color: #e74c3c;
}

textarea {
  min-height: 100px;
  resize: vertical;
  font-family: inherit;
}

.error-text {
  display: block;
  color: #e74c3c;
  font-size: 12px;
  margin-top: 5px;
  font-weight: bold;
}

/* 按鈕 */
button {
  width: 100%;
  padding: 15px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover:not(:disabled) {
  background: #219a52;
}

button:disabled {
  background: #95a5a6;
  cursor: not-allowed;
}

/* 手機版調整 */
@media (max-width: 600px) {
  .repair-form {
    margin: 1rem;
    padding: 1.5rem;
  }

  h2 {
    font-size: 1.5rem;
  }
}
</style>
