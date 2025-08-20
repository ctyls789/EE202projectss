package com.project.workorder.service;

import com.project.workorder.dto.WorkOrderCompletionRequest;
import com.project.workorder.dto.PickingRequest;
import com.project.workorder.dto.ProductionOrderRequest;
import com.project.workorder.dto.WorkOrderCreateRequest;
import com.project.workorder.dto.WorkOrderDto;
import com.project.workorder.dto.WorkOrderMaterialDto;
import com.project.workorder.service.WorkOrderService;
import com.project.depot.service.MaterialService;
import com.project.core.dao.EmployeeUserRepository;

import com.project.depot.dao.InventoryTransactionRepository;
import com.project.depot.dao.MaterialRepository;
import com.project.depot.model.InventoryTransaction;
import com.project.depot.model.Material;
import com.project.workorder.dao.BomComponentRepository;
import com.project.workorder.dao.WorkOrderMaterialRepository;
import com.project.workorder.dao.WorkOrderRepository;
import com.project.workorder.model.BomComponent;
import com.project.workorder.model.WorkOrder;
import com.project.workorder.model.WorkOrderMaterial;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 工單服務實作類別
 * 實作工單相關的業務邏輯操作。
 */
@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderMaterialRepository workOrderMaterialRepository;
    private final MaterialRepository materialRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final EmployeeUserRepository employeeUserRepository;
    private final MaterialService materialService; // 注入 MaterialService
    private final BomComponentRepository bomComponentRepository;

    /**
     * 建構子注入依賴。
     * 
     * @param workOrderRepository            工單資料庫操作介面
     * @param workOrderMaterialRepository    工單領料明細資料庫操作介面
     * @param materialRepository             物料資料庫操作介面
     * @param inventoryTransactionRepository 庫存交易紀錄資料庫操作介面
     * @param employeeUserRepository         員工使用者資料庫操作介面
     * @param materialService                物料服務
     */
    public WorkOrderServiceImpl(WorkOrderRepository workOrderRepository,
            WorkOrderMaterialRepository workOrderMaterialRepository,
            MaterialRepository materialRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            EmployeeUserRepository employeeUserRepository,
            MaterialService materialService,
            BomComponentRepository bomComponentRepository) { // Add MaterialService to constructor
        this.workOrderRepository = workOrderRepository;
        this.workOrderMaterialRepository = workOrderMaterialRepository;
        this.materialRepository = materialRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.employeeUserRepository = employeeUserRepository;
        this.materialService = materialService;
        this.bomComponentRepository = bomComponentRepository;
    }

    /**
     * 查詢所有工單。
     * 
     * @return 工單列表
     */
    @Override
    public List<WorkOrderDto> findAllWorkOrders() {
        return workOrderRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據ID查詢單一工單。
     * 
     * @param id 工單ID
     * @return Optional<WorkOrderDto> 如果找到則返回工單，否則為空
     */
    @Override
    public Optional<WorkOrderDto> findWorkOrderById(Long id) {
        return workOrderRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * 新增工單。
     * 
     * @param request 包含工單資訊的請求DTO
     * @return 儲存後的工單DTO
     */
    @Override
    @Transactional
    public WorkOrderDto createWorkOrder(WorkOrderCreateRequest request) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWoNumber(request.getWoNumber());

        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Material not found with ID: " + request.getMaterialId()));
        workOrder.setMaterial(material);
        workOrder.setRequiredQuantity(request.getRequiredQuantity());
        workOrder.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        // Assuming requestedBy and issuedBy are set elsewhere or are optional
        // For now, setting them to null or a default user if needed
        workOrder.setRequestedBy(null); // Or fetch a default user
        workOrder.setIssuedBy(null); // Or fetch a default user

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);

        // Create WorkOrderMaterial from BOM
        List<BomComponent> bomComponents = bomComponentRepository.findByParentMaterial_MaterialId(material.getMaterialId());
        for (BomComponent bomComponent : bomComponents) {
            WorkOrderMaterial woMaterial = new WorkOrderMaterial();
            woMaterial.setWorkOrder(savedWorkOrder);
            woMaterial.setMaterial(bomComponent.getComponentMaterial());
            woMaterial.setRequestedQuantity(bomComponent.getQuantity().multiply(savedWorkOrder.getRequiredQuantity()));
            // Deduct raw materials and update WorkOrderMaterial
            Material rawMaterial = woMaterial.getMaterial();
            BigDecimal quantityToDeduct = woMaterial.getRequestedQuantity(); // Deduct the requested quantity

            // Check if enough material is available
            if (rawMaterial.getStockCurrent().compareTo(quantityToDeduct) < 0) {
                throw new IllegalArgumentException("Not enough stock for material: " + rawMaterial.getMaterialName());
            }

            // Deduct stock
            rawMaterial.setStockCurrent(rawMaterial.getStockCurrent().subtract(quantityToDeduct));
            materialRepository.save(rawMaterial);

            // Record outbound transaction for raw material
            InventoryTransaction rawMaterialTransaction = new InventoryTransaction();
            rawMaterialTransaction.setMaterial(rawMaterial);
            rawMaterialTransaction.setTransactionType("PRODUCTION_OUTBOUND");
            rawMaterialTransaction.setQuantity(quantityToDeduct);
            rawMaterialTransaction.setTransactionDate(LocalDateTime.now());
            rawMaterialTransaction.setReferenceTable("work_orders");
            rawMaterialTransaction.setReferenceId(savedWorkOrder.getWoId());
            inventoryTransactionRepository.save(rawMaterialTransaction);

            // Update WorkOrderMaterial issued quantity and status
            woMaterial.setIssuedQuantity(quantityToDeduct);
            woMaterial.setStatus("ISSUED");
            workOrderMaterialRepository.save(woMaterial);
        }

        return convertToDto(savedWorkOrder);
    }

    /**
     * 建立生產工單並扣除物料庫存。
     * 
     * @param request 生產工單請求DTO
     * @return 儲存後的工單DTO
     */
    @Override
    @Transactional
    public WorkOrderDto createProductionOrder(ProductionOrderRequest request) {
        // 1. 產生一個實體物件之後導入
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWoNumber(request.getWoNumber());

        Material finishedProduct = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Finished product material not found with ID: " + request.getMaterialId()));
        workOrder.setMaterial(finishedProduct);
        workOrder.setRequiredQuantity(new BigDecimal(request.getRequiredQuantity().toString()));
        workOrder.setStatus(request.getStatus() != null ? request.getStatus() : "COMPLETED");
        // completes immediately
        workOrder.setRequestedBy(null); // Or fetch a default user
        workOrder.setIssuedBy(null); // Or fetch a default user

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);

        // 2. 減少物料的判斷與轉換型態
        for (com.project.workorder.dto.MaterialDeductionDto deduction : request.getMaterialsToDeduct()) {
            materialService.deductMaterialStock(deduction.getMaterialId(), deduction.getQuantity());

            // Record inventory transaction for raw material outbound
            InventoryTransaction transaction = new InventoryTransaction();
            Material rawMaterial = materialRepository.findById(deduction.getMaterialId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Raw material not found with ID: " + deduction.getMaterialId()));
            transaction.setMaterial(rawMaterial);
            // [修正] 使用資料庫允許的、代表「發料/消耗」的 物料。
            transaction.setTransactionType("PRODUCTION_OUTBOUND"); // 給logger(歷程記錄做的)
            transaction.setQuantity(deduction.getQuantity());
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setReferenceTable("work_orders");
            transaction.setReferenceId(savedWorkOrder.getWoId());
            inventoryTransactionRepository.save(transaction);
        }

        // 3. Increase finished product stock and record transaction
        finishedProduct.setStockCurrent(finishedProduct.getStockCurrent().add(request.getRequiredQuantity()));
        materialRepository.save(finishedProduct);

        InventoryTransaction finishedProductTransaction = new InventoryTransaction();
        finishedProductTransaction.setMaterial(finishedProduct);
        // [修正] 使用資料庫允許的、代表「收料/入庫」的 Enum。
        finishedProductTransaction.setTransactionType("PRODUCTION_INBOUND");
        finishedProductTransaction.setQuantity(request.getRequiredQuantity());
        finishedProductTransaction.setTransactionDate(LocalDateTime.now());
        finishedProductTransaction.setReferenceTable("work_orders");
        finishedProductTransaction.setReferenceId(savedWorkOrder.getWoId());
        inventoryTransactionRepository.save(finishedProductTransaction);

        return convertToDto(savedWorkOrder);
    }

    /**
     * 更新工單。
     * 
     * @param id      工單ID
     * @param request 包含更新資訊的請求DTO
     * @return 更新後的工單DTO
     */
    @Override
    @Transactional
    public WorkOrderDto updateWorkOrder(Long id, WorkOrderCreateRequest request) {
        WorkOrder existingWorkOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("WorkOrder not found with ID: " + id));

        existingWorkOrder.setWoNumber(request.getWoNumber());
        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Material not found with ID: " + request.getMaterialId()));
        existingWorkOrder.setMaterial(material);
        existingWorkOrder.setRequiredQuantity(request.getRequiredQuantity());
        existingWorkOrder.setStatus(request.getStatus() != null ? request.getStatus() : existingWorkOrder.getStatus());

        WorkOrder updatedWorkOrder = workOrderRepository.save(existingWorkOrder);
        return convertToDto(updatedWorkOrder);
    }

    /**
     * 根據ID刪除工單。
     * 
     * @param id 工單ID
     */
    @Override
    @Transactional
    public void deleteWorkOrderById(Long id) {
        if (!workOrderRepository.existsById(id)) {
            throw new EntityNotFoundException("WorkOrder not found with ID: " + id);
        }
        workOrderRepository.deleteById(id);
    }

    /**
     * 處理物料領料 (出庫) 操作。
     * 
     * @param request 領料請求DTO
     * @return 處理後的領料明細DTO
     */
    @Override
    @Transactional
    public WorkOrderMaterialDto processMaterialPicking(PickingRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(request.getWoId())
                .orElseThrow(() -> new EntityNotFoundException("WorkOrder not found with ID: " + request.getWoId()));

        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Material not found with ID: " + request.getMaterialId()));

        // Check if enough material is available in stock
        if (material.getStockCurrent().compareTo(request.getRequestedQuantity()) < 0) {
            throw new IllegalArgumentException(
                    "Not enough material in stock for material ID: " + material.getMaterialId());
        }

        // Update material stock
        material.setStockCurrent(material.getStockCurrent().subtract(request.getRequestedQuantity()));
        materialRepository.save(material);

        // Create inventory transaction for outbound
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setMaterial(material);
        // [修正] 「領料(Picking)」是發料出庫的行為，應該使用 PRODUCTION_OUTBOUND。原先的 PRODUCTION_INBOUND
        // 是不合邏輯的。
        transaction.setTransactionType("PRODUCTION_OUTBOUND");
        transaction.setQuantity(request.getRequestedQuantity());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setReferenceId(workOrder.getWoId()); // Reference to work order
        inventoryTransactionRepository.save(transaction);

        // Update or create WorkOrderMaterial
        WorkOrderMaterial workOrderMaterial = workOrderMaterialRepository
                .findByWorkOrder_WoId(workOrder.getWoId())
                .stream()
                .filter(wm -> wm.getMaterial().getMaterialId().equals(material.getMaterialId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("WorkOrderMaterial not found for material ID: " + material.getMaterialId()));

        workOrderMaterial.setWorkOrder(workOrder);
        workOrderMaterial.setMaterial(material);
        workOrderMaterial.setIssuedQuantity(
                Optional.ofNullable(workOrderMaterial.getIssuedQuantity()).orElse(BigDecimal.ZERO)
                        .add(request.getRequestedQuantity()));
        workOrderMaterial.setStatus("ISSUED"); // Or partially issued

        WorkOrderMaterial savedWorkOrderMaterial = workOrderMaterialRepository.save(workOrderMaterial);
        // Reload the entity to ensure ID is populated
        savedWorkOrderMaterial = workOrderMaterialRepository.findById(savedWorkOrderMaterial.getWoMaterialId())
                .orElseThrow(() -> new EntityNotFoundException("WorkOrderMaterial not found after save."));
        return convertToDto(savedWorkOrderMaterial);
    }

    /**
     * 根據工單ID查詢所有領料明細。
     * 
     * @param woId 工單ID
     * @return 領料明細列表
     */
    @Override
    public List<WorkOrderMaterialDto> findWorkOrderMaterialsByWorkOrderId(Long woId) {
        return workOrderMaterialRepository.findByWorkOrder_WoId(woId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 查詢所有領料明細。
     * 
     * @return 領料明細列表
     */
    @Override
    public List<WorkOrderMaterialDto> findAllWorkOrderMaterials() {
        return workOrderMaterialRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據ID查詢單一領料明細。
     * 
     * @param id 領料明細ID
     * @return Optional<WorkOrderMaterialDto> 如果找到則返回領料明細，否則為空
     */
    @Override
    public Optional<WorkOrderMaterialDto> findWorkOrderMaterialById(Long id) {
        return workOrderMaterialRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * 根據ID刪除領料明細。
     * 
     * @param id 領料明細ID
     */
    @Override
    @Transactional
    public void deleteWorkOrderMaterialById(Long id) {
        if (!workOrderMaterialRepository.existsById(id)) {
            throw new EntityNotFoundException("WorkOrderMaterial not found with ID: " + id);
        }
        workOrderMaterialRepository.deleteById(id);
    }

    @Override
    @Transactional
    public WorkOrderDto completeWorkOrder(Long id, WorkOrderCompletionRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("WorkOrder not found with ID: " + id));

        workOrder.setStatus("COMPLETED");

        // Deduct raw materials and update WorkOrderMaterial
        List<WorkOrderMaterial> workOrderMaterials = workOrderMaterialRepository.findByWorkOrder_WoId(workOrder.getWoId());
        for (WorkOrderMaterial woMaterial : workOrderMaterials) {
            Material rawMaterial = woMaterial.getMaterial();
            BigDecimal quantityToDeduct = woMaterial.getRequestedQuantity(); // Deduct the requested quantity

            // Check if enough material is available
            if (rawMaterial.getStockCurrent().compareTo(quantityToDeduct) < 0) {
                throw new IllegalArgumentException("Not enough stock for material: " + rawMaterial.getMaterialName());
            }

            // Deduct stock
            rawMaterial.setStockCurrent(rawMaterial.getStockCurrent().subtract(quantityToDeduct));
            materialRepository.save(rawMaterial);

            // Record outbound transaction for raw material
            InventoryTransaction rawMaterialTransaction = new InventoryTransaction();
            rawMaterialTransaction.setMaterial(rawMaterial);
            rawMaterialTransaction.setTransactionType("PRODUCTION_OUTBOUND");
            rawMaterialTransaction.setQuantity(quantityToDeduct);
            rawMaterialTransaction.setTransactionDate(LocalDateTime.now());
            rawMaterialTransaction.setReferenceTable("work_orders");
            rawMaterialTransaction.setReferenceId(workOrder.getWoId());
            inventoryTransactionRepository.save(rawMaterialTransaction);

            // Update WorkOrderMaterial issued quantity and status
            woMaterial.setIssuedQuantity(quantityToDeduct);
            woMaterial.setStatus("ISSUED");
            workOrderMaterialRepository.save(woMaterial);
        }

        // Increase finished product stock and record transaction
        Material finishedProduct = workOrder.getMaterial();
        finishedProduct.setStockCurrent(finishedProduct.getStockCurrent().add(request.getQuantityDone()));
        materialRepository.save(finishedProduct);

        InventoryTransaction finishedProductTransaction = new InventoryTransaction();
        finishedProductTransaction.setMaterial(finishedProduct);
        finishedProductTransaction.setTransactionType("PRODUCTION_INBOUND");
        finishedProductTransaction.setQuantity(request.getQuantityDone());
        finishedProductTransaction.setTransactionDate(LocalDateTime.now());
        finishedProductTransaction.setReferenceTable("work_orders");
        finishedProductTransaction.setReferenceId(workOrder.getWoId());
        inventoryTransactionRepository.save(finishedProductTransaction);

        // Optionally handle failed quantity, e.g., log it
        if (request.getQuantityFailed().compareTo(BigDecimal.ZERO) > 0) {
            // Log failed quantity
        }

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);
        return convertToDto(savedWorkOrder);
    }

    /**
     * 將 WorkOrder 實體轉換為 WorkOrderDto DTO。
     * 
     * @param workOrder WorkOrder 實體
     * @return WorkOrderDto DTO
     */
    private WorkOrderDto convertToDto(WorkOrder workOrder) {
        WorkOrderDto dto = new WorkOrderDto();
        dto.setWoId(workOrder.getWoId().intValue());
        dto.setWoNumber(workOrder.getWoNumber());
        dto.setMaterialId(workOrder.getMaterial().getMaterialId());
        dto.setMaterialName(workOrder.getMaterial().getMaterialName());
        dto.setRequiredQuantity(workOrder.getRequiredQuantity());
        dto.setStatus(workOrder.getStatus());
        dto.setCreatedAt(workOrder.getCreatedAt());
        dto.setUpdatedAt(workOrder.getUpdatedAt());
        return dto;
    }

    /**
     * 將 WorkOrderMaterial 實體轉換為 WorkOrderMaterialDto DTO。
     * 
     * @param workOrderMaterial WorkOrderMaterial 實體
     * @return WorkOrderMaterialDto DTO
     */
    private WorkOrderMaterialDto convertToDto(WorkOrderMaterial workOrderMaterial) {
        WorkOrderMaterialDto dto = new WorkOrderMaterialDto();
        dto.setWoMaterialId(workOrderMaterial.getWoMaterialId().intValue()); // Convert Long to Integer
        dto.setMaterialId(workOrderMaterial.getMaterial().getMaterialId()); // Assuming materialId is Long in DTO
        dto.setMaterialName(workOrderMaterial.getMaterial().getMaterialName());
        dto.setRequestedQuantity(workOrderMaterial.getRequestedQuantity());
        dto.setIssuedQuantity(workOrderMaterial.getIssuedQuantity());
        dto.setStatus(workOrderMaterial.getStatus());
        return dto;
    }
}