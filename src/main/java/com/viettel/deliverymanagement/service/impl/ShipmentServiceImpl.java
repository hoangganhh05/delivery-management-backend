package com.viettel.deliverymanagement.service.impl;

import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.constant.PaymentMethod;
import com.viettel.deliverymanagement.constant.PaymentStatus;
import com.viettel.deliverymanagement.dto.request.AssignShipperRequest;
import com.viettel.deliverymanagement.dto.request.UpdateShipmentLocationRequest;
import com.viettel.deliverymanagement.dto.request.UpdateShipmentStatusRequest;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.entity.ShipmentEntity;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.repository.ShipmentRepository;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.repository.ShipmentOfferRepository;
import com.viettel.deliverymanagement.entity.ShipmentOfferEntity;
import com.viettel.deliverymanagement.dto.response.ShipmentOfferResponse;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDateTime;
import java.util.*;
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
    private final ShipmentOfferRepository offerRepository;
    @org.springframework.context.annotation.Lazy
    private final com.viettel.deliverymanagement.service.AiService aiService;

    private static final long OFFER_MINUTES = 2;

    @Override
    @Transactional
    public void autoAssign(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));
        if (order.isAwaitingOnlinePayment()) throw new AppException("PAYMENT_REQUIRED", "Cần xác nhận thanh toán trước khi phân công");
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PAID)
            throw new AppException("INVALID_ORDER_STATUS", "Đơn hàng không ở trạng thái chờ phân công");
        offerNext(order);
    }

    @Override @Transactional(readOnly = true)
    public List<ShipmentOfferResponse> getPendingOffers(String username) {
        UserEntity shipper = userRepository.findByUsername(username).orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy shipper"));
        if (shipper.getRole() != Role.SHIPPER) throw new AppException("INVALID_SHIPPER", "Tài khoản không phải shipper");
        LocalDateTime now = LocalDateTime.now();
        return offerRepository.findByShipperIdAndStatusOrderByExpiresAtAsc(shipper.getId(), "PENDING").stream()
                .filter(o -> o.getExpiresAt().isAfter(now)).map(this::toOffer).toList();
    }

    @Override @Transactional
    public void acceptOffer(Long offerId, String username) {
        UserEntity shipper = userRepository.findByUsername(username).orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy shipper"));
        ShipmentOfferEntity offer = offerRepository.findByIdForUpdate(offerId).orElseThrow(() -> new AppException("OFFER_NOT_FOUND", "Không tìm thấy lời mời"));
        if (!Objects.equals(offer.getShipperId(), shipper.getId())) throw new AppException("OFFER_ACCESS_DENIED", "Bạn không được nhận lời mời này");
        if (!"PENDING".equals(offer.getStatus()) || !offer.getExpiresAt().isAfter(LocalDateTime.now())) throw new AppException("OFFER_EXPIRED", "Lời mời đã hết hạn");
        OrderEntity order = orderRepository.findById(offer.getOrderId()).orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PAID) throw new AppException("OFFER_UNAVAILABLE", "Đơn hàng đã được nhận");
        offer.setStatus("ACCEPTED"); offer.setRespondedAt(LocalDateTime.now()); offerRepository.save(offer);
        order.setStatus(OrderStatus.ASSIGNED); orderRepository.save(order);
        shipmentRepository.save(ShipmentEntity.builder().orderId(order.getId()).shipperId(shipper.getId()).status(OrderStatus.ASSIGNED).note("Nhận từ phân công tự động").build());
    }

    @Override @Transactional
    public void declineOffer(Long offerId, String username) {
        UserEntity shipper = userRepository.findByUsername(username).orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy shipper"));
        ShipmentOfferEntity offer = offerRepository.findByIdForUpdate(offerId).orElseThrow(() -> new AppException("OFFER_NOT_FOUND", "Không tìm thấy lời mời"));
        if (!Objects.equals(offer.getShipperId(), shipper.getId())) throw new AppException("OFFER_ACCESS_DENIED", "Bạn không được từ chối lời mời này");
        if (!"PENDING".equals(offer.getStatus())) return;
        offer.setStatus("DECLINED"); offer.setRespondedAt(LocalDateTime.now()); offerRepository.save(offer);
        OrderEntity order = orderRepository.findById(offer.getOrderId()).orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));
        if (order.getStatus() == OrderStatus.CREATED || order.getStatus() == OrderStatus.PAID) offerNext(order);
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void expireOffers() {
        for (ShipmentOfferEntity offer : offerRepository.findByStatusAndExpiresAtBefore("PENDING", LocalDateTime.now())) {
            offer.setStatus("EXPIRED"); offer.setRespondedAt(LocalDateTime.now()); offerRepository.save(offer);
            orderRepository.findById(offer.getOrderId()).ifPresent(order -> {
                if (order.getStatus() == OrderStatus.CREATED || order.getStatus() == OrderStatus.PAID) offerNext(order);
            });
        }
    }

    private void offerNext(OrderEntity order) {
        List<UserEntity> candidates = new ArrayList<>(userRepository.findByRole(Role.SHIPPER));
        candidates.removeIf(s -> !"ACTIVE".equalsIgnoreCase(s.getStatus()) || offerRepository.existsByOrderIdAndShipperIdAndStatus(order.getId(), s.getId(), "PENDING") || offerRepository.existsByOrderIdAndShipperIdAndStatus(order.getId(), s.getId(), "ACCEPTED"));
        if (candidates.isEmpty()) throw new AppException("NO_AVAILABLE_SHIPPER", "Không còn shipper đang hoạt động");

        // Chọn shipper tối ưu thông qua AI Dispatch Matching (GPS, tải trọng, uy tín)
        UserEntity shipper = null;
        try {
            var recs = aiService.recommendShippers(order.getId());
            java.util.Set<Long> candidateIds = candidates.stream().map(UserEntity::getId).collect(java.util.stream.Collectors.toSet());
            for (var rec : recs) {
                if (candidateIds.contains(rec.getShipperId())) {
                    shipper = candidates.stream().filter(c -> c.getId().equals(rec.getShipperId())).findFirst().orElse(null);
                    if (shipper != null) break;
                }
            }
        } catch (Exception e) {
            log.warn("Lỗi khi gợi ý shipper qua AI: {}. Chuyển sang chọn shipper sẵn sàng đầu tiên.", e.getMessage());
        }

        if (shipper == null) {
            shipper = candidates.get(0);
        }

        offerRepository.save(ShipmentOfferEntity.builder().orderId(order.getId()).shipperId(shipper.getId()).status("PENDING").expiresAt(LocalDateTime.now().plusMinutes(OFFER_MINUTES)).build());
        try { notificationService.createNotification(shipper.getId(), "Có đơn hàng mới cần nhận", "Bạn có 2 phút để nhận đơn #" + order.getTrackingNumber(), "SHIPMENT_OFFER", order.getId()); } catch (Exception ignored) { }
    }

    private ShipmentOfferResponse toOffer(ShipmentOfferEntity offer) {
        OrderEntity order = orderRepository.findById(offer.getOrderId()).orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));
        return ShipmentOfferResponse.builder().offerId(offer.getId()).orderId(order.getId()).trackingNumber(order.getTrackingNumber()).receiverName(order.getReceiverName()).receiverAddress(order.getReceiverAddress()).expiresAt(offer.getExpiresAt()).build();
    }

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

        if (order.isAwaitingOnlinePayment()) {
            throw new AppException("PAYMENT_REQUIRED", "Cần xác nhận thanh toán trước khi phân công giao hàng");
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
                .createdAt(java.time.LocalDateTime.now())
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
        if (request.getStatus().isDeliveryCompleted() && order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaidAt(java.time.LocalDateTime.now());
            order.setPaymentReference("COD-" + order.getTrackingNumber());
        } else if (request.getStatus() == OrderStatus.FAILED && order.getPaymentStatus() != PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.FAILED);
        }
        orderRepository.save(order);

        // Lưu bản ghi theo dõi vết vào ShipmentEntity
        ShipmentEntity shipment = ShipmentEntity.builder()
                .orderId(order.getId())
                .shipperId(assignment.getShipperId())
                .status(request.getStatus())
                .note(request.getNote())
                .proofImageUrl(request.getProofImageUrl())
                .currentLatitude(assignment.getCurrentLatitude())
                .currentLongitude(assignment.getCurrentLongitude())
                .currentAccuracyMeters(assignment.getCurrentAccuracyMeters())
                .locationUpdatedAt(assignment.getLocationUpdatedAt())
                .locationReportedAt(assignment.getLocationReportedAt())
                .createdAt(java.time.LocalDateTime.now())
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

    @Override
    @Transactional
    public void updateShipmentLocation(Long orderId, UpdateShipmentLocationRequest request, String username) {
        UserEntity actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng"));
        ShipmentEntity assignment = shipmentRepository
                .findFirstByOrderIdAndShipperIdIsNotNullOrderByIdDesc(orderId)
                .orElseThrow(() -> new AppException("SHIPMENT_NOT_ASSIGNED", "Đơn hàng chưa được phân công shipper"));

        if (actor.getRole() == Role.SHIPPER && !actor.getId().equals(assignment.getShipperId())) {
            throw new AppException("SHIPMENT_ACCESS_DENIED", "Bạn không được cập nhật vị trí cho đơn hàng này");
        }
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + orderId));
        if (!(order.getStatus() == OrderStatus.ASSIGNED
                || order.getStatus() == OrderStatus.PICKED_UP
                || order.getStatus() == OrderStatus.IN_TRANSIT
                || order.getStatus() == OrderStatus.SHIPPING)) {
            throw new AppException("INVALID_ORDER_STATUS", "Chỉ cập nhật vị trí khi đơn đang được giao");
        }

        assignment.setCurrentLatitude(request.getLatitude());
        assignment.setCurrentLongitude(request.getLongitude());
        assignment.setCurrentAccuracyMeters(request.getAccuracy());
        assignment.setLocationUpdatedAt(java.time.LocalDateTime.now(java.time.Clock.systemUTC()));
        assignment.setLocationReportedAt(request.getTimestamp() == null
                ? assignment.getLocationUpdatedAt()
                : java.time.LocalDateTime.ofInstant(request.getTimestamp(), java.time.ZoneOffset.UTC));
        shipmentRepository.save(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public com.viettel.deliverymanagement.dto.response.DriverLocationResponse getLatestLocation(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + orderId));
        boolean activeDelivery = order.getStatus() == OrderStatus.ASSIGNED
                || order.getStatus() == OrderStatus.PICKED_UP
                || order.getStatus() == OrderStatus.IN_TRANSIT
                || order.getStatus() == OrderStatus.SHIPPING;
        if (!activeDelivery) return null;

        ShipmentEntity latest = shipmentRepository.findFirstByOrderIdAndShipperIdIsNotNullOrderByIdDesc(orderId)
                .orElse(null);
        if (latest == null || latest.getCurrentLatitude() == null || latest.getCurrentLongitude() == null) return null;
        return com.viettel.deliverymanagement.dto.response.DriverLocationResponse.builder()
                .orderId(orderId)
                .latitude(latest.getCurrentLatitude())
                .longitude(latest.getCurrentLongitude())
                .accuracyMeters(latest.getCurrentAccuracyMeters())
                .reportedAt(toUtcInstant(latest.getLocationReportedAt()))
                .receivedAt(toUtcInstant(latest.getLocationUpdatedAt()))
                .build();
    }

    private java.time.Instant toUtcInstant(java.time.LocalDateTime value) {
        return value == null ? null : value.toInstant(java.time.ZoneOffset.UTC);
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
