package com.project.core.controller;

import com.project.core.dto.StatusCodeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/status-codes")
public class StatusCodeController {

    @GetMapping("/machine")
    public ResponseEntity<List<StatusCodeDto>> getMachineStatusCodes() {
        List<StatusCodeDto> statuses = new ArrayList<>();
        statuses.add(new StatusCodeDto("RUN", "運行中"));
        statuses.add(new StatusCodeDto("STOP", "停止"));
        statuses.add(new StatusCodeDto("REPAIR", "維修中"));
        return ResponseEntity.ok(statuses);
    }
}
