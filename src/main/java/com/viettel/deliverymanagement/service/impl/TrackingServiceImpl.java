package com.viettel.deliverymanagement.service.impl;

import com.viettel.deliverymanagement.dto.response.ShipmentHistoryDto;
import com.viettel.deliverymanagement.dto.response.TrackingResponse;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.entity.ShipmentEntity;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.repository.ShipmentRepository;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingServiceImpl implements TrackingService {

    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public TrackingResponse trackOrder(String trackingNumber, boolean includePrivateDetails) {
        log.info("Tra cứu hành trình đơn hàng với mã vận đơn: {}", trackingNumber);

        OrderEntity order = orderRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với mã vận đơn: " + trackingNumber));

        if (order.isAwaitingOnlinePayment()) {
            throw new AppException("ORDER_NOT_FOUND", "Mã vận đơn chỉ có thể tra cứu sau khi thanh toán được xác nhận");
        }

        List<ShipmentEntity> shipments = shipmentRepository.findByOrderIdOrderByIdDesc(order.getId());
        UserEntity shipper = shipmentRepository
                .findFirstByOrderIdAndShipperIdIsNotNullOrderByIdDesc(order.getId())
                .flatMap(shipment -> userRepository.findById(shipment.getShipperId()))
                .orElse(null);
        String shipperName = shipper == null
                ? null
                : (shipper.getFullName() != null ? shipper.getFullName() : shipper.getUsername());
        List<ShipmentHistoryDto> historyList = shipments.stream()
                .map(shipment -> ShipmentHistoryDto.builder()
                        .status(shipment.getStatus())
                        .note(includePrivateDetails ? shipment.getNote() : shipment.getStatus().getDescription())
                        .proofImageUrl(includePrivateDetails ? shipment.getProofImageUrl() : null)
                        .timestamp(shipment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        log.info("Tra cứu thành công đơn hàng {}: {} mốc lịch sử", trackingNumber, historyList.size());

        return TrackingResponse.builder()
                .trackingNumber(order.getTrackingNumber())
                .orderId(includePrivateDetails ? order.getId() : null)
                .senderName(includePrivateDetails ? order.getSenderName() : maskName(order.getSenderName()))
                .receiverName(includePrivateDetails ? order.getReceiverName() : maskName(order.getReceiverName()))
                .receiverAddress(includePrivateDetails ? order.getReceiverAddress() : maskAddress(order.getReceiverAddress()))
                .shipperName(includePrivateDetails ? shipperName : maskName(shipperName))
                .shipperPhone(includePrivateDetails && shipper != null ? shipper.getPhoneNumber() : null)
                .currentStatus(order.getStatus())
                .shippingFee(includePrivateDetails ? order.getShippingFee() : null)
                .codAmount(includePrivateDetails ? order.getCodAmount() : null)
                .totalFee(includePrivateDetails ? order.getTotalFee() : null)
                .history(historyList)
                .build();
    }

    private String maskName(String value) {
        if (value == null || value.isBlank()) return null;
        return java.util.Arrays.stream(value.trim().split("\\s+"))
                .map(part -> part.length() == 1 ? part + "***" : part.substring(0, 1) + "***")
                .collect(Collectors.joining(" "));
    }

    private String maskAddress(String value) {
        if (value == null || value.isBlank()) return null;
        String[] parts = value.split(",");
        if (parts.length < 2) return "Địa chỉ đã được ẩn";
        int visibleStart = Math.max(1, parts.length - 2);
        String visibleArea = java.util.Arrays.stream(parts, visibleStart, parts.length)
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(", "));
        return visibleArea.isEmpty() ? "Địa chỉ đã được ẩn" : "•••, " + visibleArea;
    }
}
