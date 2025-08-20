<template>
  <div class="permission-management">
    <!-- 頂部工具欄 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-title">
          <h3>權限管理</h3>
          <p>統一管理員工角色與權限</p>
        </div>
        <div class="toolbar-actions">
          <el-input
            v-model="searchText"
            placeholder="搜尋員工姓名或編號"
            prefix-icon="Search"
            style="width: 280px; margin-right: 12px"
            clearable
            @keyup.enter="focusFirstEmployee"
          />
        </div>
      </div>
    </el-card>

    <!-- 主要內容區域 -->
    <div class="main-content">
      <!-- 左側員工列表 -->
      <el-card class="employee-list-card">
        <template #header>
          <div class="card-header">
            <span>員工列表</span>
            <div class="employee-stats">
              <el-badge :value="filteredEmployees.length" class="item" type="primary" />
              <span v-if="searchText" class="filter-info">
                (共 {{ employeeUsers.length }} 位員工)
              </span>
            </div>
          </div>
        </template>

        <div class="employee-list" v-loading="loadingUsers">
          <div
            v-for="employee in filteredEmployees"
            :key="employee.employeeUserId"
            class="employee-item"
            :class="{ selected: selectedEmployee?.employeeUserId === employee.employeeUserId }"
            @click="selectEmployee(employee)"
          >
            <div class="employee-info">
              <div class="employee-name">{{ employee.firstName }} {{ employee.lastName }}</div>
              <div class="employee-details">
                <span class="employee-number">{{ employee.employeeNumber }}</span>
                <el-tag
                  :type="employee.isActive ? 'success' : 'danger'"
                  size="small"
                  style="margin-left: 8px"
                >
                  {{ employee.isActive ? '啟用' : '停用' }}
                </el-tag>
              </div>
              <div class="employee-roles">
                <el-tag
                  v-for="role in employee.roles || []"
                  :key="role"
                  :type="getRoleTagType(role)"
                  size="small"
                  style="margin: 2px 4px 2px 0"
                >
                  {{ getRoleDisplayName(role) }}
                </el-tag>
                <span v-if="!employee.roles || employee.roles.length === 0" class="no-roles">
                  尚未分配角色
                </span>
              </div>
            </div>
          </div>

          <div v-if="filteredEmployees.length === 0" class="no-data">
            <el-empty description="無符合條件的員工" />
          </div>
        </div>
      </el-card>

      <!-- 右側權限編輯區 -->
      <el-card class="permission-edit-card">
        <template #header>
          <div class="card-header">
            <span>權限編輯</span>
            <div v-if="selectedEmployee" class="permission-actions">
              <el-button
                type="primary"
                size="small"
                @click="saveEmployeeRoles"
                :loading="savingRoles"
                :disabled="!hasPermissionChanges"
              >
                儲存變更
              </el-button>
              <el-button size="small" @click="resetEmployeeRoles" :disabled="!hasPermissionChanges">
                重置
              </el-button>
            </div>
          </div>
        </template>

        <div v-if="!selectedEmployee" class="no-selection">
          <el-empty description="請選擇左側員工以編輯權限" />
        </div>

        <div v-else class="permission-editor">
          <!-- 員工資訊 -->
          <div class="employee-summary">
            <el-descriptions :column="2" size="small" border>
              <el-descriptions-item label="姓名">
                {{ selectedEmployee.firstName }} {{ selectedEmployee.lastName }}
              </el-descriptions-item>
              <el-descriptions-item label="員工編號">
                {{ selectedEmployee.employeeNumber }}
              </el-descriptions-item>
              <el-descriptions-item label="員工類型">
                <el-tag size="small">{{
                  getEmployeeTypeDisplay(selectedEmployee.employeeType)
                }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="電子郵件">
                {{ selectedEmployee.email || '未設定' }}
              </el-descriptions-item>
              <el-descriptions-item label="狀態">
                <el-tag :type="selectedEmployee.isActive ? 'success' : 'danger'" size="small">
                  {{ selectedEmployee.isActive ? '啟用' : '停用' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <el-divider content-position="left">角色與權限管理</el-divider>

          <!-- 角色選擇 -->
          <div class="role-selection">
            <el-form :model="roleForm" label-width="100px">
              <el-form-item label="分配角色">
                <el-select
                  v-model="employeeRoles"
                  multiple
                  placeholder="選擇員工角色"
                  style="width: 100%"
                  @change="updatePermissionPreview"
                  :loading="loadingRoles"
                >
                  <el-option
                    v-for="role in availableRoles"
                    :key="role.roleId"
                    :label="role.displayName || role.roleName"
                    :value="role.roleName"
                  >
                    <div style="display: flex; justify-content: space-between; align-items: center">
                      <span>{{ role.displayName || role.roleName }}</span>
                      <span style="color: #8492a6; font-size: 12px">{{ role.roleName }}</span>
                    </div>
                  </el-option>
                </el-select>
                <div style="margin-top: 8px; color: #909399; font-size: 12px">
                  選擇多個角色為員工分配相應的系統權限
                </div>
              </el-form-item>
            </el-form>
          </div>

          <!-- 權限預覽 -->
          <div class="permission-preview-section" v-if="calculatedPermissions.length > 0">
            <h4>權限預覽</h4>
            <div class="permission-preview">
              <el-tag
                v-for="permission in calculatedPermissions"
                :key="permission"
                type="info"
                size="small"
                style="margin: 4px 6px 4px 0"
              >
                {{ permission }}
              </el-tag>
            </div>
            <div style="margin-top: 8px; color: #909399; font-size: 12px">
              以上為選中角色包含的權限預覽
            </div>
          </div>

          <!-- 權限變更確認 -->
          <div class="permission-changes" v-if="hasPermissionChanges">
            <el-alert
              title="偵測到權限變更"
              type="warning"
              :description="`將為 ${selectedEmployee.firstName} ${selectedEmployee.lastName} 更新角色配置`"
              show-icon
              :closable="false"
              style="margin-bottom: 16px"
            />
          </div>

          <!-- 角色詳情 -->
          <div class="role-details" v-if="employeeRoles.length > 0">
            <h4>角色詳情</h4>
            <el-table :data="selectedRoleDetails" size="small" style="width: 100%">
              <el-table-column prop="roleName" label="角色代碼" width="120" />
              <el-table-column prop="displayName" label="角色名稱" width="150" />
              <el-table-column prop="description" label="描述" show-overflow-tooltip />
            </el-table>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, reactive } from 'vue'
import http from '../http-common'
import { ElMessage, ElMessageBox } from 'element-plus'

// 介面定義 - 參考 UserView.vue
interface EmployeeUserDto {
  employeeUserId?: number
  firstName: string
  lastName: string
  employeeNumber: string
  employeeType: 'INTERNAL' | 'SUPPLIER' | 'CONTRACTOR' | 'SYSTEM_ADMIN'
  birthDate?: string
  email?: string
  phone?: string
  photoPath?: string
  employeeDepartmentId?: number
  employeePositionId?: number
  managerEmployeeUserId?: number
  hireDate?: string
  terminationDate?: string
  username?: string
  passwordHash?: string
  isActive: boolean
  lastLogin?: string
  createdAt?: string
  updatedAt?: string
  roles?: string[] // 新增角色欄位
}

interface RoleDto {
  roleId: number
  roleName: string
  displayName: string
  description: string
  isActive: boolean
}

interface UpdateEmployeeRolesRequest {
  roleNames: string[]
  updatedBy: string
}

// 響應式數據
const searchText = ref('')
const loadingUsers = ref(false)
const loadingRoles = ref(false)
const savingRoles = ref(false)

const employeeUsers = ref<EmployeeUserDto[]>([])
const availableRoles = ref<RoleDto[]>([])
const selectedEmployee = ref<EmployeeUserDto | null>(null)
const employeeRoles = ref<string[]>([])
const calculatedPermissions = ref<string[]>([])

const roleForm = ref({
  selectedRoles: [] as string[],
})

// API 基礎路徑 - 參考 UserView.vue
const API_BASE_URL = '/employee-users'

// 計算屬性
const filteredEmployees = computed(() => {
  return employeeUsers.value.filter((employee) => {
    const fullName = `${employee.firstName} ${employee.lastName}`
    const matchesSearch =
      !searchText.value ||
      fullName.toLowerCase().includes(searchText.value.toLowerCase()) ||
      employee.employeeNumber.toLowerCase().includes(searchText.value.toLowerCase())

    return matchesSearch
  })
})

const selectedRoleDetails = computed(() => {
  return employeeRoles.value.map((roleName) => {
    const role = availableRoles.value.find((r) => r.roleName === roleName)
    return {
      roleName,
      displayName: role?.displayName || roleName,
      description: role?.description || '無描述',
    }
  })
})

const hasPermissionChanges = computed(() => {
  if (!selectedEmployee.value || !selectedEmployee.value.roles) return false

  const originalRoles = [...(selectedEmployee.value.roles || [])]
  const currentRoles = [...employeeRoles.value]

  // 檢查角色是否有變更
  if (originalRoles.length !== currentRoles.length) return true

  const sortedOriginal = originalRoles.sort()
  const sortedCurrent = currentRoles.sort()

  return !sortedOriginal.every((role, index) => role === sortedCurrent[index])
})

// 工具方法
const getRoleTagType = (role: string) => {
  const typeMap: Record<string, string> = {
    ADMIN: 'danger',
    HR_MANAGER: 'primary',
    WAREHOUSE_MANAGER: 'warning',
    WAREHOUSE_STAFF: 'info',
    WORKORDER_MANAGER: 'success',
    PURCHASE_MANAGER: 'warning',
    MACHINE_MANAGER: 'info',
    LEAVE_MANAGER: 'primary',
    EMPLOYEE_ACCOUNT_MANAGER: 'primary',
  }
  return typeMap[role] || 'info'
}

const getRoleDisplayName = (role: string) => {
  const nameMap: Record<string, string> = {
    ADMIN: '系統管理員',
    HR_MANAGER: 'HR管理者',
    WAREHOUSE_MANAGER: '倉庫管理員',
    WAREHOUSE_STAFF: '倉庫人員',
    WORKORDER_MANAGER: '工單管理員',
    PURCHASE_MANAGER: '採購管理員',
    MACHINE_MANAGER: '機台管理員',
    LEAVE_MANAGER: '請假管理員',
    EMPLOYEE_ACCOUNT_MANAGER: '員工帳號管理員',
  }
  return nameMap[role] || role
}

const getEmployeeTypeDisplay = (type: string) => {
  const typeMap: Record<string, string> = {
    INTERNAL: '內部',
    SUPPLIER: '供應商',
    CONTRACTOR: '承包商',
    SYSTEM_ADMIN: '系統管理員',
  }
  return typeMap[type] || type
}

// API 方法 - 參考 UserView.vue 的實現
const fetchEmployeeUsers = async () => {
  loadingUsers.value = true
  try {
    const response = await http.get<EmployeeUserDto[]>(API_BASE_URL)

    // 為每個員工獲取角色資訊
    const employeesWithRoles = await Promise.all(
      response.data.map(async (user) => {
        try {
          if (user.employeeUserId) {
            const rolesResponse = await http.get<string[]>(
              `${API_BASE_URL}/${user.employeeUserId}/roles`,
            )
            user.roles = Array.isArray(rolesResponse.data) ? rolesResponse.data : []
          } else {
            user.roles = []
          }
        } catch (error) {
          console.warn(`無法載入員工 ${user.employeeUserId} 的角色:`, error)
          user.roles = []
        }
        return user
      }),
    )

    employeeUsers.value = employeesWithRoles
  } catch (error: any) {
    console.error('Error fetching employee users:', error)
    ElMessage.error(`獲取員工列表失敗: ${error.response?.data || error.message}`)
  } finally {
    loadingUsers.value = false
  }
}

const fetchAvailableRoles = async () => {
  try {
    const response = await http.get<RoleDto[]>(`${API_BASE_URL}/available-roles`)
    availableRoles.value = response.data
    console.log('Available roles loaded:', response.data)
  } catch (error: any) {
    console.error('Error fetching available roles:', error)
    ElMessage.error(`獲取可用角色失敗: ${error.response?.data || error.message}`)
  }
}

const fetchEmployeeRoles = async (employeeId: number) => {
  if (!employeeId || typeof employeeId !== 'number' || employeeId <= 0) {
    console.error('無效的員工ID:', employeeId)
    employeeRoles.value = []
    calculatedPermissions.value = []
    return
  }

  loadingRoles.value = true
  try {
    const response = await http.get<string[]>(`${API_BASE_URL}/${employeeId}/roles`)

    if (Array.isArray(response.data)) {
      employeeRoles.value = response.data
      updatePermissionPreview()
      console.log('Employee roles loaded:', response.data)
    } else {
      console.warn('員工角色回應格式不正確:', response.data)
      employeeRoles.value = []
      calculatedPermissions.value = []
    }
  } catch (error: any) {
    console.error('Error fetching employee roles:', error)
    ElMessage.error(
      `獲取員工角色失敗: ${error.response?.data?.message || error.response?.data || error.message}`,
    )
    employeeRoles.value = []
    calculatedPermissions.value = []
  } finally {
    loadingRoles.value = false
  }
}

const getCurrentUser = (): string => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      return user.username || user.name || 'unknown_user'
    }
  } catch (error) {
    console.warn('無法從 localStorage 獲取用戶資訊:', error)
  }

  const token = localStorage.getItem('token')
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.sub || payload.username || 'token_user'
    } catch (error) {
      console.warn('無法解析 token:', error)
    }
  }

  return 'system_admin'
}

