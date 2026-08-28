package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.request.CreateOrderRequest;
import com.viettel.deliverymanagement.dto.request.OrderSearchRequest;
import com.viettel.deliverymanagement.dto.response.OrderResponse;
import com.viettel.deliverymanagement.dto.response.PageResponse;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request, String username);

    OrderResponse getOrderByTrackingNumber(String trackingNumber, String username);

    PageResponse<OrderResponse> searchOrders(OrderSearchRequest request, String username);

    OrderResponse cancelOrder(String trackingNumber, String username);

    List<OrderResponse> getOrdersForShipper(Long shipperId);
}
