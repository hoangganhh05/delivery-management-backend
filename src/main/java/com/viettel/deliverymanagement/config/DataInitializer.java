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

    @Value("${app.seed-vouchers:true}")
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
            ensureColumn(table, "updated_at", "DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
            ensureColumn(table, "created_at", "DATETIME NULL DEFAULT CURRENT_TIMESTAMP");
            ensureColumn(table, "created_by", "VARCHAR(50) NULL");
            ensureColumn(table, "updated_by", "VARCHAR(50) NULL");
            if (!"users".equals(table)) {
                ensureColumn(table, "is_deleted", "TINYINT(1) DEFAULT 0");
            }
        }

        // These columns were introduced after the first production schema was created.
        // Without them, every SELECT from orders/order_items fails with an SQL 500.
        ensureColumn("orders", "total_price", "DECIMAL(15,2) NULL");
        ensureColumn("order_items", "price", "DECIMAL(12,2) NULL");
        ensureColumn("order_items", "weight_gram", "INT NULL");
        ensureColumn("order_items", "declared_value", "DECIMAL(12,2) NULL");
        ensureColumn("vouchers", "active", "TINYINT(1) NOT NULL DEFAULT 1");

        log.info("Tự động kiểm tra và đồng bộ cấu trúc cột Database thành công!");
    }

    private void ensureColumn(String table, String column, String definition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    Integer.class,
                    table,
                    column
            );
            if (count != null && count == 0) {
                jdbcTemplate.execute("ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + definition);
                log.info("Đã bổ sung cột {}.{} cho schema hiện tại", table, column);
            }
        } catch (Exception e) {
            // A missing table/permission should be visible in Render logs but should not
            // prevent the application from starting when that table is not in use yet.
            log.warn("Không thể đồng bộ cột {}.{}: {}", table, column, e.getMessage());
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
