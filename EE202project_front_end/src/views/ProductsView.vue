<template>
  <div>
    
    <el-button type="primary" @click="openAddProductDialog" style="margin-bottom: 10px;">新增產品</el-button>
    <el-table :data="products" style="width: 100%">
      <el-table-column prop="name" label="產品名稱" />
      <el-table-column prop="price" label="價格" />
      <el-table-column prop="category" label="類別" />
      <el-table-column label="BOM" width="120">
        <template #default="scope">
          <el-button size="small" @click="handleRowClick(scope.row)">查看BOM</el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" size="small" @click="openProductionDialog(scope.row)">生產</el-button>
          <el-button type="danger" size="small" @click="handleDeleteProduct(scope.row)">刪除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="BOM表列表">
      <el-button type="primary" @click="openAddBomDialog">新增BOM組件</el-button>
      <el-table :data="bomComponents" style="width: 100%">
        <el-table-column label="組件名稱">
          <template #default="scope">
            <span v-if="scope.row && scope.row.componentMaterialId !== null && scope.row.componentMaterialId !== undefined">
              {{ getMaterialNameById(scope.row.componentMaterialId) }}
            </span>
            <span v-else>
              未知材料 (ID: 缺失/無效)
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="數量" />
        <el-table-column prop="notes" label="備註" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">編輯</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">刪除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="bomFormDialogVisible" :title="isEditMode ? '編輯BOM組件' : '新增BOM組件'">
      <el-form :model="currentBomComponent" label-width="150px">
        <el-form-item label="主物料ID">
          <el-input v-model="currentBomComponent.parentMaterialId" disabled></el-input>
        </el-form-item>
        <el-form-item label="組件物料">
          <el-select v-model="currentBomComponent.componentMaterialId" :multiple="!isEditMode" placeholder="從倉庫選擇物料">
            <el-option
              v-for="material in depotMaterials"
              :key="material.materialId"
              :label="material.materialName"
              :value="material.materialId"
              :disabled="isMaterialDisabled(material.materialId)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="數量">
          <el-input-number v-model="currentBomComponent.quantity" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item label="備註">
          <el-input v-model="currentBomComponent.notes"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="bomFormDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveBomComponent">儲存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Production Dialog -->
    <el-dialog v-model="productionDialogVisible" title="建立生產訂單" width="600px">
      <el-form v-if="selectedProduct" label-width="150px">
        <el-form-item label="生產產品">
          <el-input :value="selectedProduct.name" disabled />
        </el-form-item>
        <el-form-item label="生產數量">
          <el-input-number v-model="productionQuantity" :min="1" @change="updateRequiredMaterials" />
        </el-form-item>
      </el-form>

      <h3 style="margin-top: 20px; margin-bottom: 10px;">所需物料</h3>
      <el-table :data="requiredMaterials" style="width: 100%">
        <el-table-column prop="name" label="物料名稱" />
        <el-table-column prop="required" label="需求數量" />
        <el-table-column prop="available" label="可用庫存" />
        <el-table-column label="狀態">
            <template #default="scope">
                <el-tag :type="scope.row.available >= scope.row.required ? 'success' : 'danger'">
                    {{ scope.row.available >= scope.row.required ? '庫存足夠' : '庫存不足' }}
                </el-tag>
            </template>
        </el-table-column>
      </el-table>
      <div v-if="isStockInsufficient" style="color: red; margin-top: 10px;">
        警告：一種或多種物料庫存不足。
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="productionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmProduction" :disabled="isStockInsufficient">確認生產</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Add Product Dialog -->
    <el-dialog v-model="addProductDialogVisible" title="新增產品">
      <el-form :model="newMaterial" label-width="120px">
        <el-form-item label="產品名稱">
          <el-input v-model="newMaterial.materialName"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newMaterial.materialDescription"></el-input>
        </el-form-item>
        <el-form-item label="價格">
          <el-input-number v-model="newMaterial.price" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item label="類別">
          <el-select v-model="newMaterial.category" placeholder="選擇或建立類別" allow-create filterable>
            <el-option
              v-for="item in uniqueCategories"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="BOM 組件">
          <el-select
            v-model="newProductBomMaterials"
            multiple
            placeholder="選擇產品物料"
          >
            <el-option
              v-for="material in depotMaterials"
              :key="material.materialId"
              :label="material.materialName"
              :value="material.materialId"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item
          v-for="materialId in newProductBomMaterials"
          :key="materialId"
          :label="`數量：${materialId !== null && materialId !== undefined ? getMaterialNameById(materialId) : 'Unknown Material (ID: Missing/Invalid)'}`"
        >
          <el-input-number v-model="newProductBomQuantities[materialId]" :min="1"></el-input-number>
          <el-button type="danger" :icon="Delete" circle @click="removeNewProductBomMaterial(materialId)" style="margin-left: 10px;" />
        </el-form-item>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addProductDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNewProduct">儲存</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, watch } from 'vue'
