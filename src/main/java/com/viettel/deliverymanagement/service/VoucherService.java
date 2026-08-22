package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.request.ApplyVoucherRequest;

import java.math.BigDecimal;

public interface VoucherService {

    BigDecimal calculateDiscount(ApplyVoucherRequest request);
}
