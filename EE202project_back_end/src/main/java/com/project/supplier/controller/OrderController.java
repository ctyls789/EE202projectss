package com.project.supplier.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.stream.Collectors;
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
import com.project.supplier.model.PurchaseOrder;
import com.project.supplier.model.OrderInsertDTO;
import com.project.supplier.model.OrderUpdateDTO;
import com.project.supplier.service.MaterialService;
import com.project.supplier.service.OrderService;
import com.project.supplier.service.SupplierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 訂單模組 API 控制器
 * 提供訂單相關的 RESTful 介面，包含查詢、新增、更新、刪除等操作。
 */
@RestController //= @Controller + @ResponseBody  用於回傳JSON給前端
@RequestMapping("/api/order")
@Tag(name = "訂單管理", description = "處理採購訂單的相關操作")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private MaterialService materialService;

    /**
     * 顯示新增訂單的表單，提供供應商和物料列表。
     * @return 包含供應商和物料列表的 Map
     */
    @Operation(summary = "顯示新增訂單表單", description = "獲取新增訂單所需的供應商和物料列表")
    @GetMapping("/addForm")
    public Map<String,Object> showAddOrderForm() {
        Map<String, Object> data = new HashMap<>();
        data.put("suppliers", supplierService.getActiveSuppliers());
        data.put("materials", materialService.getActiveMaterials()); 
        return data; 
    }

    
    /**
     * 新增訂單。
     * @param dto 包含訂單資訊的 DTO
     * @return 成功或失敗的訊息
     */
    @Operation(summary = "新增訂單", description = "新增一筆新的採購訂單")
    @PostMapping("/insert")
    public ResponseEntity<String> insertOrder(@Parameter(description = "訂單插入請求物件", required = true) @RequestBody OrderInsertDTO dto) {
    try {
        orderService.insertOrder(
            dto.getSupplierId(),
            dto.getOrderDate(),
            dto.getOrderStatus(),
            BigDecimal.ZERO,
            dto.getMaterialIds(),
            dto.getQuantities().stream().map(BigDecimal::new).collect(Collectors.toList()),
            dto.getUnitPrices()
        );
        return ResponseEntity.ok("success");
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("error");
    }
}


    /**
     * 顯示所有訂單列表。
     * @return 所有訂單的列表
     */
    @Operation(summary = "查詢所有訂單", description = "獲取所有採購訂單的列表，包含訂單項目")
    @GetMapping("/list")
    public List<PurchaseOrder> listOrders() {
        return orderService.getAllOrdersWithItems();
    }

    /**
     * 編輯訂單，提供訂單詳細資訊、供應商和物料列表。
     * @param id 訂單ID
     * @return 包含訂單詳細資訊的 Map
     */
    @Operation(summary = "編輯訂單", description = "根據訂單ID獲取訂單詳細資訊，並提供供應商和物料列表用於編輯")
    @GetMapping("/edit/{id}")
    public ResponseEntity<Map<String, Object>> editOrder(@Parameter(description = "訂單ID", required = true) @PathVariable int id) {
        try {
            PurchaseOrder order = orderService.getOrderById(id);
            Map<String, Object> data = new HashMap<>();
            data.put("order", order);
            data.put("items", order.getItemList());
            data.put("supplier",order.getSupplier());
            data.put("suppliers", supplierService.getActiveSuppliers());
            data.put("materials", materialService.getActiveMaterials());
            
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); 
        }
    }

    /**
     * 更新訂單。
     * @param dto 包含更新資訊的 DTO
     * @return 成功或失敗的訊息
     */
    @Operation(summary = "更新訂單", description = "根據訂單ID更新採購訂單的資訊")
    @PutMapping("/update")
    public ResponseEntity<String> updateOrder(@Parameter(description = "訂單更新請求物件", required = true) @RequestBody OrderUpdateDTO dto){
        try {
            orderService.updateOrder(
            dto.getOrderId(),
            dto.getSupplierId(),
            dto.getOrderDate(),
            dto.getOrderStatus(),
            BigDecimal.ZERO,
            dto.getMaterialIds(),
            dto.getQuantities().stream().map(BigDecimal::new).collect(Collectors.toList()),
            dto.getUnitPrices()
        );
            return ResponseEntity.ok("更新成功"); 
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("更新失敗"); 
        }
    }

    /**
     * 刪除訂單。
     * @param id 訂單ID
     * @return 成功或失敗的訊息
     */
    @Operation(summary = "刪除訂單", description = "根據訂單ID刪除指定的採購訂單")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteOrder(@Parameter(description = "要刪除的訂單ID", required = true) @PathVariable int id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok("更新成功"); 
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); 
        }
    }
    
    /**
     * 取得每月採購金額統計表。
     * @return 每月採購金額的列表
     */
    @Operation(summary = "獲取每月採購金額統計", description = "獲取每月採購訂單的總金額統計列表")
    @GetMapping("/amount-per-month")
    public List<Map<String,Object>> getMonthlyPurchaseTotal(){
        return orderService.getMonthlyPurchaseTotal();
    }
    
    /**
     * 取得各供應商採購佔比統計表。
     * @return 各供應商採購佔比的列表
     */
    @Operation(summary = "獲取各供應商採購佔比統計", description = "獲取各供應商採購金額佔總採購金額的比例統計列表")
    @GetMapping("/supplier-ratio")
    public List<Map<String, Object>> getSupplierPurchaseTotal(){
        return orderService.getSupplierPurchaseTotal();
    }

}