import api from '../http-common'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

// --- 類型定義 (Type Definitions) ---
interface Product {
  id: number;
  name: string;
  price: number;
  category: string;
}

interface NewMaterial {
  materialName: string;
  materialDescription: string;
  price: number; // Assuming price is part of material for products
  category: string; // Assuming category is part of material for products
  materialType: string; // 'PRODUCT' or 'RAW_MATERIAL'
}

interface Material {
  materialId: number;
  materialName: string;
  materialType: string;
  stockCurrent: number;
  price: number; // Add price to Material interface
  category: string; // Add category to Material interface
}

interface BomComponent {
  bomComponentId: number | null;
  parentMaterialId: number | null;
  componentMaterialId: number | number[] | null;
  quantity: number;
  notes: string;
}

interface RequiredMaterial {
  id: number;
  name: string;
  required: number;
  available: number;
}



// --- 響應式狀態 (Reactive State) ---
const products = ref<Material[]>([]) // This will now be the filtered list of finished goods
const allMaterials = ref<Material[]>([]) // This remains the list of ALL products/materials
const bomComponents = ref<BomComponent[]>([])
const selectedMaterialId = ref<number | null>(null)

const dialogVisible = ref(false)
const bomFormDialogVisible = ref(false)
const isEditMode = ref(false)
const addProductDialogVisible = ref(false)

const currentBomComponent = ref<BomComponent>({
  bomComponentId: null,
  parentMaterialId: null,
  componentMaterialId: null, 
  quantity: 0,
  notes: '',
})

const newMaterial = ref<NewMaterial>({
  materialName: '',
  materialDescription: '',
  price: 0,
  category: '',
  materialType: 'PRODUCT', // Default to PRODUCT
});

const newProductBomMaterials = ref<number[]>([]); // To hold selected material IDs
const newProductBomQuantities = ref<Record<number, number>>({}); // To hold quantities for each material ID

// Production Order State
const productionDialogVisible = ref(false)
const selectedProduct = ref<Material | null>(null)
const productionQuantity = ref(1)
const requiredMaterials = ref<RequiredMaterial[]>([])
const depotMaterials = ref<Material[]>([])

// --- Computed Properties ---
const isStockInsufficient = computed(() => {
  return requiredMaterials.value.some(m => m.available < m.required);
});

const uniqueCategories = computed(() => {
  // Use categories from allMaterials
  const categories = allMaterials.value.map(m => m.category);
  return [...new Set(categories)].filter(category => category); // Return unique product categories, filter out empty ones
});

// --- 生命週期鉤子 (Lifecycle Hooks) ---
onMounted(async () => {
  await refreshAllData();
});

watch(newProductBomMaterials, (newVal, oldVal) => {
  // Add new materials to quantities with default 1
  newVal.forEach(materialId => {
    if (!(materialId in newProductBomQuantities.value)) {
      newProductBomQuantities.value[materialId] = 1;
    }
  });

  // Remove quantities for materials no longer selected
  for (const materialId in newProductBomQuantities.value) {
    if (!newVal.includes(materialId)) {
      delete newProductBomQuantities.value[materialId];
    }
  }
}, { deep: true });

