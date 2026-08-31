package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.dto.request.ApplyVoucherRequest;
import com.viettel.deliverymanagement.dto.request.CreateVoucherRequest;
import com.viettel.deliverymanagement.dto.response.VoucherCalculationResponse;
import com.viettel.deliverymanagement.entity.VoucherEntity;
import java.util.List;

public interface VoucherService {
    List<VoucherEntity> getVouchers();


    VoucherCalculationResponse calculateDiscount(ApplyVoucherRequest request);

    VoucherEntity createVoucher(CreateVoucherRequest request);
}
