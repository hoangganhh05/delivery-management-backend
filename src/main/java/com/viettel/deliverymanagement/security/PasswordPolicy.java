package com.viettel.deliverymanagement.security;

import com.viettel.deliverymanagement.exception.AppException;

import java.nio.charset.StandardCharsets;

public final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;
    private static final int BCRYPT_MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    public static void requireBcryptCompatible(String password) {
        if (password != null && password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new AppException(
                    "PASSWORD_TOO_LONG",
                    "Mật khẩu không được vượt quá 72 byte UTF-8"
            );
        }
    }

    public static void requireStrongPassword(String password) {
        requireBcryptCompatible(password);
        if (password == null || password.length() < MIN_LENGTH) {
            throw new AppException("WEAK_PASSWORD", "Mật khẩu phải có ít nhất 8 ký tự");
        }
        if (password.chars().noneMatch(Character::isUpperCase)) {
            throw new AppException("WEAK_PASSWORD", "Mật khẩu phải có ít nhất 1 chữ hoa");
        }
        if (password.chars().allMatch(Character::isLetterOrDigit)) {
            throw new AppException("WEAK_PASSWORD", "Mật khẩu phải có ít nhất 1 ký tự đặc biệt");
        }
    }
}
