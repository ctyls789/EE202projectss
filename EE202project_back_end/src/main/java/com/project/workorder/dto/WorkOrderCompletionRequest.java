package com.project.workorder.dto;

import java.math.BigDecimal;

public class WorkOrderCompletionRequest {
    private BigDecimal quantityDone;
    private BigDecimal quantityFailed;

    public BigDecimal getQuantityDone() {
        return quantityDone;
    }

    public void setQuantityDone(BigDecimal quantityDone) {
        this.quantityDone = quantityDone;
    }

    public BigDecimal getQuantityFailed() {
        return quantityFailed;
    }

    public void setQuantityFailed(BigDecimal quantityFailed) {
        this.quantityFailed = quantityFailed;
    }
}
