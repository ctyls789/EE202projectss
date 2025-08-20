package com.project.workorder.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工單資料傳輸物件 (DTO)
 * 用於向前端傳輸工單資訊。
 */
public class WorkOrderDto {
    /**
     * 工單ID
     */
    private Integer woId;
    /**
     * 工單號碼
     */
    private String woNumber;
    /**
     * 物料ID
     */
    private Long materialId;
    /**
     * 物料名稱
     */
    private String materialName;
    /**
     * 需求數量
     */
    private java.math.BigDecimal requiredQuantity;
    /**
     * 狀態
     */
    private String status;
    /**
     * 建立時間
     */
    private LocalDateTime createdAt;
    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;

    // Constructors
    public WorkOrderDto() {
    }

    public WorkOrderDto(Integer woId, String woNumber, Long materialId, String materialName,
            BigDecimal requiredQuantity, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.woId = woId;
        this.woNumber = woNumber;
        this.materialId = materialId;
        this.materialName = materialName;
        this.requiredQuantity = requiredQuantity;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Integer getWoId() {
        return woId;
    }

    public void setWoId(Integer woId) {
        this.woId = woId;
    }

    public String getWoNumber() {
        return woNumber;
    }

    public void setWoNumber(String woNumber) {
        this.woNumber = woNumber;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public BigDecimal getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(BigDecimal requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
