package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.constant.PaymentMethod;
import com.viettel.deliverymanagement.constant.PaymentStatus;
import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.dto.request.AssignShipperRequest;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.entity.ShipmentEntity;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.repository.ShipmentRepository;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.service.impl.ShipmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {
    @Mock private OrderRepository orderRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks private ShipmentServiceImpl shipmentService;

    @Test
    void assignShipper_RejectsUnpaidBankTransfer() {
        OrderEntity order = OrderEntity.builder().id(1L).status(OrderStatus.CREATED)
                .paymentMethod(PaymentMethod.VCB_QR).paymentStatus(PaymentStatus.PENDING).build();
        UserEntity shipper = UserEntity.builder().id(2L).role(Role.SHIPPER).status("ACTIVE").build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(2L)).thenReturn(Optional.of(shipper));

        assertThrows(AppException.class, () -> shipmentService.assignShipper(
                AssignShipperRequest.builder().orderId(1L).shipperId(2L).build()));

        assertEquals(OrderStatus.CREATED, order.getStatus());
        verify(orderRepository, never()).save(any(OrderEntity.class));
        verify(shipmentRepository, never()).save(any(ShipmentEntity.class));
    }
}
