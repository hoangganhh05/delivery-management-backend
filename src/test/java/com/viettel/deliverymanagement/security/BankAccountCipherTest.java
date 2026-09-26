package com.viettel.deliverymanagement.security;

import com.viettel.deliverymanagement.exception.AppException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BankAccountCipherTest {

    private static final String TEST_KEY = Base64.getEncoder().encodeToString(new byte[32]);

    @Test
    void encryptsAndDecryptsWithoutPersistingPlaintext() {
        BankAccountCipher cipher = new BankAccountCipher(TEST_KEY);

        String encrypted = cipher.encrypt("123456789012");

        assertNotEquals("123456789012", encrypted);
        assertEquals("123456789012", cipher.decrypt(encrypted));
    }

    @Test
    void refusesToEncryptWithoutAConfiguredKey() {
        BankAccountCipher cipher = new BankAccountCipher("");

        AppException exception = assertThrows(AppException.class, () -> cipher.encrypt("123456789012"));

        assertEquals("BANK_ACCOUNT_STORAGE_UNAVAILABLE", exception.getCode());
    }
}
