package com.viettel.deliverymanagement.config;

import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAccountInitializerTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @Test
    void doesNothingWhenDisabled() {
        initializer(false, "", "", false).run();
        verify(userRepository, never()).findByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void createsAdminWithEncodedPasswordWhenMissing() {
        when(userRepository.findByUsername("admin_ops")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("StrongPassword!123")).thenReturn("bcrypt-hash");

        initializer(true, "admin_ops", "StrongPassword!123", false).run();

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);
        assertThat(captor.getValue().getPassword()).isEqualTo("bcrypt-hash");
        assertThat(captor.getValue().getStatus()).isEqualTo("ACTIVE");
        assertThat(captor.getValue().getSettings()).isNotNull();
    }

    @Test
    void promotesExistingUserWithoutResettingPasswordByDefault() {
        UserEntity existing = UserEntity.builder().username("admin_ops").password("old-hash")
                .role(Role.CUSTOMER).status("ACTIVE").build();
        when(userRepository.findByUsername("admin_ops")).thenReturn(Optional.of(existing));

        initializer(true, "admin_ops", "StrongPassword!123", false).run();

        assertThat(existing.getRole()).isEqualTo(Role.ADMIN);
        assertThat(existing.getPassword()).isEqualTo("old-hash");
        verify(userRepository).save(existing);
        verify(passwordEncoder, never()).encode(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void skipsBootstrapForWeakPasswordWithoutStoppingApplication() {
        initializer(true, "admin", "admin123", false).run();
        verify(userRepository, never()).findByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    private AdminAccountInitializer initializer(boolean enabled, String username, String password, boolean reset) {
        return new AdminAccountInitializer(userRepository, passwordEncoder, enabled, username, password,
                "Quản trị hệ thống", reset);
    }
}
