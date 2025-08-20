<script setup>
import { onMounted, ref } from 'vue'

let machineList = ref(null)

// 取得狀態樣式
function getStatusClass(status) {
  switch (status) {
    case '運行中':
      return 'running'
    case '維護中':
      return 'maintenance'
    case '停機':
      return 'stopped'
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
  }
}

onMounted(async function () {
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
    if (!response.ok) {
      throw new Error('載入失敗')
    }
    let data = await response.json()
    machineList.value = data
  } catch (error) {
    console.error('載入失敗:', error)
    machineList.value = []
  }
})
</script>

<template>
  <div v-if="machineList === null">📡 資料載入中...</div>
  <div v-else>
    <table>
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
        <!-- 沒有資料時的顯示 -->
        <tr v-if="machineList.length === 0">
          <td colspan="6" style="text-align: center; padding: 40px">📂 沒有機台資料</td>
        </tr>

        <!-- 機台資料列表 -->
        <tr v-for="machine in machineList" :key="machine.machineId">
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

table th {
  background: #34495e;
  color: white;
  padding: 15px;
  text-align: left;
  font-weight: bold;
  font-size: 14px;
}

table td {
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
  font-size: 14px;
}

table tr:hover {
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
</style>
