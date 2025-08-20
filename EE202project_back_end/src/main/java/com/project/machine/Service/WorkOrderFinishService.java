package com.project.machine.Service.workOrderFinish;

import com.project.machine.Bean.WorkOderFinishBean;
import com.project.machine.Repository.WorkOrderFinishRepository;
import com.project.workorder.dao.WorkOrderRepository;
import com.project.workorder.model.WorkOrder;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkOrderFinishService {

    private final WorkOrderFinishRepository finishRepository;
    private final WorkOrderRepository workOrderRepository;

    public WorkOrderFinishService(WorkOrderFinishRepository finishRepository, WorkOrderRepository workOrderRepository) {
        this.finishRepository = finishRepository;
        this.workOrderRepository = workOrderRepository;
    }

    /**
     * 新增生產回報前，先確認工單是否存在
     * 
     * @param report 要新增的回報物件
     * @return 新增成功的回報物件
     * @throws ResourceNotFoundException 工單不存在時拋出
     */
    public WorkOderFinishBean createReport(WorkOderFinishBean report) {
        WorkOrder workOrder = report.getWorkOrder();
        if (workOrder == null || workOrder.getWoId() == null) {
            throw new IllegalArgumentException("工單資訊不可為空");
        }
        Long woId = workOrder.getWoId();
        if (!workOrderRepository.existsById(woId)) {
            throw new ResourceNotFoundException("工單不存在，無法新增回報，woId=" + woId);
        }
        return finishRepository.save(report);
    }

    /**
     * 依回報ID查詢單筆回報
     * 
     * @param reportId 回報ID
     * @return 回報物件
     * @throws ResourceNotFoundException 找不到回報時拋出
     */
    public WorkOderFinishBean getReportById(int reportId) {
        return finishRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到回報紀錄，reportId=" + reportId));
    }

    /**
     * 依工單ID查詢該工單所有生產回報紀錄
     * 
     * @param woId 工單ID
     * @return 該工單所有回報列表
     */
    public List<WorkOderFinishBean> getReportsByWorkOrderId(Long woId) {
        return finishRepository.findByWorkOrder_WoId(woId);
    }

    /**
     * 修改回報，必須存在且工單也存在
     * 
     * @param reportId     回報ID
     * @param updateReport 更新的回報物件
     * @return 更新後的回報物件
     * @throws ResourceNotFoundException 找不到回報或工單不存在時拋出
     */
    public WorkOderFinishBean updateReport(int reportId, WorkOderFinishBean updateReport) {
        WorkOderFinishBean existing = finishRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到回報紀錄，reportId=" + reportId));

        WorkOrder workOrder = updateReport.getWorkOrder();
        if (workOrder == null || workOrder.getWoId() == null) {
            throw new IllegalArgumentException("工單資訊不可為空");
        }
        Long woId = workOrder.getWoId();
        if (!workOrderRepository.existsById(woId)) {
            throw new ResourceNotFoundException("工單不存在，無法更新回報，woId=" + woId);
        }

        existing.setQuantityDone(updateReport.getQuantityDone());
        existing.setQuantityFailed(updateReport.getQuantityFailed());
        existing.setWorkOrder(updateReport.getWorkOrder());

        return finishRepository.save(existing);
    }

    /**
     * 刪除回報紀錄，依回報ID
     * 
     * @param reportId 回報ID
     * @throws ResourceNotFoundException 找不到回報時拋出
     */
    public void deleteReport(int reportId) {
        WorkOderFinishBean existing = finishRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到回報紀錄，reportId=" + reportId));
        finishRepository.delete(existing);
    }

    /**
     * 自訂例外，表示找不到資源（工單或回報紀錄）
     */
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}
