package com.viettel.deliverymanagement.config;

import com.viettel.deliverymanagement.entity.VoucherEntity;
import com.viettel.deliverymanagement.repository.VoucherRepository;
import com.viettel.deliverymanagement.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VoucherRepository voucherRepository;
    private final PermissionService permissionService;

    @Value("${app.seed-vouchers:true}")
    private boolean seedDefaultVouchers;

    @Override
    public void run(String... args) {
        permissionService.seedDefaults();
        if (seedDefaultVouchers) {
            seedVouchers();
        }
    }

    private void seedVouchers() {
        log.info("Kiểm tra các voucher khuyến mãi mặc định...");

        createVoucherIfMissing(VoucherEntity.builder()
                    .code("VIETTEL50")
                    .discountPercent(50)
                    .maxDiscountAmount(BigDecimal.valueOf(50000))
                    .minOrderAmount(BigDecimal.valueOf(100000))
                    .startDate(LocalDateTime.now().minusDays(10))
                    .endDate(LocalDateTime.now().plusYears(2))
                    .usageLimit(500)
                    .active(true)
                    .build());

        createVoucherIfMissing(VoucherEntity.builder()
                    .code("FREESHIP")
                    .discountPercent(100)
                    .maxDiscountAmount(BigDecimal.valueOf(30000))
                    .minOrderAmount(BigDecimal.valueOf(50000))
                    .startDate(LocalDateTime.now().minusDays(10))
                    .endDate(LocalDateTime.now().plusYears(2))
                    .usageLimit(1000)
                    .active(true)
                    .build());

        createVoucherIfMissing(VoucherEntity.builder()
                    .code("VIETTEL20")
                    .discountPercent(20)
                    .maxDiscountAmount(BigDecimal.valueOf(20000))
                    .minOrderAmount(BigDecimal.valueOf(50000))
                    .startDate(LocalDateTime.now().minusDays(10))
                    .endDate(LocalDateTime.now().plusYears(2))
                    .usageLimit(200)
                    .active(true)
                    .build());

        log.info("Đã bảo đảm tồn tại các voucher: VIETTEL50, FREESHIP, VIETTEL20");
    }

    private void createVoucherIfMissing(VoucherEntity voucher) {
        if (voucherRepository.findByCode(voucher.getCode()).isEmpty()) {
            voucherRepository.save(voucher);
        }
    }
}
