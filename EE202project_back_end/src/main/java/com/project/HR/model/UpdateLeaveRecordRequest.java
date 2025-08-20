package com.project.HR.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UpdateLeaveRecordRequest {

    private String reason;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private BigDecimal hours;

    // Getters and Setters

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(LocalDateTime startDatetime) {
        this.startDatetime = startDatetime;
    }

    public LocalDateTime getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(LocalDateTime endDatetime) {
        this.endDatetime = endDatetime;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }
}