package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.response.OperationsReportResponse;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/operations")
    @PreAuthorize("hasRole('ADMIN') or @permissionService.has(authentication, 'VIEW_REPORTS')")
    public ResponseData<OperationsReportResponse> operations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseData.success("Lấy báo cáo vận hành thành công", reportService.operations(from, to));
    }

    @GetMapping(value = "/orders.csv", produces = "text/csv")
    @PreAuthorize("hasRole('ADMIN') or @permissionService.has(authentication, 'EXPORT_DATA')")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders-report.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(reportService.ordersCsv(from, to));
    }
}
