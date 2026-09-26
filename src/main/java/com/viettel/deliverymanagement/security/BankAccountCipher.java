package com.viettel.deliverymanagement.security;

import com.viettel.deliverymanagement.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encrypts payout account numbers before they are persisted. The API never
 * returns the ciphertext or a full account number.
 */
@Component
public class BankAccountCipher {

    private static final byte FORMAT_VERSION = 1;
    private static final int KEY_BYTES = 32;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final String encodedKey;

    public BankAccountCipher(
            @Value("${app.bank-account-encryption-key:}") String encodedKey
    ) {
        this.encodedKey = encodedKey;
    }

    public String encrypt(String value) {
        try {
            byte[] iv = new byte[IV_BYTES];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey(), new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            ByteBuffer payload = ByteBuffer.allocate(1 + IV_BYTES + encrypted.length);
            payload.put(FORMAT_VERSION);
            payload.put(iv);
            payload.put(encrypted);
            return Base64.getEncoder().encodeToString(payload.array());
        } catch (GeneralSecurityException exception) {
            throw storageUnavailable();
        }
    }

    public String decrypt(String encryptedValue) {
        try {
            byte[] payload = Base64.getDecoder().decode(encryptedValue);
            if (payload.length <= 1 + IV_BYTES || payload[0] != FORMAT_VERSION) {
                throw new AppException("BANK_ACCOUNT_DATA_INVALID", "Dữ liệu tài khoản nhận tiền không hợp lệ");
            }

            ByteBuffer buffer = ByteBuffer.wrap(payload);
            buffer.get();
            byte[] iv = new byte[IV_BYTES];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey(), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (AppException exception) {
            throw exception;
        } catch (IllegalArgumentException | GeneralSecurityException exception) {
            throw new AppException("BANK_ACCOUNT_DATA_INVALID", "Dữ liệu tài khoản nhận tiền không hợp lệ");
        }
    }

    private SecretKey encryptionKey() {
        if (encodedKey == null || encodedKey.isBlank()) {
            throw storageUnavailable();
        }
        try {
            byte[] key = Base64.getDecoder().decode(encodedKey.trim());
            if (key.length != KEY_BYTES) {
                throw storageUnavailable();
            }
            return new SecretKeySpec(key, "AES");
        } catch (IllegalArgumentException exception) {
            throw storageUnavailable();
        }
    }

    private AppException storageUnavailable() {
        return new AppException(
                "BANK_ACCOUNT_STORAGE_UNAVAILABLE",
                "Chức năng lưu tài khoản nhận tiền chưa được cấu hình an toàn"
        );
    }
}
