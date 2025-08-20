<template>
  <div class="user-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>員工使用者列表</span>
          <div class="header-actions">
            <el-input
              v-model="searchText"
              placeholder="搜尋員工姓名或編號"
              prefix-icon="Search"
              style="width: 250px; margin-right: 12px;"
              clearable
            />
            <el-select
              v-model="departmentFilter"
              placeholder="篩選部門"
              clearable
              style="width: 150px; margin-right: 12px;"
            >
              <el-option label="全部部門" value="" />
              <el-option
                v-for="dept in availableDepartments"
                :key="dept.departmentId"
                :label="dept.description || dept.departmentName"
                :value="dept.description || dept.departmentName"
              />
            </el-select>
            <el-button type="primary" @click="openAddUserDialog">新增員工使用者</el-button>
          </div>
        </div>
      </template>
      <div class="table-container">
        <el-table :data="filteredEmployees" v-loading="loadingUsers" table-layout="auto" class="responsive-table">
          <el-table-column prop="employeeNumber" label="員工編號" class-name="col-employee-id" min-width="10%">
            <template #default="scope">
              <span class="text-truncate">{{ scope.row.employeeNumber }}</span>
            </template>
          </el-table-column>
          <el-table-column label="姓名" class-name="col-name" min-width="15%">
            <template #default="scope">
              <span class="text-truncate">{{ `${scope.row.firstName} ${scope.row.lastName}` }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="使用者名稱" class-name="col-username" min-width="12%">
            <template #default="scope">
              <span class="text-truncate">{{ scope.row.username }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="email" label="電子郵件" class-name="col-email hidden-mobile" min-width="12%">
            <template #default="scope">
              <span class="text-truncate">{{ scope.row.email || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="電話" class-name="col-phone hidden-mobile hidden-tablet" min-width="10%">
            <template #default="scope">
              <span class="text-truncate">{{ scope.row.phone || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="departmentName" label="部門" class-name="col-department" min-width="12%">
            <template #default="scope">
              <span class="text-truncate">{{ scope.row.departmentName || '未分配' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="employeeType" label="員工類型" class-name="col-type" min-width="10%">
            <template #default="scope">
              <el-tag 
                :type="getEmployeeTypeTagType(scope.row.employeeType)" 
                size="small"
              >
                {{ getEmployeeTypeDisplay(scope.row.employeeType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="角色" class-name="col-roles hidden-mobile" min-width="20%">
            <template #default="scope">
              <div v-if="scope.row.roles && scope.row.roles.length > 0" class="roles-container">
                <el-tag 
                  v-for="role in scope.row.roles" 
                  :key="role" 
                  size="small" 
                  type="info"
                  class="role-tag"
                >
                  {{ getRoleDisplayName(role) }}
                </el-tag>
              </div>
              <span v-else class="no-roles">未分配角色</span>
            </template>
          </el-table-column>
          <el-table-column prop="isActive" label="狀態" class-name="col-status" min-width="8%">
            <template #default="scope">
              <el-tag :type="scope.row.isActive ? 'success' : 'danger'" size="small">
                {{ scope.row.isActive ? '啟用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" class-name="col-actions" min-width="13%">
            <template #default="scope">
              <div class="action-buttons">
                <el-button link type="primary" size="small" @click="editUser(scope.row)">編輯</el-button>
                <el-button link type="danger" size="small" @click="deleteUser(scope.row.employeeUserId)">刪除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 新增/編輯員工使用者 Dialog -->
    <el-dialog v-model="showAddUserDialog" :title="isEditMode ? '編輯員工使用者' : '新增員工使用者'" width="600" @close="resetForm">
      <el-form :model="currentEmployeeUser" :rules="formRules" ref="userFormRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名字" prop="firstName">
              <el-input v-model="currentEmployeeUser.firstName"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓氏" prop="lastName">
              <el-input v-model="currentEmployeeUser.lastName"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="員工編號" prop="employeeNumber">
              <el-input v-model="currentEmployeeUser.employeeNumber"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="員工類型" prop="employeeType">
              <el-select v-model="currentEmployeeUser.employeeType" placeholder="選擇員工類型" style="width: 100%;">
                <el-option label="內部" value="INTERNAL"></el-option>
                <el-option label="供應商" value="SUPPLIER"></el-option>
                <el-option label="承包商" value="CONTRACTOR"></el-option>
                <el-option label="系統管理員" value="SYSTEM_ADMIN"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="currentEmployeeUser.birthDate" type="date" placeholder="選擇日期" style="width: 100%;"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="電子郵件" prop="email">
              <el-input v-model="currentEmployeeUser.email"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="電話">
              <el-input v-model="currentEmployeeUser.phone"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="照片路徑">
              <el-input v-model="currentEmployeeUser.photoPath"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部門">
              <el-select v-model="currentEmployeeUser.employeeDepartmentId" placeholder="選擇部門" style="width: 100%;">
                <el-option
                  v-for="dept in availableDepartments"
                  :key="dept.departmentId"
                  :label="dept.description || dept.departmentName"
                  :value="dept.departmentId"
                >
                  <span>{{ dept.description || dept.departmentName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ dept.departmentName }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="職位ID">
              <el-input v-model="currentEmployeeUser.employeePositionId" type="number"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="經理ID">
              <el-input v-model="currentEmployeeUser.managerEmployeeUserId" type="number"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="雇用日期">
              <el-date-picker v-model="currentEmployeeUser.hireDate" type="date" placeholder="選擇日期" style="width: 100%;"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="解雇日期">
              <el-date-picker v-model="currentEmployeeUser.terminationDate" type="date" placeholder="選擇日期" style="width: 100%;"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="使用者名稱" prop="username">
              <el-input v-model="currentEmployeeUser.username"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="密碼" v-if="!isEditMode" prop="passwordHash">
          <el-input v-model="currentEmployeeUser.passwordHash" type="password" show-password></el-input>
        </el-form-item>
         <!-- 【已修正】增加 prop，使其在編輯模式下也能被驗證 -->
         <el-form-item label="密碼" v-if="isEditMode && showPasswordEdit" prop="passwordHash">
          <el-input v-model="currentEmployeeUser.passwordHash" type="password" show-password placeholder="若不修改請留空"></el-input>
          <el-button type="text" @click="showPasswordEdit = false">取消修改密碼</el-button>
        </el-form-item>
        <el-form-item v-if="isEditMode && !showPasswordEdit">
          <el-button type="text" @click="showPasswordEdit = true">修改密碼</el-button>
        </el-form-item>
        <el-form-item label="是否啟用">
          <el-switch v-model="currentEmployeeUser.isActive"></el-switch>
        </el-form-item>
        
        <!-- ===== 新增：角色管理功能開始 ===== -->
        <el-divider content-position="left">角色與權限管理</el-divider>
        
        <el-form-item label="角色選擇">
          <el-select 
            v-model="employeeRoles" 
            multiple 
            placeholder="選擇員工角色" 
            style="width: 100%;"
            @change="updatePermissionPreview"
          >
            <el-option
              v-for="role in availableRoles"
              :key="role.roleId"
              :label="role.displayName || role.roleName"
              :value="role.roleName"
            >
              <span>{{ role.displayName || role.roleName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ role.description }}</span>
            </el-option>
          </el-select>
          <div style="margin-top: 8px; color: #909399; font-size: 12px;">
            選擇多個角色為員工分配相應的系統權限
          </div>
        </el-form-item>
        
        <el-form-item label="權限預覽" v-if="calculatedPermissions.length > 0">
          <div style="max-height: 120px; overflow-y: auto; border: 1px solid #dcdfe6; border-radius: 4px; padding: 8px;">
            <el-tag 
              v-for="permission in calculatedPermissions" 
              :key="permission" 
              size="small" 
              style="margin: 2px;"
              type="info"
            >
              {{ permission }}
            </el-tag>
          </div>
          <div style="margin-top: 4px; color: #909399; font-size: 12px;">
            以上為選中角色包含的權限預覽
          </div>
        </el-form-item>
        <!-- ===== 新增：角色管理功能結束 ===== -->
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddUserDialog = false">取消</el-button>
          <el-button type="primary" @click="saveEmployeeUser">
            {{ isEditMode ? '更新' : '新增' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive, computed } from 'vue'
import http from '../http-common'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

interface EmployeeUserDto {
  employeeUserId?: number;
  firstName: string;
  lastName: string;
  employeeNumber: string;
  employeeType: 'INTERNAL' | 'SUPPLIER' | 'CONTRACTOR' | 'SYSTEM_ADMIN';
  birthDate?: string;
  email?: string;
  phone?: string;
  photoPath?: string;
  employeeDepartmentId?: number;
  employeePositionId?: number;
  managerEmployeeUserId?: number;
  hireDate?: string;
  terminationDate?: string;
  username?: string;
  passwordHash?: string;
  isActive: boolean;
  lastLogin?: string;
  createdAt?: string;
  updatedAt?: string;
  departmentName?: string; // 新增部門名稱欄位
  roles?: string[]; // 新增角色列表欄位
}

interface DepartmentDto {
  departmentId: number;
  departmentName: string;
  description?: string;
}

// ===== 新增：角色管理功能開始 =====
interface RoleDto {
  roleId: number;
  roleName: string;
  displayName: string;
  description: string;
  isActive: boolean;
}

interface UpdateEmployeeRolesRequest {
  roleNames: string[];
  updatedBy: string;
}
// ===== 新增：角色管理功能結束 =====

const employeeUsers = ref<EmployeeUserDto[]>([])
const loadingUsers = ref(false)
const showAddUserDialog = ref(false)
const isEditMode = ref(false)
const showPasswordEdit = ref(false)

// 搜尋和篩選
const searchText = ref('')
const departmentFilter = ref('')
const availableDepartments = ref<DepartmentDto[]>([])

// ===== 新增：角色管理功能開始 =====
const availableRoles = ref<RoleDto[]>([]) // 所有可用角色
const employeeRoles = ref<string[]>([]) // 當前員工的角色名稱
const calculatedPermissions = ref<string[]>([]) // 計算出的權限預覽
// ===== 新增：角色管理功能結束 =====

const userFormRef = ref<FormInstance>()

// 計算屬性 - 篩選後的員工列表 (增強版)
const filteredEmployees = computed(() => {
  return employeeUsers.value.filter(employee => {
    const fullName = `${employee.firstName} ${employee.lastName}`
    
    // 搜尋匹配 - 支援員工編號、姓名、使用者名稱、郵件
    const matchesSearch = !searchText.value || 
      fullName.toLowerCase().includes(searchText.value.toLowerCase()) ||
      employee.employeeNumber.toLowerCase().includes(searchText.value.toLowerCase()) ||
      (employee.username && employee.username.toLowerCase().includes(searchText.value.toLowerCase())) ||
      (employee.email && employee.email.toLowerCase().includes(searchText.value.toLowerCase()))
    
    // 部門篩選匹配
    const matchesDepartment = !departmentFilter.value || 
      employee.departmentName === departmentFilter.value
    
    return matchesSearch && matchesDepartment
  })
})

const currentEmployeeUser = ref<EmployeeUserDto>({
  firstName: '',
  lastName: '',
  employeeNumber: '',
  employeeType: 'INTERNAL',
  birthDate: undefined,
  email: '',
  phone: '',
  photoPath: '',
  employeeDepartmentId: undefined,
  employeePositionId: undefined,
  managerEmployeeUserId: undefined,
  hireDate: undefined,
  terminationDate: undefined,
  username: '',
  passwordHash: '',
  isActive: true,
})

// 【已修正】增加自訂驗證器，讓密碼在編輯模式下為非必填
const validatePassword = (rule: any, value: any, callback: any) => {
  // 在「新增」模式下，密碼為必填
  if (!isEditMode.value && !value) {
    callback(new Error('請輸入密碼'));
  } else {
    // 在「編輯」模式下，密碼為選填，所以直接通過
    callback();
  }
};

const formRules = reactive<FormRules>({
  firstName: [{ required: true, message: '請輸入名字', trigger: 'blur' }],
  lastName: [{ required: true, message: '請輸入姓氏', trigger: 'blur' }],
  employeeNumber: [{ required: true, message: '請輸入員工編號', trigger: 'blur' }],
  username: [{ required: true, message: '請輸入使用者名稱', trigger: 'blur' }],
  email: [{ type: 'email', message: '請輸入有效的電子郵件地址', trigger: ['blur', 'change'] }],
  // 【已修正】使用自訂驗證器
  passwordHash: [{ validator: validatePassword, trigger: 'blur' }],
});


const API_BASE_URL = '/employee-users'

// 獲取部門列表 - 使用假資料
const fetchDepartments = async () => {
  try {
    // 使用假資料代替 API 調用
    const mockDepartments: DepartmentDto[] = [
      { departmentId: 1, departmentName: "Human Resources", description: "人力資源部" },
      { departmentId: 2, departmentName: "IT", description: "資訊部" },
      { departmentId: 3, departmentName: "Warehouse", description: "倉庫部" }
    ]
    
    availableDepartments.value = mockDepartments
    console.log('Available departments loaded (mock data):', mockDepartments)
  } catch (error: any) {
    console.error('Error loading departments:', error)
    ElMessage.error(`載入部門資料失敗: ${error.message}`)
  }
}

// ===== 新增：角色管理功能開始 =====
// 獲取所有可用角色
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

// 獲取員工角色
const fetchEmployeeRoles = async (employeeId: number) => {
  // 驗證員工ID的有效性
  if (!employeeId || typeof employeeId !== 'number' || employeeId <= 0) {
    console.error('無效的員工ID:', employeeId)
    employeeRoles.value = []
    calculatedPermissions.value = []
    return
  }
  
  try {
    const response = await http.get<string[]>(`${API_BASE_URL}/${employeeId}/roles`)
    
    // 確保回應數據是陣列
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
    ElMessage.error(`獲取員工角色失敗: ${error.response?.data?.message || error.response?.data || error.message}`)
    employeeRoles.value = []
    calculatedPermissions.value = []
  }
}

// 獲取當前用戶名稱
const getCurrentUser = (): string => {
  // 嘗試從 localStorage 獲取用戶資訊
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      return user.username || user.name || 'unknown_user'
    }
  } catch (error) {
    console.warn('無法從 localStorage 獲取用戶資訊:', error)
  }
  
  // 嘗試從其他可能的來源獲取
  const token = localStorage.getItem('token')
  if (token) {
    try {
      // 如果 token 是 JWT，可以解析獲取用戶資訊
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.sub || payload.username || 'token_user'
    } catch (error) {
      console.warn('無法解析 token:', error)
    }
  }
  
  // 如果都無法獲取，使用預設值
  return 'system_admin'
}

// 更新員工角色
const updateEmployeeRoles = async (employeeId: number, roleNames: string[]) => {
  // 驗證參數的有效性
  if (!employeeId || typeof employeeId !== 'number' || employeeId <= 0) {
    throw new Error('無效的員工ID')
  }
  
  if (!Array.isArray(roleNames)) {
    throw new Error('角色名稱必須是陣列')
  }
  
  try {
    const currentUser = getCurrentUser()
    const request: UpdateEmployeeRolesRequest = {
      roleNames: roleNames.filter(name => typeof name === 'string' && name.trim() !== ''), // 過濾無效的角色名稱
      updatedBy: currentUser
    }
    
    await http.put(`${API_BASE_URL}/${employeeId}/roles`, request)
    console.log('Employee roles updated successfully by:', currentUser, 'roles:', request.roleNames)
  } catch (error: any) {
    console.error('Error updating employee roles:', error)
    throw error // 重新拋出錯誤以便調用方處理
  }
}

// 更新權限預覽
const updatePermissionPreview = () => {
  // 簡化權限預覽邏輯 - 顯示角色的描述信息而非硬編碼權限
  const roleDescriptions: string[] = []
  
  employeeRoles.value.forEach(roleName => {
    const role = availableRoles.value.find(r => r.roleName === roleName)
    if (role) {
      // 使用角色的描述或顯示名稱作為權限預覽
      const description = role.description || role.displayName || roleName
      roleDescriptions.push(description)
    } else {
      // 如果找不到角色詳情，顯示角色名稱
      roleDescriptions.push(roleName)
    }
  })
  
  calculatedPermissions.value = roleDescriptions
}
// ===== 新增：角色管理功能結束 =====

// 獲取員工類型顯示文字
const getEmployeeTypeDisplay = (type: string): string => {
  const typeMap: Record<string, string> = {
    'INTERNAL': '內部',
    'SUPPLIER': '供應商',
    'CONTRACTOR': '承包商',
    'SYSTEM_ADMIN': '系統管理員'
  }
  return typeMap[type] || type
}

// 獲取員工類型標籤顏色
const getEmployeeTypeTagType = (type: string): string => {
  const typeMap: Record<string, string> = {
    'INTERNAL': 'primary',
    'SUPPLIER': 'success',
    'CONTRACTOR': 'warning',
    'SYSTEM_ADMIN': 'danger'
  }
  return typeMap[type] || 'info'
}

// 獲取角色顯示名稱
const getRoleDisplayName = (roleName: string): string => {
  const role = availableRoles.value.find(r => r.roleName === roleName)
  return role ? (role.displayName || role.roleName) : roleName
}

// 根據部門ID獲取部門名稱
const getDepartmentName = (departmentId?: number): string => {
  if (!departmentId) return '未分配'
  const department = availableDepartments.value.find(dept => dept.departmentId === departmentId)
  return department ? department.description || department.departmentName : '未知部門'
}

// Fetch all employee users
const fetchEmployeeUsers = async () => {
  loadingUsers.value = true
  try {
    const response = await http.get<any[]>(API_BASE_URL)
    
    const transformedData = await Promise.all(response.data.map(async (user) => {
      if (user.isActive === undefined && user.active !== undefined) {
        user.isActive = user.active;
      }
      // 根據部門ID添加部門名稱
      user.departmentName = getDepartmentName(user.employeeDepartmentId);
      
      // 載入員工角色資訊
      if (user.employeeUserId && typeof user.employeeUserId === 'number') {
        try {
          const rolesResponse = await http.get<string[]>(`${API_BASE_URL}/${user.employeeUserId}/roles`)
          user.roles = Array.isArray(rolesResponse.data) ? rolesResponse.data : []
        } catch (roleError) {
          console.warn(`無法載入員工 ${user.employeeUserId} 的角色:`, roleError)
          user.roles = []
        }
      } else {
        user.roles = []
      }
      
      return user;
    }));

    employeeUsers.value = transformedData as EmployeeUserDto[];

  } catch (error: any) {
    console.error('Error fetching employee users:', error)
    ElMessage.error(`獲取員工使用者列表失敗: ${error.response?.data || error.message}`)
  } finally {
    loadingUsers.value = false
  }
}

const resetForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields();
  }
}

// Open add employee user dialog
const openAddUserDialog = () => {
  isEditMode.value = false
  showPasswordEdit.value = true
  currentEmployeeUser.value = {
    firstName: '',
    lastName: '',
    employeeNumber: '',
    employeeType: 'INTERNAL',
    birthDate: undefined,
    email: '',
    phone: '',
    photoPath: '',
    employeeDepartmentId: undefined,
    employeePositionId: undefined,
    managerEmployeeUserId: undefined,
    hireDate: undefined,
    terminationDate: undefined,
    username: '',
    passwordHash: '',
    isActive: true,
  }
  
  // ===== 新增：角色管理功能開始 =====
  // 重置角色相關數據
  employeeRoles.value = []
  calculatedPermissions.value = []
  // ===== 新增：角色管理功能結束 =====
  
  showAddUserDialog.value = true
  userFormRef.value?.clearValidate();
}

// Save (add/update) employee user
const saveEmployeeUser = async () => {
  if (!userFormRef.value) return;
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const payload: Partial<EmployeeUserDto> = { ...currentEmployeeUser.value };

        if (isEditMode.value && !payload.passwordHash) {
            delete payload.passwordHash;
        }

        if (payload.birthDate instanceof Date) {
          payload.birthDate = (payload.birthDate as Date).toISOString().split('T')[0];
        }
        if (payload.hireDate instanceof Date) {
          payload.hireDate = (payload.hireDate as Date).toISOString().split('T')[0];
        }
        if (payload.terminationDate instanceof Date) {
          payload.terminationDate = (payload.terminationDate as Date).toISOString().split('T')[0];
        }

        if (isEditMode.value) {
          // 確保 employeeUserId 存在且為有效數字
          if (!payload.employeeUserId || typeof payload.employeeUserId !== 'number') {
            throw new Error('員工ID無效，無法更新員工資料')
          }
          
          await http.put(`${API_BASE_URL}/${payload.employeeUserId}`, payload);
          
          // ===== 新增：角色管理功能開始 =====
          // 更新員工角色（加強型別檢查）
          if (payload.employeeUserId && typeof payload.employeeUserId === 'number' && Array.isArray(employeeRoles.value)) {
            try {
              await updateEmployeeRoles(payload.employeeUserId, employeeRoles.value)
              ElMessage.success('員工使用者和角色更新成功');
            } catch (roleError: any) {
              console.error('角色更新失敗:', roleError)
              const errorMsg = roleError.response?.data?.message || roleError.response?.data || roleError.message || '未知錯誤'
              ElMessage.warning('員工資料更新成功，但角色更新失敗: ' + errorMsg);
            }
          } else {
            console.warn('員工ID無效或角色數據無效，跳過角色更新')
            ElMessage.success('員工使用者更新成功');
          }
          // ===== 新增：角色管理功能結束 =====
        } else {
          await http.post<EmployeeUserDto>(API_BASE_URL, payload)
          ElMessage.success('員工使用者新增成功')
        }
        showAddUserDialog.value = false
        // 重新載入員工資料以確保部門名稱和角色正確顯示
        await fetchEmployeeUsers()
      } catch (error: any) {
        console.error('Error saving employee user:', error)
        if (error.response && error.response.status === 409) {
          const conflictMessage = error.response.data?.message || '員工編號、使用者名稱或Email已存在。';
          ElMessage.error(`儲存失敗: ${conflictMessage}`);
        } else {
          const errorMessage = error.response?.data?.message || error.message;
          ElMessage.error(`儲存員工使用者失敗: ${errorMessage}`);
        }
      }
    } else {
      ElMessage.error('表單驗證失敗，請檢查輸入的資料！');
      return false;
    }
  });
}

// Edit employee user
const editUser = async (user: EmployeeUserDto) => {
  try {
    isEditMode.value = true
    showPasswordEdit.value = false
    currentEmployeeUser.value = {
      ...user,
      passwordHash: '',
      birthDate: user.birthDate ? new Date(user.birthDate) : undefined,
      hireDate: user.hireDate ? new Date(user.hireDate) : undefined,
      terminationDate: user.terminationDate ? new Date(user.terminationDate) : undefined,
      // 確保部門資訊正確設定
      departmentName: getDepartmentName(user.employeeDepartmentId)
    }
    
    // ===== 新增：角色管理功能開始 =====
    // 載入員工角色（加入錯誤處理）
    if (user.employeeUserId && typeof user.employeeUserId === 'number') {
      try {
        await fetchEmployeeRoles(user.employeeUserId)
      } catch (roleError: any) {
        console.error('載入員工角色失敗:', roleError)
        ElMessage.warning('員工資料載入成功，但角色資料載入失敗，角色管理功能可能受影響')
        // 重置角色數據以避免顯示錯誤的舊數據
        employeeRoles.value = []
        calculatedPermissions.value = []
      }
    } else {
      console.warn('員工ID無效，無法載入角色資料')
      employeeRoles.value = []
      calculatedPermissions.value = []
    }
    // ===== 新增：角色管理功能結束 =====
    
    showAddUserDialog.value = true
    userFormRef.value?.clearValidate();
  } catch (error: any) {
    console.error('編輯員工時發生錯誤:', error)
    ElMessage.error('載入員工資料失敗: ' + (error.message || '未知錯誤'))
  }
}

// Delete employee user
const deleteUser = async (id?: number) => {
  if (!id) return
  ElMessageBox.confirm('確定要刪除此員工使用者嗎？', '警告', {
    confirmButtonText: '確定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await http.delete(`${API_BASE_URL}/${id}`)
        ElMessage.success('員工使用者刪除成功')
        await fetchEmployeeUsers()
      } catch (error: any) {
        console.error('Error deleting employee user:', error)
        ElMessage.error(`刪除員工使用者失敗: ${error.response?.data || error.message}`)
      }
    })
    .catch(() => {
      ElMessage.info('已取消刪除')
    })
}

onMounted(async () => {
  // 先載入部門和角色資料，再載入員工資料
  await fetchDepartments() // 載入部門列表
  // ===== 新增：角色管理功能開始 =====
  await fetchAvailableRoles() // 載入可用角色
  // ===== 新增：角色管理功能結束 =====
  
  // 最後載入員工資料，確保部門名稱能正確顯示
  fetchEmployeeUsers()
})
</script>

<style scoped>
.user-view {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden; /* 防止內容溢出 */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.header-actions {
  display: flex;
  align-items: center;
}

.dialog-footer button:first-child {
  margin-right: 10px;
}

/* 響應式表格容器 */
.table-container {
  width: 100%;
  overflow-x: auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 基本表格樣式 */
:deep(.responsive-table) {
  width: 100% !important;
  min-width: 800px; /* 最小寬度確保可讀性 */
}

:deep(.responsive-table .el-table__header-wrapper) {
  overflow: visible;
}

:deep(.responsive-table .el-table__body-wrapper) {
  overflow: visible;
}

:deep(.responsive-table th) {
  background-color: #fafafa;
  font-weight: 600;
  color: #606266;
  padding: 12px 8px;
  text-align: center;
}

:deep(.responsive-table td) {
  padding: 10px 8px;
  text-align: center;
}

/* 文字截斷樣式 */
.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  display: inline-block;
}

/* 角色標籤容器 */
.roles-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
  align-items: center;
}

.role-tag {
  font-size: 12px;
  margin: 1px;
}

.no-roles {
  color: #909399;
  font-size: 12px;
  font-style: italic;
}

/* 操作按鈕容器 */
.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
}

:deep(.el-button--small) {
  padding: 4px 8px;
  font-size: 12px;
  min-width: auto;
}

/* 桌面版樣式 (>1200px) */
@media (min-width: 1201px) {
  :deep(.responsive-table) {
    min-width: 1200px;
  }
  
  .roles-container {
    justify-content: flex-start;
  }
  
  .action-buttons {
    justify-content: flex-start;
  }
}

/* 平板版樣式 (768px-1200px) */
@media (max-width: 1200px) and (min-width: 769px) {
  .hidden-tablet {
    display: none !important;
  }
  
  :deep(.responsive-table) {
    min-width: 900px;
  }
  
  :deep(.col-name) {
    min-width: 18% !important;
  }
  
  :deep(.col-roles) {
    min-width: 25% !important;
  }
  
  :deep(.col-actions) {
    min-width: 15% !important;
  }
}

/* 手機版樣式 (<768px) */
@media (max-width: 768px) {
  .hidden-mobile {
    display: none !important;
  }
  
  .table-container {
    margin: 0 -10px;
    border-radius: 0;
  }
  
  :deep(.responsive-table) {
    min-width: 600px;
  }
  
  :deep(.col-employee-id) {
    min-width: 12% !important;
  }
  
  :deep(.col-name) {
    min-width: 20% !important;
  }
  
  :deep(.col-username) {
    min-width: 18% !important;
  }
  
  :deep(.col-department) {
    min-width: 15% !important;
  }
  
  :deep(.col-type) {
    min-width: 15% !important;
  }
  
  :deep(.col-status) {
    min-width: 10% !important;
  }
  
  :deep(.col-actions) {
    min-width: 15% !important;
  }
  
  :deep(.responsive-table th),
  :deep(.responsive-table td) {
    padding: 8px 4px;
    font-size: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  :deep(.el-button--small) {
    padding: 2px 6px;
    font-size: 11px;
  }
  
  :deep(.el-tag--small) {
    padding: 0 4px;
    font-size: 10px;
    height: 20px;
    line-height: 18px;
  }
}

/* 超小螢幕優化 (<480px) */
@media (max-width: 480px) {
  :deep(.responsive-table) {
    min-width: 500px;
  }
  
  :deep(.responsive-table th),
  :deep(.responsive-table td) {
    padding: 6px 2px;
    font-size: 11px;
  }
  
  .text-truncate {
    max-width: 80px;
  }
  
  :deep(.el-tag--small) {
    font-size: 9px;
    height: 18px;
    line-height: 16px;
  }
}
</style>
