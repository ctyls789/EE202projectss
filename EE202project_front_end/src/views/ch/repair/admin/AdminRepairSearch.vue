<script setup>
import { ref } from 'vue'
import RepairEditModal from './RepairEditModal.vue'
import RepairDeleteModal from './RepairDeleteModal.vue'

const emit = defineEmits(['search-complete', 'search-clear'])

const searchId = ref('')
const searching = ref(false)
const searchResult = ref(null)
const showResults = ref(false)

const showEditModal = ref(false)
const showDeleteModal = ref(false)
const selectedRepair = ref(null)

async function handleSearch() {
  const id = searchId.value.trim()

  if (!id || isNaN(Number(id))) {
    alert('請輸入有效的報修編號')
    return
  }

  searching.value = true
  searchResult.value = null

  try {
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
    if (res.status === 404) {
      alert('查無此報修編號')
      showResults.value = true
      return
    }
    if (res.status === 401) {
      throw new Error('驗證已過期，請重新登入。')
    }
    if (res.status === 403) {
      throw new Error('您的權限不足。')
    }
    if (!res.ok) {
      throw new Error('查詢失敗')
    }
    const data = await res.json()
    searchResult.value = data
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
  searchId.value = ''
  searchResult.value = null
  showResults.value = false
  emit('search-clear')
}

function openEditModal(repair) {
  selectedRepair.value = { ...repair }
  showEditModal.value = true
}

function openDeleteModal(repair) {
  selectedRepair.value = { ...repair }
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
  handleClear()
}
</script>

<template>
  <div class="repair-search">
    <div class="search-box">
      <div class="form-row">
        <div class="form-group">
          <label for="searchId">報修編號：</label>
          <input
            id="searchId"
            type="text"
            v-model="searchId"
            placeholder="請輸入報修編號"
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

    <div v-if="showResults" class="search-results">
      <div v-if="searchResult" class="table-container">
        <table class="repair-table">
          <thead>
            <tr>
              <th>報修編號</th>
              <th>機台編號</th>
              <th>報修人員</th>
              <th>狀態</th>
              <th>報修時間</th>
              <th>描述</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>
                <strong>#{{ searchResult.repairId }}</strong>
              </td>
              <td>{{ searchResult.machineId }}</td>
              <td>👤 {{ searchResult.employeeId }}</td>
              <td>
                <span class="status-badge">
                  {{ searchResult.repairStatus }}
                </span>
              </td>
              <td>🕒 {{ new Date(searchResult.repairTime).toLocaleString() }}</td>
              <td class="description">{{ searchResult.repairDescription }}</td>
              <td>
                <button @click="openEditModal(searchResult)">✏️ 編輯</button>
                <button @click="openDeleteModal(searchResult)">🗑️ 刪除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="no-data">📭 無資料</div>
    </div>

    <RepairEditModal
      v-if="showEditModal"
      :repair="selectedRepair"
      :status-options="['待處理', '進行中', '已完成']"
      @close="showEditModal = false"
      @updated="handleUpdated"
    />
    <RepairDeleteModal
      v-if="showDeleteModal"
      :repair="selectedRepair"
      @close="showDeleteModal = false"
      @deleted="handleDeleted"
    />
  </div>
</template>

<style scoped>
.repair-search {
  margin-top: 20px;
}

.loading,
.error,
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

.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.no-data {
  background: #f8f9fa;
  color: #6c757d;
  border: 2px dashed #dee2e6;
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

input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 200px;
  background-color: white;
  transition: border-color 0.3s ease;
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

button {
  background: #3498db;
  color: white;
  border: none;
  padding: 8px 14px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: 0.3s ease;
}

button:hover {
  background: #2980b9;
}

button:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.table-container {
  overflow-x: auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.repair-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  min-width: 800px;
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

.action-links {
  white-space: nowrap;
}

.action-links button {
  background: #3498db;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: 0.3s ease;
  margin-right: 6px;
}

.action-links button:hover {
  background: #2980b9;
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

  .loading,
  .error,
  .no-data {
    font-size: 16px;
    padding: 40px 15px;
  }
}
</style>