const updateEmployeeRoles = async (employeeId: number, roleNames: string[]) => {
  if (!employeeId || typeof employeeId !== 'number' || employeeId <= 0) {
    throw new Error('無效的員工ID')
  }

  if (!Array.isArray(roleNames)) {
    throw new Error('角色名稱必須是陣列')
  }

  try {
    const currentUser = getCurrentUser()
    const request: UpdateEmployeeRolesRequest = {
      roleNames: roleNames.filter((name) => typeof name === 'string' && name.trim() !== ''),
      updatedBy: currentUser,
    }

    await http.put(`${API_BASE_URL}/${employeeId}/roles`, request)
    console.log('Employee roles updated successfully by:', currentUser, 'roles:', request.roleNames)
  } catch (error: any) {
    console.error('Error updating employee roles:', error)
    throw error
  }
}

const updatePermissionPreview = () => {
  const roleDescriptions: string[] = []

  employeeRoles.value.forEach((roleName) => {
    const role = availableRoles.value.find((r) => r.roleName === roleName)
    if (role) {
      const description = role.description || role.displayName || roleName
      roleDescriptions.push(description)
    } else {
      roleDescriptions.push(roleName)
    }
  })

  calculatedPermissions.value = roleDescriptions
}

// 頁面操作方法
const selectEmployee = async (employee: EmployeeUserDto) => {
  selectedEmployee.value = employee

  if (employee.employeeUserId) {
    await fetchEmployeeRoles(employee.employeeUserId)
  } else {
    employeeRoles.value = []
    calculatedPermissions.value = []
  }
}

