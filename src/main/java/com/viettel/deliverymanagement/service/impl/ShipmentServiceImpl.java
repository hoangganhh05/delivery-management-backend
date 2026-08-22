package com.viettel.deliverymanagement.service.impl;

import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.dto.request.AssignShipperRequest;
import com.viettel.deliverymanagement.dto.request.UpdateShipmentStatusRequest;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.entity.ShipmentEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.repository.ShipmentRepository;
import com.viettel.deliverymanagement.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;

    @Override
    @Transactional
    public void assignShipper(AssignShipperRequest request) {
        log.info("Bắt đầu phân công shipper ID {} cho đơn hàng ID {}", request.getShipperId(), request.getOrderId());

        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + request.getOrderId()));

        if (order.getStatus() != OrderStatus.CREATED) {
            log.warn("Không thể phân công đơn hàng ID {}. Trạng thái hiện tại: {}", order.getId(), order.getStatus());
            throw new AppException("INVALID_ORDER_STATUS", "Chỉ có thể phân công shipper cho đơn hàng ở trạng thái CREATED");
        }

        // Cập nhật trạng thái đơn hàng sang ASSIGNED
        order.setStatus(OrderStatus.ASSIGNED);
        orderRepository.save(order);

        // Lưu bản ghi lịch sử giao hàng vào ShipmentEntity
        ShipmentEntity shipment = ShipmentEntity.builder()
                .orderId(order.getId())
                .shipperId(request.getShipperId())
                .status(OrderStatus.ASSIGNED)
                .note(request.getNote())
                .build();

        shipmentRepository.save(shipment);
        log.info("Phân công shipper thành công cho đơn hàng ID {}", order.getId());
    }

    @Override
    @Transactional
    public void updateShipmentStatus(Long orderId, UpdateShipmentStatusRequest request) {
        log.info("Cập nhật trạng thái đơn hàng ID {} sang trạng thái {}", orderId, request.getStatus());

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + orderId));

        // Cập nhật trạng thái mới cho đơn hàng
        order.setStatus(request.getStatus());
        orderRepository.save(order);

        // Lưu bản ghi theo dõi vết vào ShipmentEntity
        ShipmentEntity shipment = ShipmentEntity.builder()
                .orderId(order.getId())
                .status(request.getStatus())
                .note(request.getNote())
                .proofImageUrl(request.getProofImageUrl())
                .build();

        shipmentRepository.save(shipment);
        log.info("Cập nhật trạng thái đơn hàng ID {} thành công", order.getId());
    }
}
