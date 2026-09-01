package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.request.ChangePasswordRequest;
import com.viettel.deliverymanagement.dto.request.UpdateProfileRequest;
import com.viettel.deliverymanagement.dto.request.UpdateUserSettingsRequest;
import com.viettel.deliverymanagement.dto.request.UpsertUserAddressRequest;
import com.viettel.deliverymanagement.dto.response.PasswordChangeResponse;
import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.dto.response.UserAddressResponse;
import com.viettel.deliverymanagement.dto.response.UserDto;
import com.viettel.deliverymanagement.dto.response.UserMeResponse;
import com.viettel.deliverymanagement.dto.response.UserSettingsResponse;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping
    public ResponseData<List<UserDto>> getUsers() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .phoneNumber(user.getPhoneNumber())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .build())
                .toList();
        return ResponseData.success("Lấy danh sách người dùng thành công", users);
    }

    @GetMapping("/me")
    public ResponseData<UserMeResponse> getCurrentUser(Authentication authentication) {
        return ResponseData.success(
                "Lấy thông tin tài khoản thành công",
                userService.getCurrentUser(authentication.getName())
        );
    }

    @PutMapping("/profile")
    public ResponseData<UserMeResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseData.success(
                "Cập nhật thông tin cá nhân thành công",
                userService.updateProfile(authentication.getName(), request)
        );
    }

    @PutMapping("/change-password")
    public ResponseData<PasswordChangeResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ResponseData.success(
                "Đổi mật khẩu thành công",
                userService.changePassword(authentication.getName(), request)
        );
    }

    @GetMapping("/addresses")
    public ResponseData<List<UserAddressResponse>> getAddresses(Authentication authentication) {
        return ResponseData.success(
                "Lấy sổ địa chỉ thành công",
                userService.getAddresses(authentication.getName())
        );
    }

    @PostMapping("/addresses")
    public ResponseData<UserAddressResponse> createAddress(
            Authentication authentication,
            @Valid @RequestBody UpsertUserAddressRequest request
    ) {
        return ResponseData.success(
                "Thêm địa chỉ thành công",
                userService.createAddress(authentication.getName(), request)
        );
    }

    @PutMapping("/addresses/{id}")
    public ResponseData<UserAddressResponse> updateAddress(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpsertUserAddressRequest request
    ) {
        return ResponseData.success(
                "Cập nhật địa chỉ thành công",
                userService.updateAddress(authentication.getName(), id, request)
        );
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseData<List<UserAddressResponse>> deleteAddress(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseData.success(
                "Xóa địa chỉ thành công",
                userService.deleteAddress(authentication.getName(), id)
        );
    }

    @PutMapping("/addresses/{id}/default")
    public ResponseData<UserAddressResponse> setDefaultAddress(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseData.success(
                "Đã đặt địa chỉ mặc định",
                userService.setDefaultAddress(authentication.getName(), id)
        );
    }

    @PutMapping("/settings")
    public ResponseData<UserSettingsResponse> updateSettings(
            Authentication authentication,
            @Valid @RequestBody UpdateUserSettingsRequest request
    ) {
        return ResponseData.success(
                "Lưu tùy chọn tài khoản thành công",
                userService.updateSettings(authentication.getName(), request)
        );
    }
}
