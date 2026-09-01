package com.viettel.deliverymanagement.security;

import com.viettel.deliverymanagement.exception.AppException;

import java.nio.charset.StandardCharsets;

public final class PasswordPolicy {

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
}
