package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.request.ApplyVoucherRequest;
import com.viettel.deliverymanagement.dto.request.CreateVoucherRequest;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.entity.VoucherEntity;
import com.viettel.deliverymanagement.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Voucher Management", description = "APIs quản lý và tính toán voucher khuyến mãi")
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/vouchers")
    public ResponseData<List<VoucherEntity>> getVouchers() {
        return ResponseData.success("Lấy danh sách voucher thành công", voucherService.getVouchers());
    }

    @PostMapping("/vouchers/calculate")
    @Operation(
            summary = "Tính toán số tiền giảm giá của voucher",
            description = "Kiểm tra tính hợp lệ của voucher (hạn sử dụng, giá trị đơn tối thiểu) và tính số tiền giảm"
    )
    public ResponseData<BigDecimal> calculateDiscount(@Valid @RequestBody ApplyVoucherRequest request) {
        BigDecimal discountAmount = voucherService.calculateDiscount(request);
        return ResponseData.success("Tính tiền giảm giá thành công", discountAmount);
    }

    @PostMapping("/vouchers")
    @Operation(
            summary = "Tạo voucher mới",
            description = "Tạo voucher mới với các thông tin: mã, phần trăm giảm, giới hạn giảm, giá trị đơn tối thiểu, thời gian hiệu lực"
    )
    public ResponseData<VoucherEntity> createVoucher(@Valid @RequestBody CreateVoucherRequest request) {
        VoucherEntity voucher = voucherService.createVoucher(request);
        return ResponseData.success("Tạo voucher thành công", voucher);
    }
}
