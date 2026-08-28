package com.viettel.deliverymanagement.config;

import com.viettel.deliverymanagement.entity.VoucherEntity;
import com.viettel.deliverymanagement.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VoucherRepository voucherRepository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.seed-vouchers:false}")
    private boolean seedDefaultVouchers;

    @Override
    public void run(String... args) {
        autoMigrateDatabaseSchema();
        if (seedDefaultVouchers) {
            seedVouchers();
        }
    }

    /**
     * Tự động chạy các lệnh ALTER TABLE an toàn để đảm bảo mọi bảng trong DB đều đủ cột audit
     * mà không cần người dùng phải mở SQL Console bên ngoài.
     */
    private void autoMigrateDatabaseSchema() {
        String[] tables = {"vouchers", "orders", "shipments", "notifications", "users"};
        for (String table : tables) {
            try {
                jdbcTemplate.execute("ALTER TABLE `" + table + "` ADD COLUMN IF NOT EXISTS updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
                jdbcTemplate.execute("ALTER TABLE `" + table + "` ADD COLUMN IF NOT EXISTS created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP");
                jdbcTemplate.execute("ALTER TABLE `" + table + "` ADD COLUMN IF NOT EXISTS created_by VARCHAR(50) NULL");
                jdbcTemplate.execute("ALTER TABLE `" + table + "` ADD COLUMN IF NOT EXISTS updated_by VARCHAR(50) NULL");
                if (!"users".equals(table)) {
                    jdbcTemplate.execute("ALTER TABLE `" + table + "` ADD COLUMN IF NOT EXISTS is_deleted TINYINT(1) DEFAULT 0");
                }
            } catch (Exception e) {
                log.debug("Auto migration note for table {}: {}", table, e.getMessage());
            }
        }
        log.info("Tự động kiểm tra và đồng bộ cấu trúc cột Database thành công!");
    }

    private void seedVouchers() {
        if (voucherRepository.count() == 0) {
            log.info("Khởi tạo mã voucher khuyến mãi mặc định...");

            VoucherEntity v1 = VoucherEntity.builder()
                    .code("VIETTEL50")
                    .discountPercent(50)
                    .maxDiscountAmount(BigDecimal.valueOf(50000))
                    .minOrderAmount(BigDecimal.valueOf(100000))
                    .startDate(LocalDateTime.now().minusDays(10))
                    .endDate(LocalDateTime.now().plusYears(2))
                    .usageLimit(500)
                    .build();

            VoucherEntity v2 = VoucherEntity.builder()
                    .code("FREESHIP")
                    .discountPercent(100)
                    .maxDiscountAmount(BigDecimal.valueOf(30000))
                    .minOrderAmount(BigDecimal.valueOf(50000))
                    .startDate(LocalDateTime.now().minusDays(10))
                    .endDate(LocalDateTime.now().plusYears(2))
                    .usageLimit(1000)
                    .build();

            VoucherEntity v3 = VoucherEntity.builder()
                    .code("VIETTEL20")
                    .discountPercent(20)
                    .maxDiscountAmount(BigDecimal.valueOf(20000))
                    .minOrderAmount(BigDecimal.valueOf(50000))
                    .startDate(LocalDateTime.now().minusDays(10))
                    .endDate(LocalDateTime.now().plusYears(2))
                    .usageLimit(200)
                    .build();

            voucherRepository.save(v1);
            voucherRepository.save(v2);
            voucherRepository.save(v3);
            log.info("Khởi tạo danh sách Voucher thành công: VIETTEL50, FREESHIP, VIETTEL20");
        }
    }
}
