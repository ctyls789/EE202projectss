package com.project.employeeuser.controller;

import com.project.employeeuser.model.EmployeeUser;
import com.project.employeeuser.service.EmployeeUserService;
// ===== 新增：角色管理功能開始 =====
import com.project.core.dto.response.RoleDto;
import com.project.core.dto.request.UpdateEmployeeRolesRequest;
// ===== 新增：角色管理功能結束 =====
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/employee-users")
@Tag(name = "員工使用者管理", description = "處理員工使用者帳戶的相關操作")
public class EmployeeUserController {

    @Autowired
    private EmployeeUserService employeeUserService;
    
    // ===== 新增：角色管理功能開始 =====
    @Autowired
    private com.project.core.service.EmployeeUserService coreEmployeeUserService;
    // ===== 新增：角色管理功能結束 =====

    @Operation(summary = "獲取所有員工使用者", description = "返回所有員工使用者帳戶的列表")
    @GetMapping
    public ResponseEntity<List<EmployeeUser>> getAllEmployeeUsers() {
        List<EmployeeUser> employeeUsers = employeeUserService.getAllEmployeeUsers();
        return ResponseEntity.ok(employeeUsers);
    }

    @Operation(summary = "根據ID獲取員工使用者", description = "根據員工使用者ID查詢單一帳戶的詳細資訊")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeUser> getEmployeeUserById(@Parameter(description = "員工使用者ID", required = true) @PathVariable Long id) {
        return employeeUserService.getEmployeeUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "建立員工使用者", description = "建立一個新的員工使用者帳戶")
    @PostMapping
    public ResponseEntity<?> createEmployeeUser(@Parameter(description = "員工使用者物件", required = true) @RequestBody EmployeeUser employeeUser) {
        if (employeeUserService.existsByUsername(employeeUser.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists.");
        }
        if (employeeUser.getEmployeeNumber() != null && employeeUserService.existsByEmployeeNumber(employeeUser.getEmployeeNumber())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Employee number already exists.");
        }
        if (employeeUser.getEmail() != null && employeeUserService.existsByEmail(employeeUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists.");
        }
        EmployeeUser createdUser = employeeUserService.createEmployeeUser(employeeUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @Operation(summary = "更新員工使用者", description = "根據員工使用者ID更新帳戶資訊")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeUser> updateEmployeeUser(@Parameter(description = "員工使用者ID", required = true) @PathVariable Long id, @Parameter(description = "包含更新資料的員工使用者物件", required = true) @RequestBody EmployeeUser employeeUserDetails) {
        try {
            EmployeeUser updatedUser = employeeUserService.updateEmployeeUser(id, employeeUserDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "刪除員工使用者", description = "根據員工使用者ID刪除指定的帳戶")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployeeUser(@Parameter(description = "要刪除的員工使用者ID", required = true) @PathVariable Long id) {
        try {
            employeeUserService.deleteEmployeeUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // ===== 新增：角色管理功能開始 =====
    @Operation(summary = "獲取員工角色", description = "根據員工使用者ID獲取其擁有的所有角色名稱")
    @GetMapping("/{id}/roles")
    public ResponseEntity<List<String>> getEmployeeRoles(@Parameter(description = "員工使用者ID", required = true) @PathVariable Long id) {
        try {
            List<String> roles = coreEmployeeUserService.getEmployeeRoles(id);
            return ResponseEntity.ok(roles);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "更新員工角色", description = "根據員工使用者ID更新其角色配置")
    @PutMapping("/{id}/roles")
    public ResponseEntity<?> updateEmployeeRoles(
            @Parameter(description = "員工使用者ID", required = true) @PathVariable Long id,
            @Parameter(description = "角色更新請求", required = true) @RequestBody UpdateEmployeeRolesRequest request) {
        try {
            coreEmployeeUserService.updateEmployeeRoles(id, request.getRoleNames(), request.getUpdatedBy());
            return ResponseEntity.ok().body("員工角色更新成功");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("找不到員工")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("找不到角色")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新員工角色失敗: " + e.getMessage());
            }
        }
    }
    
    @Operation(summary = "獲取可用角色", description = "獲取系統中所有可用的角色列表")
    @GetMapping("/available-roles")
    public ResponseEntity<List<RoleDto>> getAvailableRoles() {
        try {
            List<RoleDto> roles = coreEmployeeUserService.getAvailableRoles();
            return ResponseEntity.ok(roles);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // ===== 新增：角色管理功能結束 =====
}