package com.viettel.deliverymanagement.service.impl;

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
import com.viettel.deliverymanagement.security.PasswordPolicy;
import com.viettel.deliverymanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserMeResponse getCurrentUser(String username) {
        return toUserMe(findUserForUpdate(username));
    }

    @Override
    @Transactional
    public UserMeResponse updateProfile(String username, UpdateProfileRequest request) {
        UserEntity user = findUserForUpdate(username);
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String phoneNumber = request.getPhoneNumber().trim();

        if (userRepository.existsByEmailAndIdNot(email, user.getId())) {
            throw new AppException("EMAIL_ALREADY_EXISTS", "Địa chỉ email đã được sử dụng cho tài khoản khác");
        }
        if (userRepository.existsByPhoneNumberAndIdNot(phoneNumber, user.getId())) {
            throw new AppException("PHONE_ALREADY_EXISTS", "Số điện thoại đã được sử dụng cho tài khoản khác");
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setAvatarUrl(trimToNull(request.getAvatarUrl()));

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException("PROFILE_CONFLICT", "Email hoặc số điện thoại đã được sử dụng");
        }
        return toUserMe(user);
    }

    @Override
    @Transactional
    public PasswordChangeResponse changePassword(String username, ChangePasswordRequest request) {
        UserEntity user = findUserForUpdate(username);
        PasswordPolicy.requireBcryptCompatible(request.getCurrentPassword());
        PasswordPolicy.requireBcryptCompatible(request.getNewPassword());
        PasswordPolicy.requireBcryptCompatible(request.getConfirmPassword());
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AppException("CURRENT_PASSWORD_INVALID", "Mật khẩu hiện tại không chính xác");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException("PASSWORD_CONFIRMATION_MISMATCH", "Xác nhận mật khẩu mới không khớp");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AppException("PASSWORD_UNCHANGED", "Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        LocalDateTime changedAt = LocalDateTime.now();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(changedAt);
        userRepository.saveAndFlush(user);
        return PasswordChangeResponse.builder()
                .changed(true)
                .changedAt(changedAt)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAddressResponse> getAddresses(String username) {
        UserEntity user = findUser(username);
        return findAddressResponses(user.getId());
    }

    @Override
    @Transactional
    public UserAddressResponse createAddress(String username, UpsertUserAddressRequest request) {
        UserEntity user = findUserForUpdate(username);
        boolean makeDefault = request.isDefaultAddress() || !userAddressRepository.existsByUserId(user.getId());
        if (makeDefault) {
            userAddressRepository.clearDefaultForUser(user.getId());
        }

        UserAddressEntity address = UserAddressEntity.builder()
                .user(user)
                .defaultAddress(makeDefault)
                .build();
        copyAddressRequest(request, address);
        return toAddressResponse(userAddressRepository.saveAndFlush(address));
    }

    @Override
    @Transactional
    public UserAddressResponse updateAddress(
            String username,
            Long addressId,
            UpsertUserAddressRequest request
    ) {
        UserEntity user = findUserForUpdate(username);
        UserAddressEntity address = findOwnedAddress(addressId, user.getId());
        boolean keepDefault = address.isDefaultAddress();

        if (request.isDefaultAddress() && !keepDefault) {
            userAddressRepository.clearDefaultForUser(user.getId());
            keepDefault = true;
        }

        copyAddressRequest(request, address);
        address.setDefaultAddress(keepDefault);
        return toAddressResponse(userAddressRepository.saveAndFlush(address));
    }

    @Override
    @Transactional
    public List<UserAddressResponse> deleteAddress(String username, Long addressId) {
        UserEntity user = findUserForUpdate(username);
        UserAddressEntity address = findOwnedAddress(addressId, user.getId());
        boolean deletedDefault = address.isDefaultAddress();
        userAddressRepository.delete(address);
        userAddressRepository.flush();

        List<UserAddressEntity> remaining =
                userAddressRepository.findAllByUserIdOrderByDefaultAddressDescCreatedAtAsc(user.getId());
        if (deletedDefault && !remaining.isEmpty()) {
            UserAddressEntity replacement = remaining.get(0);
            replacement.setDefaultAddress(true);
            userAddressRepository.saveAndFlush(replacement);
            remaining = userAddressRepository.findAllByUserIdOrderByDefaultAddressDescCreatedAtAsc(user.getId());
        }
        return remaining.stream().map(this::toAddressResponse).toList();
    }

    @Override
    @Transactional
    public UserAddressResponse setDefaultAddress(String username, Long addressId) {
        UserEntity user = findUserForUpdate(username);
        UserAddressEntity address = findOwnedAddress(addressId, user.getId());
        userAddressRepository.clearDefaultForUser(user.getId());
        address.setDefaultAddress(true);
        return toAddressResponse(userAddressRepository.saveAndFlush(address));
    }

    @Override
    @Transactional
    public UserSettingsResponse updateSettings(String username, UpdateUserSettingsRequest request) {
        UserEntity user = findUserForUpdate(username);
        UserSettingsEntity settings = getOrCreateSettings(user);
        settings.setEmailNotifications(request.getEmailNotifications());
        settings.setSmsNotifications(request.getSmsNotifications());
        settings.setPushNotifications(request.getPushNotifications());
        settings.setNewOrderNotifications(request.getNewOrderNotifications());
        settings.setStatusChangeNotifications(request.getStatusChangeNotifications());
        settings.setPaymentSuccessNotifications(request.getPaymentSuccessNotifications());
        settings.setDeliveryCompleteNotifications(request.getDeliveryCompleteNotifications());
        settings.setShipperAssignmentNotifications(request.getShipperAssignmentNotifications());
        settings.setServiceAlertNotifications(request.getServiceAlertNotifications());
        settings.setLanguage(request.getLanguage().trim().toLowerCase(Locale.ROOT));
        settings.setTheme(request.getTheme());
        settings.setAccentColor(request.getAccentColor().toUpperCase(Locale.ROOT));
        return toSettingsResponse(userSettingsRepository.saveAndFlush(settings));
    }

    private UserEntity findUser(String username) {
        if (username == null || username.isBlank()) {
            throw new AppException("UNAUTHORIZED", "Phiên đăng nhập không hợp lệ");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy tài khoản đang đăng nhập"));
    }

    private UserEntity findUserForUpdate(String username) {
        if (username == null || username.isBlank()) {
            throw new AppException("UNAUTHORIZED", "Phiên đăng nhập không hợp lệ");
        }
        return userRepository.findByUsernameForUpdate(username)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy tài khoản đang đăng nhập"));
    }

    private UserAddressEntity findOwnedAddress(Long addressId, Long userId) {
        return userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AppException(
                        "ADDRESS_NOT_FOUND",
                        "Không tìm thấy địa chỉ thuộc tài khoản đang đăng nhập"
                ));
    }

    private UserSettingsEntity getOrCreateSettings(UserEntity user) {
        return userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> userSettingsRepository.save(UserSettingsEntity.defaultsFor(user)));
    }

    private UserMeResponse toUserMe(UserEntity user) {
        UserSettingsEntity settings = getOrCreateSettings(user);
        return UserMeResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .addresses(findAddressResponses(user.getId()))
                .settings(toSettingsResponse(settings))
                .build();
    }

    private List<UserAddressResponse> findAddressResponses(Long userId) {
        return userAddressRepository.findAllByUserIdOrderByDefaultAddressDescCreatedAtAsc(userId)
                .stream()
                .map(this::toAddressResponse)
                .toList();
    }

    private void copyAddressRequest(UpsertUserAddressRequest request, UserAddressEntity address) {
        address.setLabel(request.getLabel().trim());
        address.setRecipientName(request.getRecipientName().trim());
        address.setPhoneNumber(request.getPhoneNumber().trim());
        address.setAddressLine(request.getAddressLine().trim());
        address.setWard(trimToNull(request.getWard()));
        address.setDistrict(trimToNull(request.getDistrict()));
        address.setProvince(trimToNull(request.getProvince()));
        address.setPostalCode(trimToNull(request.getPostalCode()));
    }

    private UserAddressResponse toAddressResponse(UserAddressEntity address) {
        return UserAddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .recipientName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine(address.getAddressLine())
                .ward(address.getWard())
                .district(address.getDistrict())
                .province(address.getProvince())
                .postalCode(address.getPostalCode())
                .defaultAddress(address.isDefaultAddress())
                .build();
    }

    private UserSettingsResponse toSettingsResponse(UserSettingsEntity settings) {
        return UserSettingsResponse.builder()
                .emailNotifications(settings.isEmailNotifications())
                .smsNotifications(settings.isSmsNotifications())
                .pushNotifications(settings.isPushNotifications())
                .newOrderNotifications(settings.isNewOrderNotifications())
                .statusChangeNotifications(settings.isStatusChangeNotifications())
                .paymentSuccessNotifications(settings.isPaymentSuccessNotifications())
                .deliveryCompleteNotifications(settings.isDeliveryCompleteNotifications())
                .shipperAssignmentNotifications(settings.isShipperAssignmentNotifications())
                .serviceAlertNotifications(settings.isServiceAlertNotifications())
                .language(settings.getLanguage())
                .theme(settings.getTheme())
                .accentColor(settings.getAccentColor())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
