package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.dto.response.TrackingResponse;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.entity.ShipmentEntity;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.repository.ShipmentRepository;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.service.impl.TrackingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TrackingServiceImpl trackingService;

    @Test
    void trackOrder_ReturnsAssignedShipperContactDetails() {
        OrderEntity order = OrderEntity.builder()
                .trackingNumber("VT12345678")
                .senderName("Nguyen Van A")
                .receiverName("Tran Thi B")
                .status(OrderStatus.IN_TRANSIT)
                .build();
        order.setId(1L);

        ShipmentEntity shipment = ShipmentEntity.builder()
                .orderId(1L)
                .shipperId(9L)
                .status(OrderStatus.IN_TRANSIT)
                .createdAt(LocalDateTime.now())
                .build();

        UserEntity shipper = UserEntity.builder()
                .username("shipper_nam")
                .fullName("Nguyen Van Nam")
                .phoneNumber("0901234567")
                .build();

        when(orderRepository.findByTrackingNumber("VT12345678")).thenReturn(Optional.of(order));
        when(shipmentRepository.findByOrderIdOrderByIdDesc(1L)).thenReturn(List.of(shipment));
        when(shipmentRepository.findFirstByOrderIdAndShipperIdIsNotNullOrderByIdDesc(1L))
                .thenReturn(Optional.of(shipment));
        when(userRepository.findById(9L)).thenReturn(Optional.of(shipper));

        TrackingResponse response = trackingService.trackOrder("VT12345678");

        assertEquals("Nguyen Van Nam", response.getShipperName());
        assertEquals("0901234567", response.getShipperPhone());
    }
}
