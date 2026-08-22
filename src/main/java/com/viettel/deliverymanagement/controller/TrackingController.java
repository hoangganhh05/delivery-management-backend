package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.dto.response.TrackingResponse;
import com.viettel.deliverymanagement.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tracking")
@RequiredArgsConstructor
@Tag(name = "Tracking Controller", description = "APIs tra cứu hành trình vận đơn công khai")
public class TrackingController {

    private final TrackingService trackingService;

    @GetMapping("/{trackingNumber}")
    @Operation(
            summary = "Tra cứu hành trình đơn hàng công khai",
            description = "Tra cứu thông tin chi tiết và toàn bộ lịch sử trạng thái của đơn hàng bằng mã vận đơn mà không cần đăng nhập"
    )
    public ResponseData<TrackingResponse> trackOrder(@PathVariable("trackingNumber") String trackingNumber) {
        TrackingResponse response = trackingService.trackOrder(trackingNumber);
        return ResponseData.success("Tra cứu hành trình đơn hàng thành công", response);
    }
}
