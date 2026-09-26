package com.viettel.deliverymanagement.service.impl;

import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.constant.PaymentStatus;
import com.viettel.deliverymanagement.constant.PaymentMethod;
import com.viettel.deliverymanagement.dto.request.CreateOrderRequest;
import com.viettel.deliverymanagement.dto.request.OrderItemRequest;
import com.viettel.deliverymanagement.dto.request.OrderSearchRequest;
import com.viettel.deliverymanagement.dto.response.OrderResponse;
import com.viettel.deliverymanagement.dto.response.OrderItemResponse;
import com.viettel.deliverymanagement.dto.response.PageResponse;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.entity.OrderItemEntity;
import com.viettel.deliverymanagement.entity.ShipmentEntity;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.repository.ShipmentRepository;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.repository.VoucherRepository;
import com.viettel.deliverymanagement.service.OrderService;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String username) {
        UserEntity currentUser = requireUser(username);
        PaymentMethod paymentMethod = request.getPaymentMethod() == null ? PaymentMethod.COD : request.getPaymentMethod();
        if (paymentMethod == PaymentMethod.VCB_QR) {
            throw new AppException(
                    "PAYMENT_METHOD_DISABLED",
                    "Chuyển khoản QR ngân hàng không còn được hỗ trợ cho đơn hàng mới"
            );
        }
        // 1. Sinh mã vận đơn tự động (Tracking Number)
        String trackingNumber = "VT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String serviceType = request.getServiceType() == null ? "STANDARD" : request.getServiceType();
        BigDecimal shippingFee = switch (serviceType) {
            case "STANDARD" -> BigDecimal.valueOf(30000);
            case "EXPRESS" -> BigDecimal.valueOf(50000);
            default -> throw new AppException("INVALID_SERVICE_TYPE", "Gói giao hàng không hợp lệ");
        };
        if (request.getShippingFee() == null || request.getShippingFee().compareTo(shippingFee) != 0) {
            throw new AppException("SHIPPING_FEE_MISMATCH", "Phí giao hàng đã thay đổi, vui lòng tải lại và thử lại");
        }
        BigDecimal discountFee = BigDecimal.ZERO;
        Long voucherId = null;

        // Tính tổng giá trị tiền hàng (totalPrice = tổng số lượng * giá trị khai báo của từng item)
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderItemRequest item : request.getItems()) {
                if (item.getDeclaredValue() != null && item.getQuantity() != null) {
                    totalPrice = totalPrice.add(item.getDeclaredValue().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }
        }
        if (totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            totalPrice = shippingFee != null ? shippingFee : BigDecimal.ZERO;
        }

        // Validate the voucher again at order creation. The value calculated in
        // the UI may be stale by the time this transactional request arrives.
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            String code = request.getVoucherCode().trim().toUpperCase();
            var voucher = voucherRepository.findByCode(code)
                    .orElseThrow(() -> new AppException("VOUCHER_NOT_FOUND", "Mã voucher không tồn tại hoặc đã bị vô hiệu hóa"));
            if (!voucher.isActive()) {
                throw new AppException("VOUCHER_INACTIVE", "Mã voucher đã bị vô hiệu hóa");
            }

            LocalDateTime now = LocalDateTime.now();
            if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
                throw new AppException("VOUCHER_NOT_YET_VALID", "Voucher chưa đến thời gian áp dụng");
            }
            if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
                throw new AppException("VOUCHER_EXPIRED", "Voucher đã hết hạn sử dụng");
            }
            if (voucher.getUsageLimit() != null && voucher.getUsageLimit() <= 0) {
                throw new AppException("VOUCHER_OUT_OF_USAGE", "Voucher đã hết lượt sử dụng");
            }

            BigDecimal baseAmountForVoucher = totalPrice.compareTo(BigDecimal.ZERO) > 0 ? totalPrice : shippingFee;
            if (voucher.getMinOrderAmount() != null && baseAmountForVoucher.compareTo(voucher.getMinOrderAmount()) < 0) {
                throw new AppException(
                        "ORDER_AMOUNT_NOT_ENOUGH",
                        "Giá trị đơn hàng chưa đạt mức tối thiểu " + voucher.getMinOrderAmount() + " để áp dụng voucher"
                );
            }

            voucherId = voucher.getId();
            if (voucher.getDiscountPercent() != null && voucher.getDiscountPercent() > 0) {
                discountFee = shippingFee.multiply(BigDecimal.valueOf(voucher.getDiscountPercent()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            if (voucher.getMaxDiscountAmount() != null && discountFee.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discountFee = voucher.getMaxDiscountAmount();
            }
            if (discountFee.compareTo(shippingFee) > 0) {
                discountFee = shippingFee;
            }
            if (voucher.getUsageLimit() != null) {
                if (voucherRepository.consumeOneUse(voucher.getId()) != 1) {
                    throw new AppException("VOUCHER_OUT_OF_USAGE", "Voucher đã hết lượt sử dụng");
                }
            }
        }

        BigDecimal totalFee = shippingFee.subtract(discountFee);
        if (totalFee.compareTo(BigDecimal.ZERO) < 0) {
            totalFee = BigDecimal.ZERO;
        }

        // 2. Bắt đầu build OrderEntity
        OrderEntity order = OrderEntity.builder()
                .trackingNumber(trackingNumber)
                .senderId(currentUser.getId())
                .senderName(request.getSenderName())
                .senderPhone(request.getSenderPhone())
                .senderAddress(request.getSenderAddress())
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverAddress(request.getReceiverAddress())
                .weightGram(request.getWeightGram())
                .shippingFee(shippingFee)
                .serviceType(serviceType)
                .discountFee(discountFee)
                .voucherId(voucherId)
                .totalFee(totalFee)
                .totalPrice(totalPrice)
                .codAmount(request.getCodAmount() != null ? request.getCodAmount() : BigDecimal.ZERO)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.PENDING)
                .status(paymentMethod == PaymentMethod.COD ? OrderStatus.CREATED : OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        // 3. Mapping danh sách OrderItems (gồm cả price và declaredValue)
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            var items = request.getItems().stream().map(itemReq -> OrderItemEntity.builder()
                    .order(order)
                    .itemName(itemReq.getItemName())
                    .quantity(itemReq.getQuantity())
                    .weightGram(itemReq.getWeightGram())
                    .price(itemReq.getDeclaredValue() != null ? itemReq.getDeclaredValue() : BigDecimal.ZERO)
                    .declaredValue(itemReq.getDeclaredValue() != null ? itemReq.getDeclaredValue() : BigDecimal.ZERO)
                    .build()).collect(Collectors.toList());
            order.setItems(items);
        }

        // 4. Lưu vào CSDL
        OrderEntity savedOrder = orderRepository.save(order);

        // 5. Trả về DTO Response
        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByTrackingNumber(String trackingNumber, String username) {
        OrderEntity order = orderRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với mã vận đơn: " + trackingNumber));

        assertCanAccess(order, requireUser(username));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> searchOrders(OrderSearchRequest request, String username) {
        UserEntity currentUser = requireUser(username);
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("id").descending());

        Specification<OrderEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (currentUser.getRole() == Role.CUSTOMER) {
                predicates.add(cb.equal(root.get("senderId"), currentUser.getId()));
            } else if (currentUser.getRole() == Role.SHIPPER) {
                Subquery<Long> assignedOrders = query.subquery(Long.class);
                Root<ShipmentEntity> shipment = assignedOrders.from(ShipmentEntity.class);
                assignedOrders.select(shipment.get("orderId"))
                        .where(cb.equal(shipment.get("shipperId"), currentUser.getId()));
                predicates.add(root.get("id").in(assignedOrders));
            }

            // Customers can resume their payment requests. Operational lists cannot.
            if (currentUser.getRole() != Role.CUSTOMER) {
                predicates.add(cb.or(
                        cb.equal(root.get("paymentMethod"), PaymentMethod.COD),
                        cb.equal(root.get("paymentStatus"), PaymentStatus.PAID)
                ));
            }

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keywordLike = "%" + request.getKeyword().trim().toLowerCase(Locale.ROOT) + "%";
                Predicate trackingMatch = cb.like(cb.lower(root.get("trackingNumber")), keywordLike);
                Predicate senderNameMatch = cb.like(cb.lower(root.get("senderName")), keywordLike);
                Predicate senderPhoneMatch = cb.like(cb.lower(root.get("senderPhone")), keywordLike);
                Predicate receiverNameMatch = cb.like(cb.lower(root.get("receiverName")), keywordLike);
                Predicate receiverPhoneMatch = cb.like(cb.lower(root.get("receiverPhone")), keywordLike);
                predicates.add(cb.or(
                        trackingMatch,
                        senderNameMatch,
                        senderPhoneMatch,
                        receiverNameMatch,
                        receiverPhoneMatch
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<OrderEntity> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponse> responses = orderPage.getContent().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());

        return PageResponse.<OrderResponse>builder()
                .items(responses)
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(String trackingNumber, String username) {
        OrderEntity order = orderRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với mã vận đơn: " + trackingNumber));

        UserEntity currentUser = requireUser(username);
        assertCanAccess(order, currentUser);
        if (currentUser.getRole() == Role.SHIPPER) {
            throw new AppException("ORDER_ACCESS_DENIED", "Shipper không có quyền hủy đơn hàng");
        }

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PENDING) {
            throw new AppException("ORDER_CANNOT_CANCEL", "Chỉ có thể hủy đơn hàng chưa được phân công");
        }
        if (order.getStatus() != OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.CANCELLED);
            if (order.getPaymentStatus() != PaymentStatus.PAID) order.setPaymentStatus(PaymentStatus.FAILED);
            order = orderRepository.save(order);
        }
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForShipper(Long shipperId) {
        UserEntity shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new AppException("SHIPPER_NOT_FOUND", "Không tìm thấy shipper"));
        if (shipper.getRole() != Role.SHIPPER) {
            throw new AppException("INVALID_SHIPPER", "Tài khoản được chọn không phải shipper");
        }
        List<Long> orderIds = shipmentRepository.findDistinctOrderIdsByShipperId(shipperId);
        if (orderIds.isEmpty()) {
            return List.of();
        }
        return orderRepository.findByIdInOrderByIdDesc(orderIds).stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    private OrderResponse mapToOrderResponse(OrderEntity order) {
        return OrderResponse.builder()
                .id(order.getId())
                .trackingNumber(order.getTrackingNumber())
                .senderName(order.getSenderName())
                .senderPhone(order.getSenderPhone())
                .senderAddress(order.getSenderAddress())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .shippingFee(order.getShippingFee())
                .serviceType(order.getServiceType())
                .discountFee(order.getDiscountFee())
                .totalFee(order.getTotalFee())
                .totalPrice(order.getTotalPrice() != null ? order.getTotalPrice() : order.getTotalFee())
                .codAmount(order.getCodAmount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .paidAt(order.getPaidAt())
                .paymentReference(order.getPaymentReference())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(order.getItems() == null ? List.of() : order.getItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .id(item.getId())
                                .itemName(item.getItemName())
                                .quantity(item.getQuantity())
                                .weightGram(item.getWeightGram())
                                .declaredValue(item.getDeclaredValue())
                                .build())
                        .toList())
                .build();
    }

    private UserEntity requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng"));
    }

    private void assertCanAccess(OrderEntity order, UserEntity user) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getRole() == Role.CUSTOMER && user.getId().equals(order.getSenderId())) {
            return;
        }
        if (user.getRole() == Role.SHIPPER
                && shipmentRepository.existsByOrderIdAndShipperId(order.getId(), user.getId())) {
            return;
        }
        throw new AppException("ORDER_ACCESS_DENIED", "Bạn không có quyền truy cập đơn hàng này");
    }
}
