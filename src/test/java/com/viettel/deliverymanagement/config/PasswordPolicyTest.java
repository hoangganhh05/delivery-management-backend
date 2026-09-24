package com.viettel.deliverymanagement.config;

import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.security.PasswordPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    @Test
    void acceptsEightCharactersWithUppercaseAndSpecialCharacter() {
        assertThatCode(() -> PasswordPolicy.requireStrongPassword("Admin@12"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsPasswordWithoutUppercase() {
        assertThatThrownBy(() -> PasswordPolicy.requireStrongPassword("admin@123"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("chữ hoa");
    }

    @Test
    void rejectsPasswordWithoutSpecialCharacter() {
        assertThatThrownBy(() -> PasswordPolicy.requireStrongPassword("Admin123"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("đặc biệt");
    }

    @Test
    void loginCompatibilityCheckStillAcceptsLegacyWeakPassword() {
        assertThatCode(() -> PasswordPolicy.requireBcryptCompatible("legacy123"))
                .doesNotThrowAnyException();
    }
}
