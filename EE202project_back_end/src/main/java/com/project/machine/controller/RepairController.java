package com.project.machine.controller;

import com.project.machine.Bean.MachineRepairBean;
import com.project.machine.Service.repair.MachineRepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/repair")
public class RepairController {

    @Autowired
    private MachineRepairService repairService;

    //新增報修
    //POST http://localhost:8080/api/repair
    //Body JSON 範例：
    /*
     {
       "machineId": 5,
       "employeeId": 102,
       "repairDescription": "機器突然停機無法啟動"
     }
    */
    @PostMapping
    public ResponseEntity<?> insertRepair(@RequestBody MachineRepairBean request) {
        try {
            if (request.getMachineId() <= 0 || request.getEmployeeId() <= 0
                    || request.getRepairDescription() == null || request.getRepairDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("欄位不可為空");
            }

            request.setRepairStatus("待處理");
            request.setRepairTime(LocalDateTime.now());

            repairService.insertRepair(request);
            return ResponseEntity.ok("報修成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("報修失敗：" + e.getMessage());
        }
    }

    //查詢所有維修記錄（管理員）
    //GET http://localhost:8080/api/repair/admin
    @GetMapping("/admin")
    public ResponseEntity<?> getAllRepairsForAdmin() {
        try {
            List<MachineRepairBean> list = repairService.getAllRepairsForAdmin();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("載入失敗：" + e.getMessage());
        }
    }

    //查詢所有維修記錄（使用者）
    // GET http://localhost:8080/api/repair
    @GetMapping
    public ResponseEntity<?> getAllRepairs() {
        try {
            List<MachineRepairBean> list = repairService.machineRepairView();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("載入失敗：" + e.getMessage());
        }
    }

    //查詢單筆詳細資料
    //GET http://localhost:8080/api/repair/3
    @GetMapping("/{id}")
    public ResponseEntity<?> getRepairDetail(@PathVariable int id) {
        try {
            MachineRepairBean repair = repairService.findRepairById(id);
            if (repair != null) {
                return ResponseEntity.ok(repair);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到報修資料");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("錯誤：" + e.getMessage());
        }
    }

    //查詢：依機台 ID
    //GET http://localhost:8080/api/repair/search/machine/5
    @GetMapping("/search/machine/{machineId}")
    public ResponseEntity<?> searchByMachine(@PathVariable int machineId) {
        try {
            List<MachineRepairBean> list = repairService.findRepairsByMachineId(machineId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("查詢失敗：" + e.getMessage());
        }
    }

    //查詢：依狀態
    // GET http://localhost:8080/api/repair/search/status?status=待處理
    @GetMapping("/search/status")
    public ResponseEntity<?> searchByStatus(@RequestParam String status) {
        try {
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("請輸入狀態");
            }
            List<MachineRepairBean> list = repairService.findRepairsByStatus(status);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("查詢失敗：" + e.getMessage());
        }
    }

    //更新維修狀態
    // PUT http://localhost:8080/api/repair/3/status?newStatus=已完成
    @Operation(summary = "更新維修記錄狀態", description = "根據維修記錄ID更新其狀態")
    @Parameter(name = "id", description = "維修記錄ID", required = true)
    @Parameter(name = "newStatus", description = "新的維修狀態 (例如: 待處理, 處理中, 已完成)", required = true)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateRepairStatus(@PathVariable int id,
                                                @RequestParam String newStatus) {
        try {
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("狀態不可為空");
            }

            repairService.updateRepairStatus(id, newStatus);
            return ResponseEntity.ok("狀態更新成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("更新失敗：" + e.getMessage());
        }
    }
}