const saveEmployeeRoles = async () => {
  if (!selectedEmployee.value || !selectedEmployee.value.employeeUserId) {
    ElMessage.error('請選擇有效的員工')
    return
  }

  savingRoles.value = true
  try {
    await updateEmployeeRoles(selectedEmployee.value.employeeUserId, employeeRoles.value)

    // 更新本地員工數據
    const employeeIndex = employeeUsers.value.findIndex(
      (emp) => emp.employeeUserId === selectedEmployee.value!.employeeUserId,
    )
    if (employeeIndex !== -1) {
      employeeUsers.value[employeeIndex].roles = [...employeeRoles.value]
      // 同時更新 selectedEmployee 以保持一致性
      selectedEmployee.value.roles = [...employeeRoles.value]
    }

    ElMessage.success('員工角色更新成功')
  } catch (error: any) {
    console.error('更新員工角色失敗:', error)
    const errorMsg =
      error.response?.data?.message || error.response?.data || error.message || '未知錯誤'
    ElMessage.error('更新員工角色失敗: ' + errorMsg)
  } finally {
    savingRoles.value = false
  }
}

const resetEmployeeRoles = () => {
  if (!selectedEmployee.value || !selectedEmployee.value.roles) return

  employeeRoles.value = [...selectedEmployee.value.roles]
  updatePermissionPreview()
  ElMessage.info('已重置為原始角色配置')
}

