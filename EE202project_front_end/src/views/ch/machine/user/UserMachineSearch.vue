<script setup>
import { ref } from 'vue'

const emit = defineEmits(['search-complete', 'search-clear'])

const statusOptions = ['運行中', '維護中', '停機']
const selectedStatus = ref('')
const searchText = ref('')
const searching = ref(false)
const searchResults = ref([])
const showResults = ref(false)

// 取得狀態樣式
function getStatusClass(status) {
  switch (status) {
    case '運行中':
      return 'running'
    case '維護中':
      return 'maintenance'
    case '停機':
      return 'stopped'
    default:
      return 'unknown'
  }
}

// 取得狀態圖示
function getStatusIcon(status) {
  switch (status) {
    case '運行中':
      return '🟢'
    case '維護中':
      return '🟡'
    case '停機':
      return '🔴'
    default:
      return '❓'
  }
}

async function handleSearch() {
  const params = new URLSearchParams()

  // 修正：使用後端期望的參數名稱
  if (selectedStatus.value) params.append('statusFilter', selectedStatus.value)
  if (searchText.value.trim()) params.append('search', searchText.value.trim())

  if (!params.toString()) {
    alert('請至少選擇狀態或輸入關鍵字！')
    return
  }

  try {
    searching.value = true
    console.log('搜尋參數:', params.toString())
    const userJson = localStorage.getItem('user')
    const user = userJson ? JSON.parse(userJson) : null
    const token = user ? user.token : null

    if (!token) {
      throw new Error('未找到驗證權杖，請先登入。')
    }

    const res = await fetch(`http://localhost:8082/api/machines?${params.toString()}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    if (res.status === 401) {
      throw new Error('驗證已過期，請重新登入。')
    }
    if (res.status === 403) {
      throw new Error('您的權限不足。')
    }
    if (!res.ok) throw new Error('查詢失敗')
    const data = await res.json()

    console.log('搜尋結果:', data)

    searchResults.value = data
    showResults.value = true

    // 通知父組件：搜尋完成，隱藏原本列表
    emit('search-complete')
  } catch (err) {
    console.error('搜尋錯誤:', err)
    alert('查詢失敗，請稍後再試')
  } finally {
    searching.value = false
  }
}

function handleClear() {
  selectedStatus.value = ''
  searchText.value = ''
  searchResults.value = []
  showResults.value = false

  console.log('清除搜尋')
  emit('search-clear')
}
</script>

<template>
  <div class="machine-search">
    <!-- 搜尋表單：永遠顯示 -->
    <div class="search-box">
      <div class="form-row">
        <div class="form-group">
          <label>狀態：</label>
          <select v-model="selectedStatus" :disabled="searching">
            <option value="">-- 不限制 --</option>
            <option v-for="s in statusOptions" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>

        <div class="form-group">
          <label>關鍵字：</label>
          <input
            type="text"
            v-model="searchText"
            placeholder="機台名稱、ID、出廠編號..."
            :disabled="searching"
            @keyup.enter="handleSearch"
          />
        </div>
      </div>

      <div class="button-group">
        <button @click="handleSearch" :disabled="searching">
          {{ searching ? '查詢中...' : '🔍 查詢' }}
        </button>
        <button @click="handleClear" :disabled="searching">🧹 清除</button>
      </div>
    </div>

    <!-- 搜尋結果：只有搜尋後才顯示 -->
    <div v-if="showResults" class="search-results">
      <div class="result-info">
        <h3>🔎 查詢結果：共 {{ searchResults.length }} 筆</h3>
        <p class="search-conditions">
          <span v-if="selectedStatus">狀態：{{ selectedStatus }}</span>
          <span v-if="selectedStatus && searchText"> + </span>
          <span v-if="searchText">關鍵字：{{ searchText }}</span>
        </p>
      </div>

      <!-- 沒有結果 -->
      <div v-if="searchResults.length === 0" class="no-results">📭 沒有符合的機台</div>

      <!-- 搜尋結果表格 -->
      <table v-else class="results-table">
        <thead>
          <tr>
            <th>機台ID</th>
            <th>機台名稱</th>
            <th>出廠編號</th>
            <th>運行狀態</th>
            <th>機台位置</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="machine in searchResults" :key="machine.machineId">
            <td>
              <strong>#{{ machine.machineId }}</strong>
            </td>
            <td>{{ machine.machineName }}</td>
            <td>
              <code>{{ machine.serialNumber }}</code>
            </td>
            <td>
              <span :class="['status', getStatusClass(machine.mstatus)]">
                {{ getStatusIcon(machine.mstatus) }} {{ machine.mstatus }}
              </span>
            </td>
            <td>📍 {{ machine.machineLocation }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.machine-search {
  margin-bottom: 20px;
}

.search-box {
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #f8f9fa;
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-group label {
  font-weight: bold;
  color: #2c3e50;
  min-width: 60px;
}

input,
select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

select {
  min-width: 120px;
  background-color: white;
}

input {
  min-width: 200px;
  background-color: white;
}

input:focus,
select:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 5px rgba(52, 152, 219, 0.3);
}

input:disabled,
select:disabled {
  background-color: #e9ecef;
  cursor: not-allowed;
  color: #6c757d;
}

.button-group {
  display: flex;
  gap: 10px;
}

button {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  font-size: 14px;
  transition: all 0.3s ease;
}

button:first-child {
  background: #3498db;
  color: white;
}

button:first-child:hover:not(:disabled) {
  background: #2980b9;
  transform: translateY(-1px);
}

button:last-child {
  background: #95a5a6;
  color: white;
}

button:last-child:hover:not(:disabled) {
  background: #7f8c8d;
  transform: translateY(-1px);
}

button:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
}

.search-results {
  animation: fadeIn 0.3s ease-in-out;
}

.result-info {
  background: #e8f4fd;
  border: 1px solid #bee5eb;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
}

.result-info h3 {
  margin: 0 0 8px 0;
  color: #0c5460;
  font-size: 18px;
}

.search-conditions {
  margin: 0;
  color: #155724;
  font-size: 14px;
  font-weight: 500;
}

.no-results {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
  font-size: 18px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px dashed #dee2e6;
}

.results-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.results-table th {
  background: #34495e;
  color: white;
  padding: 15px;
  text-align: left;
  font-weight: bold;
  font-size: 14px;
}

.results-table td {
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
  font-size: 14px;
}

.results-table tr:hover {
  background-color: #f8f9fa;
}

.status {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  display: inline-block;
}

.status.running {
  background: #d4edda;
  color: #155724;
}

.status.maintenance {
  background: #fff3cd;
  color: #856404;
}

.status.stopped {
  background: #f8d7da;
  color: #721c24;
}

.status.unknown {
  background: #f8f9fa;
  color: #6c757d;
  border: 1px dashed #dee2e6;
}

code {
  background: #f1f2f6;
  padding: 3px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  color: #2c3e50;
  font-size: 13px;
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

/* 響應式設計 */
@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 15px;
  }

  .form-group {
    flex-direction: column;
    align-items: stretch;
  }

  .form-group label {
    min-width: auto;
    margin-bottom: 5px;
  }

  input {
    min-width: auto;
  }

  .button-group {
    justify-content: center;
  }
}
</style>
