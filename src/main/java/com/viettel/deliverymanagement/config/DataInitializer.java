package com.viettel.deliverymanagement.config;

import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.entity.VoucherEntity;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedVouchers();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            log.info("Khởi tạo tài khoản mẫu mặc định...");

            UserEntity admin = UserEntity.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản Trị Viên Hệ Thống")
                    .email("admin@viettel.vn")
                    .phoneNumber("0988888888")
                    .role(Role.ADMIN)
                    .status("ACTIVE")
                    .build();

            UserEntity shipper1 = UserEntity.builder()
                    .username("shipper1")
                    .password(passwordEncoder.encode("shipper123"))
                    .fullName("Shipper Nguyễn Văn Giao")
                    .email("shipper1@viettel.vn")
                    .phoneNumber("0977777771")
                    .role(Role.SHIPPER)
                    .status("ACTIVE")
                    .build();

            UserEntity shipper2 = UserEntity.builder()
                    .username("shipper2")
                    .password(passwordEncoder.encode("shipper123"))
                    .fullName("Shipper Trần Văn Nhanh")
                    .email("shipper2@viettel.vn")
                    .phoneNumber("0977777772")
                    .role(Role.SHIPPER)
                    .status("ACTIVE")
                    .build();

            UserEntity customer = UserEntity.builder()
                    .username("customer")
                    .password(passwordEncoder.encode("customer123"))
                    .fullName("Khách Hàng Hoàng Anh")
                    .email("customer@gmail.com")
                    .phoneNumber("0966666666")
                    .role(Role.CUSTOMER)
                    .status("ACTIVE")
                    .build();

            userRepository.save(admin);
            userRepository.save(shipper1);
            userRepository.save(shipper2);
            userRepository.save(customer);
            log.info("Khởi tạo tài khoản mẫu thành công: admin, shipper1, shipper2, customer (mật khẩu tương ứng: admin123, shipper123, customer123)");
        }
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
