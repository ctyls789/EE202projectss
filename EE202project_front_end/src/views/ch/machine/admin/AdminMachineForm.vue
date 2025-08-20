<!-- MachineCreateForm.vue -->
<script setup>
import { ref } from 'vue'

const loading = ref(false)
const message = ref('')
const messageType = ref('')

// 表單資料
const formData = ref({
  machineName: '',
  serialNumber: '',
  mstatus: '',
  machineLocation: '',
})

// 狀態選項
const statusOptions = ['運行中', '維護中', '停機']

// 新增機台
async function handleSubmit() {
  message.value = ''
  messageType.value = ''
  loading.value = true

  try {
    const userJson = localStorage.getItem('user')
    const user = userJson ? JSON.parse(userJson) : null
    const token = user ? user.token : null

    if (!token) {
      throw new Error('未找到驗證權杖，請先登入。')
    }

    const res = await fetch('http://localhost:8082/api/machines', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(formData.value),
    })

    if (res.status === 401) {
      throw new Error('驗證已過期，請重新登入。')
    }
    if (res.status === 403) {
      throw new Error('您的權限不足，無法新增機台。')
    }
    if (!res.ok) throw new Error('新增失敗')

    message.value = '✅ 機台新增成功！'
    messageType.value = 'success'

    // 清空表單
    formData.value = {
      machineName: '',
      serialNumber: '',
      mstatus: '',
      machineLocation: '',
    }
  } catch (err) {
    message.value = '❌ 錯誤：' + err.message
    messageType.value = 'error'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="form-card">
    <h2>新增機台</h2>

    <!-- 成功/錯誤訊息 -->
    <div v-if="message" :class="['message', messageType]">
      {{ message }}
    </div>

    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="machineName">機台名稱</label>
        <input
          id="machineName"
          type="text"
          v-model="formData.machineName"
          placeholder="輸入機台名稱"
          :disabled="loading"
          required
        />
      </div>

      <div class="form-group">
        <label for="serialNumber">出廠編號</label>
        <input
          id="serialNumber"
          type="text"
          v-model="formData.serialNumber"
          placeholder="輸入出廠編號"
          :disabled="loading"
          required
        />
      </div>

      <div class="form-group">
        <label for="mstatus">運行狀態</label>
        <select id="mstatus" v-model="formData.mstatus" :disabled="loading" required>
          <option value="">請選擇</option>
          <option v-for="s in statusOptions" :key="s" :value="s">{{ s }}</option>
        </select>
      </div>

      <div class="form-group">
        <label for="machineLocation">機台位置</label>
        <input
          id="machineLocation"
          type="text"
          v-model="formData.machineLocation"
          placeholder="輸入機台位置"
          :disabled="loading"
          required
        />
      </div>

      <div class="form-actions">
        <button type="submit" :disabled="loading">
          {{ loading ? '新增中...' : '➕ 新增資料' }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.form-card {
  padding: 30px 40px;
  width: 100%;
  /* 最大寬度可視需要調整 */
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.07);
  box-sizing: border-box;
}

h2 {
  font-size: 28px;
  color: #2c3e50;
  margin-bottom: 30px;
  text-align: center;
}

.form-group {
  display: flex;
  align-items: center;
  margin-bottom: 22px;
  gap: 20px;
}

label {
  width: 140px;
  font-weight: 700;
  color: #34495e;
  text-align: right;
  user-select: none;
}

input,
select {
  flex: 1;
  padding: 12px 14px;
  font-size: 16px;
  border: 1px solid #ccc;
  border-radius: 8px;
  transition: border-color 0.3s ease;
}

input:focus,
select:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 6px rgba(52, 152, 219, 0.4);
}

input:disabled,
select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.form-actions {
  text-align: center;
  margin-top: 35px;
}

button {
  background: #3498db;
  color: white;
  padding: 12px 32px;
  font-size: 18px;
  font-weight: 700;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition:
    background 0.3s ease,
    transform 0.2s ease;
  user-select: none;
}

button:hover:not(:disabled) {
  background: #2980b9;
  transform: translateY(-2px);
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.message {
  text-align: center;
  font-weight: 700;
  padding: 14px;
  border-radius: 8px;
  margin-bottom: 25px;
  font-size: 16px;
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

/* 響應式設計 */
@media (max-width: 768px) {
  .form-group {
    flex-direction: column;
    align-items: stretch;
  }

  label {
    width: 100%;
    text-align: left;
    margin-bottom: 8px;
  }

  input,
  select {
    width: 100%;
  }
}
</style>