// --- 方法 (Methods) ---
const refreshAllData = async () => {
  await fetchAllProducts(); // Now fetches products (materials of type PRODUCT)
  await fetchDepotMaterials(); // Fetches all materials (raw and products)

  // Ensure depotMaterials.value is an array before using .map
  if (!Array.isArray(depotMaterials.value)) {
    depotMaterials.value = [];
  }
  console.log('refreshAllData: depotMaterials.value before map:', depotMaterials.value);
  console.log('refreshAllData: uniqueCategories.value before map:', uniqueCategories.value); // Re-added log

  // products.value is already filtered by fetchAllProducts
};

const fetchAllProducts = async () => {
  try {
    console.log('Attempting to fetch products (materialType=PRODUCT) from /depot/materials');
    const response = await api.get<Material[]>('/depot/materials?materialType=PRODUCT');
    // Ensure response.data is an array before mapping
    if (Array.isArray(response.data)) {
      products.value = response.data.map(m => ({
        id: m.materialId,
        name: m.materialName,
        price: m.price,
        category: m.category,
        materialType: m.materialType, // Include materialType
        stockCurrent: m.stockCurrent, // Include stockCurrent
      }));
      allMaterials.value = response.data; // Store all materials for category filtering
    } else {
      products.value = [];
      allMaterials.value = [];
      console.warn('API /depot/materials?materialType=PRODUCT did not return an array:', response.data);
    }
  } catch (error) {
    console.error('Error fetching all products:', error);
    ElMessage.error('Failed to fetch all products.');
    products.value = []; // Set to empty array on error
    allMaterials.value = [];
  }
};

const fetchDepotMaterials = async () => {
  try {
    const response = await api.get<Material[]>('/depot/materials'); // Get all materials
    depotMaterials.value = response.data || []; // Ensure it's an array
    console.log('fetchDepotMaterials: depotMaterials.value after fetch:', depotMaterials.value);
  } catch (error) {
    console.error('Error fetching depot materials:', error);
    ElMessage.error('Failed to fetch raw materials stock.');
    depotMaterials.value = []; // Set to empty array on error
    console.log('fetchDepotMaterials: depotMaterials.value on error:', depotMaterials.value);
  }
};

const getMaterialNameById = (materialId: number | null | undefined) => {
  console.log(`getMaterialNameById called with materialId: ${materialId}, type: ${typeof materialId}`);
  if (materialId === null || materialId === undefined) {
    return `Unknown Material (ID: ${materialId})`;
  }
  const material = depotMaterials.value.find(m => m.materialId === materialId);
  return material ? material.materialName : `Unknown Material (ID: ${materialId})`;
};

const removeNewProductBomMaterial = (materialIdToRemove: number) => {
  const index = newProductBomMaterials.value.indexOf(materialIdToRemove);
  if (index > -1) {
    newProductBomMaterials.value.splice(index, 1);
    delete newProductBomQuantities.value[materialIdToRemove];
  }
};

const handleRowClick = async (row: Material) => {
  selectedMaterialId.value = row.id;
  console.log(`Fetching BOM components for materialId: ${row.id}`);
  await fetchBomComponents(row.id);
  dialogVisible.value = true;
};

const fetchBomComponents = async (materialId: number) => {
  try {
    const response = await api.get<BomComponent[]>(`/boms/material/${materialId}?_t=${new Date().getTime()}`);
    bomComponents.value = response.data;
    return response.data;
  } catch (error) {
    console.error('Error fetching BOM components:', error);
    ElMessage.error('Failed to fetch BOM components.');
    return [];
  }
};

const openAddBomDialog = () => {
  if (!selectedMaterialId.value) return; // Add this check
  isEditMode.value = false;
  currentBomComponent.value = {
    bomComponentId: null,
    parentMaterialId: selectedMaterialId.value,
    componentMaterialId: [], 
    quantity: 1,
    notes: '',
  };
  bomFormDialogVisible.value = true;
};

