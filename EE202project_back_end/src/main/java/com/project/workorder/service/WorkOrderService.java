package com.project.workorder.service;

import com.project.workorder.dto.WorkOrderCompletionRequest;
import com.project.workorder.dto.PickingRequest;
import com.project.workorder.dto.ProductionOrderRequest;
import com.project.workorder.dto.WorkOrderCreateRequest;
import com.project.workorder.dto.WorkOrderDto;
import com.project.workorder.dto.WorkOrderMaterialDto;
import java.util.List;
import java.util.Optional;

/**
 * 工單服務介面
 * 定義工單相關的業務邏輯操作。
 */
public interface WorkOrderService {

    /**
     * 查詢所有工單。
     * @return 工單列表
     */
    List<WorkOrderDto> findAllWorkOrders();

    /**
     * 根據ID查詢單一工單。
     * @param id 工單ID
     * @return Optional<WorkOrderDto> 如果找到則返回工單，否則為空
     */
    Optional<WorkOrderDto> findWorkOrderById(Long id);

    /**
     * 新增工單。
     * @param request 包含工單資訊的請求DTO
     * @return 儲存後的工單DTO
     */
    WorkOrderDto createWorkOrder(WorkOrderCreateRequest request);

    /**
     * 更新工單。
     * @param id 工單ID
     * @param request 包含更新資訊的請求DTO
     * @return 更新後的工單DTO
     */
    WorkOrderDto updateWorkOrder(Long id, WorkOrderCreateRequest request);

    /**
     * 根據ID刪除工單。
     * @param id 工單ID
     */
    void deleteWorkOrderById(Long id);

    /**
     * 建立生產工單並扣除物料庫存。
     * @param request 生產工單請求DTO
     * @return 儲存後的工單DTO
     */
    WorkOrderDto createProductionOrder(ProductionOrderRequest request);

    /**
     * 處理物料領料 (出庫) 操作。
     * @param request 領料請求DTO
     * @return 處理後的領料明細DTO
     */
    WorkOrderMaterialDto processMaterialPicking(PickingRequest request);

    /**
     * 根據工單ID查詢所有領料明細。
     * @param woId 工單ID
     * @return 領料明細列表
     */
    List<WorkOrderMaterialDto> findWorkOrderMaterialsByWorkOrderId(Long woId);

    /**
     * 查詢所有領料明細。
     * @return 領料明細列表
     */
    List<WorkOrderMaterialDto> findAllWorkOrderMaterials();

    /**
     * 根據ID查詢單一領料明細。
     * @param id 領料明細ID
     * @return Optional<WorkOrderMaterialDto> 如果找到則返回領料明細，否則為空
     */
    Optional<WorkOrderMaterialDto> findWorkOrderMaterialById(Long id);

    /**
     * 根據ID刪除領料明細。
     * @param id 領料明細ID
     */
    void deleteWorkOrderMaterialById(Long id);

    /**
     * 完成工單並更新庫存。
     * @param id 工單ID
     * @param request 包含完成數量的請求DTO
     * @return 更新後的工單DTO
     */
    WorkOrderDto completeWorkOrder(Long id, WorkOrderCompletionRequest request);
}