package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.request.CreateOrderRequest;
import com.viettel.deliverymanagement.dto.request.OrderSearchRequest;
import com.viettel.deliverymanagement.dto.response.OrderResponse;
import com.viettel.deliverymanagement.dto.response.PageResponse;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.service.OrderService;
import com.viettel.deliverymanagement.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ShipmentService shipmentService;

    @PostMapping
    @PreAuthorize("@permissionService.has(authentication, 'CREATE_ORDER')")
    public ResponseData<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request, Authentication authentication) {
        OrderResponse response = orderService.createOrder(request, authentication.getName());
        return ResponseData.success("Tạo đơn hàng thành công", response);
    }

    @GetMapping
    @PreAuthorize("@permissionService.has(authentication, 'VIEW_ORDERS')")
    public ResponseData<PageResponse<OrderResponse>> searchOrders(@Valid @ModelAttribute OrderSearchRequest request, Authentication authentication) {
        return ResponseData.success("Lấy danh sách đơn hàng thành công", orderService.searchOrders(request, authentication.getName()));
    }

    @GetMapping("/{trackingNumber}")
    @PreAuthorize("@permissionService.has(authentication, 'VIEW_ORDERS')")
    public ResponseData<OrderResponse> getOrderByTrackingNumber(@PathVariable String trackingNumber, Authentication authentication) {
        return ResponseData.success(
                "Lấy thông tin đơn hàng thành công",
                orderService.getOrderByTrackingNumber(trackingNumber, authentication.getName())
        );
    }

    @GetMapping("/{trackingNumber}/location")
    @PreAuthorize("@permissionService.has(authentication, 'VIEW_ORDERS')")
    public ResponseData<com.viettel.deliverymanagement.dto.response.DriverLocationResponse> getLiveDriverLocation(
            @PathVariable String trackingNumber,
            Authentication authentication) {
        OrderResponse order = orderService.getOrderByTrackingNumber(trackingNumber, authentication.getName());
        return ResponseData.success("Lấy vị trí tài xế thành công", shipmentService.getLatestLocation(order.getId()));
    }

    @PutMapping("/{trackingNumber}/cancel")
    @PreAuthorize("@permissionService.has(authentication, 'CANCEL_ORDER')")
    public ResponseData<OrderResponse> cancelOrder(@PathVariable String trackingNumber, Authentication authentication) {
        return ResponseData.success("Hủy đơn hàng thành công", orderService.cancelOrder(trackingNumber, authentication.getName()));
    }
}
