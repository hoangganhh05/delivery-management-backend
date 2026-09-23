package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.response.PaymentResponse;
import com.viettel.deliverymanagement.dto.response.PaymentRecordResponse;
import com.viettel.deliverymanagement.dto.response.QrPaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.List;

public interface PaymentService {

    PaymentResponse createVNPayPayment(Long orderId, HttpServletRequest req, String username);

    void processVNPayCallback(Map<String, String> queryParams);

    List<PaymentRecordResponse> getPayments();
    PaymentRecordResponse getPayment(Long orderId, String username);
    QrPaymentResponse getQrPayment(Long orderId, String username);
    PaymentRecordResponse confirmPayment(Long orderId, String reference);
}
