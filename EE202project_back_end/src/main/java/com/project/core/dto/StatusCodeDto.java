package com.project.core.dto;

public class StatusCodeDto {
    private String status_code;
    private String status_label;

    public StatusCodeDto(String status_code, String status_label) {
        this.status_code = status_code;
        this.status_label = status_label;
    }

    public String getStatus_code() {
        return status_code;
    }

    public void setStatus_code(String status_code) {
        this.status_code = status_code;
    }

    public String getStatus_label() {
        return status_label;
    }

    public void setStatus_label(String status_label) {
        this.status_label = status_label;
    }
}
