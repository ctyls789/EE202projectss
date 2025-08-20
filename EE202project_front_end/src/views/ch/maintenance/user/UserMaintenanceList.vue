<script setup>
import { onMounted, ref } from 'vue'

const maintenanceList = ref([])
const loading = ref(true)
const error = ref(null)

// 取得狀態樣式
function getStatusClass(status) {
  switch (status) {
    case '待處理':
      return 'status-pending'
    case '已排程':
      return 'status-scheduled'
    case '進行中':
      return 'status-in-progress'
    case '已完成':
      return 'status-completed'
    case '已取消':
      return 'status-cancelled'
    default:
      return 'status-unknown'
  }
}

// 取得狀態圖示
function getStatusIcon(status) {
  switch (status) {
    case '待處理':
      return '⏳'
    case '已排程':
      return '📅'
    case '進行中':
      return '🔧'
    case '已完成':
      return '✅'
    case '已取消':
      return '❌'
    default:
      return '❓'
  }
}

onMounted(async () => {
  loading.value = true
  error.value = null
  try {
    const userJson = localStorage.getItem('user')
    const user = userJson ? JSON.parse(userJson) : null
    const token = user ? user.token : null

    if (!token) {
      throw new Error('未找到驗證權杖，請先登入。')
    }
    const res = await fetch('http://localhost:8082/api/maintenance', {
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
    if (!res.ok) throw new Error('Network response not ok')
    const data = await res.json()
    console.log('抓到的資料:', data)
    maintenanceList.value = data
  } catch (e) {
    console.error(e)
    error.value = '資料載入失敗，請稍後再試'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="maintenance-list">
    <!-- 載入中狀態 -->
    <div v-if="loading" class="loading">📡 資料載入中...</div>

    <!-- 錯誤狀態 -->
    <div v-else-if="error" class="error">❌ {{ error }}</div>

    <!-- 無資料狀態 -->
    <div v-else-if="maintenanceList.length === 0" class="no-data">📭 目前沒有保養記錄</div>

    <!-- 資料表格 -->
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
          </tr>
        </thead>
        <tbody>
          <tr v-for="maintenance in maintenanceList" :key="maintenance.scheduleId">
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
.maintenance-list {
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
</style>
