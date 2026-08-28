package com.viettel.deliverymanagement.service.impl;

import com.viettel.deliverymanagement.dto.request.ApplyVoucherRequest;
import com.viettel.deliverymanagement.dto.request.CreateVoucherRequest;
import com.viettel.deliverymanagement.entity.VoucherEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.VoucherRepository;
import com.viettel.deliverymanagement.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VoucherEntity> getVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public BigDecimal calculateDiscount(ApplyVoucherRequest request) {
        log.info("Bắt đầu tính toán giảm giá cho mã voucher: {} với giá trị đơn hàng: {}", 
                request.getVoucherCode(), request.getOrderAmount());

        String normalizedCode = request.getVoucherCode().trim().toUpperCase();
        VoucherEntity voucher = voucherRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new AppException("VOUCHER_NOT_FOUND", "Mã voucher không tồn tại hoặc đã bị vô hiệu hóa"));

        LocalDateTime now = LocalDateTime.now();

        // 1. Kiểm tra thời gian hiệu lực bắt đầu
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            log.warn("Voucher {} chưa đến ngày bắt đầu: {}", voucher.getCode(), voucher.getStartDate());
            throw new AppException("VOUCHER_NOT_YET_VALID", "Voucher chưa đến thời gian áp dụng");
        }

        // 2. Kiểm tra thời gian hiệu lực kết thúc
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            log.warn("Voucher {} đã hết hạn vào ngày: {}", voucher.getCode(), voucher.getEndDate());
            throw new AppException("VOUCHER_EXPIRED", "Voucher đã hết hạn sử dụng");
        }

        // 3. Kiểm tra số lượt sử dụng còn lại (nếu có giới hạn)
        if (voucher.getUsageLimit() != null && voucher.getUsageLimit() <= 0) {
            log.warn("Voucher {} đã hết lượt sử dụng", voucher.getCode());
            throw new AppException("VOUCHER_OUT_OF_USAGE", "Voucher đã hết lượt sử dụng");
        }

        // 4. Kiểm tra giá trị đơn hàng tối thiểu
        if (voucher.getMinOrderAmount() != null && request.getOrderAmount().compareTo(voucher.getMinOrderAmount()) < 0) {
            log.warn("Đơn hàng {} chưa đạt giá trị tối thiểu {}", request.getOrderAmount(), voucher.getMinOrderAmount());
            throw new AppException("ORDER_AMOUNT_NOT_ENOUGH", "Giá trị đơn hàng chưa đạt mức tối thiểu " + voucher.getMinOrderAmount() + " để áp dụng voucher");
        }

        // 5. Tính số tiền giảm giá theo %
        BigDecimal discountBase = request.getShippingFee() != null
                ? request.getShippingFee()
                : request.getOrderAmount();
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (voucher.getDiscountPercent() != null && voucher.getDiscountPercent() > 0) {
            discountAmount = discountBase
                    .multiply(BigDecimal.valueOf(voucher.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // 6. Khống chế mức giảm giá tối đa (nếu có cấu hình maxDiscountAmount)
        if (voucher.getMaxDiscountAmount() != null && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
            discountAmount = voucher.getMaxDiscountAmount();
        }

        // 7. Đảm bảo số tiền giảm giá không vượt quá tổng giá trị đơn hàng
        if (discountAmount.compareTo(discountBase) > 0) {
            discountAmount = discountBase;
        }

        log.info("Tính toán thành công: Số tiền giảm giá cho voucher {} là {}", voucher.getCode(), discountAmount);
        return discountAmount;
    }

    @Override
    @Transactional
    public VoucherEntity createVoucher(CreateVoucherRequest request) {
        log.info("Bắt đầu tạo voucher mới với mã: {}", request.getCode());

        // Kiểm tra mã voucher đã tồn tại chưa
        String normalizedCode = request.getCode().trim().toUpperCase();
        if (voucherRepository.findByCode(normalizedCode).isPresent()) {
            log.warn("Mã voucher {} đã tồn tại", request.getCode());
            throw new AppException("VOUCHER_CODE_EXISTS", "Mã voucher đã tồn tại trong hệ thống");
        }

        VoucherEntity voucher = VoucherEntity.builder()
                .code(normalizedCode)
                .discountPercent(request.getDiscountPercent())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderAmount(request.getMinOrderAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .usageLimit(request.getUsageLimit())
                .build();

        VoucherEntity savedVoucher = voucherRepository.save(voucher);
        log.info("Tạo voucher thành công với ID: {}", savedVoucher.getId());
        return savedVoucher;
    }
}
