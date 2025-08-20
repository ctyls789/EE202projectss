package com.project.HR.controller;

import com.project.HR.service.LeaveRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import com.project.HR.model.CreateLeaveRecordRequest;
import com.project.HR.model.UpdateLeaveRecordRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping("/leave") // 為所有與請假相關的 URL 添加一個基礎路徑
@Tag(name = "請假紀錄網頁控制器", description = "處理請假紀錄相關網頁請求")
public class LeaveRecordController {

    private final LeaveRecordService leaveRecordService;

    // 建構子注入 (Constructor-based injection) 是推薦的最佳實踐
    @Autowired
    public LeaveRecordController(LeaveRecordService leaveRecordService) {
        this.leaveRecordService = leaveRecordService;
    }

    /**
     * 取代 LeaveRecordListServlet
     * 顯示所有請假紀錄的列表。
     */
    @Operation(summary = "顯示請假紀錄列表", description = "顯示所有請假紀錄的列表頁面")
    @GetMapping("/list")
    public String showLeaveList(Model model) {
        model.addAttribute("records", leaveRecordService.getAllLeaveRecordsAsDto());
        return "JSP/kh/leaveR"; // 返回 JSP 視圖的路徑
    }

    /**
     * 取代 LeaveRecordInsertServlet 的 doGet 方法
     * 顯示用於建立新請假紀錄的表單。
     */
    @Operation(summary = "顯示新增請假表單", description = "顯示用於建立新請假紀錄的表單頁面")
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("leaveTypes", leaveRecordService.getAllLeaveTypes());
        return "JSP/kh/leaveC";
    }

    /**
     * 取代 LeaveRecordInsertServlet 的 doPost 方法
     * 處理新增請假紀錄表單的提交。
     */
    @Operation(summary = "提交新增請假紀錄", description = "處理新增請假紀錄表單的提交")
    @PostMapping("/create")
    public String createLeaveRecord(
            @Parameter(description = "員工ID", required = true) @RequestParam Integer empid,
            @Parameter(description = "代理人ID", required = false) @RequestParam(name = "agentid", required = false) Integer agentId,
            @Parameter(description = "請假類型ID", required = true) @RequestParam(name = "leavetype") Integer leaveTypeId,
            @Parameter(description = "請假原因", required = true) @RequestParam String reason,
            @Parameter(description = "開始日期時間 (yy/MM/dd HH)", required = true) @RequestParam String startDatetime,
            @Parameter(description = "結束日期時間 (yy/MM/dd HH)", required = true) @RequestParam String endDatetime,
            @Parameter(description = "請假時數", required = true) @RequestParam BigDecimal hours,
            RedirectAttributes redirectAttributes) {
        try {
            CreateLeaveRecordRequest request = new CreateLeaveRecordRequest();
            request.setEmployeeId(empid);
            request.setAgentId(agentId);
            request.setLeaveTypeId(leaveTypeId);
            request.setReason(reason);
            // 將來自表單的字串手動解析為 LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd HH");
            request.setStartDatetime(LocalDateTime.parse(startDatetime, formatter));
            request.setEndDatetime(LocalDateTime.parse(endDatetime, formatter));
            request.setHours(hours);
            leaveRecordService.createLeaveRecord(request);
            redirectAttributes.addFlashAttribute("successMsg", "請假申請已成功提交！");
            return "redirect:/leave/list";
        } catch (DateTimeParseException | NumberFormatException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "資料格式錯誤，請檢查您輸入的日期或數字。");
            return "redirect:/leave/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "提交申請時發生錯誤: " + e.getMessage());
            return "redirect:/leave/create";
        }
    }

    /**
     * 取代 LeaveRecordDetailServlet 的 doGet 方法
     * 顯示特定請假紀錄的詳細資訊/更新表單。
     */
    @Operation(summary = "顯示請假紀錄詳細表單", description = "顯示特定請假紀錄的詳細資訊或更新表單頁面")
    @GetMapping("/detail/{uuid}")
    public String showDetailForm(@Parameter(description = "請假紀錄的唯一識別碼", required = true) @PathVariable String uuid, Model model, RedirectAttributes redirectAttributes) {
        return leaveRecordService.getByUuid(uuid)
                .map(record -> {
                    model.addAttribute("record", leaveRecordService.getLeaveRecordDtoByUuid(uuid).get());
                    return "JSP/kh/leaveDetailUD";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMsg", "找不到指定的請假紀錄 (UUID: " + uuid + ")");
                    return "redirect:/leave/list";
                });
    }

    /**
     * 取代 LeaveRecordDetailServlet 中 doPost 的 'update' 部分
     */
    @Operation(summary = "提交更新請假紀錄", description = "處理更新請假紀錄表單的提交")
    @PostMapping("/update")
    public String updateLeaveRecord(
            @Parameter(description = "請假紀錄的唯一識別碼", required = true) @RequestParam String uuid,
            @Parameter(description = "請假原因", required = true) @RequestParam String reason,
            @Parameter(description = "開始日期時間 (yy/MM/dd HH)", required = true) @RequestParam String startDatetime,
            @Parameter(description = "結束日期時間 (yy/MM/dd HH)", required = true) @RequestParam String endDatetime,
            @Parameter(description = "請假時數", required = true) @RequestParam BigDecimal hours,
            RedirectAttributes redirectAttributes) {
        try {
            UpdateLeaveRecordRequest request = new UpdateLeaveRecordRequest();
            request.setReason(reason);
            // 將來自表單的字串手動解析為 LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd HH");
            request.setStartDatetime(LocalDateTime.parse(startDatetime, formatter));
            request.setEndDatetime(LocalDateTime.parse(endDatetime, formatter));
            request.setHours(hours);
            leaveRecordService.updateLeaveRecord(uuid, request);

            redirectAttributes.addFlashAttribute("successMsg", "請假單 " + uuid.substring(0, 8) + "... 更新成功！");
            return "redirect:/leave/list";
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "資料格式錯誤，請檢查您輸入的日期。");
            return "redirect:/leave/detail/" + uuid;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "更新失敗: " + e.getMessage());
            return "redirect:/leave/detail/" + uuid;
        }
    }

    /**
     * 取代 LeaveRecordDetailServlet 中 doPost 的 'delete' 部分
     */
    @Operation(summary = "提交刪除請假紀錄", description = "處理刪除請假紀錄的提交")
    @PostMapping("/delete")
    public String deleteLeaveRecord(@Parameter(description = "要刪除的請假紀錄的唯一識別碼", required = true) @RequestParam String uuid, RedirectAttributes redirectAttributes) {
        try {
            leaveRecordService.deleteByUuid(uuid);
            redirectAttributes.addFlashAttribute("successMsg", "請假單 " + uuid.substring(0, 8) + "... 已成功刪除！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "刪除失敗: " + e.getMessage());
        }
        return "redirect:/leave/list";
    }
}