const focusFirstEmployee = () => {
  if (filteredEmployees.value.length > 0) {
    selectEmployee(filteredEmployees.value[0])
  }
}

// 初始化
onMounted(() => {
  fetchEmployeeUsers()
  fetchAvailableRoles()
})
</script>

<style scoped>
.permission-management {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 頂部工具欄 */
.toolbar-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.toolbar-title h3 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.toolbar-title p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 主要內容區域 */
.main-content {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 20px;
  min-height: calc(100vh - 200px);
  align-items: start;
}

/* 左側員工列表 */
.employee-list-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.employee-stats {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-info {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.employee-list {
  max-height: calc(100vh - 280px);
  overflow-y: auto;
  padding-right: 4px;
}

.employee-item {
  padding: 16px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 8px;
  margin-bottom: 8px;
}

.employee-item:hover {
  background: #f5f7fa;
  transform: translateX(2px);
}

.employee-item.selected {
  background: #e6f7ff;
  border-left: 4px solid #409eff;
  transform: translateX(4px);
}

.employee-info {
  width: 100%;
}

.employee-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.employee-details {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.employee-number {
  color: #606266;
  font-size: 13px;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 12px;
  font-family: monospace;
}

.employee-roles {
  margin-top: 8px;
  min-height: 24px;
}

.no-roles {
  color: #c0c4cc;
  font-size: 12px;
  font-style: italic;
}

.no-data {
  padding: 60px 20px;
  text-align: center;
}

/* 右側權限編輯區 */
.permission-edit-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: fit-content;
  max-height: calc(100vh - 180px);
}

.permission-edit-card :deep(.el-card__body) {
  max-height: calc(100vh - 240px);
  overflow-y: auto;
  padding-right: 4px;
}

.no-selection {
  padding: 100px 20px;
  text-align: center;
}

.permission-editor {
  padding: 0;
}

.employee-summary {
  margin-bottom: 24px;
}

.role-selection {
  margin-bottom: 24px;
}

.permission-preview-section {
  margin-bottom: 24px;
}

.permission-preview-section h4,
.role-details h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.permission-preview {
  min-height: 60px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 4px;
}

.role-details {
  margin-top: 24px;
}

/* 響應式設計 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 320px 1fr;
  }
}

@media (max-width: 768px) {
  .permission-management {
    padding: 12px;
  }

  .main-content {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .toolbar {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .toolbar-actions {
    width: 100%;
    justify-content: space-between;
  }

  .employee-list {
    max-height: 400px;
  }

  .permission-edit-card {
    max-height: none;
  }

  .permission-edit-card :deep(.el-card__body) {
    max-height: none;
    overflow-y: visible;
  }
}

/* 滾動條樣式 */
.employee-list::-webkit-scrollbar,
.permission-edit-card :deep(.el-card__body)::-webkit-scrollbar {
  width: 6px;
}

.employee-list::-webkit-scrollbar-track,
.permission-edit-card :deep(.el-card__body)::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.employee-list::-webkit-scrollbar-thumb,
.permission-edit-card :deep(.el-card__body)::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.employee-list::-webkit-scrollbar-thumb:hover,
.permission-edit-card :deep(.el-card__body)::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 動畫效果 */
.employee-item {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 按鈕組樣式 */
.permission-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.permission-changes {
  margin-bottom: 16px;
}

/* Element Plus 組件樣式調整 */
:deep(.el-descriptions__body .el-descriptions__table) {
  border-radius: 8px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-card__header) {
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-divider__text) {
  background: #fff;
  font-weight: 600;
  color: #303133;
}

:deep(.el-alert) {
  border-radius: 8px;
}
</style>
