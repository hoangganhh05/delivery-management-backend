package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.request.CreateOrderRequest;
import com.viettel.deliverymanagement.dto.request.OrderSearchRequest;
import com.viettel.deliverymanagement.dto.response.OrderResponse;
import com.viettel.deliverymanagement.dto.response.PageResponse;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseData<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseData.success("Tạo đơn hàng thành công", response);
    }

    @GetMapping
    public ResponseData<PageResponse<OrderResponse>> searchOrders(@ModelAttribute OrderSearchRequest request) {
        return ResponseData.success("Lấy danh sách đơn hàng thành công", orderService.searchOrders(request));
    }

    @GetMapping("/{trackingNumber}")
    public ResponseData<OrderResponse> getOrderByTrackingNumber(@PathVariable String trackingNumber) {
        return ResponseData.success(
                "Lấy thông tin đơn hàng thành công",
                orderService.getOrderByTrackingNumber(trackingNumber)
        );
    }

    @PutMapping("/{trackingNumber}/cancel")
    public ResponseData<OrderResponse> cancelOrder(@PathVariable String trackingNumber) {
        return ResponseData.success("Hủy đơn hàng thành công", orderService.cancelOrder(trackingNumber));
    }
}
