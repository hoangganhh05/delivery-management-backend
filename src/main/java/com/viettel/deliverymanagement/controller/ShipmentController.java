package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.request.AssignShipperRequest;
import com.viettel.deliverymanagement.dto.request.UpdateShipmentStatusRequest;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Shipment Management", description = "APIs quản lý giao hàng và phân công shipper")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping("/shipments/assign")
    @Operation(summary = "Phân công shipper cho đơn hàng", description = "Gán shipper cho đơn hàng ở trạng thái CREATED và chuyển trạng thái sang ASSIGNED")
    public ResponseData<Void> assignShipper(@Valid @RequestBody AssignShipperRequest request) {
        shipmentService.assignShipper(request);
        return ResponseData.success("Phân công shipper thành công", null);
    }

    @PutMapping("/shipments/orders/{orderId}/status")
    @Operation(summary = "Cập nhật trạng thái giao hàng", description = "Cập nhật trạng thái mới cho đơn hàng và ghi nhận lịch sử shipment")
    public ResponseData<Void> updateShipmentStatus(
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody UpdateShipmentStatusRequest request,
            Authentication authentication) {
        shipmentService.updateShipmentStatus(orderId, request, authentication.getName());
        return ResponseData.success("Cập nhật trạng thái giao hàng thành công", null);
    }
}
