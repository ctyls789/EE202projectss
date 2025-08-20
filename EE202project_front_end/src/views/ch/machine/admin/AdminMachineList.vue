<script setup>
import { onMounted, ref, computed } from 'vue'
import MachineEditModal from './MachineEditModal.vue'
import MachineDeleteModal from './MachineDeleteModal.vue'

// 機台列表
const machineList = ref(null)
// 視窗開關
const showEditModal = ref(false)
const showDeleteModal = ref(false)
// 選中的機台
const selectedMachine = ref(null)
//將現有資料轉為選項格式
const statusOptions = computed(() => Object.keys(statusIconMap))
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

// 載入資料
async function fetchMachines() {
  try {
    const userJson = localStorage.getItem('user')
    const user = userJson ? JSON.parse(userJson) : null
    const token = user ? user.token : null

    if (!token) {
      throw new Error('未找到驗證權杖，請先登入。')
    }
    let response = await fetch('http://localhost:8082/api/machines', {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    if (response.status === 401) {
      throw new Error('驗證已過期，請重新登入。')
    }
    if (response.status === 403) {
      throw new Error('您的權限不足。')
    }
    if (!response.ok) throw new Error('載入失敗')
    machineList.value = await response.json()
  } catch (error) {
    console.error('載入失敗:', error)
    machineList.value = []
  }
}
// 初始化載入,開啟視窗
onMounted(fetchMachines)
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
  await fetchMachines()
}
async function handleDeleted() {
  showDeleteModal.value = false
  await fetchMachines()
}
</script>

<template>
  <div v-if="machineList === null">📡 資料載入中...</div>
  <div v-else>
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
        <tr v-if="machineList.length === 0">
          <td colspan="6" style="text-align: center; padding: 40px">📂 沒有機台資料</td>
        </tr>
        <tr v-for="machine in machineList" :key="machine.machineId">
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
</style>
