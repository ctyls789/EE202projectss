<script setup>
import { ref, watch } from 'vue'
import { toRaw } from 'vue'

const props = defineProps({
  repair: Object,
  statusOptions: Array,
})
const emit = defineEmits(['close', 'updated'])

const form = ref({
  repairId: '',
  repairStatus: '',
  repairDescription: '',
})

// 資料變更時更新表單內容
watch(
  () => props.repair,
  (newVal) => {
    if (newVal) {
      form.value = {
        repairId: newVal.repairId,
        repairStatus: newVal.repairStatus || newVal.status || '',
        repairDescription: newVal.repairDescription || newVal.description || '',
      }
    }
  },
  { immediate: true },
)

function close() {
  emit('close')
}

async function save() {
  if (!form.value.repairStatus || !form.value.repairDescription.trim()) {
    alert('請完整填寫狀態與描述')
    return
  }

  try {
    const userJson = localStorage.getItem('user')
    const user = userJson ? JSON.parse(userJson) : null
    const token = user ? user.token : null

    if (!token) {
      throw new Error('未找到驗證權杖，請先登入。')
    }
    // 只更新狀態，描述要另外設計API或不更新
    const res = await fetch(
      `http://localhost:8082/api/repair/${form.value.repairId}/status?newStatus=${encodeURIComponent(form.value.repairStatus)}`,
      {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    )
    if (res.status === 401) {
      throw new Error('驗證已過期，請重新登入。')
    }
    if (res.status === 403) {
      throw new Error('您的權限不足。')
    }

    if (!res.ok) throw new Error('更新失敗')

    alert('更新成功')
    emit('updated')
  } catch (error) {
    alert('更新失敗，請稍後再試')
    console.error(error)
  }
}
</script>

<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <h3>編輯維修單 #{{ form.repairId }}</h3>
      <form @submit.prevent="save">
        <div class="form-group">
          <label>狀態</label>
          <select v-model="form.repairStatus" required>
            <option value="" disabled>請選擇狀態</option>
            <option v-for="status in statusOptions" :key="status" :value="status">
              {{ status }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>問題描述</label>
          <textarea v-model="form.repairDescription" rows="4" required></textarea>
        </div>

        <div class="button-group">
          <button type="submit">儲存</button>
          <button type="button" @click="close">取消</button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 20px 30px;
  max-width: 480px;
  width: 90%;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
  animation: fadeIn 0.3s ease;
}

h3 {
  margin-top: 0;
  margin-bottom: 20px;
  font-weight: 600;
  color: #34495e;
}

.form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 600;
  margin-bottom: 5px;
  color: #2c3e50;
}

input,
select,
textarea {
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #ccc;
  font-size: 14px;
  transition: border-color 0.3s ease;
  resize: vertical;
}

input:focus,
select:focus,
textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 5px rgba(52, 152, 219, 0.4);
}

.button-group {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: background-color 0.3s ease;
}

button[type='submit'] {
  background-color: #3498db;
  color: white;
}

button[type='submit']:hover {
  background-color: #2980b9;
}

button[type='button'] {
  background-color: #95a5a6;
  color: white;
}

button[type='button']:hover {
  background-color: #7f8c8d;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
