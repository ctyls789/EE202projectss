<script setup>
import { ref, computed } from 'vue'
import MaintenanceEditModal from './MaintenanceEditModal.vue'
import MaintenanceDeleteModal from './MaintenanceDeleteModal.vue'

const emit = defineEmits(['search-complete', 'search-clear'])

// 狀態樣式與圖示（統一跟列表一樣）
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

// 將狀態選項從狀態圖示鍵名取出
const statusOptions = computed(() => Object.keys(statusIconMap))

const searchId = ref('')
const searching = ref(false)
const searchResult = ref(null)
const showResults = ref(false)

const selectedMaintenance = ref(null)
const showEditModal = ref(false)
const showDeleteModal = ref(false)

async function handleSearch() {
  if (!searchId.value || searchId.value === '') {
    alert('請輸入保養單編號！')
    return
  }

  const id = parseInt(searchId.value)
  if (isNaN(id) || id <= 0) {
    alert('請輸入有效的保養單編號！')
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

    if (res.status === 404) {
      alert('查無該筆保養資料')
      searchResult.value = null
      showResults.value = true
    } else if (res.status === 401) {
      throw new Error('驗證已過期，請重新登入。')
    } else if (res.status === 403) {
      throw new Error('您的權限不足。')
    } else if (!res.ok) {
      throw new Error('查詢失敗')
    } else {
      const data = await res.json()
      searchResult.value = data
      showResults.value = true
      emit('search-complete')
    }
  } catch (err) {
    console.error(err)
    alert('查詢失敗，請稍後再試')
  } finally {
    searching.value = false
  }
}

function handleClear() {
  searchId.value = ''
  searchResult.value = null
  showResults.value = false
  emit('search-clear')
}

function openEditModal(maintenance) {
  selectedMaintenance.value = { ...maintenance }
  showEditModal.value = true
}

function openDeleteModal(maintenance) {
  selectedMaintenance.value = { ...maintenance }
  showDeleteModal.value = true
}

async function handleUpdated() {
  showEditModal.value = false
  if (showResults.value) {
    await handleSearch()
  }
}

async function handleDeleted() {
  showDeleteModal.value = false
  if (showResults.value) {
    // 刪除後清除搜尋結果
    handleClear()
  }
}
</script>

<template>
  <div class="maintenance-list">
    <!-- 搜尋表單 -->
    <div class="search-box">
      <div class="form-row">
        <div class="form-group">
          <label>保養單編號：</label>
          <input
            type="number"
            v-model="searchId"
            placeholder="請輸入保養單編號..."
            :disabled="searching"
            @keyup.enter="handleSearch"
            min="1"
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
        <h3 v-if="searchResult">🔎 查詢結果：找到保養單 #{{ searchId }}</h3>
        <h3 v-else>🔎 查詢結果：查無資料</h3>
      </div>

      <div v-if="searchResult" class="table-container">
        <table class="maintenance-table">
          <thead>
            <tr>
              <th>保養單編號</th>
              <th>機台編號</th>
              <th>保養人員編號</th>
              <th>保養描述</th>
              <th>保養狀態</th>
              <th>預計保養日期</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>
                <strong>#{{ searchResult.scheduleId }}</strong>
              </td>
              <td>{{ searchResult.machineId }}</td>
              <td>👤 {{ searchResult.employeeId }}</td>
              <td class="description">{{ searchResult.maintenanceDescription }}</td>
              <td>
                <span
                  class="status-badge"
                  :class="statusClassMap[searchResult.maintenanceStatus] || ''"
                >
                  {{ statusIconMap[searchResult.maintenanceStatus] || '❓' }}
                  {{ searchResult.maintenanceStatus }}
                </span>
              </td>
              <td>🕒 {{ new Date(searchResult.scheduleDate).toLocaleString() }}</td>
              <td>
                <button @click="openEditModal(searchResult)">編輯</button>
                <button @click="openDeleteModal(searchResult)">刪除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="no-result">
        <p>📭 沒有找到編號為 #{{ searchId }} 的保養記錄</p>
      </div>
    </div>

    <!-- Modal -->
    <MaintenanceEditModal
      v-if="showEditModal"
      :maintenance="selectedMaintenance"
      :status-options="statusOptions"
      @close="showEditModal = false"
      @updated="handleUpdated"
    />
    <MaintenanceDeleteModal
      v-if="showDeleteModal"
      :maintenance="selectedMaintenance"
      @close="showDeleteModal = false"
      @deleted="handleDeleted"
    />
  </div>
</template>

<style scoped>
.maintenance-list {
  margin-top: 20px;
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
  min-width: 100px;
}

input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s ease;
  min-width: 200px;
  background-color: white;
}

input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 5px rgba(52, 152, 219, 0.3);
}

input:disabled {
  background-color: #e9ecef;
  cursor: not-allowed;
  color: #6c757d;
}

.button-group {
  display: flex;
  gap: 10px;
}

.button-group button {
  padding: 10px 20px;
  font-weight: bold;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: 0.3s ease;
}

.button-group button:first-child:hover:not(:disabled) {
  background: #2980b9;
  transform: translateY(-1px);
}

.button-group button:last-child {
  background: #95a5a6;
}

.button-group button:last-child:hover:not(:disabled) {
  background: #7f8c8d;
  transform: translateY(-1px);
}

.button-group button:disabled {
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
  margin: 0;
  color: #0c5460;
  font-size: 18px;
}

.no-result {
  text-align: center;
  padding: 40px;
  background: #f8f9fa;
  border: 2px dashed #dee2e6;
  border-radius: 8px;
  color: #6c757d;
  font-size: 16px;
}

.loading,
.no-data {
  text-align: center;
  padding: 60px 20px;
  font-size: 18px;
  border-radius: 8px;
  margin: 20px 0;
}
.loading {
  background: #e8f4fd;
  color: #0c5460;
  border: 1px solid #bee5eb;
}
.no-data {
  background: #f8f9fa;
  color: #6c757d;
  border: 2px dashed #dee2e6;
}

.table-container {
  overflow-x: auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.maintenance-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
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
  text-align: center;
}

.maintenance-table td.description {
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: left;
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

button {
  margin: 0 4px;
  padding: 6px 12px;
  border: none;
  background-color: #3498db;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover {
  background-color: #2980b9;
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
