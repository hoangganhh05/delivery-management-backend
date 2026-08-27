package com.viettel.deliverymanagement.service.impl;

import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.dto.request.CreateOrderRequest;
import com.viettel.deliverymanagement.dto.request.OrderItemRequest;
import com.viettel.deliverymanagement.dto.request.OrderSearchRequest;
import com.viettel.deliverymanagement.dto.response.OrderResponse;
import com.viettel.deliverymanagement.dto.response.PageResponse;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.entity.OrderItemEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.repository.VoucherRepository;
import com.viettel.deliverymanagement.service.OrderService;
import jakarta.persistence.criteria.Predicate;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final VoucherRepository voucherRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Sinh mã vận đơn tự động (Tracking Number)
        String trackingNumber = "VT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        BigDecimal shippingFee = request.getShippingFee();
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

        // Xử lý áp dụng voucher an toàn không gây rollback transaction
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            String code = request.getVoucherCode().trim().toUpperCase();
            var voucherOpt = voucherRepository.findByCode(code);
            if (voucherOpt.isPresent()) {
                var voucher = voucherOpt.get();
                LocalDateTime now = LocalDateTime.now();
                boolean valid = true;

                if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
                    valid = false;
                }
                if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
                    valid = false;
                }
                if (voucher.getUsageLimit() != null && voucher.getUsageLimit() <= 0) {
                    valid = false;
                }

                // Kiểm tra đơn tối thiểu so với tiền hàng (hoặc cước vận chuyển)
                BigDecimal baseAmountForVoucher = totalPrice.compareTo(BigDecimal.ZERO) > 0 ? totalPrice : shippingFee;
                if (voucher.getMinOrderAmount() != null && baseAmountForVoucher.compareTo(voucher.getMinOrderAmount()) < 0) {
                    valid = false;
                }

                if (valid) {
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
                .senderName(request.getSenderName())
                .senderPhone(request.getSenderPhone())
                .senderAddress(request.getSenderAddress())
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverAddress(request.getReceiverAddress())
                .weightGram(request.getWeightGram())
                .shippingFee(shippingFee)
                .discountFee(discountFee)
                .voucherId(voucherId)
                .totalFee(totalFee)
                .totalPrice(totalPrice)
                .codAmount(request.getCodAmount() != null ? request.getCodAmount() : BigDecimal.ZERO)
                .status(OrderStatus.CREATED)
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
    public OrderResponse getOrderByTrackingNumber(String trackingNumber) {
        OrderEntity order = orderRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với mã vận đơn: " + trackingNumber));

        return mapToOrderResponse(order);
    }

    @Override
    public PageResponse<OrderResponse> searchOrders(OrderSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("id").descending());

        Specification<OrderEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keywordLike = "%" + request.getKeyword().trim() + "%";
                Predicate trackingMatch = cb.like(root.get("trackingNumber"), keywordLike);
                Predicate senderPhoneMatch = cb.like(root.get("senderPhone"), keywordLike);
                Predicate receiverPhoneMatch = cb.like(root.get("receiverPhone"), keywordLike);
                predicates.add(cb.or(trackingMatch, senderPhoneMatch, receiverPhoneMatch));
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
    public OrderResponse cancelOrder(String trackingNumber) {
        OrderEntity order = orderRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với mã vận đơn: " + trackingNumber));

        if (order.getStatus() == OrderStatus.DELIVERED
                || order.getStatus() == OrderStatus.DONE
                || order.getStatus() == OrderStatus.COMPLETED) {
            throw new AppException("ORDER_CANNOT_CANCEL", "Không thể hủy đơn hàng đã hoàn thành");
        }
        if (order.getStatus() != OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.CANCELLED);
            order = orderRepository.save(order);
        }
        return mapToOrderResponse(order);
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
                .discountFee(order.getDiscountFee())
                .totalFee(order.getTotalFee())
                .totalPrice(order.getTotalPrice() != null ? order.getTotalPrice() : order.getTotalFee())
                .codAmount(order.getCodAmount())
                .status(order.getStatus())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
