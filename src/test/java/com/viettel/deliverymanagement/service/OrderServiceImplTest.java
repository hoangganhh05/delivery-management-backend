package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.dto.request.CreateOrderRequest;
import com.viettel.deliverymanagement.dto.request.OrderItemRequest;
import com.viettel.deliverymanagement.dto.response.OrderResponse;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Tạo đơn hàng thành công khi dữ liệu đầu vào hợp lệ")
    void createOrder_Success() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setSenderName("Nguyen Van A");
        request.setSenderPhone("0987654321");
        request.setSenderAddress("Số 1 Giang Van Minh, Ba Dinh, Ha Noi");
        request.setReceiverName("Tran Thi B");
        request.setReceiverPhone("0912345678");
        request.setReceiverAddress("Số 10 Pham Van Dong, Cau Giay, Ha Noi");
        request.setWeightGram(1200);
        request.setShippingFee(BigDecimal.valueOf(35000));
        request.setCodAmount(BigDecimal.valueOf(200000));

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setItemName("Giày Thể Thao Sneaker");
        itemRequest.setQuantity(1);
        itemRequest.setWeightGram(1200);
        itemRequest.setDeclaredValue(BigDecimal.valueOf(200000));
        request.setItems(List.of(itemRequest));

        OrderEntity savedOrder = OrderEntity.builder()
                .trackingNumber("VT12345678")
                .senderName(request.getSenderName())
                .senderPhone(request.getSenderPhone())
                .senderAddress(request.getSenderAddress())
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverAddress(request.getReceiverAddress())
                .weightGram(request.getWeightGram())
                .shippingFee(request.getShippingFee())
                .discountFee(BigDecimal.ZERO)
                .totalFee(request.getShippingFee())
                .codAmount(request.getCodAmount())
                .status(OrderStatus.CREATED)
                .build();
        savedOrder.setId(1L);
        savedOrder.setCreatedAt(LocalDateTime.now());

        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("VT12345678", response.getTrackingNumber());
        assertEquals(OrderStatus.CREATED, response.getStatus());
        assertEquals("Nguyen Van A", response.getSenderName());
        assertEquals("0987654321", response.getSenderPhone());
        assertEquals("Tran Thi B", response.getReceiverName());
        assertEquals("0912345678", response.getReceiverPhone());
        assertEquals(BigDecimal.valueOf(35000), response.getTotalFee());
        assertNotNull(response.getCreatedAt());

        verify(orderRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    @DisplayName("Lấy thông tin đơn hàng theo mã vận đơn thành công khi đơn hàng tồn tại")
    void getOrderByTrackingNumber_Success() {
        // Arrange
        String trackingNumber = "VT88889999";
        OrderEntity mockOrder = OrderEntity.builder()
                .trackingNumber(trackingNumber)
                .senderName("Le Van C")
                .senderPhone("0977112233")
                .receiverName("Pham Van D")
                .receiverPhone("0988445566")
                .totalFee(BigDecimal.valueOf(50000))
                .status(OrderStatus.IN_TRANSIT)
                .build();
        mockOrder.setId(10L);
        mockOrder.setCreatedAt(LocalDateTime.now());

        when(orderRepository.findByTrackingNumberAndIsDeletedFalse(trackingNumber))
                .thenReturn(Optional.of(mockOrder));

        // Act
        OrderResponse response = orderService.getOrderByTrackingNumber(trackingNumber);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(trackingNumber, response.getTrackingNumber());
        assertEquals("Le Van C", response.getSenderName());
        assertEquals("0977112233", response.getSenderPhone());
        assertEquals("Pham Van D", response.getReceiverName());
        assertEquals("0988445566", response.getReceiverPhone());
        assertEquals(OrderStatus.IN_TRANSIT, response.getStatus());
        assertEquals(BigDecimal.valueOf(50000), response.getTotalFee());

        verify(orderRepository, times(1)).findByTrackingNumberAndIsDeletedFalse(trackingNumber);
    }

    @Test
    @DisplayName("Ném AppException khi tìm đơn hàng theo mã vận đơn không tồn tại")
    void getOrderByTrackingNumber_NotFound_ThrowsException() {
        // Arrange
        String nonExistentTrackingNumber = "VT00000000";
        when(orderRepository.findByTrackingNumberAndIsDeletedFalse(nonExistentTrackingNumber))
                .thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> 
                orderService.getOrderByTrackingNumber(nonExistentTrackingNumber)
        );

        assertEquals("ORDER_NOT_FOUND", exception.getCode());
        assertTrue(exception.getMessage().contains(nonExistentTrackingNumber));

        verify(orderRepository, times(1)).findByTrackingNumberAndIsDeletedFalse(nonExistentTrackingNumber);
    }
}
