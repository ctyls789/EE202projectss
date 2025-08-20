<script setup>
import { ref } from 'vue'

const emit = defineEmits(['search-complete', 'search-clear'])

const searchText = ref('')
const searching = ref(false)
const searchResults = ref([])
const showResults = ref(false)

const statusClassMap = {
  待處理: 'status-pending',
  已排程: 'status-scheduled',
  進行中: 'status-in-progress',
  已完成: 'status-completed',
  已取消: 'status-cancelled',
}

const statusIconMap = {
  待處理: '⏳',
  已排程: '📅',
  進行中: '🔧',
  已完成: '✅',
  已取消: '❌',
}

const getStatusClass = (status) => statusClassMap[status] || 'status-unknown'
const getStatusIcon = (status) => statusIconMap[status] || '❓'

// ✅ 單一編號查詢
async function handleSearch() {
  const id = searchText.value

  if (!id || isNaN(id)) {
    alert('請輸入有效的保養單編號（數字）')
    return
  }

  try {
    searching.value = true
    const userJson = localStorage.getItem('user')
    const user = userJson ? JSON.parse(userJson) : null
    const token = user ? user.token : null

    if (!token) {
      throw new Error('未找到驗證權杖，請先登入。')
    }
    const res = await fetch(`http://localhost:8082/api/maintenance/${id}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    const text = await res.text()
    console.log('後端回傳:', text)

    if (res.status === 401) {
      throw new Error('驗證已過期，請重新登入。')
    }
    if (res.status === 403) {
      throw new Error('您的權限不足。')
    }
    if (!res.ok) {
      if (res.status === 404) {
        searchResults.value = []
        showResults.value = true
        return
      }
      throw new Error('查詢失敗')
    }

    const data = JSON.parse(text)
    // 將單筆結果包成陣列
    searchResults.value = [data]
    showResults.value = true
    emit('search-complete')
  } catch (err) {
    console.error(err)
    alert('查詢失敗，請稍後再試')
  } finally {
    searching.value = false
  }
}

function handleClear() {
  searchText.value = ''
  searchResults.value = []
  showResults.value = false
  emit('search-clear')
}
</script>

<template>
  <div class="maintenance-search">
    <!-- 單一欄位查詢 -->
    <div class="search-box">
      <div class="form-row">
        <div class="form-group">
          <label>保養單編號：</label>
          <input
            type="text"
            v-model="searchText"
            placeholder="請輸入保養單編號"
            :disabled="searching"
            @keyup.enter="handleSearch"
            autocomplete="off"
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

    <!-- 查詢結果 -->
    <div v-if="showResults" class="search-results">
      <div class="result-info">
        <h3>🔎 查詢結果：共 {{ searchResults.length }} 筆</h3>
        <p class="search-conditions">
          <span v-if="searchText">保養單編號：{{ searchText }}</span>
        </p>
      </div>

      <div v-if="searchResults.length === 0" class="no-results">📭 沒有符合的保養記錄</div>

      <table v-else class="maintenance-table">
        <thead>
          <tr>
            <th>保養單編號</th>
            <th>機台編號</th>
            <th>保養人員編號</th>
            <th>保養描述</th>
            <th>保養狀態</th>
            <th>預計保養日期</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="maintenance in searchResults" :key="maintenance.scheduleId">
            <td>
              <strong>#{{ maintenance.scheduleId }}</strong>
            </td>
            <td>{{ maintenance.machineId }}</td>
            <td>👤 {{ maintenance.employeeId }}</td>
            <td class="description">{{ maintenance.maintenanceDescription }}</td>
            <td>
              <span class="status-badge" :class="getStatusClass(maintenance.maintenanceStatus)">
                {{ getStatusIcon(maintenance.maintenanceStatus) }}
                {{ maintenance.maintenanceStatus }}
              </span>
            </td>
            <td>🕒 {{ new Date(maintenance.scheduleDate).toLocaleString() }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.maintenance-search {
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
  min-width: 80px;
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
  min-width: 250px;
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

.maintenance-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-width: 800px;
}

.maintenance-table th {
  background: #34495e;
  color: black;
  padding: 15px;
  text-align: left;
  font-weight: bold;
  font-size: 14px;
  position: sticky;
  top: 0;
  z-index: 1;
}

.maintenance-table td {
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
  font-size: 14px;
  vertical-align: middle;
  color: black;
}

.maintenance-table tr:hover {
  background-color: #f8f9fa;
}

.maintenance-table tr:last-child td {
  border-bottom: none;
}

.status-badge {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  display: inline-block;
  white-space: nowrap;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-scheduled {
  background: #d1ecf1;
  color: #0c5460;
}

.status-in-progress {
  background: #cce5ff;
  color: #004085;
}

.status-completed {
  background: #d4edda;
  color: #155724;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
}

.status-unknown {
  background: #f8f9fa;
  color: #6c757d;
  border: 1px dashed #dee2e6;
}

.description {
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .maintenance-table {
    font-size: 12px;
    min-width: 600px;
  }

  .maintenance-table th,
  .maintenance-table td {
    padding: 8px;
  }

  .description {
    max-width: 150px;
  }

  .loading,
  .error,
  .no-data {
    font-size: 16px;
    padding: 40px 15px;
  }
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