const handleEdit = (row: BomComponent) => {
  isEditMode.value = true;
  currentBomComponent.value = { 
    ...row, 
    bomComponentId: row.bomComponentId, // Ensure bomComponentId is explicitly passed
    componentMaterialId: row.componentMaterialId 
  };
  bomFormDialogVisible.value = true;
};

const handleDelete = async (row: BomComponent) => {
  try {
    await ElMessageBox.confirm(
      `您確定要刪除此BOM組件嗎？`,
      '警告',
      {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    await api.delete(`/boms/${row.bomComponentId as number}`);
    ElMessage.success('BOM組件刪除成功！');
    if (selectedMaterialId.value) {
      await fetchBomComponents(selectedMaterialId.value);
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Error deleting BOM component:', error);
      ElMessage.error('刪除BOM組件失敗。');
    }
  }
};

const saveBomComponent = async () => {
  if (!selectedMaterialId.value) return;

  try {
    if (isEditMode.value) {
      // Handle single update for existing BOM component
      await api.put(`/boms/update`, currentBomComponent.value);
      ElMessage.success('BOM組件更新成功！');

      // Directly update the local array to avoid caching issues
      const index = bomComponents.value.findIndex(
        c => c.bomComponentId === currentBomComponent.value.bomComponentId
      );
      if (index !== -1) {
        bomComponents.value[index] = { ...currentBomComponent.value };
      } else {
        // Fallback to refetching if the item isn't found for some reason
        await fetchBomComponents(selectedMaterialId.value);
      }
    } else {
      // Handle multiple additions for new BOM components
      if (Array.isArray(currentBomComponent.value.componentMaterialId)) {
        if (currentBomComponent.value.componentMaterialId.length === 0) {
          ElMessage.error('請選擇至少一個組件物料。');
          return;
        }
        for (const componentId of currentBomComponent.value.componentMaterialId as number[]) {
          const payload = {
            parentMaterialId: selectedMaterialId.value, 
            componentMaterialId: componentId as number,
            quantity: currentBomComponent.value.quantity,
            notes: '',
          };
          await api.post(`/boms/add`, payload);
        }
        ElMessage.success('BOM組件新增成功！');
      } else {
        // Fallback for single selection if somehow not an array (should not happen with multiple select)
        if (!currentBomComponent.value.componentMaterialId) {
          ElMessage.error('請選擇組件物料。');
          return;
        }
        const payload = {
            parentMaterialId: selectedMaterialId.value, 
            componentMaterialId: currentBomComponent.value.componentMaterialId,
            quantity: currentBomComponent.value.quantity,
            notes: '',
        };
        await api.post(`/boms/add`, payload);
        ElMessage.success('BOM組件新增成功！');
      }
      // After adding new components, refetch the list to get new IDs and data
      await fetchBomComponents(selectedMaterialId.value);
    }
    bomFormDialogVisible.value = false;
  } catch (error) {
    console.error('Error saving BOM component:', error);
    ElMessage.error('儲存BOM組件失敗。');
  }
};

const openAddProductDialog = () => {
  newMaterial.value = {
    materialName: '',
    materialDescription: '',
    price: 0,
    category: '',
    materialType: 'PRODUCT', // Default to PRODUCT
  };
  newProductBomMaterials.value = []; // Reset selected BOM material IDs
  newProductBomQuantities.value = {}; // Reset quantities
  addProductDialogVisible.value = true;
};

const saveNewProduct = async () => {
  try {
    // Step 1: Create the new material (product)
    const materialPayload = {
      materialName: newMaterial.value.materialName,
      materialDescription: newMaterial.value.materialDescription,
      price: newMaterial.value.price,
      category: newMaterial.value.category,
      materialType: 'PRODUCT', // Ensure it's saved as a PRODUCT
      stockCurrent: 0, // Initial stock is zero
      unit: '個', // Default unit
      location: '未指定', // Default location
      safetyStock: 0, // Default safety stock
      reorderLevel: 0, // Default reorder level
    };
    const response = await api.post('/depot/materials', materialPayload);
    const createdMaterial = response.data;
    ElMessage.success('Product (Material) created successfully!');

    // Step 2: Add BOM components for the new product (material)
    if (newProductBomMaterials.value && newProductBomMaterials.value.length > 0) {
      for (const materialId of newProductBomMaterials.value) {
        const bomPayload = {
          parentMaterialId: createdMaterial.materialId, 
          componentMaterialId: materialId,
          quantity: newProductBomQuantities.value[materialId] || 1, 
          notes: '',
        };
        await api.post(`/boms/add`, bomPayload);
      }
      ElMessage.success('BOM components added successfully!');
    }

    addProductDialogVisible.value = false;
    await refreshAllData(); // Refresh all data to show the new product in the list
  } catch (error) {
    console.error('Error during product creation process:', error);
    ElMessage.error('An error occurred while creating the product or adding it to the depot.');
  }
};

// --- Production Methods ---
const openProductionDialog = async (product: Material) => {
  selectedProduct.value = product;
  productionQuantity.value = 1;
  await updateRequiredMaterials();
  productionDialogVisible.value = true;
};

const updateRequiredMaterials = async () => {
  if (!selectedProduct.value) return;

  const bom = await fetchBomComponents(selectedProduct.value.id as number);
  if (!bom) return;

  requiredMaterials.value = bom.map(component => {
    const depotMat = depotMaterials.value.find(m => m.materialId === component.componentMaterialId);
    return {
      id: component.componentMaterialId as number,
      name: (component.componentMaterialId !== null && component.componentMaterialId !== undefined) ? getMaterialNameById(component.componentMaterialId as number) : 'Unknown Material (ID: Missing/Invalid)',
      required: component.quantity * productionQuantity.value,
      available: depotMat ? depotMat.stockCurrent : 0,
    };
  });
};

const confirmProduction = async () => {
  if (!selectedProduct.value) return;

  const woNumber = `WO-${selectedProduct.value.name.replace(/\s/g, '')}-${Date.now()}`;

  const workOrderPayload = {
    woNumber: woNumber,
    materialId: selectedProduct.value.id as number,
    requiredQuantity: productionQuantity.value,
    status: '未開始',
  };

  try {
    await api.post('/workorder', workOrderPayload);
    ElMessage.success(`已成功為 ${selectedProduct.value.name} 建立 ${productionQuantity.value} 的生產工單！`);
    productionDialogVisible.value = false;
    await refreshAllData(); // Refresh both products and materials
  } catch (error: any) {
    console.error('Error creating work order:', error);
    ElMessage.error(`建立生產工單失敗：${error.response?.data?.message || error.message}`);
  }
}

const handleDeleteProduct = async (row: Material) => {
  try {
    await ElMessageBox.confirm(
      `確定要刪除產品 ${row.name} 嗎？這將同時刪除相關的 BOM 組件。`,
      '警告',
      {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    await api.delete(`/depot/materials/${row.id}`); // Delete material (product)
    ElMessage.success('產品刪除成功！');
    await refreshAllData(); // Refresh product list
  } catch (error) {
    if (error !== 'cancel') {
      console.error('刪除產品失敗:', error);
      ElMessage.error('刪除產品失敗。');
    }
  }
};

const isMaterialDisabled = (materialId: number) => {
  // Disable if the material is the parent material
  if (materialId === selectedMaterialId.value) {
    return true;
  }
  // In edit mode, the component being edited should not be disabled.
  if (isEditMode.value && currentBomComponent.value && materialId === currentBomComponent.value.componentMaterialId) {
    return false;
  }
  // Otherwise, disable if the material is already in the BOM components list.
  return bomComponents.value.some(component => component.componentMaterialId === materialId);
};

</script>