package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.response.PaymentResponse;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "APIs thanh toán trực tuyến qua cổng VNPay")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/vnpay/{orderId}")
    @Operation(
            summary = "Tạo URL thanh toán VNPay",
            description = "Tạo đường dẫn chuyển hướng sang cổng thanh toán Sandbox của VNPay dựa trên ID đơn hàng"
    )
    public ResponseData<PaymentResponse> createVNPayPayment(
            @PathVariable("orderId") Long orderId,
            HttpServletRequest request) {
        PaymentResponse response = paymentService.createVNPayPayment(orderId, request);
        return ResponseData.success("Tạo URL thanh toán VNPay thành công", response);
    }

    @GetMapping("/vnpay-callback")
    @Operation(
            summary = "Xử lý callback từ VNPay",
            description = "Tiếp nhận và xác thực chữ ký kết quả thanh toán từ VNPay, tự động cập nhật trạng thái đơn hàng"
    )
    public ResponseData<Void> processVNPayCallback(@RequestParam Map<String, String> queryParams) {
        paymentService.processVNPayCallback(queryParams);
        return ResponseData.success("Xử lý kết quả thanh toán VNPay thành công", null);
    }
}
