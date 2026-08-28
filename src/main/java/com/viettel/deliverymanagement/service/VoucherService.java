package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.request.ApplyVoucherRequest;
import com.viettel.deliverymanagement.dto.request.CreateVoucherRequest;
import com.viettel.deliverymanagement.entity.VoucherEntity;
import java.util.List;

import java.math.BigDecimal;

public interface VoucherService {
    List<VoucherEntity> getVouchers();


    BigDecimal calculateDiscount(ApplyVoucherRequest request);

    VoucherEntity createVoucher(CreateVoucherRequest request);
}
