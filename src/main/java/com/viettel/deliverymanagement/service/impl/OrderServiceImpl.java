package com.viettel.deliverymanagement.service.impl;

import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.dto.request.ApplyVoucherRequest;
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
import com.viettel.deliverymanagement.service.VoucherService;
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
    private final VoucherService voucherService;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Sinh mã vận đơn tự động (Tracking Number)
        String trackingNumber = "VT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        BigDecimal shippingFee = request.getShippingFee();
        BigDecimal discountFee = BigDecimal.ZERO;
        Long voucherId = null;

        // Xử lý áp dụng voucher nếu có
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            String code = request.getVoucherCode().trim().toUpperCase();
            var voucherOpt = voucherRepository.findByCode(code);
            if (voucherOpt.isPresent()) {
                var voucher = voucherOpt.get();
                voucherId = voucher.getId();
                try {
                    discountFee = voucherService.calculateDiscount(new ApplyVoucherRequest(code, shippingFee));
                } catch (Exception ignored) {
                    discountFee = BigDecimal.ZERO;
                }
            }
        }

        BigDecimal totalFee = shippingFee.subtract(discountFee);
        if (totalFee.compareTo(BigDecimal.ZERO) < 0) {
            totalFee = BigDecimal.ZERO;
        }

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
            totalPrice = totalFee != null ? totalFee : BigDecimal.ZERO;
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

        // 3. Mapping danh sách OrderItems
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            var items = request.getItems().stream().map(itemReq -> OrderItemEntity.builder()
                    .order(order)
                    .itemName(itemReq.getItemName())
                    .quantity(itemReq.getQuantity())
                    .weightGram(itemReq.getWeightGram())
                    .declaredValue(itemReq.getDeclaredValue())
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

    private OrderResponse mapToOrderResponse(OrderEntity order) {
        return OrderResponse.builder()
                .id(order.getId())
                .trackingNumber(order.getTrackingNumber())
                .senderName(order.getSenderName())
                .senderPhone(order.getSenderPhone())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
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