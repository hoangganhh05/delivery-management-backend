package com.viettel.deliverymanagement.constant;

public enum PaymentMethod {
    COD,
    /** Historical QR-transfer method. It is no longer available for new orders. */
    VCB_QR,
    VNPAY,
    MANUAL_BANK_TRANSFER,
    MANUAL_MOMO;

    public boolean isManualPayment() {
        return this == MANUAL_BANK_TRANSFER || this == MANUAL_MOMO;
    }

    public boolean requiresManualConfirmation() {
        return this == VCB_QR || isManualPayment();
    }
}
