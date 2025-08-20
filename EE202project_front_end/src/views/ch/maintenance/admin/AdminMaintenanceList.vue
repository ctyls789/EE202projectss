<script setup>
import { onMounted, ref, computed } from 'vue'
import MaintenanceEditModal from './MaintenanceEditModal.vue'
import MaintenanceDeleteModal from './MaintenanceDeleteModal.vue'

const maintenanceList = ref(null)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const selectedMaintenance = ref(null)

// 狀態樣式與圖示
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
const statusOptions = computed(() => Object.keys(statusIconMap))

async function fetchMaintenance() {
  try {
    const userJson = localStorage.getItem('user')
    const user = userJson ? JSON.parse(userJson) : null
    const token = user ? user.token : null

    if (!token) {
      throw new Error('未找到驗證權杖，請先登入。')
    }
    let res = await fetch('http://localhost:8082/api/maintenance', {
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
    if (!res.ok) throw new Error('載入失敗')
    maintenanceList.value = await res.json()
  } catch (err) {
    console.error('載入失敗：', err)
    maintenanceList.value = []
  }
}

onMounted(fetchMaintenance)

function openEditModal(item) {
  selectedMaintenance.value = { ...item }
  showEditModal.value = true
}
function openDeleteModal(item) {
  selectedMaintenance.value = item
  showDeleteModal.value = true
}
async function handleUpdated() {
  showEditModal.value = false
  await fetchMaintenance()
}
async function handleDeleted() {
  showDeleteModal.value = false
  await fetchMaintenance()
}
</script>

<template>
  <div class="maintenance-list">
    <div v-if="maintenanceList === null" class="loading">📡 資料載入中...</div>
    <div v-else-if="maintenanceList.length === 0" class="no-data">📭 目前沒有保養記錄</div>

    <div v-else class="table-container">
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
          <tr v-for="item in maintenanceList" :key="item.scheduleId">
            <td>
              <strong>#{{ item.scheduleId }}</strong>
            </td>
            <td>{{ item.machineId }}</td>
            <td>👤 {{ item.employeeId }}</td>
            <td class="description">{{ item.maintenanceDescription }}</td>
            <td>
              <span class="status-badge" :class="statusClassMap[item.maintenanceStatus]">
                {{ statusIconMap[item.maintenanceStatus] || '❓' }}
                {{ item.maintenanceStatus }}
              </span>
            </td>
            <td>🕒 {{ new Date(item.scheduleDate).toLocaleString() }}</td>
            <td>
              <button @click="openEditModal(item)">編輯</button>
              <button @click="openDeleteModal(item)">刪除</button>
            </td>
          </tr>
        </tbody>
      </table>
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
  .maintenance-table td.description {
    max-width: 150px;
  }
  .loading,
  .no-data {
    font-size: 16px;
    padding: 40px 15px;
  }
}
</style>
