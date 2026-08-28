package com.viettel.deliverymanagement.service.impl;

import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.dto.request.AssignShipperRequest;
import com.viettel.deliverymanagement.dto.request.UpdateShipmentStatusRequest;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.entity.ShipmentEntity;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.repository.ShipmentRepository;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.service.NotificationService;
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
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void assignShipper(AssignShipperRequest request) {
        log.info("Bắt đầu phân công shipper ID {} cho đơn hàng ID {}", request.getShipperId(), request.getOrderId());

        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + request.getOrderId()));

        UserEntity shipper = userRepository.findById(request.getShipperId())
                .orElseThrow(() -> new AppException("SHIPPER_NOT_FOUND", "Không tìm thấy shipper được chọn"));
        if (shipper.getRole() != Role.SHIPPER || !"ACTIVE".equalsIgnoreCase(shipper.getStatus())) {
            throw new AppException("INVALID_SHIPPER", "Tài khoản được chọn không phải shipper đang hoạt động");
        }

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PAID) {
            log.warn("Không thể phân công đơn hàng ID {}. Trạng thái hiện tại: {}", order.getId(), order.getStatus());
            throw new AppException("INVALID_ORDER_STATUS", "Chỉ có thể phân công đơn hàng mới hoặc đã thanh toán");
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

        try {
            notificationService.createNotification(
                    request.getShipperId(),
                    "Đơn hàng mới được gán",
                    "Bạn đã được phân công giao đơn hàng #" + order.getTrackingNumber() + " đến " + order.getReceiverAddress(),
                    "SHIPMENT",
                    order.getId()
            );
        } catch (Exception e) {
            log.warn("Không thể tạo thông báo: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateShipmentStatus(Long orderId, UpdateShipmentStatusRequest request, String username) {
        log.info("Cập nhật trạng thái đơn hàng ID {} sang trạng thái {}", orderId, request.getStatus());

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + orderId));

        UserEntity actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng"));
        ShipmentEntity assignment = shipmentRepository
                .findFirstByOrderIdAndShipperIdIsNotNullOrderByIdDesc(orderId)
                .orElseThrow(() -> new AppException("SHIPMENT_NOT_ASSIGNED", "Đơn hàng chưa được phân công shipper"));

        if (actor.getRole() == Role.SHIPPER && !actor.getId().equals(assignment.getShipperId())) {
            throw new AppException("SHIPMENT_ACCESS_DENIED", "Bạn không được phân công xử lý đơn hàng này");
        }
        validateTransition(order.getStatus(), request.getStatus());

        // Cập nhật trạng thái mới cho đơn hàng
        order.setStatus(request.getStatus());
        orderRepository.save(order);

        // Lưu bản ghi theo dõi vết vào ShipmentEntity
        ShipmentEntity shipment = ShipmentEntity.builder()
                .orderId(order.getId())
                .shipperId(assignment.getShipperId())
                .status(request.getStatus())
                .note(request.getNote())
                .proofImageUrl(request.getProofImageUrl())
                .build();

        shipmentRepository.save(shipment);
        log.info("Cập nhật trạng thái đơn hàng ID {} thành công", order.getId());

        try {
            if (order.getSenderId() != null) {
                notificationService.createNotification(
                        order.getSenderId(),
                        "Cập nhật trạng thái đơn hàng #" + order.getTrackingNumber(),
                        "Đơn hàng của bạn đã chuyển sang trạng thái: " + request.getStatus().name(),
                        "ORDER",
                        order.getId()
                );
            }
        } catch (Exception e) {
            log.warn("Không thể tạo thông báo: {}", e.getMessage());
        }
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case ASSIGNED -> next == OrderStatus.PICKED_UP;
            case PICKED_UP -> next == OrderStatus.IN_TRANSIT || next == OrderStatus.SHIPPING;
            case IN_TRANSIT, SHIPPING -> next == OrderStatus.DELIVERED || next == OrderStatus.FAILED;
            default -> false;
        };
        if (!valid) {
            throw new AppException(
                    "INVALID_STATUS_TRANSITION",
                    "Không thể chuyển trạng thái từ " + current + " sang " + next
            );
        }
    }
}
