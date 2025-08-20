<script setup>
import { onMounted, ref } from 'vue'
import RepairEditModal from './RepairEditModal.vue'
import RepairDeleteModal from './RepairDeleteModal.vue'

const repairList = ref([])
const loading = ref(true)
const error = ref(null)

const showEditModal = ref(false)
const showDeleteModal = ref(false)
const selectedRepair = ref(null)

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

async function fetchRepairs() {
  loading.value = true
  error.value = null
  try {
    const userJson = localStorage.getItem('user');
    const user = userJson ? JSON.parse(userJson) : null;
    const token = user ? user.token : null;

    if (!token) {
      throw new Error('未找到驗證權杖，請先登入。');
    }

    const fetchOptions = {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    };

    const res = await fetch('http://localhost:8082/api/repair', fetchOptions);

    if (res.status === 401) {
      throw new Error('驗證已過期，請重新登入。');
    }
    if (res.status === 403) {
      throw new Error('您的權限不足，無法查看此內容。');
    }
    if (!res.ok) {
      throw new Error(`網路回應錯誤，狀態碼: ${res.status}`);
    }

    repairList.value = await res.json();
  } catch (err) {
    console.error('資料載入失敗:', err);
    error.value = err.message || '資料載入失敗，請稍後再試';
  } finally {
    loading.value = false
  }
}

onMounted(fetchRepairs)

function openEditModal(repair) {
  selectedRepair.value = {
    repairId: repair.repairId,
    machineId: repair.machineId,
    employeeId: repair.employeeId,
    repairStatus: repair.repairStatus || repair.status,
    repairDescription: repair.repairDescription || repair.description,
    repairTime: repair.repairTime || repair.reportedAt,
  }
  showEditModal.value = true
}

function openDeleteModal(repair) {
  selectedRepair.value = { ...repair }
  showDeleteModal.value = true
}

async function handleUpdated() {
  showEditModal.value = false
  await fetchRepairs()
}

async function handleDeleted() {
  showDeleteModal.value = false
  await fetchRepairs()
}
</script>

<template>
  <div class="repair-list">
    <div v-if="loading" class="loading">📡 資料載入中...</div>
    <div v-else-if="error" class="error">❌ {{ error }}</div>
    <div v-else-if="repairList.length === 0" class="no-data">📭 無維修資料</div>
    <div v-else class="table-container">
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
          <tr v-for="repair in repairList" :key="repair.repairId">
            <td>
              <strong>#{{ repair.repairId }}</strong>
            </td>
            <td>{{ repair.machineId }}</td>
            <td>👤 {{ repair.employeeId }}</td>
            <td>
              <span
                class="status-badge"
                :class="getStatusClass(repair.repairStatus || repair.status)"
              >
                {{ getStatusIcon(repair.repairStatus || repair.status) }}
                {{ repair.repairStatus || repair.status }}
              </span>
            </td>
            <td>🕒 {{ new Date(repair.repairTime || repair.reportedAt).toLocaleString() }}</td>
            <td class="description">{{ repair.repairDescription || repair.description }}</td>
            <td class="action-links">
              <button @click="openEditModal(repair)">✏️ 編輯</button>
              <!-- <button @click="openDeleteModal(repair)">🗑️ 刪除</button> -->
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <RepairEditModal
      v-if="showEditModal"
      :repair="selectedRepair"
      :status-options="Object.keys({ 待處理: '', 進行中: '', 已完成: '' })"
      @close="showEditModal = false"
      @updated="handleUpdated"
    />
    <!-- <RepairDeleteModal
      v-if="showDeleteModal"
      :repair="selectedRepair"
      @close="showDeleteModal = false"
      @deleted="handleDeleted"
    /> -->
  </div>
</template>

<style scoped>
.repair-list {
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

  .loading,
  .error,
  .no-data {
    font-size: 16px;
    padding: 40px 15px;
  }
}
</style>
