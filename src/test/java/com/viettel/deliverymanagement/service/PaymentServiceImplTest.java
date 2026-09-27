package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.config.VNPayConfig;
import com.viettel.deliverymanagement.config.ManualPaymentConfig;
import com.viettel.deliverymanagement.constant.OrderStatus;
import com.viettel.deliverymanagement.constant.PaymentMethod;
import com.viettel.deliverymanagement.constant.PaymentStatus;
import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.entity.OrderEntity;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.OrderRepository;
import com.viettel.deliverymanagement.repository.ShipmentRepository;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private VNPayConfig vnPayConfig;
    @Mock private ManualPaymentConfig manualPaymentConfig;
    @InjectMocks private PaymentServiceImpl paymentService;

    @Test
    void confirmManualBankTransfer_ActivatesOrderOnlyAfterReferenceIsProvided() {
        OrderEntity order = pendingOrder(PaymentMethod.MANUAL_BANK_TRANSFER);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.confirmPayment(1L, "BANK-123");

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertEquals("BANK-123", order.getPaymentReference());
    }

    @Test
    void confirmManualBankTransfer_RejectsMissingReference() {
        OrderEntity order = pendingOrder(PaymentMethod.MANUAL_BANK_TRANSFER);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(AppException.class, () -> paymentService.confirmPayment(1L, " "));
        assertEquals(PaymentStatus.PENDING, order.getPaymentStatus());
        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void confirmBankTransfer_CannotMarkCodAsPaid() {
        OrderEntity order = pendingOrder(PaymentMethod.COD);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(AppException.class, () -> paymentService.confirmPayment(1L, "BANK-123"));
        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void vnpayCallback_RejectsMissingMerchantConfiguration() {
        assertThrows(AppException.class, () -> paymentService.processVNPayCallback(Map.of()));
        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void qrPayment_RejectsMissingBankAccountConfiguration() {
        AppException exception = assertThrows(
                AppException.class,
                () -> paymentService.getQrPayment(1L, "customer")
        );

        assertEquals("PAYMENT_GATEWAY_UNAVAILABLE", exception.getCode());
        verify(orderRepository, never()).findById(any());
    }

    @Test
    void availableMethods_AlwaysIncludesCodAndOnlyConfiguredManualMethods() {
        when(manualPaymentConfig.availableMethods()).thenReturn(List.of(
                PaymentMethod.COD, PaymentMethod.MANUAL_MOMO
        ));

        assertEquals(List.of(PaymentMethod.COD, PaymentMethod.MANUAL_MOMO),
                paymentService.getAvailablePaymentMethods());
    }

    @Test
    void manualMomoInstructions_OnlyExposeConfiguredRecipientForOrderOwner() {
        OrderEntity order = pendingOrder(PaymentMethod.MANUAL_MOMO);
        order.setSenderId(2L);
        UserEntity customer = UserEntity.builder().username("customer").role(Role.CUSTOMER).build();
        customer.setId(2L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customer));
        when(manualPaymentConfig.isAvailable(PaymentMethod.MANUAL_MOMO)).thenReturn(true);
        when(manualPaymentConfig.getMomoPhone()).thenReturn("0900000000");
        when(manualPaymentConfig.getMomoAccountName()).thenReturn("Delivery Test");

        var instructions = paymentService.getManualPaymentInstructions(1L, "customer");

        assertNotNull(instructions);
        assertEquals(PaymentMethod.MANUAL_MOMO, instructions.getMethod());
        assertEquals("0900000000", instructions.getRecipientValue());
        assertEquals("THANHTOAN VT12345678", instructions.getTransferContent());
    }

    private OrderEntity pendingOrder(PaymentMethod method) {
        return OrderEntity.builder().id(1L).trackingNumber("VT12345678")
                .senderName("Khach hang").totalFee(BigDecimal.valueOf(30000))
                .paymentMethod(method).paymentStatus(PaymentStatus.PENDING)
                .status(OrderStatus.PENDING).build();
    }
}
