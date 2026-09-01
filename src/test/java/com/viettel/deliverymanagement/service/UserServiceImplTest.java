package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.constant.Gender;
import com.viettel.deliverymanagement.constant.Role;
import com.viettel.deliverymanagement.constant.Theme;
import com.viettel.deliverymanagement.dto.request.ChangePasswordRequest;
import com.viettel.deliverymanagement.dto.request.UpdateProfileRequest;
import com.viettel.deliverymanagement.dto.request.UpdateUserSettingsRequest;
import com.viettel.deliverymanagement.dto.request.UpsertUserAddressRequest;
import com.viettel.deliverymanagement.dto.response.PasswordChangeResponse;
import com.viettel.deliverymanagement.dto.response.UserAddressResponse;
import com.viettel.deliverymanagement.dto.response.UserMeResponse;
import com.viettel.deliverymanagement.dto.response.UserSettingsResponse;
import com.viettel.deliverymanagement.entity.UserAddressEntity;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.entity.UserSettingsEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.UserAddressRepository;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.repository.UserSettingsRepository;
import com.viettel.deliverymanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("GET me tạo settings mặc định cho tài khoản cũ chưa có settings")
    void getCurrentUser_CreatesDefaultSettingsForLegacyUser() {
        UserEntity user = testUser(7L, "customer");
        when(userRepository.findByUsernameForUpdate("customer")).thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(7L)).thenReturn(Optional.empty());
        when(userSettingsRepository.save(any(UserSettingsEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userAddressRepository.findAllByUserIdOrderByDefaultAddressDescCreatedAtAsc(7L))
                .thenReturn(List.of());

        UserMeResponse response = userService.getCurrentUser("customer");

        assertEquals(7L, response.getId());
        assertEquals("customer", response.getUsername());
        assertTrue(response.getSettings().isEmailNotifications());
        assertFalse(response.getSettings().isSmsNotifications());
        assertEquals(Theme.LIGHT, response.getSettings().getTheme());
        assertTrue(response.getAddresses().isEmpty());
        verify(userSettingsRepository).save(any(UserSettingsEntity.class));
    }

    @Test
    @DisplayName("Không cho cập nhật profile bằng email của tài khoản khác")
    void updateProfile_DuplicateEmailRejected() {
        UserEntity user = testUser(7L, "customer");
        UpdateProfileRequest request = profileRequest();
        when(userRepository.findByUsernameForUpdate("customer")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("new@example.com", 7L)).thenReturn(true);

        AppException exception = assertThrows(
                AppException.class,
                () -> userService.updateProfile("customer", request)
        );

        assertEquals("EMAIL_ALREADY_EXISTS", exception.getCode());
        verify(userRepository, never()).saveAndFlush(any(UserEntity.class));
    }

    @Test
    @DisplayName("Đổi mật khẩu kiểm tra mật khẩu cũ và lưu BCrypt hash mới")
    void changePassword_Success() {
        UserEntity user = testUser(7L, "customer");
        user.setPassword("old-hash");
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("old-password")
                .newPassword("new-password-123")
                .confirmPassword("new-password-123")
                .build();
        when(userRepository.findByUsernameForUpdate("customer")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-password-123", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-password-123")).thenReturn("new-hash");

        PasswordChangeResponse response = userService.changePassword("customer", request);

        assertTrue(response.isChanged());
        assertNotNull(response.getChangedAt());
        assertEquals("new-hash", user.getPassword());
        assertEquals(response.getChangedAt(), user.getPasswordChangedAt());
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    @DisplayName("Từ chối đổi mật khẩu khi mật khẩu hiện tại sai")
    void changePassword_WrongCurrentPasswordRejected() {
        UserEntity user = testUser(7L, "customer");
        user.setPassword("old-hash");
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("wrong-password")
                .newPassword("new-password-123")
                .confirmPassword("new-password-123")
                .build();
        when(userRepository.findByUsernameForUpdate("customer")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

        AppException exception = assertThrows(
                AppException.class,
                () -> userService.changePassword("customer", request)
        );

        assertEquals("CURRENT_PASSWORD_INVALID", exception.getCode());
        verify(userRepository, never()).saveAndFlush(any(UserEntity.class));
    }

    @Test
    @DisplayName("Địa chỉ đầu tiên tự động trở thành địa chỉ mặc định")
    void createAddress_FirstAddressBecomesDefault() {
        UserEntity user = testUser(7L, "customer");
        when(userRepository.findByUsernameForUpdate("customer")).thenReturn(Optional.of(user));
        when(userAddressRepository.existsByUserId(7L)).thenReturn(false);
        when(userAddressRepository.saveAndFlush(any(UserAddressEntity.class)))
                .thenAnswer(invocation -> {
                    UserAddressEntity address = invocation.getArgument(0);
                    address.setId(11L);
                    return address;
                });

        UserAddressResponse response = userService.createAddress("customer", addressRequest(false));

        assertEquals(11L, response.getId());
        assertTrue(response.isDefaultAddress());
        verify(userAddressRepository).clearDefaultForUser(7L);
    }

    @Test
    @DisplayName("Không thể sửa địa chỉ không thuộc tài khoản trong JWT")
    void updateAddress_OtherUsersAddressRejected() {
        UserEntity user = testUser(7L, "customer");
        when(userRepository.findByUsernameForUpdate("customer")).thenReturn(Optional.of(user));
        when(userAddressRepository.findByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> userService.updateAddress("customer", 99L, addressRequest(false))
        );

        assertEquals("ADDRESS_NOT_FOUND", exception.getCode());
        verify(userAddressRepository, never()).saveAndFlush(any(UserAddressEntity.class));
    }

    @Test
    @DisplayName("Lưu toàn bộ tùy chọn thông báo và giao diện vào user_settings")
    void updateSettings_PersistsAllPreferences() {
        UserEntity user = testUser(7L, "customer");
        UserSettingsEntity settings = UserSettingsEntity.defaultsFor(user);
        UpdateUserSettingsRequest request = UpdateUserSettingsRequest.builder()
                .emailNotifications(false)
                .smsNotifications(true)
                .pushNotifications(false)
                .newOrderNotifications(false)
                .statusChangeNotifications(true)
                .paymentSuccessNotifications(false)
                .deliveryCompleteNotifications(true)
                .shipperAssignmentNotifications(true)
                .serviceAlertNotifications(false)
                .language("EN")
                .theme(Theme.DARK)
                .accentColor("#abcdef")
                .build();
        when(userRepository.findByUsernameForUpdate("customer")).thenReturn(Optional.of(user));
        when(userSettingsRepository.findByUserId(7L)).thenReturn(Optional.of(settings));
        when(userSettingsRepository.saveAndFlush(settings)).thenReturn(settings);

        UserSettingsResponse response = userService.updateSettings("customer", request);

        assertFalse(response.isEmailNotifications());
        assertTrue(response.isSmsNotifications());
        assertEquals("en", response.getLanguage());
        assertEquals(Theme.DARK, response.getTheme());
        assertEquals("#ABCDEF", response.getAccentColor());
    }

    private UserEntity testUser(Long id, String username) {
        UserEntity user = UserEntity.builder()
                .username(username)
                .password("encoded")
                .fullName("Nguyen Van A")
                .email("a@example.com")
                .phoneNumber("0987654321")
                .role(Role.CUSTOMER)
                .status("ACTIVE")
                .build();
        user.setId(id);
        return user;
    }

    private UpdateProfileRequest profileRequest() {
        return UpdateProfileRequest.builder()
                .fullName("New Name")
                .email("NEW@example.com")
                .phoneNumber("0912345678")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .gender(Gender.OTHER)
                .avatarUrl("https://example.com/avatar.png")
                .build();
    }

    private UpsertUserAddressRequest addressRequest(boolean defaultAddress) {
        return UpsertUserAddressRequest.builder()
                .label("Nhà riêng")
                .recipientName("Nguyen Van A")
                .phoneNumber("0987654321")
                .addressLine("123 Nguyen Trai")
                .ward("Phường 1")
                .district("Quận 1")
                .province("TP.HCM")
                .postalCode("700000")
                .defaultAddress(defaultAddress)
                .build();
    }
}
