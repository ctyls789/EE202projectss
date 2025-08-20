package com.project.HR.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.HR.model.CreateLeaveRecordRequest;
import com.project.HR.model.LeaveRecordDto;
import com.project.HR.model.UpdateLeaveRecordRequest;
import com.project.HR.service.LeaveRecordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "請假紀錄管理", description = "處理員工請假紀錄的相關操作")
@RequestMapping("/api/leave") // 使用一個專用的路徑前綴 (如 /api) 來區分 API 和網頁請求
public class LeaveRecordApiController {

    private final LeaveRecordService leaveRecordService;

    @Autowired
    public LeaveRecordApiController(LeaveRecordService leaveRecordService) {
        this.leaveRecordService = leaveRecordService;
    }
    
    /**
     * 為「新增請假」表單提供初始資料 (如：假別列表)
     * 
     * @return 包含表單所需資料的 Map 物件
     */
    @Operation(summary = "獲取請假表單所需資料", description = "提供建立請假表單所需的初始資料，例如假別列表")
    @GetMapping("/form-data")
    public ResponseEntity<Map<String, Object>> getLeaveFormData() {
        Map<String, Object> formData = new HashMap<>();
        formData.put("leaveTypes", leaveRecordService.getAllLeaveTypes());
        return ResponseEntity.ok(formData);
    }

    /**
     * 獲取所有請假紀錄
     * 
     * @return 包含所有請假紀錄的 JSON 陣列
     */
    @Operation(summary = "獲取所有請假紀錄", description = "返回系統中所有請假紀錄的列表")
    @GetMapping("/records")
    public ResponseEntity<List<LeaveRecordDto>> getAllLeaveRecords() {
        List<LeaveRecordDto> records = leaveRecordService.getAllLeaveRecordsAsDto();
        return ResponseEntity.ok(records);
    }

    /**
     * 根據 UUID 獲取單筆請假紀錄
     * 
     * @param uuid 請假紀錄的唯一識別碼
     * @return 如果找到，返回該筆請假紀錄和 200 OK；否則返回 404 Not Found
     */
    @Operation(summary = "根據UUID獲取單筆請假紀錄", description = "根據請假紀錄的唯一識別碼(UUID)查詢詳細資訊")
    @Parameter(name = "uuid", description = "請假紀錄的唯一識別碼", required = true)
    @GetMapping("/records/{uuid}")
    public ResponseEntity<LeaveRecordDto> getLeaveRecordByUuid(@PathVariable String uuid) {
        return leaveRecordService.getLeaveRecordDtoByUuid(uuid)
                .map(ResponseEntity::ok) // 如果 Optional 存在，則用其值建立一個 200 OK 的 ResponseEntity
                .orElse(ResponseEntity.notFound().build()); // 如果 Optional 為空，則建立一個 404 Not Found
    }

    /**
     * 新增一筆請假紀錄
     * 
     * @param request 從請求的 JSON body 映射過來的物件
     * @return 新建立的請假紀錄和 201 Created 狀態碼
     */
    @Operation(summary = "新增請假紀錄", description = "新增一筆新的員工請假紀錄")
    @Parameter(name = "request", description = "請假紀錄的建立請求物件", required = true)
    @PostMapping("/records")
    public ResponseEntity<LeaveRecordDto> createLeaveRecord(@RequestBody CreateLeaveRecordRequest request) {
        LeaveRecordDto savedRecord = leaveRecordService.createLeaveRecord(request);
        // 回傳 201 Created 狀態碼和新建立的資源
        return new ResponseEntity<>(savedRecord, HttpStatus.CREATED);
    }

    /**
     * 根據 UUID 更新一筆請假紀錄
     * 
     * @param uuid    要更新的請假紀錄的 UUID
     * @param request 包含更新資料的請求物件
     * @return 如果成功，返回更新後的紀錄和 200 OK；如果找不到紀錄，返回 404 Not Found
     */
    @Operation(summary = "更新請假紀錄", description = "根據UUID更新指定的請假紀錄")
    @Parameter(name = "uuid", description = "要更新的請假紀錄的唯一識別碼", required = true)
    @Parameter(name = "request", description = "包含更新資料的請求物件", required = true)
    @PutMapping("/records/{uuid}")
    public ResponseEntity<LeaveRecordDto> updateLeaveRecord(@PathVariable String uuid,
            @RequestBody UpdateLeaveRecordRequest request) {
        try {
            return leaveRecordService.updateLeaveRecord(uuid, request)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根據 UUID 刪除一筆請假紀錄
     * 
     * @param uuid 要刪除的請假紀錄的 UUID
     * @return 如果成功，返回 204 No Content；如果找不到紀錄，返回 404 Not Found
     */
    @Operation(summary = "刪除請假紀錄", description = "根據UUID刪除指定的請假紀錄")
    @Parameter(name = "uuid", description = "要刪除的請假紀錄的唯一識別碼", required = true)
    @DeleteMapping("/records/{uuid}")
    public ResponseEntity<Void> deleteLeaveRecord(@PathVariable String uuid) {
        // 先檢查紀錄是否存在，以便回傳正確的 404 狀態碼
        if (leaveRecordService.getByUuid(uuid).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        leaveRecordService.deleteByUuid(uuid);
        // 刪除成功，回傳 204 No Content，表示伺服器成功處理請求，但沒有內容可以返回
        return ResponseEntity.noContent().build();
    }
}