package com.project.core.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class EmployeeUserCreateRequest {
    private String firstName;
    private String lastName;
    private String employeeNumber;
    private String employeeType;
    private LocalDate birthDate;
    private String email;
    private String phone;
    private String photoPath;
    private Integer employeeDepartmentId;
    private Integer employeePositionId;
    private Integer managerEmployeeUserId;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private String username;
    private String passwordHash; // 接收明文密碼
    private Boolean isActive = true;
}