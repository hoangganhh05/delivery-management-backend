package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.response.DashboardResponse;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Dashboard Controller", description = "APIs thống kê và báo cáo Dashboard Analytics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard/stats")
    @Operation(
            summary = "Lấy dữ liệu thống kê Dashboard",
            description = "Thống kê tổng số đơn hàng, số đơn giao thành công, số đơn bị hủy và tổng doanh thu từ các đơn thành công"
    )
    public ResponseData<DashboardResponse> getDashboardStats() {
        DashboardResponse stats = dashboardService.getDashboardStats();
        return ResponseData.success("Lấy thông tin thống kê dashboard thành công", stats);
    }
}
