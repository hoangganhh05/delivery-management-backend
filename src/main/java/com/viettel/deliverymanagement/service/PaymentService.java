package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentService {

    PaymentResponse createVNPayPayment(Long orderId, HttpServletRequest req, String username);

    void processVNPayCallback(Map<String, String> queryParams);
}
