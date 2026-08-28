package com.viettel.deliverymanagement.service.impl;

import com.viettel.deliverymanagement.config.VNPayConfig;
import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.dto.response.PaymentResponse;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.entity.ShipmentEntity;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.repository.ShipmentRepository;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final VNPayConfig vnPayConfig;

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse createVNPayPayment(Long orderId, HttpServletRequest req, String username) {
        log.info("Khởi tạo thanh toán VNPay cho đơn hàng ID: {}", orderId);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + orderId));

        UserEntity currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng"));
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(order.getSenderId())) {
            throw new AppException("PAYMENT_ACCESS_DENIED", "Bạn không có quyền thanh toán đơn hàng này");
        }
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PENDING) {
            throw new AppException("INVALID_PAYMENT_STATUS", "Đơn hàng không ở trạng thái có thể thanh toán");
        }

        long amount = order.getTotalFee().multiply(BigDecimal.valueOf(100)).longValue();

        String vnp_TxnRef = order.getId() + "_" + VNPayConfig.getRandomNumber(6);
        String vnp_IpAddr = VNPayConfig.getIpAddress(req);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNPayConfig.VNP_VERSION);
        vnp_Params.put("vnp_Command", VNPayConfig.VNP_COMMAND);
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", VNPayConfig.VNP_CURR_CODE);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + order.getTrackingNumber());
        vnp_Params.put("vnp_OrderType", VNPayConfig.VNP_ORDER_TYPE);
        vnp_Params.put("vnp_Locale", VNPayConfig.VNP_LOCALE);
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(vnPayConfig.getVnpHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPayConfig.VNP_PAY_URL + "?" + queryUrl;

        log.info("Tạo URL thanh toán VNPay thành công cho đơn hàng ID {}", order.getId());

        return PaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .message("Tạo URL thanh toán VNPay thành công")
                .status("SUCCESS")
                .build();
    }

    @Override
    @Transactional
    public void processVNPayCallback(Map<String, String> queryParams) {
        log.info("Nhận callback kết quả giao dịch từ VNPay");

        String vnp_SecureHash = queryParams.get("vnp_SecureHash");
        Map<String, String> fields = new HashMap<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (entry.getKey().startsWith("vnp_") && !entry.getKey().equals("vnp_SecureHash") && !entry.getKey().equals("vnp_SecureHashType")) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }

        String signValue = VNPayConfig.hashAllFields(fields, vnPayConfig.getVnpHashSecret());
        if (!signValue.equalsIgnoreCase(vnp_SecureHash)) {
            log.error("Chữ ký VNPay không khớp: expected={}, actual={}", signValue, vnp_SecureHash);
            throw new AppException("INVALID_CHECKSUM", "Chữ ký bảo mật giao dịch VNPay không hợp lệ");
        }

        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String vnp_TxnRef = queryParams.get("vnp_TxnRef");
        String vnp_TransactionNo = queryParams.get("vnp_TransactionNo");

        Long orderId = Long.parseLong(vnp_TxnRef.split("_")[0]);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + orderId));

        if ("00".equals(vnp_ResponseCode)) {
            if (order.getStatus() == OrderStatus.PAID) {
                log.info("Bỏ qua callback VNPay lặp lại cho đơn hàng ID: {}", orderId);
                return;
            }
            if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PENDING) {
                throw new AppException("INVALID_PAYMENT_STATUS", "Trạng thái đơn hàng không còn phù hợp để xác nhận thanh toán");
            }
            log.info("Giao dịch VNPay thành công cho đơn hàng ID: {}, TransactionNo: {}", orderId, vnp_TransactionNo);
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            // Lưu log lịch sử shipment theo dõi vết
            ShipmentEntity shipment = ShipmentEntity.builder()
                    .orderId(order.getId())
                    .status(OrderStatus.PAID)
                    .note("Đã thanh toán trực tuyến thành công qua VNPay (Mã giao dịch: " + vnp_TransactionNo + ")")
                    .build();
            shipmentRepository.save(shipment);
        } else {
            log.warn("Giao dịch VNPay thất bại cho đơn hàng ID: {}, ResponseCode: {}", orderId, vnp_ResponseCode);
            throw new AppException("PAYMENT_FAILED", "Thanh toán VNPay không thành công với mã phản hồi: " + vnp_ResponseCode);
        }
    }
}
