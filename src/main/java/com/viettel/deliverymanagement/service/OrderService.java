package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.request.CreateOrderRequest;
import com.viettel.deliverymanagement.dto.request.OrderSearchRequest;
import com.viettel.deliverymanagement.dto.response.OrderResponse;
import com.viettel.deliverymanagement.dto.response.PageResponse;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderByTrackingNumber(String trackingNumber);

    PageResponse<OrderResponse> searchOrders(OrderSearchRequest request);

    OrderResponse cancelOrder(String trackingNumber);
}
