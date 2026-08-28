package com.viettel.deliverymanagement.service.impl;

import com.viettel.deliverymanagement.dto.response.ShipmentHistoryDto;
import com.viettel.deliverymanagement.dto.response.TrackingResponse;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.entity.ShipmentEntity;
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
    public TrackingResponse trackOrder(String trackingNumber) {
        log.info("Tra cứu hành trình đơn hàng với mã vận đơn: {}", trackingNumber);

        OrderEntity order = orderRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với mã vận đơn: " + trackingNumber));

        List<ShipmentEntity> shipments = shipmentRepository.findByOrderIdOrderByIdDesc(order.getId());
        String shipperName = shipmentRepository
                .findFirstByOrderIdAndShipperIdIsNotNullOrderByIdDesc(order.getId())
                .flatMap(shipment -> userRepository.findById(shipment.getShipperId()))
                .map(user -> user.getFullName() != null ? user.getFullName() : user.getUsername())
                .orElse(null);

        List<ShipmentHistoryDto> historyList = shipments.stream()
                .map(shipment -> ShipmentHistoryDto.builder()
                        .status(shipment.getStatus())
                        .note(shipment.getNote())
                        .proofImageUrl(shipment.getProofImageUrl())
                        .timestamp(shipment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        log.info("Tra cứu thành công đơn hàng {}: {} mốc lịch sử", trackingNumber, historyList.size());

        return TrackingResponse.builder()
                .trackingNumber(order.getTrackingNumber())
                .orderId(order.getId())
                .senderName(order.getSenderName())
                .receiverName(order.getReceiverName())
                .shipperName(shipperName)
                .currentStatus(order.getStatus())
                .shippingFee(order.getShippingFee())
                .codAmount(order.getCodAmount())
                .totalFee(order.getTotalFee())
                .history(historyList)
                .build();
    }
}
