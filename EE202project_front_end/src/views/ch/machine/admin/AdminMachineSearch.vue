<script setup>
import { ref, computed } from 'vue'
import MachineEditModal from './MachineEditModal.vue'
import MachineDeleteModal from './MachineDeleteModal.vue'

const emit = defineEmits(['search-complete', 'search-clear'])

//將現有資料轉為選項格式
const statusOptions = computed(() => Object.keys(statusIconMap))
const selectedStatus = ref('')
const searchText = ref('')
const searching = ref(false)
const searchResults = ref([])
const showResults = ref(false)

const selectedMachine = ref(null)
const showEditModal = ref(false)
const showDeleteModal = ref(false)

// 狀態樣式
const statusClassMap = {
  運行中: 'running',
  維護中: 'maintenance',
  停機: 'stopped',
}
const statusIconMap = {
  運行中: '🟢',
  維護中: '🟡',
  停機: '🔴',
}

async function handleSearch() {
  const params = new URLSearchParams()
  if (selectedStatus.value) params.append('statusFilter', selectedStatus.value)
  if (searchText.value.trim()) params.append('search', searchText.value.trim())

  if (!params.toString()) {
    alert('請至少選擇狀態或輸入關鍵字！')
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
    searchResults.value = data
    showResults.value = true

    // 通知父組件：搜尋完成，隱藏原本列表
    emit('search-complete')
  } catch (err) {
    console.error(err)
    alert('查詢失敗，請稍後再試')
  } finally {
    searching.value = false
  }
}

function handleClear() {
  // 清空所有搜尋條件和結果
  selectedStatus.value = ''
  searchText.value = ''
  searchResults.value = []
  showResults.value = false

  // 通知父組件：清除搜尋，顯示原本列表
  emit('search-clear')
}

function openEditModal(machine) {
  selectedMachine.value = { ...machine }
  showEditModal.value = true
}
function openDeleteModal(machine) {
  selectedMachine.value = machine
  showDeleteModal.value = true
}
async function handleUpdated() {
  showEditModal.value = false
  // 重新執行搜尋以更新結果
  if (showResults.value) {
    await handleSearch()
  }
}
async function handleDeleted() {
  showDeleteModal.value = false
  // 重新執行搜尋以更新結果
  if (showResults.value) {
    await handleSearch()
  }
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
            placeholder="機台名稱、ID、位置..."
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

      <!-- 資料表 -->
      <table>
        <thead>
          <tr>
            <th>機台ID</th>
            <th>機台名稱</th>
            <th>出廠編號</th>
            <th>運行狀態</th>
            <th>機台位置</th>
            <th>管理操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="searchResults.length === 0">
            <td colspan="6" style="text-align: center; padding: 40px">📂 沒有機台資料</td>
          </tr>
          <tr v-for="machine in searchResults" :key="machine.machineId">
            <td>
              <strong>#{{ machine.machineId }}</strong>
            </td>
            <td>{{ machine.machineName }}</td>
            <td>
              <code>{{ machine.serialNumber }}</code>
            </td>
            <td>
              <span :class="['status', statusClassMap[machine.mstatus] || '']">
                {{ statusIconMap[machine.mstatus] || '❓' }} {{ machine.mstatus }}
              </span>
            </td>
            <td>📍 {{ machine.machineLocation }}</td>
            <td class="action-links">
              <button @click="openEditModal(machine)">✏️ 編輯</button>
              <button @click="openDeleteModal(machine)">🗑️ 刪除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 編輯 Modal -->
    <MachineEditModal
      v-if="showEditModal"
      :machine="selectedMachine"
      :status-options="statusOptions"
      @close="showEditModal = false"
      @updated="handleUpdated"
    />
    <!-- 刪除 Modal -->
    <MachineDeleteModal
      v-if="showDeleteModal"
      :machine="selectedMachine"
      @close="showDeleteModal = false"
      @deleted="handleDeleted"
    />
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

/* 統一表格樣式與 AdminMachineList.vue 相同 */
table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

th {
  background: #34495e;
  color: white;
  padding: 15px;
  text-align: left;
  font-weight: bold;
  font-size: 14px;
}

td {
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
  font-size: 14px;
}

tr:hover {
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

.action-links {
  white-space: nowrap;
}

code {
  background: #f1f2f6;
  padding: 3px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  color: #2c3e50;
  font-size: 13px;
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

/* 搜尋表單特有樣式 */
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

.button-group button {
  padding: 10px 20px;
  font-weight: bold;
}

.button-group button:first-child {
  background: #3498db;
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
