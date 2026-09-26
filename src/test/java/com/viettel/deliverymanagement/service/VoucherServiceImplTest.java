package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.request.ApplyVoucherRequest;
import com.viettel.deliverymanagement.dto.response.VoucherCalculationResponse;
import com.viettel.deliverymanagement.entity.VoucherEntity;
import com.viettel.deliverymanagement.repository.VoucherRepository;
import com.viettel.deliverymanagement.service.impl.VoucherServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    @Test
    void calculateDiscount_ReturnsRemainingShippingCharge() {
        VoucherEntity voucher = VoucherEntity.builder()
                .code("HALF")
                .discountPercent(50)
                .usageLimit(10)
                .active(true)
                .build();
        when(voucherRepository.findByCode("HALF")).thenReturn(Optional.of(voucher));

        ApplyVoucherRequest request = ApplyVoucherRequest.builder()
                .voucherCode("half")
                .orderAmount(BigDecimal.valueOf(250000))
                .shippingFee(BigDecimal.valueOf(30000))
                .build();

        VoucherCalculationResponse response = voucherService.calculateDiscount(request);

        assertEquals(0, response.getDiscountAmount().compareTo(BigDecimal.valueOf(15000)));
        assertEquals(0, response.getFinalAmount().compareTo(BigDecimal.valueOf(15000)));
        assertEquals(0, response.getOrderAmount().compareTo(BigDecimal.valueOf(250000)));
    }
}
