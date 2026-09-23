package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.response.PaymentResponse;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.dto.response.PaymentRecordResponse;
import com.viettel.deliverymanagement.dto.response.QrPaymentResponse;
import com.viettel.deliverymanagement.dto.request.ConfirmPaymentRequest;
import com.viettel.deliverymanagement.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "APIs thanh toán trực tuyến qua cổng VNPay")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @permissionService.has(authentication, 'VIEW_PAYMENTS')")
    public ResponseData<List<PaymentRecordResponse>> getPayments() {
        return ResponseData.success("Lấy danh sách thanh toán thành công", paymentService.getPayments());
    }

    @GetMapping("/orders/{orderId}")
    public ResponseData<PaymentRecordResponse> getPayment(@PathVariable Long orderId, Authentication authentication) {
        return ResponseData.success("Lấy thanh toán thành công", paymentService.getPayment(orderId, authentication.getName()));
    }

    @GetMapping("/orders/{orderId}/qr")
    public ResponseData<QrPaymentResponse> getQr(@PathVariable Long orderId, Authentication authentication) {
        return ResponseData.success("Lấy thông tin chuyển khoản thành công", paymentService.getQrPayment(orderId, authentication.getName()));
    }

    @PutMapping("/orders/{orderId}/confirm")
    @PreAuthorize("@permissionService.has(authentication, 'MANAGE_PAYMENTS')")
    public ResponseData<PaymentRecordResponse> confirm(@PathVariable Long orderId,
            @Valid @RequestBody ConfirmPaymentRequest request) {
        return ResponseData.success("Xác nhận thanh toán thành công",
                paymentService.confirmPayment(orderId, request.getReference()));
    }

    @GetMapping("/vnpay/{orderId}")
    @Operation(
            summary = "Tạo URL thanh toán VNPay",
            description = "Tạo đường dẫn chuyển hướng sang cổng thanh toán Sandbox của VNPay dựa trên ID đơn hàng"
    )
    public ResponseData<PaymentResponse> createVNPayPayment(
            @PathVariable("orderId") Long orderId,
            HttpServletRequest request,
            Authentication authentication) {
        PaymentResponse response = paymentService.createVNPayPayment(orderId, request, authentication.getName());
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
