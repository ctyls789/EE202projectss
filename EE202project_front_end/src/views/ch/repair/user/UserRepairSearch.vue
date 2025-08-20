<script setup>
import { ref } from 'vue'

const emit = defineEmits(['search-complete', 'search-clear'])

const searchId = ref('')
const searching = ref(false)
const searchResultList = ref([])
const showResult = ref(false)

function getStatusClass(status) {
  switch (status) {
    case '待處理':
      return 'status-pending'
    case '進行中':
      return 'status-in-progress'
    case '已完成':
      return 'status-completed'
    default:
      return 'status-unknown'
  }
}

function getStatusIcon(status) {
  switch (status) {
    case '待處理':
      return '⏳'
    case '進行中':
      return '🔧'
    case '已完成':
      return '✅'
    default:
      return '❓'
  }
}

async function handleSearch() {
  const id = String(searchId.value).trim()
  if (!id) {
    alert('請輸入報修編號')
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
    const res = await fetch(`http://localhost:8082/api/repair/${id}`, {
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
    if (!res.ok) {
      if (res.status === 404) {
        alert('找不到該筆報修資料')
        searchResultList.value = []
        showResult.value = false
        return
      }
      throw new Error('查詢失敗')
    }
    const data = await res.json()
    searchResultList.value = data ? [data] : []
    showResult.value = searchResultList.value.length > 0
    emit('search-complete')
  } catch (err) {
    console.error(err)
    alert('查詢失敗，請稍後再試')
  } finally {
    searching.value = false
  }
}

function handleClear() {
  searchId.value = ''
  searchResultList.value = []
  showResult.value = false
  emit('search-clear')
}
</script>

<template>
  <div class="repair-search">
    <!-- 搜尋表單 -->
    <div class="search-box">
      <div class="form-row">
        <div class="form-group">
          <label>報修編號：</label>
          <input
            type="text"
            v-model="searchId"
            placeholder="請輸入報修編號"
            :disabled="searching"
            @keyup.enter="handleSearch"
            pattern="\d*"
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

    <!-- 搜尋結果 -->
    <div v-if="showResult" class="search-results">
      <div class="result-info">
        <h3>🔎 查詢結果</h3>
      </div>

      <table class="repair-table">
        <thead>
          <tr>
            <th>報修編號</th>
            <th>機台編號</th>
            <th>報修人員</th>
            <th>狀態</th>
            <th>報修時間</th>
            <th>描述</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="repair in searchResultList" :key="repair.repairId">
            <td>
              <strong>#{{ repair.repairId }}</strong>
            </td>
            <td>{{ repair.machineId }}</td>
            <td>👤 {{ repair.employeeId }}</td>
            <td>
              <span class="status-badge" :class="getStatusClass(repair.repairStatus)">
                {{ getStatusIcon(repair.repairStatus) }} {{ repair.repairStatus }}
              </span>
            </td>
            <td>🕒 {{ new Date(repair.repairTime).toLocaleString() }}</td>
            <td class="description">{{ repair.repairDescription }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
/* 這裡請直接用你提供的列表頁 repair-table 相關 CSS，例： */
.repair-search {
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

/* 搜尋結果表格，跟列表頁風格一致 */
.repair-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  min-width: 800px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.repair-table th {
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

.repair-table td {
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
  font-size: 14px;
  vertical-align: middle;
  color: black;
}

.repair-table tr:hover {
  background-color: #f8f9fa;
}

.repair-table tr:last-child td {
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

.status-in-progress {
  background: #cce5ff;
  color: #004085;
}

.status-completed {
  background: #d4edda;
  color: #155724;
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
  .repair-table {
    font-size: 12px;
    min-width: 600px;
  }

  .repair-table th,
  .repair-table td {
    padding: 8px;
  }

  .description {
    max-width: 150px;
  }
}
</style>
