package com.viettel.deliverymanagement.config;

import com.viettel.deliverymanagement.constant.PaymentMethod;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Public recipient details for payment methods that are reconciled manually.
 * These values are intentionally separate from the legacy VietQR configuration.
 */
@Getter
@Component
public class ManualPaymentConfig {

    @Value("${manual-payment.bank.enabled:false}")
    private boolean bankTransferEnabled;

    @Value("${manual-payment.bank.name:}")
    private String bankName;

    @Value("${manual-payment.bank.account-number:}")
    private String bankAccountNumber;

    @Value("${manual-payment.bank.account-name:}")
    private String bankAccountName;

    @Value("${manual-payment.momo.enabled:false}")
    private boolean momoEnabled;

    @Value("${manual-payment.momo.phone:}")
    private String momoPhone;

    @Value("${manual-payment.momo.account-name:}")
    private String momoAccountName;

    public boolean isAvailable(PaymentMethod method) {
        return switch (method) {
            case COD -> true;
            case MANUAL_BANK_TRANSFER -> bankTransferEnabled
                    && hasText(bankName) && hasText(bankAccountNumber) && hasText(bankAccountName);
            case MANUAL_MOMO -> momoEnabled && hasText(momoPhone) && hasText(momoAccountName);
            default -> false;
        };
    }

    public List<PaymentMethod> availableMethods() {
        List<PaymentMethod> methods = new ArrayList<>(List.of(PaymentMethod.COD));
        if (isAvailable(PaymentMethod.MANUAL_BANK_TRANSFER)) {
            methods.add(PaymentMethod.MANUAL_BANK_TRANSFER);
        }
        if (isAvailable(PaymentMethod.MANUAL_MOMO)) {
            methods.add(PaymentMethod.MANUAL_MOMO);
        }
        return List.copyOf(methods);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
