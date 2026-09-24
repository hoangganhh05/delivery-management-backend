package com.viettel.deliverymanagement.config;

import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.entity.UserSettingsEntity;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.security.PasswordPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Slf4j
@Component
@Order(100)
public class AdminAccountInitializer implements CommandLineRunner {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_@.-]{3,50}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String username;
    private final String password;
    private final String fullName;
    private final boolean resetPassword;

    public AdminAccountInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.enabled:false}") boolean enabled,
            @Value("${app.bootstrap-admin.username:}") String username,
            @Value("${app.bootstrap-admin.password:}") String password,
            @Value("${app.bootstrap-admin.full-name:System Administrator}") String fullName,
            @Value("${app.bootstrap-admin.reset-password:false}") boolean resetPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.resetPassword = resetPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedFullName = fullName == null || fullName.isBlank()
                ? "System Administrator"
                : fullName.trim();
        try {
            validateConfiguration(normalizedUsername);
        } catch (RuntimeException exception) {
            log.error("Bỏ qua bootstrap Admin vì cấu hình không hợp lệ: {}", exception.getMessage());
            return;
        }

        userRepository.findByUsername(normalizedUsername).ifPresentOrElse(existing -> {
            boolean changed = false;
            if (existing.getRole() != Role.ADMIN) {
                existing.setRole(Role.ADMIN);
                changed = true;
                log.warn("Đã nâng vai trò tài khoản bootstrap '{}' lên ADMIN", normalizedUsername);
            }
            if (resetPassword) {
                existing.setPassword(passwordEncoder.encode(password));
                existing.setPasswordChangedAt(LocalDateTime.now());
                changed = true;
                log.warn("Đã đặt lại mật khẩu tài khoản bootstrap '{}' theo biến môi trường", normalizedUsername);
            }
            if (changed) {
                userRepository.save(existing);
            } else {
                log.info("Tài khoản bootstrap Admin '{}' đã tồn tại; không thay đổi mật khẩu", normalizedUsername);
            }
            if (!"ACTIVE".equalsIgnoreCase(existing.getStatus())) {
                log.warn("Tài khoản bootstrap Admin '{}' đang có trạng thái {}; initializer không tự mở khóa",
                        normalizedUsername, existing.getStatus());
            }
        }, () -> {
            UserEntity admin = UserEntity.builder()
                    .username(normalizedUsername)
                    .password(passwordEncoder.encode(password))
                    .fullName(normalizedFullName)
                    .role(Role.ADMIN)
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .build();
            admin.attachSettings(UserSettingsEntity.defaultsFor(admin));
            userRepository.save(admin);
            log.warn("Đã tạo tài khoản bootstrap Admin '{}' từ biến môi trường", normalizedUsername);
        });
    }

    private void validateConfiguration(String normalizedUsername) {
        if (!USERNAME_PATTERN.matcher(normalizedUsername).matches()) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_USERNAME phải dài 3-50 ký tự và chỉ chứa chữ, số, _, @, . hoặc -"
            );
        }
        try {
            PasswordPolicy.requireStrongPassword(password);
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_PASSWORD phải có ít nhất 8 ký tự, 1 chữ hoa và 1 ký tự đặc biệt",
                    exception
            );
        }
    }
}
