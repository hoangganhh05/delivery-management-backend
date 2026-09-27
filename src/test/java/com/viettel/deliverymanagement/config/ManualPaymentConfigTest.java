package com.viettel.deliverymanagement.config;

import com.viettel.deliverymanagement.constant.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManualPaymentConfigTest {

    @Test
    void onlyCodIsAvailableUntilManualRecipientDetailsAreComplete() {
        ManualPaymentConfig config = new ManualPaymentConfig();
        ReflectionTestUtils.setField(config, "bankTransferEnabled", true);
        ReflectionTestUtils.setField(config, "bankName", "VCB");
        ReflectionTestUtils.setField(config, "bankAccountNumber", "0123456789");

        assertEquals(List.of(PaymentMethod.COD), config.availableMethods());

        ReflectionTestUtils.setField(config, "bankAccountName", "Delivery Test");
        assertEquals(List.of(PaymentMethod.COD, PaymentMethod.MANUAL_BANK_TRANSFER), config.availableMethods());
    }
}
